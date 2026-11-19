(() => {
  const byId = id => document.getElementById(id);
  const pageHeader = byId('browser-page-header');
  const title = byId('browser-page-title');
  const settings = byId('tab-pane-settings');
  const downloads = byId('caspian-downloads-modal');
  const query = byId('settings-search-input');
  const empty = byId('settings-search-empty');
  const bottomSheet = byId('bottom-sheet');
  let destination = 'sheet';
  let sheetTab = 'engine';

  const bridgeCall = (method, ...args) => {
    const bridge = window.CaspianBridge;
    if (typeof bridge?.[method] !== 'function') return false;
    bridge[method](...args);
    return true;
  };

  window.setBrowserChrome = (...palette) => {
    ['background', 'surface', 'raised', 'outline', 'text', 'muted', 'accent'].forEach((token, index) => {
      document.documentElement.style.setProperty(`--browser-${token}`, palette[index]);
    });
  };

  const preferences = [
    { id: 'browser-search-engine', get: 'getDefaultSearchEngine', set: 'setSearchEngine', fallback: 'Google' },
    { id: 'browser-gestures', get: 'getBrowserGesturesEnabled', set: 'setBrowserGesturesEnabled', fallback: true },
    { id: 'browser-reduce-motion', get: 'getBrowserReduceMotion', set: 'setBrowserReduceMotion', fallback: false }
  ];

  function refreshPreferences() {
    for (const { id, get, fallback } of preferences) {
      const input = byId(id);
      const bridge = window.CaspianBridge;
      const value = typeof bridge?.[get] === 'function' ? bridge[get]() : fallback;
      if (input.type === 'checkbox') input.checked = value;
      else input.value = value;
    }
    document.body.classList.toggle('browser-reduce-motion', byId('browser-reduce-motion').checked);
  }

  for (const { id, set } of preferences) {
    const input = byId(id);
    input.addEventListener('change', () => {
      bridgeCall(set, input.type === 'checkbox' ? input.checked : input.value);
      if (id === 'browser-reduce-motion') document.body.classList.toggle('browser-reduce-motion', input.checked);
    });
  }

  function filterSettings() {
    const terms = query.value.toLowerCase().trim().split(/\s+/).filter(Boolean);
    let matches = 0;
    for (const section of settings.children) {
      if (section === empty || section.contains(query)) continue;
      const rows = [...section.querySelectorAll('.browser-setting-row')];
      const heading = section.querySelector('h2')?.textContent ?? '';
      for (const row of rows) row.hidden = !matchesTerms(heading + ' ' + row.textContent, terms);
      section.hidden = rows.length ? rows.every(row => row.hidden) : !matchesTerms(section.textContent, terms);
      if (!section.hidden) matches++;
    }
    empty.hidden = matches > 0;
  }

  function matchesTerms(text, terms) {
    const normalized = text.toLowerCase().replace(/\s+/g, ' ');
    return terms.every(term => normalized.includes(term));
  }

  query.addEventListener('input', filterSettings);

  window.showBrowserDestination = (next, tab = '') => {
    if (destination === 'sheet') {
      const current = document.querySelector('.tab-nav-btn.active')?.dataset.tab;
      if (current && current !== 'settings') sheetTab = current;
    }
    destination = next;
    document.body.dataset.browserDestination = next;
    pageHeader.hidden = next === 'sheet';
    downloads.style.display = next === 'downloads' ? 'flex' : 'none';
    refreshPreferences();

    if (next === 'downloads') {
      title.textContent = 'Downloads';
      window.refreshBrowserDownloads?.();
    } else if (next === 'settings') {
      title.textContent = 'Settings';
      window.switchToTab?.('settings', true);
      query.value = '';
      filterSettings();
      bottomSheet.scrollTop = 0;
      window.refreshBrowserSheet?.();
    } else {
      const focusId = tab === 'waveguard' ? 'chk-waveguard-trackers' : tab === 'chat' ? 'chat-limit-tuning' : '';
      window.switchToTab?.(focusId ? 'engine' : tab || sheetTab, true);
      window.refreshBrowserSheet?.();
      if (focusId) requestAnimationFrame(() => byId(focusId)?.scrollIntoView({ block: 'center' }));
    }
    if (next !== 'sheet') title.focus({ preventScroll: true });
  };

  window.openBrowserSettingsPage = () => {
    if (!bridgeCall('openBrowserSettings')) window.showBrowserDestination('settings');
  };
  window.openDownloadsModal = () => {
    if (!bridgeCall('openBrowserDownloads')) window.showBrowserDestination('downloads');
  };
  window.backFromBrowserDestination = () => {
    if (!bridgeCall('backFromBrowserPanel')) window.showBrowserDestination('sheet');
  };

  const openTool = tool => {
    if (!bridgeCall('openBrowserTool', tool)) window.showBrowserDestination('sheet', tool);
  };
  byId('browser-page-back').addEventListener('click', window.backFromBrowserDestination);
  byId('browser-sheet-downloads').addEventListener('click', window.openDownloadsModal);
  byId('browser-settings-downloads').addEventListener('click', window.openDownloadsModal);
  byId('browser-open-firefox').addEventListener('click', () => bridgeCall('openFirefoxBrowser'));
  byId('browser-settings-casks').addEventListener('click', () => window.openControlCasksModal?.());
  byId('browser-settings-shield').addEventListener('click', () => openTool('waveguard'));
  byId('settings-chat-performance').addEventListener('click', () => openTool('chat'));
})();
