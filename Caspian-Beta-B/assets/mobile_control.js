// ======================================================
// CASPIAN ANDROID - MOBILE CONTROL SHEET JS
// ======================================================

(function() {
  const sheetBackdrop = document.getElementById('sheet-backdrop');
  const bottomSheet = document.getElementById('bottom-sheet');
  const dragArea = document.getElementById('sheet-drag-area');
  const themeToggleBtn = document.getElementById('theme-toggle-btn');
  const themeBtnDark = document.getElementById('theme-btn-dark');
  const themeBtnLight = document.getElementById('theme-btn-light');
  const resetThemeBtn = document.getElementById('reset-theme-btn');
  const powerToggleBtn = document.getElementById('power-toggle-btn');
  const convertBtn = document.getElementById('convert-btn');
  const copyBtn = document.getElementById('copy-btn');
  const exportDropdownTrigger = document.getElementById('export-dropdown-trigger');
  const exportMenu = document.getElementById('export-menu');

  const appCardHub = document.getElementById('app-card-hub');
  const appCardGpt = document.getElementById('app-card-chatgpt');
  const appCardGemini = document.getElementById('app-card-gemini');
  const newTabBtn = document.getElementById('new-tab-btn');
  const closeAllTabsBtn = document.getElementById('close-all-tabs-btn');

  const debugRecToggleBtn = document.getElementById('debug-rec-toggle-btn');
  const debugRecDot = document.getElementById('debug-rec-dot');
  const debugRecSub = document.getElementById('debug-rec-sub');
  let isRecordingLogs = false;
  let nigelClickCount = 0;
  let relockClickCount = 0;
  let lastNigelTapTime = 0;
  let lastRelockTapTime = 0;

  const startPicker = document.getElementById('gradient-start-picker');
  const endPicker = document.getElementById('gradient-end-picker');
  const startHex = document.getElementById('gradient-start-hex');
  const endHex = document.getElementById('gradient-end-hex');

  const bgColorPicker = document.getElementById('bg-color-picker');
  const bgColorHex = document.getElementById('bg-color-hex');
  const nigelFactCard = document.getElementById('nigel-fact-card');
  const nigelFactText = document.getElementById('nigel-fact-text');

  let activeTheme = 'light';
  let selectedDarkBg = '#050811';
  let limitVal = 5;
  let globalActive = true;

  function syncAppVersion() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getAppVersion === 'function') {
        const v = window.CaspianBridge.getAppVersion();
        const brandTags = document.querySelectorAll('.sheet-brand-tag');
        brandTags.forEach(el => el.textContent = 'V' + v);
      }
    } catch(e) {}
  }

  function updateDebugRecUI() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.isDebugRecording === 'function') {
        isRecordingLogs = window.CaspianBridge.isDebugRecording();
      }
    } catch(e) {}

    const recDot = document.getElementById('debug-rec-dot');
    const recBtn = document.getElementById('debug-rec-toggle-btn');
    const recSub = document.getElementById('debug-rec-sub');

    if (recDot) recDot.classList.toggle('active', isRecordingLogs);
    if (recBtn) recBtn.textContent = isRecordingLogs ? 'Stop & Save' : 'Start Rec';
    if (recSub) recSub.textContent = isRecordingLogs ? 'Logging active... Perform actions now!' : 'Record console errors, network events & app diagnostics to file.';
  }

  // Dynamic Event Delegation for Log Recorder Toggle & Re-lock Badge with 1.5s Rapid Window
  document.addEventListener('click', (e) => {
    const recBtn = document.getElementById('debug-rec-toggle-btn');
    if (recBtn && (e.target === recBtn || recBtn.contains(e.target))) {
      isRecordingLogs = !isRecordingLogs;
      if (window.CaspianBridge && typeof window.CaspianBridge.toggleDebugRecording === 'function') {
        window.CaspianBridge.toggleDebugRecording(isRecordingLogs);
      }
      setTimeout(updateDebugRecUI, 200);
      return;
    }

    const devUnlockedBadge = document.getElementById('dev-unlocked-badge');
    if (devUnlockedBadge && (e.target === devUnlockedBadge || devUnlockedBadge.contains(e.target))) {
      e.stopPropagation();
      const now = Date.now();
      if (now - lastRelockTapTime > 1500) {
        relockClickCount = 1;
      } else {
        relockClickCount++;
      }
      lastRelockTapTime = now;

      const targetDevCard = document.getElementById('developer-options-card');
      if (relockClickCount >= 7) {
        if (targetDevCard) targetDevCard.style.display = 'none';
        nigelClickCount = 0;
        relockClickCount = 0;
        if (isRecordingLogs) {
          isRecordingLogs = false;
          if (window.CaspianBridge && typeof window.CaspianBridge.toggleDebugRecording === 'function') {
            window.CaspianBridge.toggleDebugRecording(false);
          }
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("🔒 Developer Options Locked!");
        }
      } else if (relockClickCount >= 4) {
        const remaining = 7 - relockClickCount;
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`Tap ${remaining} more times to lock Developer Options.`);
        }
      }
    }
  });

  function renderOpenTabs() {
    const container = document.getElementById('tabs-list-container');
    const countBadge = document.getElementById('tab-count-badge');
    if (!container) return;

    let tabs = [];
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getOpenTabs === 'function') {
        const jsonStr = window.CaspianBridge.getOpenTabs();
        if (jsonStr) {
          tabs = JSON.parse(jsonStr);
        }
      }
    } catch(e) {}

    if (countBadge) {
      countBadge.textContent = tabs.length === 1 ? '1 Tab' : `${tabs.length} Tabs`;
    }

    if (tabs.length === 0) {
      container.innerHTML = '<div style="font-size: 12px; color: var(--text-sub); text-align: center; padding: 12px;">No active browser tabs open</div>';
      return;
    }

    let html = '<div class="tab-card-grid">';
    tabs.forEach(tab => {
      let iconB64 = '';
      if (tab.service === 'gemini') {
        iconB64 = window.GEMINI_ICON_B64 || '';
      } else if (tab.service === 'chatgpt') {
        iconB64 = window.GPT_ICON_B64 || '';
      }

      const activeClass = tab.active ? 'active' : '';
      const activeBadge = tab.active ? '<span style="font-size: 9px; font-weight: 800; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 6px; border-radius: 6px;">ACTIVE</span>' : '';

      html += `
        <div class="chrome-tab-card ${activeClass}" data-tabid="${tab.id}">
          <div class="chrome-tab-header">
            <div style="display: flex; align-items: center; gap: 6px; overflow: hidden;">
              ${iconB64 ? `<img src="${iconB64}" style="width: 16px; height: 16px; border-radius: 4px;" />` : ''}
              <span class="chrome-tab-title">${tab.title || 'Browser Tab'}</span>
            </div>
            <button class="chrome-tab-close" data-closeid="${tab.id}" title="Close Tab">&times;</button>
          </div>
          <div class="chrome-tab-url" style="font-size: 10px; color: var(--text-sub); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px;">
            ${tab.url || ''}
          </div>
          <div style="display: flex; justify-content: flex-end; margin-top: 6px;">
            ${activeBadge}
          </div>
        </div>
      `;
    });
    html += '</div>';

    container.innerHTML = html;

    // Bind Tab Card clicks & Close buttons
    container.querySelectorAll('.chrome-tab-card').forEach(card => {
      card.addEventListener('click', (e) => {
        if (e.target.classList.contains('chrome-tab-close')) return;
        const tabId = parseInt(card.dataset.tabid);
        if (window.CaspianBridge && typeof window.CaspianBridge.switchTab === 'function') {
          window.CaspianBridge.switchTab(tabId);
          setTimeout(renderOpenTabs, 100);
        }
      });
    });

    container.querySelectorAll('.chrome-tab-close').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const tabId = parseInt(btn.dataset.closeid);
        if (window.CaspianBridge && typeof window.CaspianBridge.closeTab === 'function') {
          window.CaspianBridge.closeTab(tabId);
          setTimeout(renderOpenTabs, 150);
        }
      });
    });
  }

  // Restore saved limit, power switch state, and tabs on load
  function restoreSavedSettings() {
    syncAppVersion();
    updateDebugRecUI();
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
        const jsonStr = window.CaspianBridge.getSettings();
        if (jsonStr) {
          const prefs = JSON.parse(jsonStr);
          if (prefs.limit !== undefined) {
            limitVal = parseInt(prefs.limit);
            document.querySelectorAll('.limit-pill').forEach(p => {
              const val = parseInt(p.dataset.val);
              p.classList.toggle('active', val === limitVal);
            });
            const activeBadge = document.getElementById('active-limit-badge');
            if (activeBadge) {
              activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Turn' : 'Turns'}`;
            }
          }
          if (prefs.globalActive !== undefined) {
            globalActive = (prefs.globalActive === true || prefs.globalActive === 'true');
            const statusDot = document.getElementById('status-dot');
            const statusTitle = document.getElementById('status-title');
            const statusSub = document.getElementById('status-sub');

            if (statusDot) statusDot.classList.toggle('active', globalActive);
            if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Pruning Active' : 'Chat Pruning Disabled';
            if (statusSub) statusSub.textContent = globalActive ? 'Lag Fixer & DOM Limit Active' : 'Pruning paused via Master Power Switch';
          }
        }
      }
    } catch(e) {}

    renderOpenTabs();
  }

  // Nigel Facts List & 7-Tap Rapid Easter Egg Developer Unlocking (1.5s timeout)
  const DEV_FACTS = [
    "Legend has it Nigel spent his time building Caspian instead of studying for his End-Sem exams or preparing for company placement interviews tomorrow... Absolute madman! 💀",
    "Nigel's favorite music genre is 'whatever he likes at the moment'. Down for NEFFEX anytime!",
    "Nigel makes extensions and web tools that actually solve real problems.",
    "Did you know? Nigel built Lsync, Caspian, and Scrobby all with custom aesthetic UIs!"
  ];
  let currentFactIdx = 0;

  if (nigelFactCard && nigelFactText) {
    nigelFactCard.addEventListener('click', () => {
      const now = Date.now();
      if (now - lastNigelTapTime > 1500) {
        nigelClickCount = 1;
      } else {
        nigelClickCount++;
      }
      lastNigelTapTime = now;

      currentFactIdx = (currentFactIdx + 1) % DEV_FACTS.length;
      nigelFactText.style.opacity = '0';
      setTimeout(() => {
        nigelFactText.textContent = DEV_FACTS[currentFactIdx];
        nigelFactText.style.opacity = '1';
      }, 150);

      const targetDevCard = document.getElementById('developer-options-card');
      if (nigelClickCount >= 7) {
        if (targetDevCard) targetDevCard.style.display = 'block';
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("🛠️ Developer Options Unlocked!");
        }
      } else if (nigelClickCount >= 4) {
        const remaining = 7 - nigelClickCount;
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`Tap ${remaining} more times to unlock Developer Options.`);
        }
      }
    });
  }

  // Theme Presets Map
  const presets = {
    caspian: { start: '#A2A9A9', end: '#1B4264' },
    cyan: { start: '#06b6d4', end: '#0891b2' },
    violet: { start: '#a855f7', end: '#7c3aed' },
    azure: { start: '#3b82f6', end: '#1d4ed8' },
    emerald: { start: '#10b981', end: '#047857' }
  };

  function applyCustomGradient(start, end) {
    document.documentElement.style.setProperty('--accent', start);
    document.documentElement.style.setProperty('--secondary', end);
    document.documentElement.style.setProperty('--accent-glow', `${start}55`);
    document.documentElement.style.setProperty('--accent-gradient', `linear-gradient(135deg, ${start}, ${end})`);

    if (startPicker) startPicker.value = start;
    if (endPicker) endPicker.value = end;
    if (startHex) startHex.value = start.toUpperCase();
    if (endHex) endHex.value = end.toUpperCase();
  }

  function applyCustomBg(colorHex) {
    if (activeTheme === 'dark') {
      selectedDarkBg = colorHex;
      document.documentElement.style.setProperty('--sheet-bg', colorHex);
    } else {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff');
    }
    if (bgColorPicker) bgColorPicker.value = colorHex;
    if (bgColorHex) bgColorHex.value = colorHex.toUpperCase();
  }

  // Bind Background Tone Presets
  document.querySelectorAll('.bg-preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.bg-preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const bgHex = btn.dataset.bg;
      if (bgHex) applyCustomBg(bgHex);
    });
  });

  if (bgColorPicker && bgColorHex) {
    bgColorPicker.addEventListener('input', (e) => {
      bgColorHex.value = e.target.value.toUpperCase();
      applyCustomBg(e.target.value);
    });
    bgColorHex.addEventListener('change', (e) => {
      let val = e.target.value;
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        applyCustomBg(val);
      }
    });
  }

  // Bind Quick Presets
  document.querySelectorAll('.preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const pKey = btn.dataset.preset;
      if (presets[pKey]) {
        applyCustomGradient(presets[pKey].start, presets[pKey].end);
      }
    });
  });

  // Color Pickers
  if (startPicker && startHex) {
    startPicker.addEventListener('input', (e) => {
      startHex.value = e.target.value.toUpperCase();
      applyCustomGradient(e.target.value, endPicker ? endPicker.value : '#1B4264');
    });
    startHex.addEventListener('change', (e) => {
      let val = e.target.value;
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        startPicker.value = val;
        applyCustomGradient(val, endPicker ? endPicker.value : '#1B4264');
      }
    });
  }

  if (endPicker && endHex) {
    endPicker.addEventListener('input', (e) => {
      endHex.value = e.target.value.toUpperCase();
      applyCustomGradient(startPicker ? startPicker.value : '#A2A9A9', e.target.value);
    });
    endHex.addEventListener('change', (e) => {
      let val = e.target.value;
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        endPicker.value = val;
        applyCustomGradient(startPicker ? startPicker.value : '#A2A9A9', val);
      }
    });
  }

  // Reset Defaults
  if (resetThemeBtn) {
    resetThemeBtn.addEventListener('click', () => {
      applyCustomGradient('#A2A9A9', '#1B4264');
      applyCustomBg('#ffffff');
      setTheme('light');
    });
  }

  // Sync Theme with Host Web Page (Direct JS Execution -> No Activity Recreation -> No Crashing!)
  function syncHostPageTheme(t) {
    const isDark = (t === 'dark');
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.setSystemNightMode === 'function') {
        window.CaspianBridge.setSystemNightMode(isDark);
      }
      if (isDark) {
        document.documentElement.classList.add('dark');
        document.documentElement.classList.remove('light');
        document.documentElement.setAttribute('data-theme', 'dark');
        document.documentElement.style.colorScheme = 'dark';
      } else {
        document.documentElement.classList.add('light');
        document.documentElement.classList.remove('dark');
        document.documentElement.setAttribute('data-theme', 'light');
        document.documentElement.style.colorScheme = 'light';
      }
      localStorage.setItem('theme', isDark ? 'dark' : 'light');
      localStorage.setItem('colorMode', isDark ? 'dark' : 'light');
    } catch(e) {}
  }

  // Theme Toggles (Default Light)
  function setTheme(t) {
    activeTheme = t || 'light';
    document.documentElement.setAttribute('data-theme', activeTheme);
    if (activeTheme === 'light') {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff');
    } else {
      document.documentElement.style.setProperty('--sheet-bg', selectedDarkBg);
    }
    if (themeBtnDark) themeBtnDark.classList.toggle('active', activeTheme === 'dark');
    if (themeBtnLight) themeBtnLight.classList.toggle('active', activeTheme === 'light');

    syncHostPageTheme(activeTheme);
  }

  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      setTheme(activeTheme === 'light' ? 'dark' : 'light');
    });
  }
  if (themeBtnDark) themeBtnDark.addEventListener('click', () => setTheme('dark'));
  if (themeBtnLight) themeBtnLight.addEventListener('click', () => setTheme('light'));

  // Resizable Drag Area Hitbox
  const targetDragArea = dragArea || document.querySelector('.sheet-drag-area') || document.querySelector('.sheet-drag-handle');
  if (targetDragArea && bottomSheet) {
    let startY, startHeight;

    targetDragArea.addEventListener('touchstart', (e) => {
      try { e.preventDefault(); } catch(err) {}
      const touch = e.touches[0];
      startY = touch.clientY;
      startHeight = bottomSheet.offsetHeight;
      bottomSheet.style.transition = 'none';
    }, { passive: false });

    targetDragArea.addEventListener('touchmove', (e) => {
      try { e.preventDefault(); } catch(err) {}
      const touch = e.touches[0];
      const deltaY = startY - touch.clientY;
      const newHeight = startHeight + deltaY;
      const vhHeight = Math.max(25, Math.min(95, (newHeight / window.innerHeight) * 100));
      bottomSheet.style.height = vhHeight + 'vh';
      bottomSheet.style.maxHeight = '95vh';
    }, { passive: false });

    targetDragArea.addEventListener('touchend', () => {
      bottomSheet.style.transition = 'transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), height 0.3s ease';
    });
  }

  // App Icon Cards: Open new tab for selected platform (Hub, ChatGPT, Gemini)
  if (appCardHub) {
    appCardHub.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('hub');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (appCardGpt) {
    appCardGpt.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('chatgpt');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (appCardGemini) {
    appCardGemini.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('gemini');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (newTabBtn) {
    newTabBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('chatgpt');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (closeAllTabsBtn) {
    closeAllTabsBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.closeAllTabs === 'function') {
        window.CaspianBridge.closeAllTabs();
        setTimeout(renderOpenTabs, 150);
      }
    });
  }

  // Mobile Tab Navigation
  document.querySelectorAll('.tab-nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-nav-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.style.display = 'none');

      btn.classList.add('active');
      const targetTab = btn.dataset.tab;
      const pane = document.getElementById(`tab-pane-${targetTab}`);
      if (pane) pane.style.display = 'block';

      if (targetTab === 'sites') {
        renderOpenTabs();
      }
    });
  });

  // Master Power Switch
  if (powerToggleBtn) {
    powerToggleBtn.addEventListener('click', () => {
      globalActive = !globalActive;
      const statusDot = document.getElementById('status-dot');
      const statusTitle = document.getElementById('status-title');
      const statusSub = document.getElementById('status-sub');

      if (statusDot) statusDot.classList.toggle('active', globalActive);
      if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Pruning Active' : 'Chat Pruning Disabled';
      if (statusSub) statusSub.textContent = globalActive ? 'Lag Fixer & DOM Limit Active' : 'Pruning paused via Master Power Switch';

      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('globalActive', JSON.stringify(globalActive));
      }
    });
  }

  // Limit Pills Selection
  document.querySelectorAll('.limit-pill[data-val]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.limit-pill[data-val]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      limitVal = parseInt(pill.dataset.val);

      const activeBadge = document.getElementById('active-limit-badge');
      if (activeBadge) {
        activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Turn' : 'Turns'}`;
      }

      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('limit', limitVal);
      }
    });
  });

  // Convert Chat Handler
  if (convertBtn) {
    convertBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation('convert');
      }
    });
  }

  // Export Dropdown Trigger
  if (exportDropdownTrigger && exportMenu) {
    exportDropdownTrigger.addEventListener('click', (e) => {
      e.stopPropagation();
      exportMenu.classList.toggle('active');
    });
  }

  // Export Options Handler
  document.querySelectorAll('.export-opt-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const fmt = btn.dataset.fmt;
      if (exportMenu) exportMenu.classList.remove('active');
      
      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation(fmt);
      }
    });
  });

  // Copy Button
  if (copyBtn) {
    copyBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation('copy');
      }
    });
  }

  // Initialize saved settings on load
  restoreSavedSettings();
})();
