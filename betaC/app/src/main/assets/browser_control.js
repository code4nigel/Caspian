// ======================================================
// CASPIAN ANDROID - MOBILE CONTROL SHEET JS
// ======================================================

(function () {
  let sfxVolume = 0.5;
  let masterSFXMuted = false;

  function setMasterMute(muted) {
    masterSFXMuted = !!muted;
    try {
      localStorage.setItem('master_sfx_muted', masterSFXMuted ? 'true' : 'false');
      if (window.CaspianBridge) {
        if (typeof window.CaspianBridge.setMasterSfxMuted === 'function') {
          window.CaspianBridge.setMasterSfxMuted(masterSFXMuted);
        }
        if (typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('master_sfx_muted', masterSFXMuted ? 'true' : 'false');
        }
      }
    } catch (e) { }

    document.documentElement.classList.toggle('sfx-muted', masterSFXMuted);

    const unmutedIcon = document.getElementById('mute-icon-unmuted');
    const mutedIcon = document.getElementById('mute-icon-muted');
    const masterToggle = document.getElementById('toggle-sfx-master');

    if (unmutedIcon) unmutedIcon.style.display = masterSFXMuted ? 'none' : 'block';
    if (mutedIcon) mutedIcon.style.display = masterSFXMuted ? 'block' : 'none';
    if (masterToggle) masterToggle.checked = !masterSFXMuted;
  }

  // Immediate startup check for saved mute preference
  try {
    const initMuted = localStorage.getItem('master_sfx_muted') === 'true';
    if (initMuted) {
      masterSFXMuted = true;
      document.documentElement.classList.add('sfx-muted');
    }
  } catch (e) { }

  const ALL_SFX_FILES = [
    'tap_main.mp3',
    'tap_button.mp3',
    'tap_alternate.mp3',
    'pop_button.mp3',
    'pop_button_v2.mp3',
    'pop_click.mp3',
    'pop_unknown_v1.mp3'
  ];

  let sfxConfig = {
    tm_tabs: localStorage.getItem('sfx_file_tm_tabs') || 'pop_button.mp3',
    ta: localStorage.getItem('sfx_file_ta') || 'pop_click.mp3',
    tb_clicks: localStorage.getItem('sfx_file_tb_clicks') || 'tap_button.mp3',
    tm_header: localStorage.getItem('sfx_file_tm_header') || 'tap_main.mp3',
    tb_close: localStorage.getItem('sfx_file_tb_close') || 'tap_button.mp3',
    tb_modal: localStorage.getItem('sfx_file_tb_modal') || 'tap_button.mp3'
  };

  const sfxAudioPool = {};

  function preloadAllSFX() {
    try {
      ALL_SFX_FILES.forEach(fileName => {
        if (!sfxAudioPool[fileName]) {
          const a = new Audio(`sfx/${fileName}`);
          a.load();
          sfxAudioPool[fileName] = a;
        }
      });
    } catch (e) { }
  }

  // Preload all sounds on startup
  setTimeout(preloadAllSFX, 100);

  function getSFXFileForType(type) {
    const saved = localStorage.getItem(`sfx_file_${type}`);
    if (saved) return saved;
    if (sfxConfig[type]) return sfxConfig[type];
    if (type === 'tm_tabs') return 'pop_button.mp3';
    if (type === 'tm_header') return 'tap_main.mp3';
    if (type === 'ta') return 'pop_click.mp3';
    if (type === 'tb_clicks') return 'pop_click.mp3';
    return 'pop_click.mp3';
  }

  function playSFX(type) {
    try {
      if (masterSFXMuted) return;
      let enabled = true;
      if (type === 'tm_tabs') enabled = localStorage.getItem('sfx_enabled_tm_tabs') !== 'false';
      else if (type === 'ta') enabled = localStorage.getItem('sfx_enabled_ta') !== 'false';
      else if (type === 'tb_clicks') enabled = localStorage.getItem('sfx_enabled_tb_clicks') !== 'false';
      else if (type === 'tm_header') enabled = localStorage.getItem('sfx_enabled_tm_header') !== 'false';
      else if (type === 'tb_close') enabled = localStorage.getItem('sfx_enabled_tb_close') !== 'false';
      else if (type === 'tb_modal') enabled = localStorage.getItem('sfx_enabled_tb_modal') !== 'false';

      if (!enabled) return;

      const fileName = getSFXFileForType(type);
      if (window.CaspianBridge && typeof window.CaspianBridge.playAssetSound === 'function') {
        window.CaspianBridge.playAssetSound(`sfx/${fileName}`);
      }
    } catch (e) { }
  }

  window.syncPrunerSettingsFromNative = function(limit, mode, enabled) {
    limitVal = limit;
    globalActive = enabled;
    localStorage.setItem('limit', limit);
    localStorage.setItem('chat_limit_enabled', enabled ? 'true' : 'false');
    localStorage.setItem('chat_pruning_mode', mode);

    const statusDot = document.getElementById('status-dot');
    const statusTitle = document.getElementById('status-title');
    const toggleBtn = document.getElementById('toggle-chat-limit-btn');
    const activeBadge = document.getElementById('active-limit-badge');

    if (statusDot) statusDot.classList.toggle('active', enabled);
    if (statusTitle) statusTitle.textContent = enabled ? 'Chat Message Limit: ON' : 'Chat Message Limit: OFF';
    if (toggleBtn) {
      toggleBtn.textContent = enabled ? 'ON' : 'OFF';
      toggleBtn.className = enabled ? 'oneui-pill-btn primary' : 'oneui-pill-btn secondary';
    }
    if (activeBadge) {
      activeBadge.textContent = limit >= 9999 ? '∞ All' : `${limit} ${limit === 1 ? 'Message' : 'Messages'}`;
    }

    document.querySelectorAll('.limit-pill').forEach(p => {
      const val = parseInt(p.dataset.val);
      p.classList.toggle('active', val === limit);
    });

    const btnSliding = document.getElementById('btn-mode-sliding');
    const btnTail = document.getElementById('btn-mode-tail');
    if (btnSliding) btnSliding.classList.toggle('active', mode === 'sliding_window');
    if (btnTail) btnTail.classList.toggle('active', mode === 'tail');
  };

  function previewSFXFile(fileName) {
    try {
      if (masterSFXMuted) return;
      if (window.CaspianBridge && typeof window.CaspianBridge.playAssetSound === 'function') {
        window.CaspianBridge.playAssetSound(`sfx/${fileName}`);
      }
    } catch(e) {}
  }

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

  const harborTabsGrid = document.getElementById('harbor-tabs-grid');
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
  let globalActive = false;

  function syncAppVersion() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getAppVersion === 'function') {
        const v = window.CaspianBridge.getAppVersion();
        const brandTags = document.querySelectorAll('.sheet-brand-tag');
        brandTags.forEach(el => el.textContent = 'V' + v);
      }
    } catch (e) { }
  }

  function formatMarkdown(text) {
    if (!text) return '';
    let html = text
      // Escape basic HTML
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    // Headers
    html = html.replace(/^###\s+(.*$)/gim, '<div style="font-weight: 700; font-size: 12px; color: #00E5FF; margin: 8px 0 3px 0;">$1</div>');
    html = html.replace(/^##\s+(.*$)/gim, '<div style="font-weight: 800; font-size: 13px; color: #10B981; margin: 10px 0 4px 0;">$1</div>');
    html = html.replace(/^#\s+(.*$)/gim, '<div style="font-weight: 900; font-size: 14px; color: #FFFFFF; margin: 12px 0 6px 0;">$1</div>');

    // Bold & Italics
    html = html.replace(/\*\*\*(.*?)\*\*\*/gim, '<strong><em>$1</em></strong>');
    html = html.replace(/\*\*(.*?)\*\*/gim, '<strong style="color: #FFFFFF;">$1</strong>');
    html = html.replace(/\*(.*?)\*/gim, '<em style="color: #DFE2F0;">$1</em>');
    html = html.replace(/__(.*?)__/gim, '<strong style="color: #FFFFFF;">$1</strong>');
    html = html.replace(/_(.*?)_/gim, '<em style="color: #DFE2F0;">$1</em>');

    // Inline code
    html = html.replace(/`([^`]+)`/gim, '<code style="background: rgba(255,255,255,0.12); color: #00E5FF; padding: 2px 5px; border-radius: 4px; font-family: monospace; font-size: 10px;">$1</code>');

    // Unordered lists (- item or * item)
    html = html.replace(/^\s*[\-\*]\s+(.*$)/gim, '<div style="display: flex; gap: 6px; margin: 3px 0 3px 4px;"><span style="color: #00E5FF;">•</span><span>$1</span></div>');

    // Numbered lists (1. item)
    html = html.replace(/^\s*(\d+)\.\s+(.*$)/gim, '<div style="display: flex; gap: 6px; margin: 3px 0 3px 4px;"><span style="color: #10B981; font-weight: bold;">$1.</span><span>$2</span></div>');

    // Horizontal rules
    html = html.replace(/^---$/gim, '<hr style="border: none; border-top: 1px solid rgba(255,255,255,0.12); margin: 8px 0;">');

    // Paragraphs / line breaks
    html = html.replace(/\n\n/g, '<div style="height: 6px;"></div>');
    html = html.replace(/\n/g, '<br>');

    return html;
  }

  let latestUpdateInfo = null;

  function showUpdateModal(info) {
    if (!info) return;
    const modal = document.getElementById('caspian-update-modal');
    if (!modal) return;
    
    document.getElementById('update-modal-tag').textContent = 'v' + info.cleanVersion;
    if (info.publishedAt) {
      document.getElementById('update-modal-date').textContent = '📅 ' + info.publishedAt.substring(0, 10);
    }
    if (info.apkSize > 0) {
      document.getElementById('update-modal-size').textContent = '📦 ' + (info.apkSize / (1024 * 1024)).toFixed(1) + ' MB';
    }

    const changelogEl = document.getElementById('update-modal-changelog');
    if (changelogEl) {
      changelogEl.innerHTML = formatMarkdown(info.changelogBody || 'No release notes provided.');
    }

    const progressContainer = document.getElementById('update-progress-container');
    if (progressContainer) progressContainer.style.display = 'none';

    const actionBtn = document.getElementById('update-modal-action-btn');
    if (actionBtn) {
      actionBtn.disabled = false;
      actionBtn.innerHTML = '<span>🚀 Update & Install</span>';
    }

    modal.style.display = 'flex';
  }

  function hideUpdateModal() {
    try { playSFX('tb_clicks'); } catch (e) {}
    const modal = document.getElementById('caspian-update-modal');
    if (modal) modal.style.display = 'none';
  }

  function showUpToDateModal(info) {
    const modal = document.getElementById('caspian-uptodate-modal');
    if (!modal) return;
    try { playSFX('tb_clicks'); } catch (e) {}

    const curVer = window.CaspianBridge && typeof window.CaspianBridge.getAppVersion === 'function' ? window.CaspianBridge.getAppVersion() : '1.1.42-BetaC';
    const tag = (info && info.tagName) ? info.tagName : ('v' + curVer);

    const curEl = document.getElementById('uptodate-current-ver');
    if (curEl) curEl.textContent = 'v' + curVer;

    const gitEl = document.getElementById('uptodate-github-tag');
    if (gitEl) gitEl.textContent = tag;

    modal.style.display = 'flex';
  }

  function hideUpToDateModal() {
    try { playSFX('tb_clicks'); } catch (e) {}
    const modal = document.getElementById('caspian-uptodate-modal');
    if (modal) modal.style.display = 'none';
  }

  // Update Check Callback from Android Java
  window.onUpdateCheckResult = function (info) {
    console.log('Update check result:', info);
    latestUpdateInfo = info;
    const checkBtnText = document.getElementById('check-updates-btn-text');
    if (checkBtnText) checkBtnText.textContent = 'Check Updates';

    const statusSub = document.getElementById('updater-status-sub');
    const statusDot = document.getElementById('updater-status-dot');
    const curVer = window.CaspianBridge && typeof window.CaspianBridge.getAppVersion === 'function' ? window.CaspianBridge.getAppVersion() : '1.1.42-BetaC';

    if (info && info.hasUpdate) {
      if (statusSub) statusSub.textContent = `🌟 Update Available: v${info.cleanVersion} • Tap to view notes & install.`;
      if (statusDot) statusDot.style.background = '#10b981';

      // Pulsating badge in header brand tag
      const brandTags = document.querySelectorAll('.sheet-brand-tag');
      brandTags.forEach(el => {
        el.innerHTML = `V${curVer} <span style="background:#10b981; color:#000; font-size:9px; padding:1px 5px; border-radius:4px; margin-left:4px; font-weight:800;">NEW v${info.cleanVersion}</span>`;
        el.style.cursor = 'pointer';
        el.onclick = () => showUpdateModal(latestUpdateInfo);
      });

      showUpdateModal(info);
    } else {
      if (statusSub) statusSub.textContent = `✅ App is up to date (V${curVer})`;
      showUpToDateModal(info);
    }
  };

  window.onUpdateCheckError = function (msg) {
    const checkBtnText = document.getElementById('check-updates-btn-text');
    if (checkBtnText) checkBtnText.textContent = 'Check Updates';
    const statusSub = document.getElementById('updater-status-sub');
    if (statusSub) statusSub.textContent = 'Update check failed: ' + (msg || 'Network error');
  };

  window.onUpdateDownloadProgress = function (percent, downloadedBytes, totalBytes) {
    const progressContainer = document.getElementById('update-progress-container');
    if (progressContainer) progressContainer.style.display = 'block';

    const percentEl = document.getElementById('update-progress-percent');
    if (percentEl) percentEl.textContent = (percent >= 0 ? percent : 0) + '%';

    const barEl = document.getElementById('update-progress-bar');
    if (barEl) barEl.style.width = (percent >= 0 ? percent : 100) + '%';

    const textEl = document.getElementById('update-progress-text');
    if (textEl && totalBytes > 0) {
      textEl.textContent = `Downloading: ${(downloadedBytes / (1024 * 1024)).toFixed(1)} MB / ${(totalBytes / (1024 * 1024)).toFixed(1)} MB`;
    }
  };

  window.onUpdateDownloadComplete = function (initiated) {
    const textEl = document.getElementById('update-progress-text');
    if (textEl) textEl.textContent = '✅ Download complete! Prompting installer...';
    const actionBtn = document.getElementById('update-modal-action-btn');
    if (actionBtn) {
      actionBtn.innerHTML = '<span>✅ Ready to Install</span>';
      actionBtn.disabled = true;
    }
  };

  window.onUpdateDownloadError = function (err) {
    const textEl = document.getElementById('update-progress-text');
    if (textEl) textEl.textContent = '❌ Download failed: ' + err;
    const actionBtn = document.getElementById('update-modal-action-btn');
    if (actionBtn) {
      actionBtn.innerHTML = '<span>Retry Download</span>';
      actionBtn.disabled = false;
    }
  };

  // Immediate Setup for Updater UI listeners
  setTimeout(() => {
    const checkUpdatesBtn = document.getElementById('check-updates-btn');
    if (checkUpdatesBtn) {
      checkUpdatesBtn.addEventListener('click', () => {
        try {
          playSFX('tb_clicks');
        } catch (e) { }
        const checkBtnText = document.getElementById('check-updates-btn-text');
        if (checkBtnText) checkBtnText.textContent = 'Checking...';
        if (window.CaspianBridge && typeof window.CaspianBridge.checkForAppUpdates === 'function') {
          window.CaspianBridge.checkForAppUpdates(true);
        }
      });
    }

    const updateActionBtn = document.getElementById('update-modal-action-btn');
    if (updateActionBtn) {
      updateActionBtn.addEventListener('click', () => {
        if (!latestUpdateInfo || !latestUpdateInfo.apkDownloadUrl) {
          if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast('No APK download URL found.');
          }
          return;
        }
        try {
          playSFX('tb_clicks');
        } catch (e) { }
        updateActionBtn.disabled = true;
        updateActionBtn.innerHTML = '<span>⏳ Downloading...</span>';
        if (window.CaspianBridge && typeof window.CaspianBridge.downloadAndInstallUpdate === 'function') {
          window.CaspianBridge.downloadAndInstallUpdate(latestUpdateInfo.apkDownloadUrl, latestUpdateInfo.apkFileName);
        }
      });
    }

    const closeUpdateModalBtn = document.getElementById('close-update-modal-btn');
    if (closeUpdateModalBtn) closeUpdateModalBtn.addEventListener('click', hideUpdateModal);

    const updateModalLaterBtn = document.getElementById('update-modal-later-btn');
    if (updateModalLaterBtn) updateModalLaterBtn.addEventListener('click', hideUpdateModal);
  }, 200);

  function updateDebugRecUI() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.isDebugRecording === 'function') {
        isRecordingLogs = window.CaspianBridge.isDebugRecording();
      }
    } catch (e) { }

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

  // Tab Grouping, Multi-Select, & Filter State
  let tabGroups = [];
  try {
    const savedGroupsStr = localStorage.getItem('caspian_tab_groups');
    if (savedGroupsStr) tabGroups = JSON.parse(savedGroupsStr);
    if (window.CaspianBridge && typeof window.CaspianBridge.getPref === 'function') {
      const prefGroups = window.CaspianBridge.getPref('caspian_tab_groups', null);
      if (prefGroups) tabGroups = JSON.parse(prefGroups);
    }
  } catch (e) { tabGroups = []; }

  let isMultiSelectMode = false;
  let selectedTabIds = new Set();
  let activeGroupId = null;
  let selectedGroupColor = '#ef4444';
  let editingGroupId = null;
  let activeTabFilter = 'all'; // 'all', 'groups', 'single'
  let lastDeletedGroup = null;
  let cachedOpenTabs = [];

  function saveTabGroups() {
    try {
      const jsonStr = JSON.stringify(tabGroups);
      localStorage.setItem('caspian_tab_groups', jsonStr);
      if (window.CaspianBridge && typeof window.CaspianBridge.savePref === 'function') {
        window.CaspianBridge.savePref('caspian_tab_groups', jsonStr);
      }
    } catch (e) { }
  }

  function renderOpenTabs() {
    const container = document.getElementById('tabs-list-container');
    const countBadge = document.getElementById('tab-count-badge');
    const insideHeader = document.getElementById('inside-group-header');
    const groupToolbar = document.getElementById('floating-grouping-toolbar');
    if (!container) return;

    let tabs = [];
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getOpenTabs === 'function') {
        const jsonStr = window.CaspianBridge.getOpenTabs();
        if (jsonStr) {
          tabs = JSON.parse(jsonStr);
        }
      }
    } catch (e) { }
    cachedOpenTabs = tabs;

    if (countBadge) {
      countBadge.textContent = tabs.length === 1 ? '1 Tab' : `${tabs.length} Tabs`;
    }

    if (typeof updateUndoButtonState === 'function') {
      updateUndoButtonState();
    }

    if (tabs.length === 0) {
      container.innerHTML = '<div style="font-size: 12px; color: var(--text-sub); text-align: center; padding: 12px;">No active browser tabs open</div>';
      if (insideHeader) insideHeader.style.display = 'none';
      if (groupToolbar) groupToolbar.style.display = 'none';
      return;
    }

    // Floating Multi-Select Toolbar Visibility
    if (groupToolbar) {
      groupToolbar.style.display = isMultiSelectMode ? 'block' : 'none';
      const selectCount = document.getElementById('grouping-select-count');
      if (selectCount) selectCount.textContent = `${selectedTabIds.size} Selected`;
      const favBtn = document.getElementById('toolbar-favorite-btn');
      if (favBtn && isMultiSelectMode) {
        const selTabs = cachedOpenTabs.filter(t => selectedTabIds.has(t.id));
        const allFav = selTabs.length > 0 && selTabs.every(t => t.isFavorite);
        favBtn.textContent = allFav ? '★' : '⭐';
        favBtn.title = allFav ? 'Unfavorite Selected Tabs' : 'Favorite Selected Tabs';
      }
    }

    // Inside Group View Header
    const activeGroup = tabGroups.find(g => g.id === activeGroupId);
    if (activeGroup && insideHeader) {
      const groupTabs = tabs.filter(t => activeGroup.tabIds.includes(t.id));
      insideHeader.style.display = 'block';
      const colorDot = document.getElementById('group-banner-color-dot');
      const titleLabel = document.getElementById('group-banner-title');
      const countLabel = document.getElementById('group-banner-count');
      if (colorDot) colorDot.style.background = activeGroup.color || '#3b82f6';
      if (titleLabel) titleLabel.textContent = activeGroup.title || 'Tab Group';
      if (countLabel) countLabel.textContent = `${groupTabs.length} Tabs`;

      tabs = groupTabs; // Render only inner group tabs!
    } else {
      if (insideHeader) insideHeader.style.display = 'none';
    }

    let html = '<div class="tab-card-grid">';

    let selectedGroupColor = '#ef4444';
    let selectedGroupEmoji = '📁';

    // Render Group Cards if in Main View and filter allows groups
    if (!activeGroupId && (activeTabFilter === 'all' || activeTabFilter === 'groups')) {
      tabGroups.forEach(group => {
        const groupTabs = tabs.filter(t => group.tabIds.includes(t.id));
        if (groupTabs.length === 0) return; // Skip empty groups

        const isGroupActive = groupTabs.some(t => t.active);
        const activeBadge = isGroupActive ? '<span style="font-size: 9px; font-weight: 800; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 6px; border-radius: 6px;">ACTIVE</span>' : '';
        const groupFavBadge = group.isFavorite ? '<span style="color: #eab308; font-size: 11px; margin-right: 2px;" title="Favorited Group">⭐</span>' : '';
        const groupIcon = group.icon || '📁';

        html += `
          <div class="chrome-tab-card group-card ${isGroupActive ? 'active' : ''}" data-groupid="${group.id}" style="border-left: 6px solid ${group.color || '#3b82f6'};">
            <div class="chrome-tab-header">
              <div style="display: flex; align-items: center; gap: 6px; overflow: hidden;">
                ${groupFavBadge}
                <span style="font-size: 16px;">${groupIcon}</span>
                <span class="chrome-tab-title" style="font-weight: 800; color: ${group.color || 'var(--text-main)'};">${group.title || 'Tab Group'}</span>
              </div>
              <button class="chrome-group-menu-btn" data-groupmenuid="${group.id}" title="Group Options" style="background: none; border: none; font-size: 16px; color: var(--text-sub); cursor: pointer; padding: 2px 6px;">⋮</button>
            </div>
            <div class="chrome-tab-url" style="font-size: 11px; color: var(--text-sub); margin-top: 4px;">
              ${groupTabs.length} Open ${groupTabs.length === 1 ? 'Tab' : 'Tabs'} inside group
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
              <span style="font-size: 10px; font-weight: 700; color: var(--text-sub);">Tap to view inner tabs</span>
              <div>${activeBadge}</div>
            </div>
          </div>
        `;
      });
    }

    // Render Single Tabs if filter allows single tabs
    if (activeGroupId || activeTabFilter === 'all' || activeTabFilter === 'single') {
      const displayTabs = activeGroupId ? tabs : tabs.filter(t => !tabGroups.some(g => g.tabIds.includes(t.id)));
      const processedSplitTabs = new Set();

      const resolveTabFavicon = (t) => {
        let iconB64 = t.faviconB64 || '';
        const urlLower = (t.url || '').toLowerCase();
        const serviceLower = (t.service || '').toLowerCase();
        if (!iconB64) {
          if (serviceLower === 'gemini' || urlLower.includes('gemini.google.com')) iconB64 = window.GEMINI_ICON_B64 || '';
          else if (serviceLower === 'chatgpt' || urlLower.includes('chatgpt.com') || urlLower.includes('openai.com')) iconB64 = window.GPT_ICON_B64 || '';
          else if (serviceLower === 'google' || serviceLower === 'google_search' || urlLower.includes('google.com') || urlLower.includes('google.')) iconB64 = window.GOOGLE_ICON_B64 || '';
          else if (serviceLower === 'youtube' || urlLower.includes('youtube.com') || urlLower.includes('youtu.be')) iconB64 = window.YOUTUBE_ICON_B64 || '';
          else if (t.url && !t.url.startsWith('file:') && !t.url.startsWith('caspian:')) {
            try {
              const parsedHost = new URL(t.url).hostname;
              if (parsedHost) {
                iconB64 = `https://www.google.com/s2/favicons?domain=${parsedHost}&sz=64`;
              }
            } catch (e) {}
          }
        }
        return iconB64;
      };

      displayTabs.forEach(tab => {
        if (processedSplitTabs.has(tab.id)) return;

        const partnerId = tab.splitPartnerId;
        const partnerTab = (partnerId && partnerId !== -1) ? tabs.find(t => t.id === partnerId) : null;

        if (partnerTab && !activeGroupId) {
          processedSplitTabs.add(tab.id);
          processedSplitTabs.add(partnerTab.id);

          const isPairActive = tab.active || partnerTab.active || tab.isSplitActive || partnerTab.isSplitActive;
          const activeClass = isPairActive ? 'active' : '';
          const isSelected = selectedTabIds.has(tab.id) || selectedTabIds.has(partnerTab.id);
          const selectedClass = isSelected ? 'selected' : '';
          const selectCheckbox = isMultiSelectMode ? `<span style="font-size: 14px; margin-right: 4px;">${isSelected ? '☑️' : '⏹️'}</span>` : '';

          const icon1 = resolveTabFavicon(tab);
          const icon2 = resolveTabFavicon(partnerTab);

          const activeBadge = isPairActive ? '<span style="font-size: 9px; font-weight: 800; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 6px; border-radius: 6px;">ACTIVE</span>' : '';
          const splitBadge = `<span style="font-size: 9px; font-weight: 800; color: #00E5FF; background: rgba(0,229,255,0.18); border: 1px solid rgba(0,229,255,0.4); padding: 2px 6px; border-radius: 6px; margin-right: 4px;">🔀 SPLIT</span>`;

          const t1Title = tab.title || 'Tab 1';
          const t2Title = partnerTab.title || 'Tab 2';
          const subText = `${t1Title} • ${t2Title}`;

          const optionMenuBtn = `<button class="chrome-tab-menu-btn icon-btn" data-tabmenuid="${tab.id}" title="Tab Options" style="font-size: 14px; width: 22px; height: 22px; border: none; background: none; color: var(--text-sub); cursor: pointer; display: inline-flex; align-items: center; justify-content: center; margin-right: 2px;">⋮</button>`;

          const splitBorderStyle = 'border: 1.5px solid rgba(0, 229, 255, 0.6); box-shadow: 0 0 10px rgba(0, 229, 255, 0.2);';

          html += `
            <div class="chrome-tab-card ${activeClass} ${selectedClass} split-tab-active" data-tabid="${tab.id}" data-splittab1="${tab.id}" data-splittab2="${partnerTab.id}" style="${splitBorderStyle}">
              <div class="chrome-tab-header">
                <div style="display: flex; align-items: center; gap: 6px; overflow: hidden;">
                  ${selectCheckbox}
                  <div style="display: flex; align-items: center; gap: 3px; flex-shrink: 0;">
                    ${icon1 ? `<img src="${icon1}" style="width: 14px; height: 14px; border-radius: 3px; object-fit: cover;" onerror="this.style.display='none'" />` : ''}
                    ${icon2 ? `<img src="${icon2}" style="width: 14px; height: 14px; border-radius: 3px; object-fit: cover;" onerror="this.style.display='none'" />` : ''}
                  </div>
                  <span class="chrome-tab-title">Split Tabs</span>
                </div>
                <div style="display: flex; align-items: center; gap: 2px;">
                  ${optionMenuBtn}
                  <button class="chrome-tab-close" data-closeid="${tab.id}" title="Close Tab">&times;</button>
                </div>
              </div>
              <div class="chrome-tab-url" style="font-size: 10px; color: var(--text-sub); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px; display: flex; align-items: center; gap: 4px;">
                <span>${subText}</span>
              </div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
                <div style="display: flex; align-items: center; gap: 4px;"></div>
                <div style="display: flex; align-items: center;">${splitBadge}${activeBadge}</div>
              </div>
            </div>
          `;
          return;
        }
        let iconB64 = tab.faviconB64 || '';
        const urlLower = (tab.url || '').toLowerCase();
        const serviceLower = (tab.service || '').toLowerCase();
        if (!iconB64) {
          if (serviceLower === 'gemini' || urlLower.includes('gemini.google.com')) iconB64 = window.GEMINI_ICON_B64 || '';
          else if (serviceLower === 'chatgpt' || urlLower.includes('chatgpt.com') || urlLower.includes('openai.com')) iconB64 = window.GPT_ICON_B64 || '';
          else if (serviceLower === 'google' || serviceLower === 'google_search' || urlLower.includes('google.com') || urlLower.includes('google.')) iconB64 = window.GOOGLE_ICON_B64 || '';
          else if (serviceLower === 'youtube' || urlLower.includes('youtube.com') || urlLower.includes('youtu.be')) iconB64 = window.YOUTUBE_ICON_B64 || '';
          else if (tab.url && !tab.url.startsWith('file:') && !tab.url.startsWith('caspian:')) {
            try {
              const parsedHost = new URL(tab.url).hostname;
              if (parsedHost) {
                iconB64 = `https://www.google.com/s2/favicons?domain=${parsedHost}&sz=64`;
              }
            } catch (e) {}
          }
        }

        const isPdf = serviceLower === 'pdf' || urlLower.includes('pdf_viewer.html');
        const pdfBadge = isPdf ? '<span style="font-size: 8.5px; font-weight: 800; color: #f43f5e; background: rgba(244,63,94,0.15); border: 1px solid rgba(244,63,94,0.3); padding: 1px 5px; border-radius: 4px; margin-right: 3px;">PDF</span>' : '';

        const isSelected = selectedTabIds.has(tab.id);
        const selectedClass = isSelected ? 'selected' : '';
        const activeClass = tab.active ? 'active' : '';
        const splitClass = tab.isSplit ? 'split-tab-active' : '';
        const activeBadge = tab.active ? '<span style="font-size: 9px; font-weight: 800; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 6px; border-radius: 6px;">ACTIVE</span>' : '';
        const splitBadge = tab.isSplit ? `<span style="font-size: 9px; font-weight: 800; color: #00E5FF; background: rgba(0,229,255,0.18); border: 1px solid rgba(0,229,255,0.4); padding: 2px 6px; border-radius: 6px; margin-right: 4px;">🔀 SPLIT ${tab.splitRole === 'primary' ? 'PANE 1' : 'PANE 2'}</span>` : '';
        const selectCheckbox = isMultiSelectMode ? `<span style="font-size: 14px; margin-right: 4px;">${isSelected ? '☑️' : '⏹️'}</span>` : '';

        const isYoutubeTab = serviceLower === 'youtube' || urlLower.includes('youtube.com') || urlLower.includes('youtu.be');
        const isBgAudio = tab.isPlayingAudio && !tab.active;
        const isAudioActive = tab.isPlayingAudio === true;

        let audioBadge = '';
        if (isAudioActive) {
          audioBadge = `
            <button class="chrome-tab-mute-btn ${tab.isMuted ? 'muted' : 'playing'}" data-muteid="${tab.id}" title="${isBgAudio ? 'Playing Audio in Background - Tap to mute' : 'Playing Audio - Tap to mute'}" style="display: flex; align-items: center; gap: 4px; font-size: 9px; font-weight: 800; color: ${tab.isMuted ? '#f43f5e' : '#00E5FF'}; background: ${tab.isMuted ? 'rgba(244,63,94,0.15)' : 'rgba(0,229,255,0.18)'}; border: 1px solid ${tab.isMuted ? 'rgba(244,63,94,0.4)' : 'rgba(0,229,255,0.4)'}; border-radius: 6px; padding: 2px 7px; cursor: pointer;">
              <span>${tab.isMuted ? '🔇' : (isYoutubeTab ? '🎙️' : '🔊')}</span>
              <span>${tab.isMuted ? 'Muted' : (isBgAudio ? 'BG Audio' : 'Playing')}</span>
            </button>
          `;
        } else if (tab.isMuted === true) {
          audioBadge = `
            <button class="chrome-tab-mute-btn muted" data-muteid="${tab.id}" title="Unmute Tab" style="display: flex; align-items: center; gap: 4px; font-size: 9px; font-weight: 700; color: #f43f5e; background: rgba(244,63,94,0.15); border: 1px solid rgba(244,63,94,0.3); border-radius: 6px; padding: 2px 6px; cursor: pointer;">
              <span>🔇</span>
              <span>Muted</span>
            </button>
          `;
        }

        const ytMicBadge = (isYoutubeTab && tab.isPlayingAudio) ? `<span style="font-size: 11px; margin-right: 2px;" title="${isBgAudio ? 'YouTube Audio Playing in Background' : 'YouTube Audio Playing'}">🎙️</span>` : '';

        const isDefaultCask = !tab.caskId || tab.caskId === 'cask_caspian' || (tab.caskName && (tab.caskName.toLowerCase().includes('caspian') || tab.caskName.toLowerCase().includes('default')));
        const caskBadge = (!isDefaultCask && tab.caskIcon) ? `<span style="font-size: 9.5px; font-weight: 600; color: var(--text-muted); background: var(--input-bg, rgba(128,128,128,0.1)); border: none; padding: 2px 7px; border-radius: 6px; display: inline-flex; align-items: center; gap: 3px;" title="Container Vault: ${tab.caskName || 'Cask'}"><span>${tab.caskIcon}</span><span>${tab.caskName ? tab.caskName.split(' ')[0] : 'Cask'}</span></span>` : '';

        const favStarBadge = tab.isFavorite ? '<span style="color: #eab308; font-size: 11px; margin-right: 2px;" title="Favorited Tab">⭐</span>' : '';
        const optionMenuBtn = `<button class="chrome-tab-menu-btn icon-btn" data-tabmenuid="${tab.id}" title="Tab Options" style="font-size: 14px; width: 22px; height: 22px; border: none; background: none; color: var(--text-sub); cursor: pointer; display: inline-flex; align-items: center; justify-content: center; margin-right: 2px;">⋮</button>`;

        const splitBorderStyle = tab.isSplit ? 'border: 1.5px solid rgba(0, 229, 255, 0.6); box-shadow: 0 0 10px rgba(0, 229, 255, 0.2);' : '';

        html += `
          <div class="chrome-tab-card ${activeClass} ${selectedClass} ${splitClass}" data-tabid="${tab.id}" style="${splitBorderStyle}">
            <div class="chrome-tab-header">
              <div style="display: flex; align-items: center; gap: 6px; overflow: hidden;">
                ${selectCheckbox}
                ${ytMicBadge}
                ${favStarBadge}
                ${pdfBadge}
                ${(!isPdf && iconB64) ? `<img src="${iconB64}" style="width: 16px; height: 16px; border-radius: 4px; object-fit: cover;" onerror="this.style.display='none'" />` : ''}
                <span class="chrome-tab-title">${tab.title || 'Browser Tab'}</span>
              </div>
              <div style="display: flex; align-items: center; gap: 2px;">
                ${optionMenuBtn}
                <button class="chrome-tab-close" data-closeid="${tab.id}" title="Close Tab">&times;</button>
              </div>
            </div>
            <div class="chrome-tab-url" style="font-size: 10px; color: var(--text-sub); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px; display: flex; align-items: center; gap: 4px;">
              ${tab.nickname ? `🏷️ <strong style="color: #10b981;">${tab.nickname}</strong>` : tab.url || ''}
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
              <div style="display: flex; align-items: center; gap: 4px;">${audioBadge}${caskBadge}</div>
              <div style="display: flex; align-items: center;">${splitBadge}${activeBadge}</div>
            </div>
          </div>
        `;
      });
    }

    html += '</div>';
    container.innerHTML = html;

    // Bind Group Cards Click, Menu, & Swipe Right Listeners (Fix #3)
    container.querySelectorAll('.chrome-tab-card.group-card').forEach(card => {
      const groupId = card.dataset.groupid;
      const group = tabGroups.find(g => g.id === groupId);

      let touchStartX = 0;
      let diffX = 0;

      card.addEventListener('touchstart', (e) => {
        touchStartX = e.touches[0].clientX;
        diffX = 0;
      }, { passive: true });

      card.addEventListener('touchmove', (e) => {
        diffX = e.touches[0].clientX - touchStartX;
        if (diffX > 20) {
          card.style.transform = `translateX(${diffX}px)`;
        }
      }, { passive: true });

      card.addEventListener('touchend', () => {
        card.style.transform = '';
        if (diffX > 80 && group) {
          playSFX('tb_clicks');
          openGroupOptionsMenu(group);
        }
      });
      card.addEventListener('contextmenu', (e) => e.preventDefault());

      card.addEventListener('click', (e) => {
        if (e.target.classList.contains('chrome-group-menu-btn')) return;
        playSFX('tb_clicks');
        activeGroupId = groupId;
        renderOpenTabs();
      });

      const menuBtn = card.querySelector('.chrome-group-menu-btn');
      if (menuBtn && group) {
        menuBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          playSFX('tb_clicks');
          openGroupOptionsMenu(group);
        });
      }
    });

    container.querySelectorAll('.chrome-tab-menu-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        const tabId = parseInt(btn.dataset.tabmenuid);
        const tab = tabs.find(t => t.id === tabId);
        if (tab) openTabOptionsMenu(tab);
      });
    });

    container.querySelectorAll('.chrome-tab-mute-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        const muteId = parseInt(btn.dataset.muteid);
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleTabMute === 'function') {
          window.CaspianBridge.toggleTabMute(muteId);
          setTimeout(renderOpenTabs, 100);
        }
      });
    });

    window.renderOpenTabs = renderOpenTabs;

    // Bind Normal Tab Cards touch gestures (1-finger drag & drop grouping vs 2-finger multi-select)
    container.querySelectorAll('.chrome-tab-card:not(.group-card)').forEach(card => {
      let touchStartX = 0;
      let touchStartY = 0;
      let lastMoveX = 0;
      let lastMoveY = 0;
      let diffX = 0;
      let diffY = 0;
      let isSwipe = false;
      let isDrag = false;
      let pressTimer = null;
      let cachedTargets = [];

      const onTouchStart = (e) => {
        // 2-finger touch strictly activates Multi-Select mode
        if (e.touches.length >= 2) {
          clearTimeout(pressTimer);
          isDrag = false;
          const tabId = parseInt(card.dataset.tabid);
          if (!isMultiSelectMode) {
            isMultiSelectMode = true;
            selectedTabIds.clear();
            selectedTabIds.add(tabId);
            if (navigator.vibrate) navigator.vibrate(60);
            playSFX('tb_clicks');
            renderOpenTabs();
          }
          return;
        }

        const touch = e.touches[0];
        touchStartX = touch.clientX;
        touchStartY = touch.clientY;
        lastMoveX = touch.clientX;
        lastMoveY = touch.clientY;
        diffX = 0;
        diffY = 0;
        isSwipe = false;
        isDrag = false;

        clearTimeout(pressTimer);
        // Responsive 150ms press-and-hold to activate fluid tab dragging
        pressTimer = setTimeout(() => {
          if (!isSwipe && e.touches && e.touches.length === 1) {
            isDrag = true;
            card.classList.add('dragging');
            card.style.zIndex = '10000';
            card.style.pointerEvents = 'none'; // Pass-through touch events for 2D collision
            card.style.willChange = 'transform';
            card.style.transition = 'none';

            const tabId = parseInt(card.dataset.tabid);
            if (isMultiSelectMode && selectedTabIds.has(tabId)) {
              container.querySelectorAll('.chrome-tab-card').forEach(c => {
                const cId = parseInt(c.dataset.tabid);
                if (selectedTabIds.has(cId) && c !== card) {
                  c.classList.add('dragging');
                  c.style.opacity = '0.7';
                }
              });
            }

            cachedTargets = Array.from(container.querySelectorAll('.chrome-tab-card')).map(c => ({
              el: c,
              rect: c.getBoundingClientRect()
            }));

            const headerZone = document.getElementById('group-view-header') || document.getElementById('inside-group-header');
            if (activeGroupId && headerZone && headerZone.offsetParent !== null) {
              cachedTargets.push({
                el: headerZone,
                rect: headerZone.getBoundingClientRect(),
                isHeader: true
              });
            }

            if (navigator.vibrate) navigator.vibrate(40);
            playSFX('tb_clicks');
          }
        }, 150);
      };

      const onTouchMove = (e) => {
        if (e.touches.length >= 2) {
          clearTimeout(pressTimer);
          isDrag = false;
          return;
        }

        const touch = e.touches[0];
        lastMoveX = touch.clientX;
        lastMoveY = touch.clientY;
        diffX = touch.clientX - touchStartX;
        diffY = touch.clientY - touchStartY;
        const moveDist = Math.hypot(diffX, diffY);

        if (isDrag) {
          e.preventDefault(); // Lock scrolling during card drag
          card.style.transform = `translate3d(${diffX}px, ${diffY}px, 0) scale(1.06)`;

          // Fast 2D spatial collision detection for hover target & live space-giving displacement
          const touchX = touch.clientX;
          const touchY = touch.clientY;
          let currentTarget = null;

          for (let i = 0; i < cachedTargets.length; i++) {
            const item = cachedTargets[i];
            if (item.el === card) continue;
            const r = item.rect;
            if (touchX >= r.left && touchX <= r.right && touchY >= r.top && touchY <= r.bottom) {
              currentTarget = item.el;
              break;
            }
          }

          cachedTargets.forEach(item => {
            if (item.el === card) return;
            if (item.el === currentTarget) {
              if (!item.el.classList.contains('drop-target')) {
                item.el.classList.add('drop-target');
              }
              item.el.style.transform = 'scale(0.96) translateY(6px)';
              item.el.style.transition = 'transform 0.15s ease-out';
            } else {
              item.el.classList.remove('drop-target');
              item.el.style.transform = '';
              item.el.style.transition = 'transform 0.15s ease-out';
            }
          });
          return;
        }

        // Cancel timer only on clear scroll/swipe displacement
        if (!isDrag && moveDist > 12) {
          if (Math.abs(diffX) > Math.abs(diffY) * 2.0) {
            isSwipe = true;
          }
          clearTimeout(pressTimer);
        }

        if (isSwipe) {
          e.preventDefault();
          card.style.transform = `translateX(${diffX}px)`;
        }
      };

      const onTouchEnd = () => {
        clearTimeout(pressTimer);
        const wasDrag = isDrag;
        const wasSwipe = isSwipe;

        // Unconditionally unlock card state & clean all transforms
        card.classList.remove('dragging');
        card.style.zIndex = '';
        card.style.transform = '';
        card.style.pointerEvents = '';
        card.style.willChange = '';
        isDrag = false;

        container.querySelectorAll('.chrome-tab-card').forEach(c => {
          c.classList.remove('drop-target');
          c.classList.remove('dragging');
          c.style.transform = '';
          c.style.transition = '';
          c.style.opacity = '';
        });

        if (wasDrag) {
          const activeDropTarget = container.querySelector('.chrome-tab-card.drop-target, #group-view-header.drop-target, #inside-group-header.drop-target') ||
            (() => {
              const item = cachedTargets.find(t =>
                t.el !== card &&
                lastMoveX >= t.rect.left && lastMoveX <= t.rect.right &&
                lastMoveY >= t.rect.top && lastMoveY <= t.rect.bottom
              );
              return item ? item.el : null;
            })();

          container.querySelectorAll('.chrome-tab-card, #group-view-header, #inside-group-header').forEach(c => c.classList.remove('drop-target'));
          cachedTargets = [];

          if (activeDropTarget) {
            const sourceTabId = parseInt(card.dataset.tabid);

            // Case 0: Dropped onto Group View Header to remove tab from group
            if (activeDropTarget.id === 'group-view-header' || activeDropTarget.id === 'inside-group-header' || activeDropTarget.closest('#inside-group-header') || activeDropTarget.closest('#group-view-header')) {
              const currentGroup = tabGroups.find(g => g.id === activeGroupId);
              if (currentGroup) {
                const moveIds = (isMultiSelectMode && selectedTabIds.size > 0) ? Array.from(selectedTabIds) : [sourceTabId];
                currentGroup.tabIds = currentGroup.tabIds.filter(id => !moveIds.includes(id));
                saveTabGroups();
                if (currentGroup.tabIds.length === 0) {
                  activeGroupId = null;
                }
                isMultiSelectMode = false;
                selectedTabIds.clear();
                if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                  window.CaspianBridge.showToast(`Removed ${moveIds.length} tab(s) from "${currentGroup.title}"!`);
                }
              }
              setTimeout(renderOpenTabs, 50);
              return;
            }

            // Case A: Dropped onto an existing Group Card (.group-card)
            if (activeDropTarget.classList.contains('group-card')) {
              const targetGroupId = activeDropTarget.dataset.groupid;
              const targetGroup = tabGroups.find(g => g.id === targetGroupId);
              if (targetGroup) {
                const moveIds = (isMultiSelectMode && selectedTabIds.size > 0) ? Array.from(selectedTabIds) : [sourceTabId];
                moveIds.forEach(id => {
                  if (!targetGroup.tabIds.includes(id)) targetGroup.tabIds.push(id);
                });
                tabGroups.forEach(g => {
                  if (g.id !== targetGroupId) g.tabIds = g.tabIds.filter(id => !moveIds.includes(id));
                });
                saveTabGroups();
                isMultiSelectMode = false;
                selectedTabIds.clear();
                if (navigator.vibrate) navigator.vibrate(60);
                playSFX('ta');
                if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                  window.CaspianBridge.showToast(`Added ${moveIds.length} tab(s) to "${targetGroup.title}"!`);
                }
              }
              setTimeout(renderOpenTabs, 50);
              return;
            }

            // Case B: Dropped onto another Tab card in main view -> Reorder tabs (Single or Bulk Multi-Selected Tabs!)
            if (!activeGroupId && !activeDropTarget.classList.contains('group-card')) {
              const targetTabId = parseInt(activeDropTarget.dataset.tabid);
              if (sourceTabId !== targetTabId) {
                const allTabsJson = window.CaspianBridge && typeof window.CaspianBridge.getOpenTabs === 'function' ? window.CaspianBridge.getOpenTabs() : null;
                const allTabs = allTabsJson ? JSON.parse(allTabsJson) : [];

                if (isMultiSelectMode && selectedTabIds.size > 0) {
                  const selectedSet = new Set(selectedTabIds);
                  selectedSet.add(sourceTabId);

                  const selectedTabs = allTabs.filter(t => selectedSet.has(t.id));
                  const unselectedTabs = allTabs.filter(t => !selectedSet.has(t.id));

                  let insertIndex = unselectedTabs.findIndex(t => t.id === targetTabId);
                  if (insertIndex === -1) insertIndex = unselectedTabs.length;

                  unselectedTabs.splice(insertIndex, 0, ...selectedTabs);
                  const newIds = unselectedTabs.map(t => t.id);
                  if (window.CaspianBridge && typeof window.CaspianBridge.reorderTabs === 'function') {
                    window.CaspianBridge.reorderTabs(JSON.stringify(newIds));
                  }
                  isMultiSelectMode = false;
                  selectedTabIds.clear();
                  if (navigator.vibrate) navigator.vibrate(40);
                  playSFX('tb_clicks');
                  if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                    window.CaspianBridge.showToast(`Moved ${selectedTabs.length} tabs together!`);
                  }
                  setTimeout(renderOpenTabs, 50);
                  return;
                } else {
                  const sourceIdx = allTabs.findIndex(t => t.id === sourceTabId);
                  const targetIdx = allTabs.findIndex(t => t.id === targetTabId);

                  if (sourceIdx !== -1 && targetIdx !== -1 && sourceIdx !== targetIdx) {
                    const [moved] = allTabs.splice(sourceIdx, 1);
                    allTabs.splice(targetIdx, 0, moved);
                    const newIds = allTabs.map(t => t.id);
                    if (window.CaspianBridge && typeof window.CaspianBridge.reorderTabs === 'function') {
                      window.CaspianBridge.reorderTabs(JSON.stringify(newIds));
                    }
                    if (navigator.vibrate) navigator.vibrate(30);
                    playSFX('tb_clicks');
                    setTimeout(renderOpenTabs, 50);
                    return;
                  }
                }
              }
            }

            // Case C: Inside an active group -> Reorder tabs inside group (Single or Bulk Multi-Selected!)
            if (activeGroupId) {
              const currentGroup = tabGroups.find(g => g.id === activeGroupId);
              if (currentGroup) {
                const targetTabId = parseInt(activeDropTarget.dataset.tabid);
                if (isMultiSelectMode && selectedTabIds.size > 0) {
                  const selectedSet = new Set(selectedTabIds);
                  selectedSet.add(sourceTabId);
                  const selectedInGroup = currentGroup.tabIds.filter(id => selectedSet.has(id));
                  const unselectedInGroup = currentGroup.tabIds.filter(id => !selectedSet.has(id));
                  let insertIndex = unselectedInGroup.indexOf(targetTabId);
                  if (insertIndex === -1) insertIndex = unselectedInGroup.length;
                  unselectedInGroup.splice(insertIndex, 0, ...selectedInGroup);
                  currentGroup.tabIds = unselectedInGroup;
                  saveTabGroups();
                  isMultiSelectMode = false;
                  selectedTabIds.clear();
                  if (navigator.vibrate) navigator.vibrate(40);
                  playSFX('tb_clicks');
                  if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                    window.CaspianBridge.showToast(`Moved ${selectedInGroup.length} tabs together!`);
                  }
                  setTimeout(renderOpenTabs, 50);
                  return;
                } else {
                  const sIdx = currentGroup.tabIds.indexOf(sourceTabId);
                  const tIdx = currentGroup.tabIds.indexOf(targetTabId);
                  if (sIdx !== -1 && tIdx !== -1 && sIdx !== tIdx) {
                    const [moved] = currentGroup.tabIds.splice(sIdx, 1);
                    currentGroup.tabIds.splice(tIdx, 0, moved);
                    saveTabGroups();
                    playSFX('tb_clicks');
                    setTimeout(renderOpenTabs, 50);
                    return;
                  }
                }
              }
            }
          }
          return;
        }

        if (wasSwipe) {
          if (diffX > 80) {
            const tabId = parseInt(card.dataset.tabid);
            const tab = tabs.find(t => t.id === tabId);
            if (tab) openTabOptionsMenu(tab);
          } else if (diffX < -80) {
            const tabId = parseInt(card.dataset.tabid);
            const tab = tabs.find(t => t.id === tabId);
            if (tab && tab.isFavorite) {
              playSFX('tb_alert');
              if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                window.CaspianBridge.showToast('⭐ Favorited tabs are locked. Unfavorite first to close.');
              }
              card.style.transition = 'transform 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
              card.style.transform = '';
            } else {
              triggerCloseTab(tabId);
            }
          }
          isSwipe = false;
        }
      };

      card.addEventListener('touchstart', onTouchStart, { passive: false });
      card.addEventListener('touchmove', onTouchMove, { passive: false });
      card.addEventListener('touchend', onTouchEnd);
      card.addEventListener('touchcancel', onTouchEnd);
      card.addEventListener('contextmenu', (e) => e.preventDefault());

      // Two-tap (double tap) vs single tap handling
      let lastCardTapTime = 0;
      let cardClickTimer = null;

      // Click Event for switching tab or toggling selection in multi-select mode
      card.addEventListener('click', (e) => {
        if (e.target.classList.contains('chrome-tab-close') || e.target.closest('.chrome-tab-close') || e.target.closest('.chrome-tab-menu-btn') || e.target.closest('.chrome-tab-mute-btn')) return;

        // If card was dragged or swiped significantly, ignore click
        if (Math.abs(diffX) > 15 || Math.abs(diffY) > 15) return;

        const tabId = parseInt(card.dataset.tabid);

        const now = Date.now();
        const timeSinceLastTap = now - lastCardTapTime;
        lastCardTapTime = now;

        // Two-Tap Gesture (Double Tap) activates or toggles multi-select!
        if (timeSinceLastTap < 350 && timeSinceLastTap > 0) {
          if (cardClickTimer) {
            clearTimeout(cardClickTimer);
            cardClickTimer = null;
          }
          if (navigator.vibrate) navigator.vibrate(50);
          playSFX('tb_clicks');
          if (!isMultiSelectMode) {
            isMultiSelectMode = true;
            selectedTabIds.clear();
            selectedTabIds.add(tabId);
          } else {
            if (selectedTabIds.has(tabId)) {
              selectedTabIds.delete(tabId);
              if (selectedTabIds.size === 0) isMultiSelectMode = false;
            } else {
              selectedTabIds.add(tabId);
            }
          }
          renderOpenTabs();
          return;
        }

        if (isMultiSelectMode) {
          playSFX('tb_clicks');
          if (selectedTabIds.has(tabId)) {
            selectedTabIds.delete(tabId);
            if (selectedTabIds.size === 0) {
              isMultiSelectMode = false;
            }
          } else {
            selectedTabIds.add(tabId);
          }
          renderOpenTabs();
          return;
        }

        // Single tap when not in multi-select mode: switch tab after brief delay to differentiate from double tap
        cardClickTimer = setTimeout(() => {
          container.querySelectorAll('.chrome-tab-card').forEach(c => {
            c.classList.remove('active');
          });
          card.classList.add('active');

          if (window.CaspianBridge && typeof window.CaspianBridge.switchTab === 'function') {
            window.CaspianBridge.switchTab(tabId);
            setTimeout(renderOpenTabs, 400);
          }
        }, 220);
      });
    });

    // Close buttons binding
    container.querySelectorAll('.chrome-tab-close').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const tabId = parseInt(btn.dataset.closeid);
        const tab = (cachedOpenTabs || []).find(t => t.id === tabId);
        if (tab && tab.isFavorite) {
          playSFX('tb_alert');
          if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast('⭐ Favorited tabs are locked. Unfavorite first to close.');
          }
          return;
        }
        triggerCloseTab(tabId);
      });
    });
  }

  // Restore saved limit, power switch state, and tabs on load
  function restoreSavedSettings() {
    syncAppVersion();
    updateDebugRecUI();

    // Immediate theme synchronization on invocation
    try {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme) {
        setTheme(savedTheme);
      }
      const savedHeight = localStorage.getItem('saved_sheet_height') || '88vh';
      if (bottomSheet) {
        bottomSheet.style.height = savedHeight;
      }
    } catch (e) { }

    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
        const jsonStr = window.CaspianBridge.getSettings();
        if (jsonStr) {
          const prefs = JSON.parse(jsonStr);
          if (prefs.saved_sheet_height && bottomSheet) {
            bottomSheet.style.height = prefs.saved_sheet_height;
            localStorage.setItem('saved_sheet_height', prefs.saved_sheet_height);
          }
          if (prefs.limit !== undefined) {
            limitVal = parseInt(prefs.limit);
            document.querySelectorAll('.limit-pill').forEach(p => {
              const val = parseInt(p.dataset.val);
              p.classList.toggle('active', val === limitVal);
            });
            const activeBadge = document.getElementById('active-limit-badge');
            if (activeBadge) {
              activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Message' : 'Messages'}`;
            }
          }

          // Restore Chat Message Limit Enabled State
          const limitEnabled = prefs.chat_limit_enabled !== undefined ? (prefs.chat_limit_enabled === true || prefs.chat_limit_enabled === 'true') : (localStorage.getItem('chat_limit_enabled') === 'true');
          globalActive = limitEnabled;
          const statusDot = document.getElementById('status-dot');
          const statusTitle = document.getElementById('status-title');
          const toggleLimitBtn = document.getElementById('toggle-chat-limit-btn');
          if (statusDot) statusDot.classList.toggle('active', limitEnabled);
          if (statusTitle) statusTitle.textContent = limitEnabled ? 'Chat Message Limit: ON' : 'Chat Message Limit: OFF';
          if (toggleLimitBtn) {
            toggleLimitBtn.textContent = limitEnabled ? 'ON' : 'OFF';
            toggleLimitBtn.className = limitEnabled ? 'oneui-pill-btn primary' : 'oneui-pill-btn secondary';
          }

          // Restore AdBlocker State
          const adblockVal = prefs.adblock_enabled !== undefined ? (prefs.adblock_enabled === true || prefs.adblock_enabled === 'true') : (localStorage.getItem('adblock_enabled') !== 'false');
          const toggleAdblockBtn = document.getElementById('toggle-adblock-btn');
          const adblockDot = document.getElementById('adblock-dot');
          if (toggleAdblockBtn) {
            toggleAdblockBtn.textContent = adblockVal ? 'Enabled' : 'Disabled';
            toggleAdblockBtn.className = adblockVal ? 'oneui-pill-btn primary' : 'oneui-pill-btn secondary';
          }
          if (adblockDot) adblockDot.classList.toggle('active', adblockVal);

          // Restore AdBlocker Sub-options
          const adblockSubOptions = [
            { id: 'chk-adblock-yt', key: 'adblock_yt_enabled' },
            { id: 'chk-adblock-skip', key: 'adblock_yt_autoskip_enabled' },
            { id: 'chk-waveguard-trackers', key: 'waveguard_trackers', waveguardType: 'trackers' },
            { id: 'chk-waveguard-cosmetic', key: 'waveguard_cosmetic', waveguardType: 'cosmetic' },
            { id: 'chk-waveguard-defuser', key: 'waveguard_defuser', waveguardType: 'defuser' },
            { id: 'chk-waveguard-popups', key: 'waveguard_popups', waveguardType: 'popups' }
          ];

          adblockSubOptions.forEach(opt => {
            const el = document.getElementById(opt.id);
            if (el) {
              const isChk = (prefs && prefs[opt.key] !== undefined) ? (prefs[opt.key] !== 'false' && prefs[opt.key] !== false) : (localStorage.getItem(opt.key) !== 'false');
              el.checked = isChk;
              if (!el.dataset.bound) {
                el.dataset.bound = 'true';
                el.addEventListener('change', () => {
                  playSFX('tb_clicks');
                  const val = el.checked ? 'true' : 'false';
                  localStorage.setItem(opt.key, val);
                  if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                    window.CaspianBridge.saveSetting(opt.key, val);
                  }
                  if (opt.waveguardType && window.CaspianBridge && typeof window.CaspianBridge.setWaveguardSetting === 'function') {
                    window.CaspianBridge.setWaveguardSetting(opt.waveguardType, el.checked);
                  }
                  if (opt.waveguardType === 'trackers') {
                    localStorage.setItem('adblock_enabled', val);
                    updateEngineCardUI(cardAB, toggleAdblockBtn, null, document.getElementById('adblock-dot'), 'adblock_enabled');
                  }
                });
              }
            }
          });
          if (typeof window.syncWaveguardUI === 'function') window.syncWaveguardUI();

          const startCol = prefs.theme_start_color || localStorage.getItem('theme_start_color') || '#A2A9A9';
          const endCol = prefs.theme_end_color || localStorage.getItem('theme_end_color') || '#1B4264';
          const bgCol = prefs.theme_bg_color || localStorage.getItem('theme_bg_color') || '#050811';

          applyCustomGradient(startCol, endCol);
          applyCustomBg(bgCol);

          // Highlight matching Accent Preset button
          document.querySelectorAll('.preset-btn').forEach(btn => {
            const pKey = btn.dataset.preset;
            if (presets[pKey] && presets[pKey].start.toLowerCase() === startCol.toLowerCase()) {
              btn.classList.add('active');
            } else {
              btn.classList.remove('active');
            }
          });

          // Highlight matching Background Tone button
          document.querySelectorAll('.bg-preset-btn').forEach(btn => {
            const isMatch = btn.dataset.bg && btn.dataset.bg.toLowerCase() === bgCol.toLowerCase();
            btn.classList.toggle('active', isMatch);
          });

          if (prefs.theme_icon_shape) {
            selectedShapeVal = prefs.theme_icon_shape;
            var cssSelect = document.getElementById('custom-shape-select');
            if (cssSelect) {
              cssSelect.querySelectorAll('.caspian-select-option').forEach(o => {
                var isActive = o.dataset.val === selectedShapeVal;
                o.classList.toggle('active', isActive);
                if (isActive) {
                  var st = document.getElementById('selected-shape-text');
                  if (st) st.textContent = o.textContent;
                }
              });
            }
          }

          if (prefs.themeMode !== undefined) {
            setTheme(prefs.themeMode);
          } else {
            setTheme(localStorage.getItem('theme') || 'dark');
          }
          updateIconPreview(startCol, endCol, selectedShapeVal);

          // Restore visual preferences
          var openDur = prefs.sheetOpenDuration !== undefined ? parseInt(prefs.sheetOpenDuration) : 150;
          var txtOpenDur = document.getElementById('txt-open-dur');
          if (txtOpenDur) txtOpenDur.textContent = `${openDur} ms`;

          var closeDur = prefs.sheetCloseDuration !== undefined ? parseInt(prefs.sheetCloseDuration) : 150;
          var txtCloseDur = document.getElementById('txt-close-dur');
          if (txtCloseDur) txtCloseDur.textContent = `${closeDur} ms`;

          var tapDur = prefs.theme_button_tap_duration !== undefined ? parseInt(prefs.theme_button_tap_duration) : 100;
          var txtTapDur = document.getElementById('txt-tap-dur');
          if (txtTapDur) txtTapDur.textContent = `${tapDur} ms`;

          const actScale = (prefs && prefs.action_button_scale) || localStorage.getItem('action_button_scale') || '1.0';
          document.querySelectorAll('.btn-action-btn-scale').forEach(b => b.classList.toggle('active', b.dataset.scale === actScale));

          const ytScale = (prefs && prefs.yt_pod_scale) || localStorage.getItem('yt_pod_scale') || '1.0';
          document.querySelectorAll('.btn-yt-pod-scale').forEach(b => b.classList.toggle('active', b.dataset.scale === ytScale));

          const ytTimelineBehavior = (prefs && prefs.yt_timeline_default_behavior) || localStorage.getItem('yt_timeline_default_behavior') || 'fullscreen_only';
          document.querySelectorAll('.yt-timeline-mode-pill').forEach(b => {
            const isActive = b.dataset.behavior === ytTimelineBehavior;
            b.classList.toggle('active', isActive);
            b.classList.toggle('secondary', !isActive);
          });
          const ytTimelineBehaviorLabel = document.getElementById('yt-timeline-behavior-label');
          if (ytTimelineBehaviorLabel) {
            const ytTimelineLabelMap = {
              'fullscreen_only': 'Fullscreen Only',
              'both': 'Both Layouts',
              'vertical_only': 'Vertical Only',
              'manual_only': 'Off (Manual)'
            };
            ytTimelineBehaviorLabel.textContent = ytTimelineLabelMap[ytTimelineBehavior] || 'Fullscreen Only';
          }

          const googleScale = (prefs && prefs.google_dock_scale) || localStorage.getItem('google_dock_scale') || '1.0';
          document.querySelectorAll('.btn-google-dock-scale').forEach(b => b.classList.toggle('active', b.dataset.scale === googleScale));

          const chatgptScale = (prefs && prefs.chatgpt_dock_scale) || localStorage.getItem('chatgpt_dock_scale') || '1.0';
          document.querySelectorAll('.btn-chatgpt-dock-scale').forEach(b => b.classList.toggle('active', b.dataset.scale === chatgptScale));

          const geminiScale = (prefs && prefs.gemini_dock_scale) || localStorage.getItem('gemini_dock_scale') || '1.0';
          document.querySelectorAll('.btn-gemini-dock-scale').forEach(b => b.classList.toggle('active', b.dataset.scale === geminiScale));

          var animStyle = prefs.sheetAnimationStyle !== undefined ? prefs.sheetAnimationStyle : 'genie';
          var selectAnimStyle = document.getElementById('select-anim-style');
          if (selectAnimStyle) {
            selectAnimStyle.querySelectorAll('.caspian-select-option').forEach(o => {
              var isActive = o.dataset.val === animStyle;
              o.classList.toggle('active', isActive);
              if (isActive) {
                var label = document.getElementById('selected-anim-style-text');
                if (label) label.textContent = o.textContent;
              }
            });
          }

          // Restore Master Mute
          const savedMute = prefs.master_sfx_muted !== undefined ? (prefs.master_sfx_muted === true || prefs.master_sfx_muted === 'true') : (localStorage.getItem('master_sfx_muted') === 'true');
          setMasterMute(savedMute);

          // Restore SFX settings & Volume Slider
          let savedVol = 0.5;
          if (prefs.sfx_volume !== undefined && !isNaN(parseFloat(prefs.sfx_volume))) {
            savedVol = parseFloat(prefs.sfx_volume);
          } else if (localStorage.getItem('sfx_volume') !== null && !isNaN(parseFloat(localStorage.getItem('sfx_volume')))) {
            savedVol = parseFloat(localStorage.getItem('sfx_volume'));
          }
          sfxVolume = savedVol;
          const volSlider = document.getElementById('sfx-volume-slider');
          const volPercent = document.getElementById('sfx-volume-percent');
          if (volSlider && volPercent) {
            volSlider.value = sfxVolume;
            volPercent.textContent = Math.round(sfxVolume * 100) + '%';
          }

          const sfxKeys = [
            { id: 'toggle-sfx-tm-tabs', key: 'sfx_enabled_tm_tabs' },
            { id: 'toggle-sfx-ta', key: 'sfx_enabled_ta' },
            { id: 'toggle-sfx-tb-clicks', key: 'sfx_enabled_tb_clicks' },
            { id: 'toggle-sfx-tm-header', key: 'sfx_enabled_tm_header' },
            { id: 'toggle-sfx-tb-close', key: 'sfx_enabled_tb_close' },
            { id: 'toggle-sfx-tb-modal', key: 'sfx_enabled_tb_modal' }
          ];

          sfxKeys.forEach(item => {
            const el = document.getElementById(item.id);
            if (el) {
              const isChecked = prefs[item.key] !== 'false';
              el.checked = isChecked;
              localStorage.setItem(item.key, isChecked ? 'true' : 'false');
            }
          });

          // Restore SFX dropdown choices from Android Preferences and localStorage
          const sfxDropdownKeys = ['tm_tabs', 'ta', 'tb_clicks', 'tm_header', 'tb_close', 'tb_modal'];
          sfxDropdownKeys.forEach(k => {
            const sel = document.getElementById(`select-sfx-${k.replace('_', '-')}`);
            const savedVal = (prefs && prefs[`sfx_file_${k}`]) || localStorage.getItem(`sfx_file_${k}`) || sfxConfig[k];
            if (savedVal) {
              sfxConfig[k] = savedVal;
              localStorage.setItem(`sfx_file_${k}`, savedVal);
              if (sel) {
                sel.value = savedVal;
              }
            }

            if (sel && !sel.dataset.bound) {
              sel.dataset.bound = 'true';
              sel.addEventListener('change', () => {
                const chosen = sel.value;
                sfxConfig[k] = chosen;
                localStorage.setItem(`sfx_file_${k}`, chosen);
                if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                  window.CaspianBridge.saveSetting(`sfx_file_${k}`, chosen);
                }
                previewSFXFile(chosen);
              });
            }
          });

          sfxKeys.forEach(item => {
            const el = document.getElementById(item.id);
            if (el && !el.dataset.bound) {
              el.dataset.bound = 'true';
              el.addEventListener('change', () => {
                const isChecked = el.checked;
                localStorage.setItem(item.key, isChecked ? 'true' : 'false');
                if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                  window.CaspianBridge.saveSetting(item.key, isChecked ? 'true' : 'false');
                }
                if (isChecked) {
                  playSFX('tb_clicks');
                }
              });
            }
          });

          const btnSaveSfx = document.getElementById('btn-save-sfx-mapping');
          if (btnSaveSfx && !btnSaveSfx.dataset.bound) {
            btnSaveSfx.dataset.bound = 'true';
            btnSaveSfx.addEventListener('click', () => {
              sfxDropdownKeys.forEach(k => {
                const sel = document.getElementById(`select-sfx-${k.replace('_', '-')}`);
                if (sel) {
                  const val = sel.value;
                  sfxConfig[k] = val;
                  localStorage.setItem(`sfx_file_${k}`, val);
                  if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                    window.CaspianBridge.saveSetting(`sfx_file_${k}`, val);
                  }
                }
              });
              sfxKeys.forEach(item => {
                const el = document.getElementById(item.id);
                if (el) {
                  const isChecked = el.checked;
                  localStorage.setItem(item.key, isChecked ? 'true' : 'false');
                  if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                    window.CaspianBridge.saveSetting(item.key, isChecked ? 'true' : 'false');
                  }
                }
              });
              const testSfx = sfxConfig['ta'] || 'pop_click.mp3';
              previewSFXFile(testSfx);
              if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                window.CaspianBridge.showToast('✅ Sound Effects & SFX Mapping Saved!');
              }
            });
          }

          // Restore PDF Export Mode
          const pdfModeSelect = document.getElementById('pdf-export-mode-select');
          if (pdfModeSelect) {
            const savedMode = (prefs && prefs.pdfExportMode) || localStorage.getItem('pdfExportMode') || 'html';
            pdfModeSelect.value = savedMode;
            if (window.CaspianBridge && typeof window.CaspianBridge.setPdfExportMode === 'function') {
              window.CaspianBridge.setPdfExportMode(savedMode);
            }
          }

          // Restore Custom Dropdown Export Settings from preferences
          const exportKeys = {
            'export_chatgpt_normal': 'select-export-chatgpt-normal',
            'export_chatgpt_temp': 'select-export-chatgpt-temp',
            'export_gemini_normal': 'select-export-gemini-normal',
            'export_gemini_temp': 'select-export-gemini-temp',
            'active_refresh_rate': 'select-refresh-rate',
            'sheetAnimationStyle': 'select-anim-style'
          };

          for (const [prefKey, elementId] of Object.entries(exportKeys)) {
            const val = (prefs && prefs[prefKey]) || localStorage.getItem(prefKey);
            if (val) {
              const selectEl = document.getElementById(elementId);
              if (selectEl) {
                selectEl.querySelectorAll('.caspian-select-option').forEach(o => {
                  const isActive = o.dataset.val === val;
                  o.classList.toggle('active', isActive);
                  if (isActive) {
                    const st = selectEl.querySelector('.caspian-select-trigger span');
                    if (st) st.textContent = o.textContent;
                  }
                });
              }
            }
          }

          // Restore STT Engine Settings & Deepgram Usage Tracker
          window.updateDeepgramUsageBadge = function (totalSeconds) {
            const badge = document.getElementById('deepgram-usage-badge');
            if (badge) {
              const sec = parseInt(totalSeconds, 10) || 0;
              const cost = (sec * 0.000073).toFixed(3);
              badge.textContent = `⏱️ ${sec}s Used (~$${cost} / $200.00 Credit)`;
            }
          };

          const initialUsedSec = (prefs && prefs.deepgram_used_seconds) || localStorage.getItem('deepgram_used_seconds') || 0;
          window.updateDeepgramUsageBadge(initialUsedSec);

          // Restore STT Engine Settings using Caspian Pill Grid Buttons (.cc-stt-pill)
          const sttPills = document.querySelectorAll('.cc-stt-pill');
          if (sttPills.length > 0) {
            const updateSttKeyVisibility = (val) => {
              const dg = document.getElementById('stt-key-container-deepgram');
              const hf = document.getElementById('stt-key-container-huggingface');
              if (dg) dg.style.display = val === 'deepgram' ? 'block' : 'none';
              if (hf) hf.style.display = val === 'huggingface' ? 'block' : 'none';
            };

            const savedVal = (prefs && prefs.stt_engine_mode) || localStorage.getItem('stt_engine_mode') || 'deepgram';
            updateSttKeyVisibility(savedVal);

            sttPills.forEach(pill => {
              const isActive = pill.dataset.engine === savedVal;
              pill.classList.toggle('active', isActive);

              if (!pill.dataset.bound) {
                pill.dataset.bound = 'true';
                pill.addEventListener('click', (e) => {
                  e.stopPropagation();
                  if (typeof playSFX === 'function') playSFX('tb_clicks');
                  sttPills.forEach(p => p.classList.remove('active'));
                  pill.classList.add('active');

                  const newVal = pill.dataset.engine;
                  localStorage.setItem('stt_engine_mode', newVal);
                  if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                    window.CaspianBridge.saveSetting('stt_engine_mode', newVal);
                  }
                  updateSttKeyVisibility(newVal);

                  if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                    window.CaspianBridge.showToast(`🎙️ STT Engine set to ${pill.textContent}`);
                  }
                });
              }
            });
          }

          const bindSttKeyInput = (id, keyName) => {
            const input = document.getElementById(id);
            if (input) {
              const savedVal = (prefs && prefs[keyName]) || localStorage.getItem(keyName) || '';
              input.value = savedVal;
              input.onchange = () => {
                const val = input.value.trim();
                localStorage.setItem(keyName, val);
                if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
                  window.CaspianBridge.saveSetting(keyName, val);
                }
                if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
                  window.CaspianBridge.showToast(`Saved ${keyName}`);
                }
              };
            }
          };
          bindSttKeyInput('input-deepgram-key', 'deepgram_api_key');
          bindSttKeyInput('input-huggingface-key', 'huggingface_api_key');

          const visualsDetails = document.getElementById('settings-card-visuals');
          if (visualsDetails) {
            visualsDetails.open = localStorage.getItem('settings_open_visuals') === 'true';
            visualsDetails.addEventListener('toggle', () => {
              localStorage.setItem('settings_open_visuals', visualsDetails.open ? 'true' : 'false');
            });
          }

          const audioDetails = document.getElementById('settings-card-audio');
          if (audioDetails) {
            audioDetails.open = localStorage.getItem('settings_open_audio') === 'true';
            audioDetails.addEventListener('toggle', () => {
              localStorage.setItem('settings_open_audio', audioDetails.open ? 'true' : 'false');
            });
          }

          const advancedDetails = document.getElementById('settings-card-advanced');
          if (advancedDetails) {
            advancedDetails.open = localStorage.getItem('settings_open_advanced') === 'true';
            advancedDetails.addEventListener('toggle', () => {
              localStorage.setItem('settings_open_advanced', advancedDetails.open ? 'true' : 'false');
            });
          }
        }
      }
    } catch (e) { }

    renderOpenTabs();
    initDevExclusiveThemes();
    if (typeof window.updateDevHudCounters === 'function') {
      window.updateDevHudCounters();
    }
  }

  // Bind live audio preview when selecting dropdown choices
  document.querySelectorAll('.sfx-sound-select').forEach(sel => {
    sel.addEventListener('change', (e) => {
      e.stopPropagation();
      if (sel.value) {
        previewSFXFile(sel.value);
      }
    });
  });

  // Save SFX Mapping Button Handler
  const btnSaveSfxMapping = document.getElementById('btn-save-sfx-mapping');
  if (btnSaveSfxMapping) {
    btnSaveSfxMapping.addEventListener('click', () => {
      const sfxDropdownKeys = ['tm_tabs', 'ta', 'tb_clicks', 'tm_header', 'tb_close', 'tb_modal'];
      sfxDropdownKeys.forEach(k => {
        const sel = document.getElementById(`select-sfx-${k.replace('_', '-')}`);
        if (sel && sel.value) {
          const val = sel.value;
          sfxConfig[k] = val;
          localStorage.setItem(`sfx_file_${k}`, val);
          if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting(`sfx_file_${k}`, val);
          }
        }
      });
      playSFX('tm_header');
      if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
        window.CaspianBridge.showToast("💾 SFX mapping saved successfully!");
      }
    });
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
        if (targetDevCard) {
          targetDevCard.style.display = 'block';
          if (typeof window.updateDevHudCounters === 'function') window.updateDevHudCounters();
        }
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

  const CASPIAN_LEVELS = [
    { level: 1, req: 0, name: 'LVL 1 RIPPLE DRIFTER 💧' },
    { level: 2, req: 30, name: 'LVL 2 SHALLOW DIVER 🌊' },
    { level: 3, req: 80, name: 'LVL 3 STREAM NAVIGATOR 🚣' },
    { level: 4, req: 160, name: 'LVL 4 CASPIAN WAVE RIDER 🏄' },
    { level: 5, req: 270, name: 'LVL 5 TIDE WEAVER 🌊' },
    { level: 6, req: 420, name: 'LVL 6 CORAL EXPLORER 🪸' },
    { level: 7, req: 620, name: 'LVL 7 ABYSSAL VOYAGER 🌌' },
    { level: 8, req: 880, name: 'LVL 8 CASPIAN LEVIATHAN 🐋' },
    { level: 9, req: 1200, name: 'LVL 9 POSEIDON\'S CHOSEN 🔱' },
    { level: 10, req: 1600, name: 'LVL 10 SOVEREIGN OF CASPIAN 👑' },
    { level: 11, req: 2200, name: 'LVL 11 MYTHIC OCEAN LORD 🌟' },
    { level: 12, req: 3000, name: 'LVL 12 ETERNAL DEPTH MASTER 💎' }
  ];

  function getCaspianLevelInfo(clicks) {
    let currentIdx = 0;
    for (let i = 0; i < CASPIAN_LEVELS.length; i++) {
      if (clicks >= CASPIAN_LEVELS[i].req) {
        currentIdx = i;
      } else {
        break;
      }
    }
    const current = CASPIAN_LEVELS[currentIdx];
    const next = CASPIAN_LEVELS[currentIdx + 1] || { req: current.req + 1000, name: `LVL ${current.level + 1} TRANSCENDENT TITAN ⚡` };
    const range = Math.max(1, next.req - current.req);
    const progress = Math.max(0, clicks - current.req);
    const pct = Math.min(100, Math.max(0, Math.floor((progress / range) * 100)));
    return {
      level: current.level,
      name: current.name,
      currentReq: current.req,
      nextReq: next.req,
      pct: pct
    };
  }

  const LEVEL_UP_QUOTES = [
    `Oh boy, you have reached LVL 1 RIPPLE DRIFTER! You are a real Caspianer! 🌊`,
    `Level 2 Unlocked! Shallow Diver navigating the digital currents! 🌊`,
    `Level 3 Stream Navigator! Rowing smoothly across the Caspian! 🚣`,
    `Level 4 Wave Rider! Pure Caspian resonance, mastering the tides! 🏄`,
    `Level 5 Tide Weaver achieved! Man is truly committed to the flow! ⚡`,
    `Level 6 Coral Explorer! Discovering secrets of the lake bottom! 🪸`,
    `Level 7 Abyssal Voyager! The tides of Caspian obey your every click! 🌌`,
    `Level 8 Leviathan! Caspian Sea waters flow straight through your veins! 🐋`,
    `Level 9 Poseidon's Chosen! Lord of the Caspian Tides and Waves! 🔱`,
    `Level 10 Sovereign of Caspian! Supreme Ocean King of the Flow! 👑`,
    `Level 11 Mythic Ocean Lord! Nigel is weeping tears of joy! 🌟`,
    `Level 12 Depth Master! The Caspian Sea bows to your greatness! 💎`
  ];

  async function sha256Hex(str) {
    try {
      const msgBuffer = new TextEncoder().encode(str.trim());
      const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    } catch (e) {
      let hash = 0;
      for (let i = 0; i < str.length; i++) {
        hash = ((hash << 5) - hash) + str.charCodeAt(i);
        hash |= 0;
      }
      return String(hash);
    }
  }

  // Developer Master Key Hash (SHA-256 of secret developer passkey)
  const DEV_MASTER_KEY_HASH = '6c7adc1f0cba6da6fde065d70004998b2f922cc0bddfe24b4980f3a3350ef220';

  window.updateDevHudCounters = function() {
    let clicks = 0;
    if (window.CaspianBridge && typeof window.CaspianBridge.getActionButtonClicks === 'function') {
      clicks = window.CaspianBridge.getActionButtonClicks() || 0;
    } else {
      clicks = parseInt(localStorage.getItem('action_btn_click_count') || '0', 10);
    }
    const cycles = Math.floor(clicks / 2);
    const totalEl = document.getElementById('dev-hud-total-clicks');
    const cyclesEl = document.getElementById('dev-hud-total-cycles');
    const tierEl = document.getElementById('dev-hud-tier');
    const barEl = document.getElementById('dev-hud-progress-bar');
    const pctEl = document.getElementById('dev-hud-synergy-pct');
    const subEl = document.getElementById('dev-hud-subtext');

    if (totalEl) totalEl.textContent = clicks.toLocaleString();
    if (cyclesEl) cyclesEl.textContent = cycles.toLocaleString();

    const info = getCaspianLevelInfo(clicks);
    if (tierEl) tierEl.textContent = info.name;
    if (barEl) barEl.style.width = info.pct + '%';
    if (pctEl) pctEl.textContent = `${info.pct}% to LVL ${info.level + 1}`;

    const savedLevel = parseInt(localStorage.getItem('caspian_fan_level') || '1', 10);
    let currentQuote = localStorage.getItem('caspian_fan_quote');

    if (info.level > savedLevel || !currentQuote) {
      currentQuote = LEVEL_UP_QUOTES[info.level - 1] || `Level ${info.level} achieved! You are a supreme Caspian legend! 👑`;
      localStorage.setItem('caspian_fan_level', String(info.level));
      localStorage.setItem('caspian_fan_quote', currentQuote);
      if (info.level > savedLevel) {
        playSFX('ta');
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`🎉 LEVEL UP! You reached ${info.name}!`);
        }
      }
    }

    if (subEl) {
      subEl.textContent = currentQuote;
    }
  };

  function initDevExclusiveThemes() {
    // Cosmic Gemini
    const geminiBtn = document.querySelector('.cosmic-gemini-chip');
    if (geminiBtn && !geminiBtn.dataset.bound) {
      geminiBtn.dataset.bound = 'true';
      geminiBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        document.documentElement.removeAttribute('data-theme-preset');
        document.body.classList.remove('theme-ena-shine');
        localStorage.removeItem('theme_preset');
        applyCustomGradient('#7c3aed', '#1e1b4b');
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("✨ Cosmic Gemini Theme Activated!");
        }
      });
    }

    // Ena Shine (Golden Premium)
    const enaBtn = document.querySelector('.ena-shine-chip');
    if (enaBtn && !enaBtn.dataset.bound) {
      enaBtn.dataset.bound = 'true';
      enaBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        document.documentElement.setAttribute('data-theme-preset', 'ena_shine');
        document.body.classList.add('theme-ena-shine');
        localStorage.setItem('theme_preset', 'ena_shine');
        applyCustomGradient('#fbbf24', '#78350f');
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("👑 Ena Shine (Golden Premium) Activated! ✨");
        }
      });
    }
  }

  // Theme Presets Map
  const presets = {
    caspian: { start: '#A2A9A9', end: '#1B4264' },
    flow: { start: '#627D94', end: '#183652' },
    cyan: { start: '#06b6d4', end: '#0891b2' },
    violet: { start: '#a855f7', end: '#7c3aed' },
    azure: { start: '#3b82f6', end: '#1d4ed8' },
    emerald: { start: '#10b981', end: '#047857' }
  };

  function applyCustomGradient(start, end) {
    document.documentElement.style.setProperty('--accent', start, 'important');
    document.documentElement.style.setProperty('--secondary', end, 'important');
    document.documentElement.style.setProperty('--accent-glow', `${start}55`, 'important');
    document.documentElement.style.setProperty('--accent-gradient', `linear-gradient(135deg, ${start}, ${end})`, 'important');

    if (startPicker) startPicker.value = start;
    if (endPicker) endPicker.value = end;
    if (startHex) startHex.value = start.toUpperCase();
    if (endHex) endHex.value = end.toUpperCase();

    if (start.toLowerCase() === '#fbbf24' && end.toLowerCase() === '#78350f') {
      document.documentElement.setAttribute('data-theme-preset', 'ena_shine');
      document.body.classList.add('theme-ena-shine');
      document.body.classList.remove('theme-flow');
      localStorage.setItem('theme_preset', 'ena_shine');
    } else if (start.toLowerCase() === '#627d94' && end.toLowerCase() === '#183652') {
      document.documentElement.setAttribute('data-theme-preset', 'flow');
      document.body.classList.add('theme-flow');
      document.body.classList.remove('theme-ena-shine');
      localStorage.setItem('theme_preset', 'flow');
    } else {
      document.documentElement.removeAttribute('data-theme-preset');
      document.body.classList.remove('theme-ena-shine');
      document.body.classList.remove('theme-flow');
      localStorage.removeItem('theme_preset');
    }

    localStorage.setItem('theme_start_color', start);
    localStorage.setItem('theme_end_color', end);
    if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
      window.CaspianBridge.saveSetting('theme_start_color', start);
      window.CaspianBridge.saveSetting('theme_end_color', end);
    }

    const shapeSelect = document.getElementById('icon-shape-select');
    updateIconPreview(start, end, selectedShapeVal);
  }


  var selectedShapeVal = 'circle';

  function updateIconPreview(start, end, shape) {
    const box = document.getElementById('icon-preview-box');
    if (!start) {
      const sp = document.getElementById('gradient-start-picker');
      start = sp ? sp.value : '#A2A9A9';
    }
    if (!end) {
      const ep = document.getElementById('gradient-end-picker');
      end = ep ? ep.value : '#1B4264';
    }
    if (!shape) {
      shape = selectedShapeVal;
    }

    if (box) {
      box.style.background = `linear-gradient(135deg, ${start}, ${end})`;
      if (shape === 'squircle') box.style.borderRadius = '12px';
      else if (shape === 'rounded') box.style.borderRadius = '8px';
      else if (shape === 'circle') box.style.borderRadius = '50%';
      else if (shape === 'square') box.style.borderRadius = '2px';
    }
    try {
      localStorage.setItem('caspian_icon_shape', shape);
      if (window.CaspianBridge && typeof window.CaspianBridge.updateFloatingTheme === 'function') {
        window.CaspianBridge.updateFloatingTheme(start, end, shape);
      }
    } catch (e) { }
  }

  // Custom Dropdown Handling (Action Button Shape)
  var customShapeSelect = document.getElementById('custom-shape-select');
  var selectedShapeText = document.getElementById('selected-shape-text');

  if (customShapeSelect && selectedShapeText) {
    customShapeSelect.querySelector('.caspian-select-trigger').addEventListener('click', (e) => {
      e.stopPropagation();
      document.querySelectorAll('.caspian-select').forEach(other => {
        if (other !== customShapeSelect) other.classList.remove('open');
      });
      customShapeSelect.classList.toggle('open');
    });

    customShapeSelect.querySelectorAll('.caspian-select-option').forEach(opt => {
      opt.addEventListener('click', (e) => {
        e.stopPropagation();
        customShapeSelect.querySelectorAll('.caspian-select-option').forEach(o => o.classList.remove('active'));
        opt.classList.add('active');
        selectedShapeVal = opt.dataset.val;
        selectedShapeText.textContent = opt.textContent;
        customShapeSelect.classList.remove('open');

        var sp = document.getElementById('gradient-start-picker');
        var ep = document.getElementById('gradient-end-picker');
        updateIconPreview(sp ? sp.value : '#A2A9A9', ep ? ep.value : '#1B4264', selectedShapeVal);
      });
    });
  }

  // Caspian Export Configurator Dropdowns (ChatGPT & Gemini Normal / Temp Chat Engines)
  const exportSelectConfigs = [
    { id: 'select-export-chatgpt-normal', key: 'export_chatgpt_normal', defaultVal: 'api', name: 'ChatGPT Normal Chat' },
    { id: 'select-export-chatgpt-temp', key: 'export_chatgpt_temp', defaultVal: 'fiber', name: 'ChatGPT Temp Chat' },
    { id: 'select-export-gemini-normal', key: 'export_gemini_normal', defaultVal: 'sweeper', name: 'Gemini Normal Chat' },
    { id: 'select-export-gemini-temp', key: 'export_gemini_temp', defaultVal: 'sweeper', name: 'Gemini Temp Chat' }
  ];

  exportSelectConfigs.forEach(cfg => {
    const el = document.getElementById(cfg.id);
    if (!el) return;

    let savedVal = localStorage.getItem(cfg.key) || cfg.defaultVal;
    if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
      try {
        const s = JSON.parse(window.CaspianBridge.getSettings());
        if (s && s[cfg.key]) savedVal = s[cfg.key];
      } catch (e) {}
    }

    const triggerText = el.querySelector('.caspian-select-trigger span');
    el.querySelectorAll('.caspian-select-option').forEach(opt => {
      if (opt.dataset.val === savedVal) {
        opt.classList.add('active');
        if (triggerText) triggerText.textContent = opt.textContent;
      } else {
        opt.classList.remove('active');
      }
    });

    const trigger = el.querySelector('.caspian-select-trigger');
    if (trigger) {
      trigger.addEventListener('click', (e) => {
        e.stopPropagation();
        document.querySelectorAll('.caspian-select').forEach(other => {
          if (other !== el) other.classList.remove('open');
        });
        el.classList.toggle('open');
      });
    }

    el.querySelectorAll('.caspian-select-option').forEach(opt => {
      opt.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        el.querySelectorAll('.caspian-select-option').forEach(o => o.classList.remove('active'));
        opt.classList.add('active');
        const selectedVal = opt.dataset.val;
        if (triggerText) triggerText.textContent = opt.textContent;
        el.classList.remove('open');

        localStorage.setItem(cfg.key, selectedVal);
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting(cfg.key, selectedVal);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`⚙️ ${cfg.name}: ${opt.textContent.trim()}`);
        }
      });
    });
  });

  document.addEventListener('click', () => {
    document.querySelectorAll('.caspian-select').forEach(el => el.classList.remove('open'));
  });

  document.querySelectorAll('.preset-theme-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const s = chip.dataset.start;
      const e = chip.dataset.end;
      if (s && e) {
        applyCustomGradient(s, e);
        updateIconPreview(s, e, selectedShapeVal);
      }
    });
  });

  function applyCustomBg(colorHex) {
    if (activeTheme === 'dark') {
      selectedDarkBg = colorHex;
      document.documentElement.style.setProperty('--sheet-bg', colorHex, 'important');
    } else {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff', 'important');
    }
    if (bgColorPicker) bgColorPicker.value = colorHex;
    if (bgColorHex) bgColorHex.value = colorHex.toUpperCase();

    localStorage.setItem('theme_bg_color', colorHex);
    if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
      window.CaspianBridge.saveSetting('theme_bg_color', colorHex);
    }
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
      playSFX('tm_header');
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
      if (window.CaspianBridge && typeof window.CaspianBridge.toggleHostPageTheme === 'function') {
        window.CaspianBridge.toggleHostPageTheme(isDark);
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
    } catch (e) { }
  }

  // Theme Toggles (Default Light)
  function setTheme(t) {
    activeTheme = t || 'dark';
    document.documentElement.setAttribute('data-theme', activeTheme);
    document.documentElement.classList.toggle('dark', activeTheme === 'dark');
    document.documentElement.classList.toggle('light', activeTheme === 'light');
    if (activeTheme === 'light') {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff');
      document.documentElement.style.setProperty('--card-bg', '#ffffff');
      document.documentElement.style.setProperty('--bg-card', '#ffffff');
      document.documentElement.style.setProperty('--bg-deep', '#f8fafc');
      document.documentElement.style.setProperty('--text-main', '#0f172a');
      document.documentElement.style.setProperty('--text-muted', '#64748b');
      document.documentElement.style.setProperty('--border-glass', 'rgba(0, 0, 0, 0.08)');
      document.documentElement.style.setProperty('--input-bg', 'rgba(0, 0, 0, 0.04)');
    } else {
      document.documentElement.style.setProperty('--sheet-bg', selectedDarkBg);
      document.documentElement.style.setProperty('--card-bg', 'rgba(28, 37, 65, 0.75)');
      document.documentElement.style.setProperty('--bg-card', '#121824');
      document.documentElement.style.setProperty('--bg-deep', '#050811');
      document.documentElement.style.setProperty('--text-main', '#f8fafc');
      document.documentElement.style.setProperty('--text-muted', '#94a3b8');
      document.documentElement.style.setProperty('--border-glass', 'rgba(255, 255, 255, 0.12)');
      document.documentElement.style.setProperty('--input-bg', 'rgba(255, 255, 255, 0.06)');
    }
    if (themeBtnDark) themeBtnDark.classList.toggle('active', activeTheme === 'dark');
    if (themeBtnLight) themeBtnLight.classList.toggle('active', activeTheme === 'light');

    syncHostPageTheme(activeTheme);
    localStorage.setItem('theme', activeTheme);

    if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
      window.CaspianBridge.saveSetting('themeMode', activeTheme);
    }
  }

  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      playSFX('tm_header');
      setTheme(activeTheme === 'light' ? 'dark' : 'light');
    });
  }
  if (themeBtnDark) themeBtnDark.addEventListener('click', () => { playSFX('tm_header'); setTheme('dark'); });
  if (themeBtnLight) themeBtnLight.addEventListener('click', () => { playSFX('tm_header'); setTheme('light'); });

  // Resizable Drag Area Hitbox
  const targetDragArea = dragArea || document.querySelector('.sheet-drag-area') || document.querySelector('.sheet-drag-handle');
  if (targetDragArea && bottomSheet) {
    let startY, startHeight;
    let lastClientY = 0;
    let startTime = 0;

    targetDragArea.addEventListener('touchstart', (e) => {
      try { e.preventDefault(); } catch (err) { }
      const touch = e.touches[0];
      startY = touch.clientY;
      lastClientY = touch.clientY;
      startHeight = bottomSheet.offsetHeight;
      startTime = Date.now();
      bottomSheet.style.transition = 'none';
    }, { passive: false });

    targetDragArea.addEventListener('touchmove', (e) => {
      try { e.preventDefault(); } catch (err) { }
      const touch = e.touches[0];
      lastClientY = touch.clientY;
      const deltaY = startY - touch.clientY;
      const newHeight = startHeight + deltaY;
      const vhHeight = Math.max(20, Math.min(95, (newHeight / window.innerHeight) * 100));
      bottomSheet.style.height = vhHeight + 'vh';
      bottomSheet.style.maxHeight = '95vh';
    }, { passive: false });

    targetDragArea.addEventListener('touchend', () => {
      bottomSheet.style.transition = 'transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), height 0.3s ease';
      const displacementY = lastClientY - startY;
      const timeElapsed = Date.now() - startTime;
      const velocityY = displacementY / timeElapsed; // px/ms
      const currentHeightVh = (bottomSheet.offsetHeight / window.innerHeight) * 100;

      if ((displacementY > 120 && velocityY > 0.8) || currentHeightVh < 30) {
        // Dragged down quickly or all the way to bottom! Close the sheet natively.
        if (window.CaspianBridge && typeof window.CaspianBridge.closeSheet === 'function') {
          window.CaspianBridge.closeSheet();
        }
        setTimeout(() => {
          bottomSheet.style.height = localStorage.getItem('saved_sheet_height') || '88vh';
        }, 350);
      } else {
        // Snap to closest stable layout position (e.g. 50vh, 70vh, 88vh)
        let snapVh = 88;
        if (currentHeightVh < 55) {
          snapVh = 50;
        } else if (currentHeightVh < 75) {
          snapVh = 70;
        } else {
          snapVh = 88;
        }
        bottomSheet.style.height = snapVh + 'vh';
        localStorage.setItem('saved_sheet_height', snapVh + 'vh');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('saved_sheet_height', snapVh + 'vh');
        }
      }
    });
  }

  // Reload Active Tab WebView Button
  const reloadBtn = document.getElementById('reload-btn');
  if (reloadBtn) {
    reloadBtn.addEventListener('click', () => {
      playSFX('tm_header');
      if (window.CaspianBridge && typeof window.CaspianBridge.reloadActiveTab === 'function') {
        window.CaspianBridge.reloadActiveTab();
      }
    });
  }

  // Open Duration Plus/Minus
  const btnOpenDurMinus = document.getElementById('btn-open-dur-minus');
  const btnOpenDurPlus = document.getElementById('btn-open-dur-plus');
  const txtOpenDur = document.getElementById('txt-open-dur');

  if (btnOpenDurMinus && btnOpenDurPlus && txtOpenDur) {
    btnOpenDurMinus.addEventListener('click', () => {
      let val = parseInt(txtOpenDur.textContent) || 350;
      val = Math.max(0, val - 10);
      txtOpenDur.textContent = `${val} ms`;
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('sheetOpenDuration', val.toString());
      }
    });
    btnOpenDurPlus.addEventListener('click', () => {
      let val = parseInt(txtOpenDur.textContent) || 350;
      val = Math.min(1500, val + 10);
      txtOpenDur.textContent = `${val} ms`;
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('sheetOpenDuration', val.toString());
      }
    });
  }

  // Close Duration Plus/Minus
  const btnCloseDurMinus = document.getElementById('btn-close-dur-minus');
  const btnCloseDurPlus = document.getElementById('btn-close-dur-plus');
  const txtCloseDur = document.getElementById('txt-close-dur');

  if (btnCloseDurMinus && btnCloseDurPlus && txtCloseDur) {
    btnCloseDurMinus.addEventListener('click', () => {
      let val = parseInt(txtCloseDur.textContent) || 300;
      val = Math.max(0, val - 10);
      txtCloseDur.textContent = `${val} ms`;
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('sheetCloseDuration', val.toString());
      }
    });
    btnCloseDurPlus.addEventListener('click', () => {
      let val = parseInt(txtCloseDur.textContent) || 300;
      val = Math.min(1500, val + 10);
      txtCloseDur.textContent = `${val} ms`;
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('sheetCloseDuration', val.toString());
      }
    });
  }

  // Button Tap Bounce Duration Plus/Minus
  const btnTapDurMinus = document.getElementById('btn-tap-dur-minus');
  const btnTapDurPlus = document.getElementById('btn-tap-dur-plus');
  const txtTapDur = document.getElementById('txt-tap-dur');

  if (btnTapDurMinus && btnTapDurPlus && txtTapDur) {
    btnTapDurMinus.addEventListener('click', () => {
      let val = parseInt(txtTapDur.textContent) || 100;
      val = Math.max(0, val - 10);
      txtTapDur.textContent = `${val} ms`;
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('theme_button_tap_duration', val.toString());
      }
    });
    btnTapDurPlus.addEventListener('click', () => {
      let val = parseInt(txtTapDur.textContent) || 100;
      val = Math.min(500, val + 10);
      txtTapDur.textContent = `${val} ms`;
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('theme_button_tap_duration', val.toString());
      }
    });
  }

  // Animation Style Select Options
  const selectAnimStyle = document.getElementById('select-anim-style');
  if (selectAnimStyle) {
    const trigger = selectAnimStyle.querySelector('.caspian-select-trigger');
    const optionsContainer = selectAnimStyle.querySelector('.caspian-select-options');
    const label = document.getElementById('selected-anim-style-text');

    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      optionsContainer.classList.toggle('open');
    });

    selectAnimStyle.querySelectorAll('.caspian-select-option').forEach(opt => {
      opt.addEventListener('click', (e) => {
        e.stopPropagation();
        selectAnimStyle.querySelectorAll('.caspian-select-option').forEach(o => o.classList.remove('active'));
        opt.classList.add('active');
        const val = opt.dataset.val;
        label.textContent = opt.textContent;
        optionsContainer.classList.remove('open');

        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('sheetAnimationStyle', val);
        }
      });
    });

    document.addEventListener('click', () => {
      optionsContainer.classList.remove('open');
    });
  }

  function handleCreateNewTab(service) {
    playSFX('tb_clicks');
    if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
      window.CaspianBridge.createNewTab(service);
      setTimeout(() => {
        if (activeGroupId) {
          const group = tabGroups.find(g => g.id === activeGroupId);
          if (group && window.CaspianBridge && typeof window.CaspianBridge.getOpenTabs === 'function') {
            try {
              const openTabs = JSON.parse(window.CaspianBridge.getOpenTabs());
              if (openTabs.length > 0) {
                const latestTab = openTabs[openTabs.length - 1];
                if (!group.tabIds.includes(latestTab.id)) {
                  group.tabIds.push(latestTab.id);
                  saveTabGroups();
                }
              }
            } catch (e) { }
          }
        }
        renderOpenTabs();
      }, 150);
    }
  }


  // ==========================================
  // HARBOR TABS ENGINE (EDITABLE TABS & FAVICONS)
  // ==========================================

  const DEFAULT_HARBOR_TABS = [
    { id: 'harbor_hub', name: 'Caspian Hub', url: 'caspian://hub', service: 'hub', icon: 'hub', isDefault: true, isLocked: true },
    { id: 'harbor_chatgpt', name: 'ChatGPT', url: 'https://chatgpt.com/', service: 'chatgpt', icon: 'chatgpt', isDefault: true, isLocked: false },
    { id: 'harbor_gemini', name: 'Google Gemini', url: 'https://gemini.google.com/', service: 'gemini', icon: 'gemini', isDefault: true, isLocked: false },
    { id: 'harbor_google', name: 'Google Search', url: 'https://www.google.com/', service: 'google', icon: 'google', isDefault: true, isLocked: false },
    { id: 'harbor_youtube', name: 'YouTube', url: 'https://www.youtube.com/', service: 'youtube', icon: 'youtube', isDefault: true, isLocked: false }
  ];

  let harborTabs = JSON.parse(JSON.stringify(DEFAULT_HARBOR_TABS));
  let harborUndoStack = [];

  function pushHarborHistory() {
    try {
      harborUndoStack.push(JSON.parse(JSON.stringify(harborTabs)));
      if (harborUndoStack.length > 25) harborUndoStack.shift();
      updateHarborUndoButton();
    } catch (e) {}
  }

  function updateHarborUndoButton() {
    const btn = document.getElementById('btn-undo-harbor-edit');
    if (btn) {
      btn.style.display = (isHarborEditing && harborUndoStack.length > 0) ? 'inline-flex' : 'none';
    }
  }

  let isHarborEditing = false;
  let isHarborExpanded = false;

  function loadHarborTabs() {
    try {
      let saved = null;
      if (window.CaspianBridge && typeof window.CaspianBridge.getPref === 'function') {
        saved = window.CaspianBridge.getPref('caspian_harbor_tabs', null);
      }
      if (!saved) saved = localStorage.getItem('caspian_harbor_tabs');
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) {
          harborTabs = parsed;
        }
      }
    } catch (e) {}

    // Ensure Caspian Hub is ALWAYS at index 0 and locked
    const hubIdx = harborTabs.findIndex(t => t.id === 'harbor_hub' || t.service === 'hub');
    if (hubIdx === -1) {
      harborTabs.unshift({ id: 'harbor_hub', name: 'Caspian Hub', url: 'caspian://hub', service: 'hub', icon: 'hub', isDefault: true, isLocked: true });
    } else if (hubIdx > 0) {
      const [hubItem] = harborTabs.splice(hubIdx, 1);
      hubItem.isLocked = true;
      hubItem.isDefault = true;
      harborTabs.unshift(hubItem);
    } else {
      harborTabs[0].isLocked = true;
      harborTabs[0].isDefault = true;
    }
  }

  function saveHarborTabs() {
    try {
      const jsonStr = JSON.stringify(harborTabs);
      localStorage.setItem('caspian_harbor_tabs', jsonStr);
      if (window.CaspianBridge && typeof window.CaspianBridge.savePref === 'function') {
        window.CaspianBridge.savePref('caspian_harbor_tabs', jsonStr);
      }
    } catch (e) {}
  }

  function renderHarborTabs() {
    const grid = document.getElementById('harbor-tabs-grid');
    const banner = document.getElementById('harbor-edit-banner');
    if (!grid) return;

    if (banner) banner.style.display = isHarborEditing ? 'flex' : 'none';
    updateHarborUndoButton();

    let html = '';
    harborTabs.forEach((tab, index) => {
      let iconMarkup = '';
      if (tab.service === 'hub' || tab.icon === 'hub') {
        iconMarkup = `
          <div style="width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #1B4264, #A2A9A9); display: flex; align-items: center; justify-content: center; margin-bottom: 8px;">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="2.5">
              <path d="M4 7c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
              <path d="M4 12c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
              <path d="M4 17c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
            </svg>
          </div>
        `;
      } else if (tab.service === 'chatgpt' || tab.icon === 'chatgpt') {
        iconMarkup = `<img src="${window.GPT_ICON_B64 || ''}" class="app-icon-img" alt="${tab.name}" />`;
      } else if (tab.service === 'gemini' || tab.icon === 'gemini') {
        iconMarkup = `<img src="${window.GEMINI_ICON_B64 || ''}" class="app-icon-img" alt="${tab.name}" />`;
      } else if (tab.service === 'google' || tab.icon === 'google') {
        iconMarkup = `<img src="${window.GOOGLE_ICON_B64 || ''}" class="app-icon-img" alt="${tab.name}" />`;
      } else if (tab.service === 'youtube' || tab.icon === 'youtube') {
        iconMarkup = `<img src="${window.YOUTUBE_ICON_B64 || ''}" class="app-icon-img" alt="${tab.name}" />`;
      } else if (tab.icon && (tab.icon.startsWith('http') || tab.icon.startsWith('data:image'))) {
        iconMarkup = `<img src="${tab.icon}" class="app-icon-img" alt="${tab.name}" onerror="this.onerror=null;this.src='https://www.google.com/s2/favicons?domain=${encodeURIComponent(tab.url || '')}&sz=64';" />`;
      } else if (tab.icon && tab.icon.length <= 4) {
        iconMarkup = `
          <div style="width: 44px; height: 44px; border-radius: 12px; background: var(--input-bg); border: 1px solid var(--border-glass); display: flex; align-items: center; justify-content: center; margin-bottom: 8px; font-size: 22px;">
            ${tab.icon}
          </div>
        `;
      } else {
        let faviconUrl = '';
        try {
          const host = new URL(tab.url).hostname;
          faviconUrl = `https://www.google.com/s2/favicons?domain=${host}&sz=64`;
        } catch (e) {
          faviconUrl = '';
        }
        iconMarkup = faviconUrl
          ? `<img src="${faviconUrl}" class="app-icon-img" alt="${tab.name}" />`
          : `<div style="width: 44px; height: 44px; border-radius: 12px; background: var(--input-bg); display: flex; align-items: center; justify-content: center; margin-bottom: 8px; font-size: 20px;">⚓</div>`;
      }

      const isLocked = tab.isLocked === true;
      const editingClass = (isHarborEditing && !isLocked) ? 'editing-shake' : '';

      html += `
        <div class="app-icon-card harbor-tab-card ${editingClass}" data-harborid="${tab.id}" data-index="${index}">
          ${iconMarkup}
          <span class="app-icon-label">${tab.name}</span>
        </div>
      `;
    });

    grid.innerHTML = html;
    attachHarborCardListeners();
  }

  function triggerViolentLockedShake(card) {
    if (!card) return;
    try { playSFX('tb_alert'); } catch (err) {}
    if (navigator.vibrate) navigator.vibrate([60, 50, 80]);
    card.classList.remove('harbor-locked-shake');
    void card.offsetWidth;
    card.classList.add('harbor-locked-shake');
    setTimeout(() => {
      card.classList.remove('harbor-locked-shake');
    }, 460);

    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('🔒 Caspian Hub is a core platform tab and cannot be edited or removed.');
    }
  }

  function attachHarborCardListeners() {
    const grid = document.getElementById('harbor-tabs-grid');
    if (!grid) return;

    grid.querySelectorAll('.harbor-tab-card').forEach(card => {
      let touchStartX = 0;
      let touchStartY = 0;
      let lastMoveX = 0;
      let lastMoveY = 0;
      let diffX = 0;
      let diffY = 0;
      let longPressTimer = null;
      let pressTimer = null;
      let isDrag = false;
      let isSwipe = false;
      let cachedTargets = [];

      const idx = parseInt(card.dataset.index);
      const tab = harborTabs[idx];
      const isLocked = tab ? (tab.isLocked === true || tab.service === 'hub') : false;

      const onTouchStart = (e) => {
        const touch = e.touches[0];
        touchStartX = touch.clientX;
        touchStartY = touch.clientY;
        lastMoveX = touch.clientX;
        lastMoveY = touch.clientY;
        diffX = 0;
        diffY = 0;
        isDrag = false;
        isSwipe = false;

        clearTimeout(longPressTimer);
        clearTimeout(pressTimer);

        if (isHarborEditing) {
          // In editing mode: a short hold (~150ms) initiates drag reordering for editable tabs
          if (!isLocked) {
            cachedTargets = Array.from(grid.querySelectorAll('.harbor-tab-card')).map(el => ({
              el,
              rect: el.getBoundingClientRect(),
              index: parseInt(el.dataset.index)
            }));

            pressTimer = setTimeout(() => {
              isDrag = true;
              card.classList.add('dragging');
              card.style.zIndex = '999';
              card.style.animation = 'none'; // pause wiggle while dragging
              if (navigator.vibrate) navigator.vibrate(35);
              try { playSFX('tb_clicks'); } catch (err) {}
            }, 150);
          }
        } else {
          // In normal mode: 500ms long press toggles editing mode
          longPressTimer = setTimeout(() => {
            isHarborEditing = true;
            if (navigator.vibrate) navigator.vibrate(50);
            try { playSFX('tb_clicks'); } catch (err) {}
            renderHarborTabs();
          }, 500);
        }
      };

      const onTouchMove = (e) => {
        const touch = e.touches[0];
        lastMoveX = touch.clientX;
        lastMoveY = touch.clientY;
        diffX = touch.clientX - touchStartX;
        diffY = touch.clientY - touchStartY;
        const moveDist = Math.hypot(diffX, diffY);

        if (isDrag) {
          try { e.preventDefault(); } catch (err) {}
          card.style.transform = `translate3d(${diffX}px, ${diffY}px, 0) scale(1.08)`;

          // Spatial 2D collision detection with other harbor cards
          const touchX = touch.clientX;
          const touchY = touch.clientY;
          let currentTarget = null;

          for (let i = 0; i < cachedTargets.length; i++) {
            const item = cachedTargets[i];
            if (item.el === card) continue;
            // Caspian Hub (index 0) cannot be moved or displaced
            if (item.index === 0) continue;
            const r = item.rect;
            if (touchX >= r.left && touchX <= r.right && touchY >= r.top && touchY <= r.bottom) {
              currentTarget = item.el;
              break;
            }
          }

          cachedTargets.forEach(item => {
            if (item.el === card) return;
            if (item.el === currentTarget) {
              if (!item.el.classList.contains('drop-target')) {
                item.el.classList.add('drop-target');
              }
              item.el.style.transform = 'scale(0.92) translateY(4px)';
              item.el.style.transition = 'transform 0.15s ease-out';
            } else {
              item.el.classList.remove('drop-target');
              item.el.style.transform = '';
              item.el.style.transition = 'transform 0.15s ease-out';
            }
          });
          return;
        }

        // Cancel long press & drag timers on clear movement
        if (moveDist > 10) {
          clearTimeout(longPressTimer);
          if (!isDrag) clearTimeout(pressTimer);
        }

        // Detect horizontal swipe only when in editing mode
        if (isHarborEditing && !isDrag && moveDist > 16 && Math.abs(diffX) > Math.abs(diffY) * 1.4) {
          isSwipe = true;
          try { e.preventDefault(); } catch (err) {}
          card.style.transform = `translateX(${diffX}px)`;
        }
      };

      const onTouchEnd = () => {
        clearTimeout(longPressTimer);
        clearTimeout(pressTimer);
        const wasDrag = isDrag;
        const wasSwipe = isSwipe;

        // Reset drag visuals on current card
        card.classList.remove('dragging');
        card.style.zIndex = '';
        card.style.transform = '';
        card.style.animation = '';
        isDrag = false;

        // Reset drop targets
        grid.querySelectorAll('.harbor-tab-card').forEach(c => {
          c.classList.remove('drop-target');
          c.classList.remove('dragging');
          c.style.transform = '';
          c.style.transition = '';
        });

        // 1. Drag & Drop Reorder (only in editing mode)
        if (isHarborEditing && wasDrag) {
          const activeDropTarget = grid.querySelector('.harbor-tab-card.drop-target') || (() => {
            const item = cachedTargets.find(t =>
              t.el !== card && t.index > 0 &&
              lastMoveX >= t.rect.left && lastMoveX <= t.rect.right &&
              lastMoveY >= t.rect.top && lastMoveY <= t.rect.bottom
            );
            return item ? item.el : null;
          })();

          cachedTargets = [];

          if (activeDropTarget) {
            const sourceIdx = parseInt(card.dataset.index);
            const targetIdx = parseInt(activeDropTarget.dataset.index);

            if (sourceIdx !== -1 && targetIdx > 0 && sourceIdx !== targetIdx) {
              pushHarborHistory();
              const [moved] = harborTabs.splice(sourceIdx, 1);
              harborTabs.splice(targetIdx, 0, moved);
              saveHarborTabs();
              if (navigator.vibrate) navigator.vibrate(35);
              try { playSFX('tb_clicks'); } catch (err) {}
              renderHarborTabs();
              return;
            }
          }
          renderHarborTabs();
          return;
        }

        // 2. Swipe Gestures (strictly only in editing mode)
        if (isHarborEditing && wasSwipe) {
          isSwipe = false;

          // Swipe Left (< -70px): Delete / Remove Harbor Tab
          if (diffX < -70) {
            if (isLocked) {
              // Caspian Hub cannot be deleted! Violently shake to warn user
              triggerViolentLockedShake(card);
              card.style.transition = 'transform 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
              card.style.transform = '';
              return;
            }

            // Animate swipe-to-delete
            card.style.transition = 'transform 0.22s ease-out, opacity 0.22s ease-out';
            card.style.transform = 'translateX(-120%)';
            card.style.opacity = '0';

            pushHarborHistory();
            const name = tab ? tab.name : 'Tab';
            harborTabs.splice(idx, 1);
            saveHarborTabs();
            try { playSFX('tb_close'); } catch (err) {}
            if (navigator.vibrate) navigator.vibrate(40);
            if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
              window.CaspianBridge.showToast(`🗑️ Removed "${name}" from Harbor.`);
            }
            setTimeout(renderHarborTabs, 200);
            return;
          }

          // Swipe Right (> 70px): Open Tab Editor
          if (diffX > 70) {
            if (isLocked) {
              triggerViolentLockedShake(card);
              card.style.transition = 'transform 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
              card.style.transform = '';
              return;
            }
            card.style.transition = 'transform 0.25s ease';
            card.style.transform = '';
            if (tab) openHarborTabEditor(tab);
            return;
          }

          // Incomplete swipe -> snap back smoothly
          card.style.transition = 'transform 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
          card.style.transform = '';
          return;
        }

        // If not in editing mode, ensure transform is cleared
        card.style.transform = '';
      };

      card.addEventListener('touchstart', onTouchStart, { passive: false });
      card.addEventListener('touchmove', onTouchMove, { passive: false });
      card.addEventListener('touchend', onTouchEnd);
      card.addEventListener('touchcancel', () => {
        clearTimeout(longPressTimer);
        clearTimeout(pressTimer);
        isDrag = false;
        isSwipe = false;
        card.style.transform = '';
        card.classList.remove('dragging');
      });

      card.addEventListener('contextmenu', (e) => {
        e.preventDefault();
        if (!isHarborEditing) {
          isHarborEditing = true;
          try { playSFX('tb_clicks'); } catch (err) {}
          renderHarborTabs();
        }
      });

      // Click / Tap Event
      card.addEventListener('click', (e) => {
        if (Math.abs(diffX) > 15 || Math.abs(diffY) > 15) return;
        if (!tab) return;

        if (isHarborEditing) {
          if (isLocked) {
            // Caspian Hub is completely un-editable: violently shake instead of opening editor
            triggerViolentLockedShake(card);
            return;
          }
          // Normal editable tab: tapping in editing mode opens editor options
          openHarborTabEditor(tab);
          return;
        }

        // Normal mode: Launch tab
        try { playSFX('tb_clicks'); } catch (err) {}
        if (tab.service === 'hub') {
          if (window.CaspianBridge && typeof window.CaspianBridge.openLaunchHubInNewTab === 'function') {
            window.CaspianBridge.openLaunchHubInNewTab();
          }
        } else if (tab.service === 'google') {
          if (window.CaspianBridge && typeof window.CaspianBridge.openNewTab === 'function') {
            window.CaspianBridge.openNewTab('https://www.google.com/');
          }
        } else if (tab.service && ['chatgpt', 'gemini', 'youtube'].includes(tab.service)) {
          if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
            window.CaspianBridge.createNewTab(tab.service);
          } else if (window.CaspianBridge && typeof window.CaspianBridge.openNewTab === 'function') {
            window.CaspianBridge.openNewTab(tab.url);
          }
        } else {
          if (window.CaspianBridge && typeof window.CaspianBridge.openNewTab === 'function') {
            window.CaspianBridge.openNewTab(tab.url || 'https://google.com');
          }
        }
      });
    });
  }

  // Harbor Editing Banner Action Buttons: Done, Undo, Reset
  const btnDoneHarborEdit = document.getElementById('btn-done-harbor-edit');
  if (btnDoneHarborEdit) {
    btnDoneHarborEdit.addEventListener('click', (e) => {
      e.stopPropagation();
      try { playSFX('tb_clicks'); } catch (err) {}
      isHarborEditing = false;
      harborUndoStack = [];
      renderHarborTabs();
    });
  }

  const btnUndoHarborEdit = document.getElementById('btn-undo-harbor-edit');
  if (btnUndoHarborEdit) {
    btnUndoHarborEdit.addEventListener('click', (e) => {
      e.stopPropagation();
      if (harborUndoStack.length > 0) {
        const previousState = harborUndoStack.pop();
        harborTabs = previousState;
        saveHarborTabs();
        try { playSFX('tb_clicks'); } catch (err) {}
        if (navigator.vibrate) navigator.vibrate(30);
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast('↩️ Undid last Harbor action!');
        }
        renderHarborTabs();
        updateHarborUndoButton();
      }
    });
  }

  const btnResetHarborEdit = document.getElementById('btn-reset-harbor-edit');
  if (btnResetHarborEdit) {
    btnResetHarborEdit.addEventListener('click', (e) => {
      e.stopPropagation();
      pushHarborHistory();
      harborTabs = JSON.parse(JSON.stringify(DEFAULT_HARBOR_TABS));
      saveHarborTabs();
      try { playSFX('tb_close'); } catch (err) {}
      if (navigator.vibrate) navigator.vibrate(40);
      if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
        window.CaspianBridge.showToast('↺ Reset Harbor Tabs to default layout!');
      }
      renderHarborTabs();
      updateHarborUndoButton();
    });
  }

  // Expand / Shrink View Toggle Button
  const harborExpandBtn = document.getElementById('harbor-expand-btn');
  const harborContainer = document.getElementById('harbor-tabs-container');
  if (harborExpandBtn && harborContainer) {
    harborExpandBtn.addEventListener('click', () => {
      playSFX('tb_clicks');
      isHarborExpanded = !isHarborExpanded;
      if (isHarborExpanded) {
        harborContainer.classList.remove('harbor-container-compact');
        harborContainer.classList.add('harbor-container-expanded');
        harborExpandBtn.textContent = 'Shrink View ▴';
      } else {
        harborContainer.classList.remove('harbor-container-expanded');
        harborContainer.classList.add('harbor-container-compact');
        harborExpandBtn.textContent = 'Expand View ▾';
      }
    });
  }

  // Pin Tab to Harbor function (from Tab Options Menu)
  function pinTabToHarbor(tab) {
    if (!tab) return;
    const url = tab.url || '';
    if (!url) return;

    const existing = harborTabs.find(t => t.url && t.url.toLowerCase() === url.toLowerCase());
    if (existing) {
      playSFX('tb_alert');
      if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
        window.CaspianBridge.showToast(`⚓ "${existing.name}" is already in Harbor Tabs!`);
      }
      return;
    }

    let tabName = tab.nickname || tab.title || 'Web Tab';
    let icon = tab.faviconB64 || '';
    if (!icon) {
      try {
        const host = new URL(url).hostname;
        if (host) icon = `https://www.google.com/s2/favicons?domain=${host}&sz=64`;
      } catch (e) {}
    }

    const newHarborTab = {
      id: 'harbor_' + Date.now(),
      name: tabName,
      url: url,
      service: 'web',
      icon: icon,
      isDefault: false,
      isLocked: false
    };

    pushHarborHistory();
    harborTabs.push(newHarborTab);
    saveHarborTabs();
    playSFX('tb_clicks');
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast(`⚓ Pinned "${tabName}" to Harbor Tabs!`);
    }
    renderHarborTabs();
  }

  // ==========================================
  // HARBOR TAB EDITOR MODAL LOGIC
  // ==========================================

  let editingHarborTab = null;
  let harborEditorInitialized = false;

  function closeHarborModal() {
    try { playSFX('tb_modal'); } catch (e) {}
    const modal = document.getElementById('harbor-tab-editor-modal');
    if (modal) modal.style.display = 'none';
    editingHarborTab = null;
  }

  function updateHarborIconPreview(iconVal) {
    const previewEmoji = document.getElementById('harbor-icon-preview-emoji');
    const previewImg = document.getElementById('harbor-icon-preview-img');
    if (!previewEmoji || !previewImg) return;

    if (iconVal && (iconVal.startsWith('http') || iconVal.startsWith('data:image'))) {
      previewImg.src = iconVal;
      previewImg.style.display = 'block';
      previewEmoji.style.display = 'none';
    } else {
      previewImg.style.display = 'none';
      previewEmoji.style.display = 'block';
      previewEmoji.textContent = iconVal || '🌐';
    }
  }

  function updateUndoButtonState(forcedState) {
    const undoBtn = document.getElementById('undo-closed-tab-btn');
    if (!undoBtn) return;
    let hasUndo = false;
    if (typeof forcedState === 'boolean') {
      hasUndo = forcedState;
    } else {
      try {
        if (window.CaspianBridge && typeof window.CaspianBridge.hasClosedTabsToUndo === 'function') {
          hasUndo = window.CaspianBridge.hasClosedTabsToUndo();
        }
      } catch (e) {}
    }
    undoBtn.style.display = hasUndo ? 'inline-flex' : 'none';
  }
  window.updateUndoButtonState = updateUndoButtonState;

    function closeHarborEditorModal() {
    try { playSFX('tb_modal'); } catch (err) {}
    const modal = document.getElementById('harbor-tab-editor-modal');
    if (modal) {
      modal.style.display = 'none';
      modal.style.setProperty('display', 'none', 'important');
      modal.style.setProperty('pointer-events', 'none', 'important');
    }
    editingHarborTab = null;
  }

  function saveHarborEditorTab() {
    if (!editingHarborTab) return;
    try { playSFX('tb_clicks'); } catch (err) {}
    const nameInput = document.getElementById('harbor-edit-name-input');
    const urlInput = document.getElementById('harbor-edit-url-input');
    const iconInput = document.getElementById('harbor-edit-icon-input');
    pushHarborHistory();
    if (nameInput && nameInput.value.trim()) {
      editingHarborTab.name = nameInput.value.trim();
    }
    if (urlInput && urlInput.value.trim()) {
      let u = urlInput.value.trim();
      if (!u.startsWith('http://') && !u.startsWith('https://')) {
        u = 'https://' + u;
      }
      editingHarborTab.url = u;
    }
    if (iconInput && iconInput.value.trim()) {
      editingHarborTab.icon = iconInput.value.trim();
    }
    saveHarborTabs();
    closeHarborEditorModal();
    renderHarborTabs();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('⚓ Harbor Tab updated!');
    }
  }

  function deleteHarborEditorTab() {
    if (!editingHarborTab || editingHarborTab.isLocked) return;
    try { playSFX('tb_close'); } catch (err) {}
    pushHarborHistory();
    const name = editingHarborTab.name;
    harborTabs = harborTabs.filter(t => t.id !== editingHarborTab.id);
    saveHarborTabs();
    closeHarborEditorModal();
    renderHarborTabs();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast(`🗑️ Removed "${name}" from Harbor.`);
    }
  }

  function autoFetchHarborFavicon() {
    try { playSFX('tb_clicks'); } catch (err) {}
    const urlInput = document.getElementById('harbor-edit-url-input');
    const iconInput = document.getElementById('harbor-edit-icon-input');
    const rawUrl = urlInput ? urlInput.value.trim() : '';
    if (rawUrl) {
      try {
        const parsedUrl = (rawUrl.startsWith('http://') || rawUrl.startsWith('https://')) ? rawUrl : 'https://' + rawUrl;
        const host = new URL(parsedUrl).hostname;
        if (host) {
          const favUrl = `https://www.google.com/s2/favicons?domain=${host}&sz=64`;
          if (iconInput) {
            iconInput.value = favUrl;
            updateHarborIconPreview(favUrl);
          }
        }
      } catch (err) {
        console.warn('Auto favicon parse error:', err);
      }
    }
  }

  function selectHarborPresetIcon(icon) {
    try { playSFX('tb_clicks'); } catch (err) {}
    const iconInput = document.getElementById('harbor-edit-icon-input');
    if (iconInput) {
      iconInput.value = icon;
      updateHarborIconPreview(icon);
    }
  }

  function openHarborTabEditor(tab) {
    if (!tab) return;
    editingHarborTab = tab;
    const modal = document.getElementById('harbor-tab-editor-modal');
    if (!modal) return;

    const nameInput = document.getElementById('harbor-edit-name-input');
    const urlInput = document.getElementById('harbor-edit-url-input');
    const iconInput = document.getElementById('harbor-edit-icon-input');
    const lockedNotice = document.getElementById('harbor-locked-notice');
    const deleteBtn = document.getElementById('harbor-delete-btn');

    if (nameInput) nameInput.value = tab.name || '';
    if (urlInput) urlInput.value = tab.url || '';
    if (iconInput) iconInput.value = tab.icon || '';

    // Locked status
    const isLocked = tab.isLocked === true;
    if (lockedNotice) lockedNotice.style.display = isLocked ? 'block' : 'none';
    if (deleteBtn) deleteBtn.style.display = isLocked ? 'none' : 'block';

    updateHarborIconPreview(tab.icon || '🌐');

    if (iconInput) {
      iconInput.oninput = () => {
        updateHarborIconPreview(iconInput.value.trim());
      };
    }

    modal.style.display = 'flex';
    modal.style.setProperty('display', 'flex', 'important');
    modal.style.setProperty('z-index', '99999999', 'important');
    modal.style.setProperty('pointer-events', 'auto', 'important');
    modal.style.setProperty('visibility', 'visible', 'important');
  }

  // Setup Event Delegation & Direct Listeners for Harbor Tab Editor
  function initHarborEditorListeners() {
    const modal = document.getElementById('harbor-tab-editor-modal');
    if (!modal) return;

    const handleAction = (e) => {
      const target = e.target;
      if (target === modal) {
        closeHarborEditorModal();
        return;
      }

      const presetBtn = target.closest('.harbor-preset-icon-btn');
      if (presetBtn) {
        if (e) e.stopPropagation();
        const icon = presetBtn.dataset.icon || presetBtn.textContent.trim();
        selectHarborPresetIcon(icon);
        return;
      }

      const autoFavBtn = target.closest('#harbor-use-site-favicon-btn');
      if (autoFavBtn) {
        if (e) e.stopPropagation();
        autoFetchHarborFavicon();
        return;
      }

      const saveBtn = target.closest('#harbor-editor-save-btn');
      if (saveBtn) {
        if (e) e.stopPropagation();
        saveHarborEditorTab();
        return;
      }

      const cancelBtn = target.closest('#harbor-editor-cancel-btn') || target.closest('#harbor-editor-close-btn');
      if (cancelBtn) {
        if (e) e.stopPropagation();
        closeHarborEditorModal();
        return;
      }

      const delBtn = target.closest('#harbor-delete-btn');
      if (delBtn) {
        if (e) e.stopPropagation();
        deleteHarborEditorTab();
        return;
      }
    };

    modal.addEventListener('click', handleAction);
    modal.addEventListener('touchend', (e) => {
      const btn = e.target.closest('button, .harbor-preset-icon-btn');
      if (btn || e.target === modal) {
        try { e.preventDefault(); } catch (err) {}
        handleAction(e);
      }
    }, { passive: false });
  }

  // Initialize Harbor Tabs on startup
  loadHarborTabs();
  renderHarborTabs();
  initHarborEditorListeners();

  if (closeAllTabsBtn) {
    closeAllTabsBtn.addEventListener('click', () => {
      const nonFavorites = (cachedOpenTabs || []).filter(t => !t.isFavorite);
      if (cachedOpenTabs.length > 0 && nonFavorites.length === 0) {
        playSFX('tb_alert');
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast('⭐ All tabs are favorited and locked!');
        }
        return;
      }
      playSFX('tb_close');
      if (window.CaspianBridge && typeof window.CaspianBridge.closeAllTabs === 'function') {
        window.CaspianBridge.closeAllTabs();
        setTimeout(() => {
          renderOpenTabs();
          updateUndoButtonState();
        }, 150);
      }
    });
  }

  const undoClosedTabBtn = document.getElementById('undo-closed-tab-btn');
  if (undoClosedTabBtn) {
    undoClosedTabBtn.addEventListener('click', () => {
      try { playSFX('tb_clicks'); } catch (e) {}
      if (lastDeletedGroup) {
        tabGroups.push(lastDeletedGroup.group);
        saveTabGroups();
        lastDeletedGroup = null;
      }
      if (window.CaspianBridge && typeof window.CaspianBridge.restoreLastClosedTab === 'function') {
        window.CaspianBridge.restoreLastClosedTab();
      }
      setTimeout(() => {
        if (typeof window.renderOpenTabs === 'function') {
          window.renderOpenTabs();
        }
        updateUndoButtonState();
      }, 150);
    });
  }

  // Mobile Tab Navigation (Engine, Tabs, Settings) with Scroll-to-Top & Memory Return
  const tabSavedScrollPos = { engine: 0, sites: 0, settings: 0 };

  const getSheetScroll = () => {
    const sc = document.querySelector('.mobile-sheet-content') || document.querySelector('.mobile-content') || document.documentElement || document.body;
    return window.scrollY || document.documentElement.scrollTop || document.body.scrollTop || (sc ? sc.scrollTop : 0);
  };

  const setSheetScroll = (y) => {
    window.scrollTo({ top: y, behavior: 'smooth' });
    const sc = document.querySelector('.mobile-sheet-content') || document.querySelector('.mobile-content');
    if (sc && sc.scrollTo) {
      sc.scrollTo({ top: y, behavior: 'smooth' });
    }
  };

  document.querySelectorAll('.tab-nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const targetTab = btn.dataset.tab;
      const isAlreadyActive = btn.classList.contains('active');
      const pane = document.getElementById(`tab-pane-${targetTab}`);

      if (isAlreadyActive && pane) {
        playSFX('tm_tabs');
        const currentY = getSheetScroll();
        if (currentY > 30) {
          tabSavedScrollPos[targetTab] = currentY;
          setSheetScroll(0);
        } else if (tabSavedScrollPos[targetTab] > 30) {
          const targetY = tabSavedScrollPos[targetTab];
          setSheetScroll(targetY);
          tabSavedScrollPos[targetTab] = 0;
        } else {
          setSheetScroll(0);
        }
        return;
      }

      playSFX('tm_tabs');
      document.querySelectorAll('.tab-nav-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => {
        p.classList.remove('active');
        p.style.display = 'none';
      });

      btn.classList.add('active');
      if (pane) {
        pane.style.display = 'block';
        requestAnimationFrame(() => {
          pane.classList.add('active');
        });
      }

      if (targetTab === 'sites') {
        requestAnimationFrame(() => {
          renderOpenTabs();
        });
      } else if (targetTab === 'engine') {
        if (typeof window.syncWaveguardUI === 'function') window.syncWaveguardUI();
      }
    });
  });

  // Master Power Switch
  if (powerToggleBtn) {
    powerToggleBtn.addEventListener('click', () => {
      playSFX('tm_header');
      globalActive = !globalActive;
      const statusDot = document.getElementById('status-dot');
      const statusTitle = document.getElementById('status-title');
      const statusSub = document.getElementById('status-sub');

      if (statusDot) statusDot.classList.toggle('active', globalActive);
      if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Message Limit: ON' : 'Chat Message Limit: OFF';
      if (statusSub) statusSub.textContent = globalActive ? 'it limites the amout of message shown from below so all message above it will get prune or cut out this is done to improve performance and reduce lagging.' : 'Message Limit paused via Master Power Switch';

      const activePill = document.querySelector('.limit-pill[data-val].active');
      const limitVal = activePill ? parseInt(activePill.dataset.val) : 6;
      const activeModePill = document.querySelector('.pruner-mode-pill.active');
      const mode = activeModePill ? (activeModePill.dataset.mode || 'sliding_window') : 'sliding_window';

      if (window.CaspianBridge && typeof window.CaspianBridge.applyPruningSettings === 'function') {
        window.CaspianBridge.applyPruningSettings(limitVal, mode, globalActive);
      } else if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('chat_limit_enabled', String(globalActive));
        window.CaspianBridge.saveSetting('globalActive', JSON.stringify(globalActive));
      }
    });
  }

  // Click to Expand Message Limit Subtitle description
  const statusSub = document.getElementById('status-sub');
  if (statusSub) {
    statusSub.addEventListener('click', () => {
      statusSub.classList.toggle('expanded');
    });
  }

  // Pruning Mode Selection (Sliding Window vs Tail Window)
  document.querySelectorAll('.pruner-mode-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.pruner-mode-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      const mode = pill.dataset.mode || 'sliding_window';
      const activePill = document.querySelector('.limit-pill[data-val].active');
      const limitVal = activePill ? parseInt(activePill.dataset.val) : 6;

      if (window.CaspianBridge && typeof window.CaspianBridge.applyPruningSettings === 'function') {
        window.CaspianBridge.applyPruningSettings(limitVal, mode, globalActive);
      } else if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('chat_pruning_mode', mode);
      }
    });
  });

  // Limit Pills Selection
  document.querySelectorAll('.limit-pill[data-val]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.limit-pill[data-val]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      const limitVal = parseInt(pill.dataset.val);
      const activeModePill = document.querySelector('.pruner-mode-pill.active');
      const mode = activeModePill ? (activeModePill.dataset.mode || 'sliding_window') : 'sliding_window';

      if (window.CaspianBridge && typeof window.CaspianBridge.applyPruningSettings === 'function') {
        window.CaspianBridge.applyPruningSettings(limitVal, mode, globalActive);
      } else if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('chat_message_limit', limitVal);
      }
    });
  });

  // 2-Way Sync from Native Toolbar
  window.syncPrunerSettingsFromNative = function(limit, mode, enabled) {
    if (limit !== undefined && limit !== null) {
      document.querySelectorAll('.limit-pill[data-val]').forEach(p => {
        p.classList.toggle('active', parseInt(p.dataset.val) === parseInt(limit));
      });
      const badge = document.getElementById('active-limit-badge');
      if (badge) {
        badge.textContent = limit >= 9999 ? '∞ Unlimited' : (limit + ' Messages');
      }
    }
    if (mode !== undefined && mode !== null) {
      document.querySelectorAll('.pruner-mode-pill').forEach(p => {
        p.classList.toggle('active', p.dataset.mode === mode);
      });
    }
    if (enabled !== undefined && enabled !== null) {
      globalActive = (enabled === true || enabled === 'true');
      const statusDot = document.getElementById('status-dot');
      const statusTitle = document.getElementById('status-title');
      const statusSub = document.getElementById('status-sub');
      if (statusDot) statusDot.classList.toggle('active', globalActive);
      if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Message Limit: ON' : 'Chat Message Limit: OFF';
      if (statusSub) statusSub.textContent = globalActive ? 'it limites the amout of message shown from below so all message above it will get prune or cut out this is done to improve performance and reduce lagging.' : 'Message Limit paused via Master Power Switch';
    }
  };

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

    // Close when clicking outside of the trigger and menu
    document.addEventListener('click', (e) => {
      if (exportMenu.classList.contains('active')) {
        if (!exportMenu.contains(e.target) && e.target !== exportDropdownTrigger && !exportDropdownTrigger.contains(e.target)) {
          exportMenu.classList.remove('active');
        }
      }
    });
  }

  // Export Options Handler
  document.querySelectorAll('.export-opt-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const fmt = btn.dataset.fmt;
      if (exportMenu) exportMenu.classList.remove('active');

      if (fmt === 'nativepdf' || fmt === 'styledpdf') {
        const progressOverlay = document.getElementById('pdf-progress-overlay');
        const progressTimer = document.getElementById('progress-timer');
        if (progressOverlay && progressTimer) {
          progressOverlay.style.display = 'flex';
          let count = 3;
          progressTimer.textContent = `Typesetting math formulas... ${count}s`;
          const interval = setInterval(() => {
            count--;
            if (count === 2) {
              progressTimer.textContent = `Preparing document layout... ${count}s`;
            } else if (count === 1) {
              progressTimer.textContent = `Launching Android Print... ${count}s`;
            } else if (count <= 0) {
              clearInterval(interval);
              progressOverlay.style.display = 'none';
            }
          }, 1000);
        }
      }

      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation(fmt);
      }
    });
  });

  // Reload Active Tab Button
  const reloadTabBtn = document.getElementById('reload-tab-btn');
  if (reloadTabBtn) {
    reloadTabBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.reloadActiveTab === 'function') {
        window.CaspianBridge.reloadActiveTab();
      }
    });
  }

  // Close custom dropdown selects when clicking outside
  document.addEventListener('click', () => {
    document.querySelectorAll('.caspian-select').forEach(sel => {
      sel.classList.remove('open');
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

  // PDF Export Engine Select Listener
  const pdfModeSelect = document.getElementById('pdf-export-mode-select');
  if (pdfModeSelect) {
    pdfModeSelect.addEventListener('change', () => {
      const mode = pdfModeSelect.value;
      try { localStorage.setItem('pdfExportMode', mode); } catch (e) { }
      if (window.CaspianBridge) {
        if (typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('pdfExportMode', JSON.stringify(mode));
        }
        if (typeof window.CaspianBridge.setPdfExportMode === 'function') {
          window.CaspianBridge.setPdfExportMode(mode);
        }
      }
    });
  }

  function initCustomSelect(id, storageKey) {
    const selectEl = document.getElementById(id);
    if (!selectEl) return;

    const trigger = selectEl.querySelector('.caspian-select-trigger');
    const textEl = trigger.querySelector('span');

    trigger.addEventListener('click', (e) => {
      e.stopPropagation();
      document.querySelectorAll('.caspian-select').forEach(sel => {
        if (sel !== selectEl) sel.classList.remove('open');
      });
      selectEl.classList.toggle('open');
    });

    selectEl.querySelectorAll('.caspian-select-option').forEach(opt => {
      opt.addEventListener('click', (e) => {
        e.stopPropagation();
        selectEl.querySelectorAll('.caspian-select-option').forEach(o => o.classList.remove('active'));
        opt.classList.add('active');
        textEl.textContent = opt.textContent;
        selectEl.classList.remove('open');

        const val = opt.dataset.val;
        try {
          localStorage.setItem(storageKey, val);
          if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting(storageKey, val);
          }
        } catch (err) { }
      });
    });
  }

  initCustomSelect('select-refresh-rate', 'active_refresh_rate');
  initCustomSelect('select-anim-style', 'sheetAnimationStyle');

  // Audio SFX Volume & Toggle listeners
  const volSlider = document.getElementById('sfx-volume-slider');
  const volPercent = document.getElementById('sfx-volume-percent');
  if (volSlider && volPercent) {
    volSlider.addEventListener('input', () => {
      const vol = parseFloat(volSlider.value);
      sfxVolume = vol;
      volPercent.textContent = Math.round(vol * 100) + '%';
      localStorage.setItem('sfx_volume', vol.toString());
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('sfx_volume', vol.toString());
      }
      if (window.CaspianBridge && typeof window.CaspianBridge.setSfxVolume === 'function') {
        window.CaspianBridge.setSfxVolume(vol);
      }
    });
  }

  // Intercept External Links (e.g. GitHub link in Settings tab)
  document.addEventListener('click', (e) => {
    const link = e.target.closest('a');
    if (link && link.href && (link.href.startsWith('http://') || link.href.startsWith('https://'))) {
      e.preventDefault();
      if (window.CaspianBridge && typeof window.CaspianBridge.addNewTab === 'function') {
        window.CaspianBridge.addNewTab('web', '', link.href, false);
      }
      if (window.CaspianBridge && typeof window.CaspianBridge.hideControlSheet === 'function') {
        window.CaspianBridge.hideControlSheet();
      }
    }
  });

  const muteBtn = document.getElementById('mute-toggle-btn');
  if (muteBtn) {
    muteBtn.addEventListener('click', () => {
      setMasterMute(!masterSFXMuted);
    });
  }

  const masterToggle = document.getElementById('toggle-sfx-master');
  if (masterToggle) {
    masterToggle.addEventListener('change', () => {
      setMasterMute(!masterToggle.checked);
    });
  }

  const sfxToggles = [
    { id: 'toggle-sfx-tm-tabs', key: 'sfx_enabled_tm_tabs' },
    { id: 'toggle-sfx-ta', key: 'sfx_enabled_ta' },
    { id: 'toggle-sfx-tb-clicks', key: 'sfx_enabled_tb_clicks' },
    { id: 'toggle-sfx-tm-header', key: 'sfx_enabled_tm_header' },
    { id: 'toggle-sfx-tb-close', key: 'sfx_enabled_tb_close' },
    { id: 'toggle-sfx-tb-modal', key: 'sfx_enabled_tb_modal' }
  ];

  sfxToggles.forEach(item => {
    const el = document.getElementById(item.id);
    if (el) {
      el.addEventListener('change', () => {
        const checkedStr = el.checked ? 'true' : 'false';
        localStorage.setItem(item.key, checkedStr);
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting(item.key, checkedStr);
        }
        if (el.checked) {
          playSFX(item.key.replace('sfx_enabled_', ''));
        }
      });
    }
  });

  // Audio SFX Sound File Selector Dropdowns
  const sfxDropdowns = [
    { id: 'select-sfx-tm-tabs', key: 'tm_tabs' },
    { id: 'select-sfx-ta', key: 'ta' },
    { id: 'select-sfx-tb-clicks', key: 'tb_clicks' },
    { id: 'select-sfx-tm-header', key: 'tm_header' },
    { id: 'select-sfx-tb-close', key: 'tb_close' },
    { id: 'select-sfx-tb-modal', key: 'tb_modal' }
  ];

  sfxDropdowns.forEach(item => {
    const sel = document.getElementById(item.id);
    if (sel) {
      sel.addEventListener('change', () => {
        const chosenFile = sel.value;
        sfxConfig[item.key] = chosenFile;
        localStorage.setItem(`sfx_file_${item.key}`, chosenFile);
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting(`sfx_file_${item.key}`, chosenFile);
        }
        previewSFXFile(chosenFile);
      });
    }
  });

  // Initialize saved settings on load
  restoreSavedSettings();

  function openGroupOptionsMenu(group) {
    const modal = document.getElementById('group-options-modal');
    const titleInput = document.getElementById('group-modal-title-input');
    const colorDot = document.getElementById('group-modal-color-dot');
    const headerTitle = document.getElementById('group-modal-header-title');
    if (!modal || !titleInput) return;

    editingGroupId = group.id;
    titleInput.value = group.title || '';
    if (colorDot) colorDot.style.background = group.color || '#3b82f6';
    if (headerTitle) headerTitle.textContent = `Group: ${group.title || 'Tab Group'}`;
    selectedGroupColor = group.color || '#ef4444';
    selectedGroupEmoji = group.icon || '📁';

    document.querySelectorAll('.modal-group-color-dot').forEach(d => {
      d.classList.toggle('active', d.dataset.color === selectedGroupColor);
    });

    document.querySelectorAll('.modal-group-emoji-dot').forEach(d => {
      d.classList.toggle('active', d.dataset.emoji === selectedGroupEmoji);
      d.onclick = () => {
        playSFX('tb_clicks');
        document.querySelectorAll('.modal-group-emoji-dot').forEach(x => x.classList.remove('active'));
        d.classList.add('active');
        selectedGroupEmoji = d.dataset.emoji || '📁';
      };
    });

    const favIcon = document.getElementById('group-fav-star-icon');
    const favText = document.getElementById('group-fav-star-text');
    if (favIcon) favIcon.textContent = group.isFavorite ? '⭐' : '☆';
    if (favText) favText.textContent = group.isFavorite ? 'Favorited' : 'Favorite';

    modal.style.setProperty('display', 'flex', 'important');
    modal.style.setProperty('pointer-events', 'auto', 'important');
    modal.style.setProperty('visibility', 'visible', 'important');

    const closeX = document.getElementById('group-modal-close-x');
    const cancelBtn = document.getElementById('group-modal-cancel-btn');
    const closeModal = () => {
      playSFX('tb_modal');
      modal.style.display = 'none';
      editingGroupId = null;
    };
    if (closeX) closeX.onclick = closeModal;
    if (cancelBtn) cancelBtn.onclick = closeModal;

    const favBtn = document.getElementById('group-modal-favorite-btn');
    if (favBtn) {
      favBtn.onclick = () => {
        playSFX('tb_clicks');
        group.isFavorite = !group.isFavorite;
        saveTabGroups();
        if (window.CaspianBridge && typeof window.CaspianBridge.setGroupTabsFavorite === 'function') {
          window.CaspianBridge.setGroupTabsFavorite(JSON.stringify(group.tabIds), group.isFavorite);
        }
        if (favIcon) favIcon.textContent = group.isFavorite ? '⭐' : '☆';
        if (favText) favText.textContent = group.isFavorite ? 'Favorited' : 'Favorite';
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(group.isFavorite ? `⭐ Group "${group.title}" Favorited!` : `Group "${group.title}" Unfavorited`);
        }
        renderOpenTabs();
      };
    }

    const ungroupBtn = document.getElementById('group-modal-ungroup-btn');
    if (ungroupBtn) {
      ungroupBtn.onclick = () => {
        playSFX('tb_modal');
        tabGroups = tabGroups.filter(g => g.id !== group.id);
        saveTabGroups();
        modal.style.display = 'none';
        editingGroupId = null;
        renderOpenTabs();
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`📂 Group "${group.title}" dissolved. Tabs remain open.`);
        }
      };
    }

    const deleteBtn = document.getElementById('group-modal-delete-btn');
    if (deleteBtn) {
      deleteBtn.onclick = () => {
        playSFX('tb_close');
        lastDeletedGroup = { group: Object.assign({}, group), tabIds: [...group.tabIds] };
        if (window.CaspianBridge && typeof window.CaspianBridge.closeMultipleTabs === 'function') {
          window.CaspianBridge.closeMultipleTabs(JSON.stringify(group.tabIds));
        }
        tabGroups = tabGroups.filter(g => g.id !== group.id);
        saveTabGroups();
        modal.style.display = 'none';
        editingGroupId = null;
        showUndoToast(`Group "${group.title}" deleted`);
        renderOpenTabs();
      };
    }

    const saveBtn = document.getElementById('group-modal-save-btn');
    if (saveBtn) {
      saveBtn.onclick = () => {
        playSFX('tb_modal');
        group.title = titleInput.value.trim() || 'Tab Group';
        group.color = selectedGroupColor;
        group.icon = selectedGroupEmoji;
        saveTabGroups();
        modal.style.display = 'none';
        editingGroupId = null;
        renderOpenTabs();
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`Saved Group "${group.title}"!`);
        }
      };
    }
  }

  function openTabOptionsMenu(tab) {
    const modal = document.getElementById('tab-options-modal');
    const nicknameInput = document.getElementById('tab-nickname-input');
    const urlDisplay = document.getElementById('tab-url-display');
    if (!modal || !nicknameInput || !urlDisplay) return;

    editingTabId = tab.id;
    nicknameInput.value = tab.nickname || '';
    urlDisplay.value = tab.url || '';
    modal.style.display = 'flex';

    const pinHarborBtn = document.getElementById('modal-pin-harbor-btn');
    if (pinHarborBtn) {
      pinHarborBtn.onclick = () => {
        try { playSFX('tb_clicks'); } catch(e) {}
        pinTabToHarbor(tab);
        modal.style.display = 'none';
      };
    }

    const separateSplitRow = document.getElementById('modal-separate-split-row');
    const separateSplitBtn = document.getElementById('modal-separate-split-btn');
    if (separateSplitRow) {
      const isBonded = (tab.splitPartnerId && tab.splitPartnerId !== -1) || tab.isSplit;
      if (isBonded) {
        separateSplitRow.style.display = 'block';
        if (separateSplitBtn) {
          separateSplitBtn.onclick = () => {
            try { playSFX('tb_modal'); } catch(e) {}
            if (window.CaspianBridge && typeof window.CaspianBridge.separateSplitTabs === 'function') {
              window.CaspianBridge.separateSplitTabs(tab.id);
            }
            modal.style.display = 'none';
            if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
              window.CaspianBridge.showToast('Tabs unbonded into standalone tabs');
            }
            setTimeout(renderOpenTabs, 100);
          };
        }
      } else {
        separateSplitRow.style.display = 'none';
      }
    }

    const groupActionsRow = document.getElementById('modal-group-actions-row');
    const leaveGroupBtn = document.getElementById('modal-leave-group-btn');
    const parentGroup = tabGroups.find(g => g.tabIds.includes(tab.id));
    if (groupActionsRow) {
      if (parentGroup) {
        groupActionsRow.style.display = 'block';
        if (leaveGroupBtn) {
          leaveGroupBtn.onclick = () => {
            playSFX('tb_modal');
            parentGroup.tabIds = parentGroup.tabIds.filter(id => id !== tab.id);
            saveTabGroups();
            if (parentGroup.tabIds.length === 0 && activeGroupId === parentGroup.id) {
              activeGroupId = null;
            }
            modal.style.display = 'none';
            if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
              window.CaspianBridge.showToast(`Moved tab out of "${parentGroup.title}"!`);
            }
            setTimeout(renderOpenTabs, 50);
          };
        }
      } else {
        groupActionsRow.style.display = 'none';
      }
    }

    const favBtn = document.getElementById('modal-favorite-btn');
    const favIcon = document.getElementById('fav-star-icon');
    const favText = document.getElementById('fav-star-text');
    if (favIcon) favIcon.textContent = tab.isFavorite ? '⭐' : '☆';
    if (favText) favText.textContent = tab.isFavorite ? 'Favorited' : 'Favorite';

    if (favBtn) {
      favBtn.onclick = () => {
        playSFX('tb_clicks');
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleTabFavorite === 'function') {
          window.CaspianBridge.toggleTabFavorite(tab.id);
        }
        tab.isFavorite = !tab.isFavorite;
        if (favIcon) favIcon.textContent = tab.isFavorite ? '⭐' : '☆';
        if (favText) favText.textContent = tab.isFavorite ? 'Favorited' : 'Favorite';
      };
    }

    // Modal actions
    const cancelBtn = document.getElementById('modal-cancel-btn');
    cancelBtn.onclick = () => {
      playSFX('tb_modal');
      nicknameInput.blur();
      urlDisplay.blur();
      const dropMenu = document.getElementById('tab-cask-dropdown-menu');
      if (dropMenu) dropMenu.style.display = 'none';
      modal.style.display = 'none';
      if (typeof window.renderOpenTabs === 'function') {
        window.renderOpenTabs();
      }
    };

    const copyUrlBtn = document.getElementById('modal-copy-url-btn');
    copyUrlBtn.onclick = () => {
      playSFX('tb_modal');
      if (window.CaspianBridge && typeof window.CaspianBridge.copyToClipboard === 'function') {
        window.CaspianBridge.copyToClipboard(urlDisplay.value);
      }
    };

    // Custom Caspian Cask Selector Dropdown in Tab Options
    const caskTrigger = document.getElementById('tab-cask-custom-trigger');
    const caskMenu = document.getElementById('tab-cask-dropdown-menu');
    const caskHiddenInput = document.getElementById('tab-cask-selected-id');
    const caskIconDisp = document.getElementById('tab-cask-icon-display');
    const caskNameDisp = document.getElementById('tab-cask-name-display');
    const caskTagDisp = document.getElementById('tab-cask-tag-display');
    const caskChevron = document.getElementById('tab-cask-chevron');

    // Retrieve casks list from bridge or cached data
    let casksList = null;
    if (window.CaspianBridge && typeof window.CaspianBridge.getCaspianCasks === 'function') {
      try {
        const raw = window.CaspianBridge.getCaspianCasks();
        if (raw) {
          const parsed = JSON.parse(raw);
          if (parsed && Array.isArray(parsed.casks) && parsed.casks.length > 0) {
            casksList = parsed.casks;
            controlCasksData = parsed;
          }
        }
      } catch (e) {}
    }
    if (!casksList && controlCasksData && Array.isArray(controlCasksData.casks) && controlCasksData.casks.length > 0) {
      casksList = controlCasksData.casks;
    }
    if (!casksList || casksList.length === 0) {
      casksList = [
        { id: 'cask_caspian', name: 'Caspian Cask', icon: '🌊', color: '#1B4264', isDefault: true },
        { id: 'cask_pacific', name: 'Pacific Cask', icon: '⚓', color: '#0284C7', isDefault: false }
      ];
    }

    const currentCaskId = tab.caskId || 'cask_caspian';
    if (caskHiddenInput) caskHiddenInput.value = currentCaskId;

    const initialCask = casksList.find(c => c.id === currentCaskId) || casksList[0];
    if (caskIconDisp) caskIconDisp.textContent = initialCask.icon || '🌊';
    if (caskNameDisp) caskNameDisp.textContent = initialCask.name || 'Caspian Cask';
    if (caskTagDisp) caskTagDisp.textContent = initialCask.isDefault ? 'Default' : 'Vault';

    if (caskMenu) {
      caskMenu.style.display = 'none';
      if (caskChevron) caskChevron.style.transform = 'rotate(0deg)';

      caskMenu.innerHTML = casksList.map(c => {
        const isSel = (c.id === currentCaskId);
        return `
          <div class="cask-dropdown-item" data-cask-id="${c.id}" style="display:flex; align-items:center; justify-content:space-between; padding:9px 10px; border-radius:10px; background:${isSel ? 'var(--accent-glow, rgba(0,229,255,0.1))' : 'transparent'}; border:1px solid ${isSel ? 'var(--accent, #00E5FF)' : 'transparent'}; cursor:pointer; transition:all 0.15s ease;">
            <div style="display:flex; align-items:center; gap:10px;">
              <div style="width:30px; height:30px; border-radius:8px; background:var(--input-bg, rgba(128,128,128,0.08)); border:1px solid ${c.color || 'var(--border-glass)'}; display:flex; align-items:center; justify-content:center; font-size:15px;">
                ${c.icon || '🌊'}
              </div>
              <div>
                <div style="font-size:12px; font-weight:700; color:var(--text-main);">${c.name}</div>
                <div style="font-size:9.5px; color:var(--text-muted);">${c.isDefault ? 'Default Cask' : 'Custom Vault'}</div>
              </div>
            </div>
            <div>
              ${isSel ? '<span style="font-size:11px; color:#10b981; font-weight:800;">✓ Active</span>' : ''}
            </div>
          </div>
        `;
      }).join('');

      if (caskTrigger) {
        caskTrigger.onclick = (e) => {
          e.stopPropagation();
          playSFX('tb_clicks');
          const isExpanded = caskMenu.style.display === 'flex';
          caskMenu.style.display = isExpanded ? 'none' : 'flex';
          if (caskChevron) caskChevron.style.transform = isExpanded ? 'rotate(0deg)' : 'rotate(180deg)';
        };
      }

      caskMenu.querySelectorAll('.cask-dropdown-item').forEach(item => {
        item.onclick = (e) => {
          e.stopPropagation();
          playSFX('tb_clicks');
          const selId = item.getAttribute('data-cask-id');
          if (caskHiddenInput) caskHiddenInput.value = selId;
          const chosen = casksList.find(c => c.id === selId);
          if (chosen) {
            if (caskIconDisp) caskIconDisp.textContent = chosen.icon || '🌊';
            if (caskNameDisp) caskNameDisp.textContent = chosen.name;
            if (caskTagDisp) caskTagDisp.textContent = chosen.isDefault ? 'Default' : 'Vault';
          }
          caskMenu.querySelectorAll('.cask-dropdown-item').forEach(other => {
            const isIt = (other.getAttribute('data-cask-id') === selId);
            other.style.background = isIt ? 'var(--accent-glow, rgba(0,229,255,0.1))' : 'transparent';
            other.style.borderColor = isIt ? 'var(--accent, #00E5FF)' : 'transparent';
            const checkContainer = other.querySelector('div:last-child');
            if (checkContainer) {
              checkContainer.innerHTML = isIt ? '<span style="font-size:11px; color:#10b981; font-weight:800;">✓ Active</span>' : '';
            }
          });
          caskMenu.style.display = 'none';
          if (caskChevron) caskChevron.style.transform = 'rotate(0deg)';
        };
      });
    }

    const clearNicknameBtn = document.getElementById('modal-clear-nickname-btn');
    if (clearNicknameBtn) {
      clearNicknameBtn.onclick = () => {
        playSFX('tb_modal');
        nicknameInput.blur();
        urlDisplay.blur();
        nicknameInput.value = '';
        const url = urlDisplay.value.trim();
        const currentCask = caskHiddenInput ? caskHiddenInput.value : (tab.caskId || 'cask_caspian');
        if (window.CaspianBridge && typeof window.CaspianBridge.updateTabDetails === 'function') {
          try {
            window.CaspianBridge.updateTabDetails(tab.id, '', url, currentCask);
          } catch (e) {
            window.CaspianBridge.updateTabDetails(tab.id, '', url);
          }
        }
        if (caskMenu) caskMenu.style.display = 'none';
        modal.style.display = 'none';
        setTimeout(() => {
          if (typeof window.renderOpenTabs === 'function') {
            window.renderOpenTabs();
          }
        }, 100);
      };
    }

    const saveBtn = document.getElementById('modal-save-btn');
    if (saveBtn) {
      saveBtn.onclick = () => {
        playSFX('tb_modal');
        nicknameInput.blur();
        urlDisplay.blur();
        const nick = nicknameInput.value.trim();
        const url = urlDisplay.value.trim();
        const newCaskId = caskHiddenInput ? caskHiddenInput.value : (tab.caskId || 'cask_caspian');
        const oldCaskId = tab.caskId || 'cask_caspian';
        const caskChanged = (newCaskId !== oldCaskId);

        if (window.CaspianBridge && typeof window.CaspianBridge.updateTabDetails === 'function') {
          try {
            window.CaspianBridge.updateTabDetails(tab.id, nick, url, newCaskId);
          } catch (err) {
            window.CaspianBridge.updateTabDetails(tab.id, nick, url);
            if (caskChanged && typeof window.CaspianBridge.changeTabCask === 'function') {
              window.CaspianBridge.changeTabCask(tab.id, newCaskId);
            }
          }
        } else if (caskChanged && window.CaspianBridge && typeof window.CaspianBridge.changeTabCask === 'function') {
          window.CaspianBridge.changeTabCask(tab.id, newCaskId);
        }

        if (caskMenu) caskMenu.style.display = 'none';
        modal.style.display = 'none';
        setTimeout(() => {
          if (typeof window.renderOpenTabs === 'function') {
            window.renderOpenTabs();
          }
        }, 150);
      };
    }
  }

  function triggerCloseTab(tabId) {
    const tab = (cachedOpenTabs || []).find(t => t.id === tabId);
    if (tab && tab.isFavorite) {
      playSFX('tb_alert');
      if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
        window.CaspianBridge.showToast('⭐ Favorited tabs are locked. Unfavorite first to close.');
      }
      return;
    }
    playSFX('tb_close');
    if (window.CaspianBridge && typeof window.CaspianBridge.closeTab === 'function') {
      window.CaspianBridge.closeTab(tabId);
      setTimeout(() => {
        if (typeof window.renderOpenTabs === 'function') {
          window.renderOpenTabs();
        }
        updateUndoButtonState();
      }, 150);
    }
  }

  function showUndoToast() {
    updateUndoButtonState();
  }

  // YouTube Controls & AdBlocker Event Listeners
  document.addEventListener('DOMContentLoaded', () => {
    const ytSeekBack10Btn = document.getElementById('yt-seek-back-10-btn');
    if (ytSeekBack10Btn) {
      ytSeekBack10Btn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (window.CaspianBridge && typeof window.CaspianBridge.seekYouTube === 'function') {
          window.CaspianBridge.seekYouTube(-10);
        }
      });
    }

    const ytSeekBackBtn = document.getElementById('yt-seek-back-btn');
    if (ytSeekBackBtn) {
      ytSeekBackBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (window.CaspianBridge && typeof window.CaspianBridge.seekYouTube === 'function') {
          window.CaspianBridge.seekYouTube(-5);
        }
      });
    }

    const ytPlayPauseBtn = document.getElementById('yt-play-pause-btn');
    if (ytPlayPauseBtn) {
      ytPlayPauseBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (window.CaspianBridge && typeof window.CaspianBridge.togglePlayYouTube === 'function') {
          window.CaspianBridge.togglePlayYouTube();
        }
      });
    }

    const ytSeekFwdBtn = document.getElementById('yt-seek-fwd-btn');
    if (ytSeekFwdBtn) {
      ytSeekFwdBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (window.CaspianBridge && typeof window.CaspianBridge.seekYouTube === 'function') {
          window.CaspianBridge.seekYouTube(5);
        }
      });
    }

    const ytSeekFwd10Btn = document.getElementById('yt-seek-fwd-10-btn');
    if (ytSeekFwd10Btn) {
      ytSeekFwd10Btn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (window.CaspianBridge && typeof window.CaspianBridge.seekYouTube === 'function') {
          window.CaspianBridge.seekYouTube(10);
        }
      });
    }

    let isYtFloatingRemoteOpen = true;
    const ytTogglePopupBtn = document.getElementById('yt-toggle-popup-btn');

    window.syncYtFloatPodState = function (isOpen) {
      isYtFloatingRemoteOpen = !!isOpen;
      if (ytTogglePopupBtn) {
        ytTogglePopupBtn.classList.toggle('active', isYtFloatingRemoteOpen);
        ytTogglePopupBtn.style.opacity = isYtFloatingRemoteOpen ? '1' : '0.7';
      }
    };

    if (ytTogglePopupBtn) {
      ytTogglePopupBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        isYtFloatingRemoteOpen = !isYtFloatingRemoteOpen;
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleFloatingYouTubeRemote === 'function') {
          window.CaspianBridge.toggleFloatingYouTubeRemote(isYtFloatingRemoteOpen);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(isYtFloatingRemoteOpen ? "🚀 YouTube Float Pod Opened!" : "YouTube Float Pod Closed");
        }
      });
    }

    document.querySelectorAll('.yt-speed-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        playSFX('tb_clicks');
        const speed = pill.dataset.speed;
        document.querySelectorAll('.yt-speed-pill').forEach(p => p.classList.toggle('active', p === pill));
        if (window.CaspianBridge && typeof window.CaspianBridge.setYouTubeSpeed === 'function') {
          window.CaspianBridge.setYouTubeSpeed(parseFloat(speed));
        }
      });
    });

    document.querySelectorAll('.yt-quality-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        playSFX('tb_clicks');
        const quality = pill.dataset.quality;
        document.querySelectorAll('.yt-quality-pill').forEach(p => p.classList.toggle('active', p === pill));
        if (window.CaspianBridge && typeof window.CaspianBridge.setYouTubeQuality === 'function') {
          window.CaspianBridge.setYouTubeQuality(quality);
        }
      });
    });

    // Engine Tab Cards & Master Power Toggle Logic
    const cardTS = document.getElementById('card-temp-saver');
    const cardCL = document.getElementById('card-chat-limit');
    const cardCC = document.getElementById('card-caspian-current');
    const cardAB = document.getElementById('card-adblocker');
    const cardYT = document.getElementById('youtube-control-card');
    const cardToolbars = document.getElementById('toolbars-control-card');

    const toggleTSBtn = document.getElementById('toggle-temp-saver-btn');
    const toggleCLBtn = document.getElementById('toggle-chat-limit-btn');
    const toggleCCBtn = document.getElementById('toggle-caspian-current-btn');
    const toggleAdblockBtn = document.getElementById('toggle-adblock-btn');
    const toggleYTBtn = document.getElementById('toggle-yt-engine-btn');
    const toggleAllToolbarsBtn = document.getElementById('toggle-all-toolbars-btn');

    const chatLimitHeader = document.getElementById('chat-limit-header');
    const chatLimitBody = document.getElementById('chat-limit-body');
    const ccHeader = document.getElementById('caspian-current-header');
    const ccBody = document.getElementById('caspian-current-body');
    const adblockHeader = document.getElementById('adblock-header');
    const adblockBody = document.getElementById('adblock-body');
    const ytControlHeader = document.getElementById('yt-control-header');
    const ytControlBody = document.getElementById('yt-control-body');
    const ytStatusDot = document.getElementById('yt-live-status-dot');

    const toolbarsControlHeader = document.getElementById('toolbars-control-header');
    const toolbarsControlBody = document.getElementById('toolbars-control-body');
    const toolbarsMasterDot = document.getElementById('toolbars-master-dot');

    const googleDockDot = document.getElementById('google-dock-status-dot');
    const toggleGoogleDockBtn = document.getElementById('toggle-google-dock-btn');
    const googleDockTogglePopupBtn = document.getElementById('google-dock-toggle-popup-btn');
    const chkGoogleDockAutoCollapse = document.getElementById('chk-google-dock-autocollapse');

    const ytDockDot = document.getElementById('yt-dock-status-dot');
    const toggleYtDockBtn = document.getElementById('toggle-yt-dock-btn');
    const ytToolbarLaunchBtn = document.getElementById('yt-toolbar-launch-btn');
    const chkYtRemoteAutoCollapse = document.getElementById('chk-yt-remote-autocollapse');

    const chatgptDockDot = document.getElementById('chatgpt-dock-status-dot');
    const toggleChatgptDockBtn = document.getElementById('toggle-chatgpt-dock-btn');
    const chatgptToolbarLaunchBtn = document.getElementById('chatgpt-toolbar-launch-btn');
    const chkChatgptDockAutoCollapse = document.getElementById('chk-chatgpt-dock-autocollapse');

    const geminiDockDot = document.getElementById('gemini-dock-status-dot');
    const toggleGeminiDockBtn = document.getElementById('toggle-gemini-dock-btn');
    const geminiToolbarLaunchBtn = document.getElementById('gemini-toolbar-launch-btn');
    const chkGeminiDockAutoCollapse = document.getElementById('chk-gemini-dock-autocollapse');

    // Helper to update card states
    function updateEngineCardUI(card, toggleBtn, body, dotEl, key) {
      const isEnabled = key === 'google_dock_enabled'
        ? (localStorage.getItem(key) === 'true')
        : (localStorage.getItem(key) !== 'false');
      if (card) {
        card.classList.toggle('disabled', !isEnabled);
        card.style.opacity = isEnabled ? '1' : '0.45';
        card.style.filter = isEnabled ? 'none' : 'grayscale(0.6)';
      }
      if (body) body.style.display = isEnabled ? 'block' : 'none';
      if (dotEl) dotEl.classList.toggle('active', isEnabled);
      if (toggleBtn) {
        toggleBtn.textContent = isEnabled ? 'ON' : 'OFF';
        toggleBtn.className = isEnabled ? 'oneui-pill-btn primary' : 'oneui-pill-btn secondary';
      }
      if (key === 'chat_limit_enabled') {
        const statusTitle = document.getElementById('status-title');
        if (statusTitle) statusTitle.textContent = 'Chat Message Limit: ' + (isEnabled ? 'ON' : 'OFF');
      }
    }

    function updateDockItemUI(dotEl, toggleBtn, isEnabled) {
      if (dotEl) dotEl.classList.toggle('active', isEnabled);
      if (toggleBtn) {
        toggleBtn.textContent = isEnabled ? 'ON' : 'OFF';
        toggleBtn.className = isEnabled ? 'oneui-pill-btn primary' : 'oneui-pill-btn secondary';
      }
    }

    function updateToolbarsMasterUI() {
      const isGoogle = localStorage.getItem('google_dock_enabled') === 'true';
      const isYt = localStorage.getItem('yt_dock_enabled') !== 'false';
      const isGpt = localStorage.getItem('chatgpt_dock_enabled') !== 'false';
      const isGemini = localStorage.getItem('gemini_dock_enabled') !== 'false';

      updateDockItemUI(googleDockDot, toggleGoogleDockBtn, isGoogle);
      updateDockItemUI(ytDockDot, toggleYtDockBtn, isYt);
      updateDockItemUI(chatgptDockDot, toggleChatgptDockBtn, isGpt);
      updateDockItemUI(geminiDockDot, toggleGeminiDockBtn, isGemini);

      const anyOn = isGoogle || isYt || isGpt || isGemini;
      if (toolbarsMasterDot) toolbarsMasterDot.classList.toggle('active', anyOn);
      if (toggleAllToolbarsBtn) {
        toggleAllToolbarsBtn.textContent = anyOn ? 'ON' : 'OFF';
        toggleAllToolbarsBtn.className = anyOn ? 'oneui-pill-btn primary' : 'oneui-pill-btn secondary';
      }
    }

    // Initial Sync
    updateEngineCardUI(cardTS, toggleTSBtn, null, document.getElementById('ts-status-dot'), 'temp_saver_enabled');
    updateEngineCardUI(cardCL, toggleCLBtn, chatLimitBody, document.getElementById('status-dot'), 'chat_limit_enabled');
    updateEngineCardUI(cardCC, toggleCCBtn, ccBody, document.getElementById('cc-status-dot'), 'caspian_current_enabled');
    updateEngineCardUI(cardAB, toggleAdblockBtn, adblockBody, document.getElementById('adblock-dot'), 'adblock_enabled');
    updateEngineCardUI(cardYT, toggleYTBtn, ytControlBody, ytStatusDot, 'yt_engine_enabled');
    updateToolbarsMasterUI();

    // 1. Temporary Chat Saver Toggle
    if (toggleTSBtn) {
      toggleTSBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('temp_saver_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('temp_saver_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('temp_saver_enabled', next ? 'true' : 'false');
        }
        updateEngineCardUI(cardTS, toggleTSBtn, null, document.getElementById('ts-status-dot'), 'temp_saver_enabled');
      });
    }

    // 2. Chat Limit Accordion & Toggle
    if (chatLimitHeader && chatLimitBody) {
      chatLimitHeader.addEventListener('click', (e) => {
        if (e.target === toggleCLBtn || (toggleCLBtn && toggleCLBtn.contains(e.target))) return;
        const isOpen = chatLimitBody.style.display !== 'none';
        chatLimitBody.style.display = isOpen ? 'none' : 'block';
      });
    }
    if (toggleCLBtn) {
      toggleCLBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('chat_limit_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('chat_limit_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('chat_limit_enabled', next ? 'true' : 'false');
        }
        updateEngineCardUI(cardCL, toggleCLBtn, chatLimitBody, document.getElementById('status-dot'), 'chat_limit_enabled');
        if (window.CaspianBridge && typeof window.CaspianBridge.applyPruningSettings === 'function') {
          window.CaspianBridge.applyPruningSettings(parseInt(limitVal, 10), currentPrunerMode, next);
        }
      });
    }

    // Pruner Mode Pills (Sliding Window vs Tail Window)
    const prunerModePills = document.querySelectorAll('.pruner-mode-pill');
    let currentPrunerMode = localStorage.getItem('chat_pruning_mode') || 'sliding_window';
    prunerModePills.forEach(pill => {
      pill.classList.toggle('active', pill.dataset.mode === currentPrunerMode);
      pill.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        prunerModePills.forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        currentPrunerMode = pill.dataset.mode;
        localStorage.setItem('chat_pruning_mode', currentPrunerMode);
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('chat_pruning_mode', currentPrunerMode);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.applyPruningSettings === 'function') {
          window.CaspianBridge.applyPruningSettings(parseInt(limitVal, 10), currentPrunerMode, localStorage.getItem('chat_limit_enabled') !== 'false');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`Pruning Mode: ${pill.textContent.trim()}`);
        }
      });
    });

    // Visible Messages Limit Pills (2, 4, 6, 8, 10, ...)
    const limitPills = document.querySelectorAll('.pill-grid .limit-pill');
    let savedLimit = localStorage.getItem('chat_message_limit') || '5';
    limitVal = parseInt(savedLimit, 10) || 5;
    const initialBadge = document.getElementById('active-limit-badge');
    if (initialBadge) initialBadge.textContent = limitVal >= 9999 ? '∞ Unlimited' : `${limitVal} Messages`;

    limitPills.forEach(pill => {
      pill.classList.toggle('active', pill.dataset.val === String(limitVal));
      pill.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        limitPills.forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        limitVal = parseInt(pill.dataset.val, 10);
        localStorage.setItem('chat_message_limit', String(limitVal));
        const badge = document.getElementById('active-limit-badge');
        if (badge) badge.textContent = limitVal >= 9999 ? '∞ Unlimited' : `${limitVal} Messages`;
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('chat_message_limit', String(limitVal));
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.applyPruningSettings === 'function') {
          window.CaspianBridge.applyPruningSettings(limitVal, currentPrunerMode, localStorage.getItem('chat_limit_enabled') !== 'false');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`Message Limit: ${limitVal >= 9999 ? 'Unlimited' : limitVal + ' messages'}`);
        }
      });
    });

    // 3. Caspian Current Accordion & Toggle
    if (ccHeader && ccBody) {
      ccHeader.addEventListener('click', (e) => {
        if (e.target === toggleCCBtn || (toggleCCBtn && toggleCCBtn.contains(e.target))) return;
        const isOpen = ccBody.style.display !== 'none';
        ccBody.style.display = isOpen ? 'none' : 'block';
      });
    }
    if (toggleCCBtn) {
      toggleCCBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('caspian_current_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('caspian_current_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('caspian_current_enabled', next ? 'true' : 'false');
        }
        updateEngineCardUI(cardCC, toggleCCBtn, ccBody, document.getElementById('cc-status-dot'), 'caspian_current_enabled');
      });
    }

    // Caspian Drift Settings Wiring (API Key, Speech Engine, Language Accent)
    const sttPills = document.querySelectorAll('.cc-stt-pill');
    const apiKeyLabel = document.getElementById('cc-api-key-label');
    const apiKeyContainer = document.getElementById('cc-api-key-container');
    const apiKeyInput = document.getElementById('whisper-api-key-input');
    const saveApiKeyBtn = document.getElementById('save-whisper-api-key-btn');

    let currentSttEngine = localStorage.getItem('stt_engine_mode') || 'deepgram';
    if (window.CaspianBridge && typeof window.CaspianBridge.getPref === 'function') {
      currentSttEngine = window.CaspianBridge.getPref('stt_engine_mode', currentSttEngine);
    }

    function updateSttEngineUI(engine) {
      currentSttEngine = engine;
      sttPills.forEach(p => {
        if (p.dataset.engine === engine) p.classList.add('active');
        else p.classList.remove('active');
      });

      if (engine === 'deepgram') {
        if (apiKeyLabel) apiKeyLabel.textContent = 'DEEPGRAM API KEY';
        if (apiKeyContainer) apiKeyContainer.style.display = 'flex';
        let savedKey = localStorage.getItem('deepgram_api_key') || '';
        if (window.CaspianBridge && typeof window.CaspianBridge.getPref === 'function') {
          savedKey = window.CaspianBridge.getPref('deepgram_api_key', savedKey);
        }
        if (apiKeyInput) {
          apiKeyInput.placeholder = 'Paste Deepgram API Key...';
          apiKeyInput.value = savedKey;
        }
      } else if (engine === 'huggingface') {
        if (apiKeyLabel) apiKeyLabel.textContent = 'HUGGINGFACE API TOKEN';
        if (apiKeyContainer) apiKeyContainer.style.display = 'flex';
        let savedKey = localStorage.getItem('huggingface_api_key') || '';
        if (window.CaspianBridge && typeof window.CaspianBridge.getPref === 'function') {
          savedKey = window.CaspianBridge.getPref('huggingface_api_key', savedKey);
        }
        if (apiKeyInput) {
          apiKeyInput.placeholder = 'Paste Hugging Face Token (hf_...)...';
          apiKeyInput.value = savedKey;
        }
      } else if (engine === 'android_native') {
        if (apiKeyLabel) apiKeyLabel.textContent = 'ON-DEVICE NATIVE RECOGNIZER';
        if (apiKeyContainer) apiKeyContainer.style.display = 'none';
      }
    }

    sttPills.forEach(pill => {
      pill.addEventListener('click', () => {
        playSFX('tb_clicks');
        const selectedEngine = pill.dataset.engine;
        localStorage.setItem('stt_engine_mode', selectedEngine);
        if (window.CaspianBridge && typeof window.CaspianBridge.savePref === 'function') {
          window.CaspianBridge.savePref('stt_engine_mode', selectedEngine);
        }
        updateSttEngineUI(selectedEngine);
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`STT Model: ${pill.textContent.trim()}`);
        }
      });
    });

    updateSttEngineUI(currentSttEngine);

    // Save API Key Handler
    if (saveApiKeyBtn && apiKeyInput) {
      const handleSaveKey = () => {
        playSFX('tb_clicks');
        const keyVal = (apiKeyInput.value || '').trim();
        const prefKey = currentSttEngine === 'huggingface' ? 'huggingface_api_key' : 'deepgram_api_key';
        localStorage.setItem(prefKey, keyVal);
        if (window.CaspianBridge && typeof window.CaspianBridge.savePref === 'function') {
          window.CaspianBridge.savePref(prefKey, keyVal);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          const modelName = currentSttEngine === 'huggingface' ? 'HuggingFace' : 'Deepgram';
          window.CaspianBridge.showToast(keyVal ? `✅ ${modelName} API Key Saved` : `⚠️ ${modelName} Key Cleared`);
        }
        saveApiKeyBtn.textContent = '✓ Saved';
        setTimeout(() => {
          saveApiKeyBtn.textContent = 'Save';
        }, 1500);
      };

      saveApiKeyBtn.addEventListener('click', handleSaveKey);
      apiKeyInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          handleSaveKey();
        }
      });
    }

    // Language Accent Pills
    const langPills = document.querySelectorAll('.cc-lang-pill');
    let savedLang = localStorage.getItem('caspian_drift_lang') || 'auto';
    if (window.CaspianBridge && typeof window.CaspianBridge.getPref === 'function') {
      savedLang = window.CaspianBridge.getPref('caspian_drift_lang', savedLang);
    }
    langPills.forEach(pill => {
      if (pill.dataset.lang === savedLang) {
        pill.classList.add('active');
      } else {
        pill.classList.remove('active');
      }
      pill.addEventListener('click', () => {
        playSFX('tb_clicks');
        langPills.forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        const selected = pill.dataset.lang;
        localStorage.setItem('caspian_drift_lang', selected);
        if (window.CaspianBridge && typeof window.CaspianBridge.savePref === 'function') {
          window.CaspianBridge.savePref('caspian_drift_lang', selected);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`Accent set to: ${pill.textContent.trim()}`);
        }
      });
    });

    // 4. Waveguard AdBlocker Accordion & Toggle
    if (adblockHeader && adblockBody) {
      adblockHeader.addEventListener('click', (e) => {
        if (e.target === toggleAdblockBtn || (toggleAdblockBtn && toggleAdblockBtn.contains(e.target))) return;
        const isOpen = adblockBody.style.display !== 'none';
        adblockBody.style.display = isOpen ? 'none' : 'block';
        if (!isOpen && typeof window.syncWaveguardUI === 'function') {
          window.syncWaveguardUI();
        }
      });
    }
    if (toggleAdblockBtn) {
      toggleAdblockBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('adblock_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('adblock_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('adblock_enabled', next ? 'true' : 'false');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.setWaveguardSetting === 'function') {
          window.CaspianBridge.setWaveguardSetting('global', next);
        }
        const chkTrackers = document.getElementById('chk-waveguard-trackers');
        if (chkTrackers) chkTrackers.checked = next;
        updateEngineCardUI(cardAB, toggleAdblockBtn, adblockBody, document.getElementById('adblock-dot'), 'adblock_enabled');
      });
    }

    // Waveguard Bi-Directional Synchronization
    window.syncWaveguardUI = function() {
      if (!window.CaspianBridge || typeof window.CaspianBridge.getWaveguardStats !== 'function') return;
      try {
        const statsStr = window.CaspianBridge.getWaveguardStats();
        const stats = JSON.parse(statsStr || '{}');
        const isGlobal = stats.globalEnabled !== undefined ? stats.globalEnabled : (localStorage.getItem('adblock_enabled') !== 'false');

        localStorage.setItem('adblock_enabled', isGlobal ? 'true' : 'false');
        updateEngineCardUI(cardAB, toggleAdblockBtn, null, document.getElementById('adblock-dot'), 'adblock_enabled');

        const chkTrackers = document.getElementById('chk-waveguard-trackers');
        if (chkTrackers) chkTrackers.checked = !!isGlobal;

        const chkCosmetic = document.getElementById('chk-waveguard-cosmetic');
        if (chkCosmetic) chkCosmetic.checked = stats.cosmeticEnabled !== false;

        const chkDefuser = document.getElementById('chk-waveguard-defuser');
        if (chkDefuser) chkDefuser.checked = stats.defuserEnabled !== false;

        const chkPopups = document.getElementById('chk-waveguard-popups');
        if (chkPopups) chkPopups.checked = stats.easyPrivacyEnabled !== false;

        const totalBadge = document.getElementById('waveguard-total-badge');
        if (totalBadge) totalBadge.textContent = (stats.totalBlocked || 0) + ' Blocked';

        const subTitle = document.getElementById('waveguard-stats-subtitle');
        if (subTitle) subTitle.textContent = (stats.ruleCount || 227) + ' filters active & compiling';

        const verLabel = document.getElementById('waveguard-filter-ver-label');
        if (verLabel) verLabel.textContent = 'Database: Waveguard Active (' + (stats.ruleCount || 227) + ' filters)';
      } catch (e) {
        console.warn('Failed to sync Waveguard UI: ', e);
      }
    };

    const btnUpdateWaveguard = document.getElementById('btn-waveguard-update-filters');
    if (btnUpdateWaveguard) {
      btnUpdateWaveguard.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        btnUpdateWaveguard.textContent = '⏳ Updating...';
        btnUpdateWaveguard.disabled = true;
        if (window.CaspianBridge && typeof window.CaspianBridge.updateWaveguardLists === 'function') {
          window.CaspianBridge.updateWaveguardLists();
        }
        setTimeout(() => {
          btnUpdateWaveguard.textContent = '🔄 Update Lists';
          btnUpdateWaveguard.disabled = false;
          if (typeof window.syncWaveguardUI === 'function') window.syncWaveguardUI();
        }, 2000);
      });
    }

    // 5. YouTube Player Controls Accordion & Toggle
    if (ytControlHeader && ytControlBody) {
      ytControlHeader.addEventListener('click', (e) => {
        if (e.target === toggleYTBtn || (toggleYTBtn && toggleYTBtn.contains(e.target)) ||
            (ytTogglePopupBtn && ytTogglePopupBtn.contains(e.target))) return;
        const isOpen = ytControlBody.style.display !== 'none';
        ytControlBody.style.display = isOpen ? 'none' : 'block';
      });
    }
    if (toggleYTBtn) {
      toggleYTBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('yt_engine_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('yt_engine_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('yt_engine_enabled', next ? 'true' : 'false');
        }
        updateEngineCardUI(cardYT, toggleYTBtn, ytControlBody, ytStatusDot, 'yt_engine_enabled');
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleFloatingYouTubeRemote === 'function') {
          window.CaspianBridge.toggleFloatingYouTubeRemote(next);
        }
      });
    }

    // 5.5 Unified Floating Toolbars & Docks Accordion & Handlers
    if (toolbarsControlHeader && toolbarsControlBody) {
      toolbarsControlHeader.addEventListener('click', (e) => {
        if (e.target === toggleAllToolbarsBtn || (toggleAllToolbarsBtn && toggleAllToolbarsBtn.contains(e.target))) return;
        const isOpen = toolbarsControlBody.style.display !== 'none';
        toolbarsControlBody.style.display = isOpen ? 'none' : 'block';
      });
    }

    if (toggleAllToolbarsBtn) {
      toggleAllToolbarsBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        const isGoogle = localStorage.getItem('google_dock_enabled') === 'true';
        const isYt = localStorage.getItem('yt_dock_enabled') !== 'false';
        const isGpt = localStorage.getItem('chatgpt_dock_enabled') !== 'false';
        const isGemini = localStorage.getItem('gemini_dock_enabled') !== 'false';
        const anyOn = isGoogle || isYt || isGpt || isGemini;
        const nextState = !anyOn;

        localStorage.setItem('google_dock_enabled', nextState ? 'true' : 'false');
        localStorage.setItem('yt_dock_enabled', nextState ? 'true' : 'false');
        localStorage.setItem('chatgpt_dock_enabled', nextState ? 'true' : 'false');
        localStorage.setItem('gemini_dock_enabled', nextState ? 'true' : 'false');

        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('google_dock_enabled', nextState ? 'true' : 'false');
            window.CaspianBridge.saveSetting('yt_dock_enabled', nextState ? 'true' : 'false');
            window.CaspianBridge.saveSetting('chatgpt_dock_enabled', nextState ? 'true' : 'false');
            window.CaspianBridge.saveSetting('gemini_dock_enabled', nextState ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.toggleGoogleSearchDock === 'function') {
            window.CaspianBridge.toggleGoogleSearchDock(nextState);
          }
          if (typeof window.CaspianBridge.toggleFloatingYouTubeRemote === 'function') {
            window.CaspianBridge.toggleFloatingYouTubeRemote(nextState);
          }
          if (typeof window.CaspianBridge.toggleChatGPTDock === 'function') {
            window.CaspianBridge.toggleChatGPTDock(nextState);
          }
          if (typeof window.CaspianBridge.toggleGeminiDock === 'function') {
            window.CaspianBridge.toggleGeminiDock(nextState);
          }
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(nextState ? "🚀 All Floating Toolbars Enabled" : "🔌 All Floating Toolbars Disabled");
          }
        }
        updateToolbarsMasterUI();
      });
    }

    // Google Dock handlers
    if (googleDockTogglePopupBtn) {
      googleDockTogglePopupBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        localStorage.setItem('google_dock_enabled', 'true');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('google_dock_enabled', 'true');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleGoogleSearchDock === 'function') {
          window.CaspianBridge.toggleGoogleSearchDock(true);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("🚀 Google Toolbar Opened!");
        }
        updateToolbarsMasterUI();
      });
    }
    if (toggleGoogleDockBtn) {
      toggleGoogleDockBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('google_dock_enabled') === 'true';
        let next = !current;
        localStorage.setItem('google_dock_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('google_dock_enabled', next ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.toggleGoogleSearchDock === 'function') {
            window.CaspianBridge.toggleGoogleSearchDock(next);
          } else if (typeof window.CaspianBridge.toggleGoogleDock === 'function') {
            window.CaspianBridge.toggleGoogleDock(next);
          }
        }
        updateToolbarsMasterUI();
      });
    }
    if (chkGoogleDockAutoCollapse) {
      let isAuto = localStorage.getItem('google_dock_autocollapse') !== 'false';
      chkGoogleDockAutoCollapse.checked = isAuto;
      chkGoogleDockAutoCollapse.addEventListener('change', () => {
        playSFX('tb_clicks');
        let val = chkGoogleDockAutoCollapse.checked;
        localStorage.setItem('google_dock_autocollapse', val ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('google_dock_autocollapse', val ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.setGoogleDockAutoCollapse === 'function') {
            window.CaspianBridge.setGoogleDockAutoCollapse(val);
          }
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(val ? "Google Dock Auto-Collapse: ON" : "Google Dock Auto-Collapse: OFF");
          }
        }
      });
    }

    // YouTube Float Pod handlers
    if (ytToolbarLaunchBtn) {
      ytToolbarLaunchBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        localStorage.setItem('yt_dock_enabled', 'true');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('yt_dock_enabled', 'true');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleFloatingYouTubeRemote === 'function') {
          window.CaspianBridge.toggleFloatingYouTubeRemote(true);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("🚀 YouTube Float Pod Opened!");
        }
        updateToolbarsMasterUI();
      });
    }
    if (toggleYtDockBtn) {
      toggleYtDockBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('yt_dock_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('yt_dock_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('yt_dock_enabled', next ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.toggleFloatingYouTubeRemote === 'function') {
            window.CaspianBridge.toggleFloatingYouTubeRemote(next);
          }
        }
        updateToolbarsMasterUI();
      });
    }
    if (chkYtRemoteAutoCollapse) {
      let isAuto = localStorage.getItem('yt_pod_autocollapse') === 'true';
      chkYtRemoteAutoCollapse.checked = isAuto;
      chkYtRemoteAutoCollapse.addEventListener('change', () => {
        playSFX('tb_clicks');
        let val = chkYtRemoteAutoCollapse.checked;
        localStorage.setItem('yt_pod_autocollapse', val ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('yt_pod_autocollapse', val ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.setYtRemoteAutoCollapse === 'function') {
            window.CaspianBridge.setYtRemoteAutoCollapse(val);
          }
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(val ? "YouTube Float Pod Auto-Collapse: ON" : "YouTube Float Pod Auto-Collapse: OFF");
          }
        }
      });
    }

    // YouTube Float Pod - Timeline Default Behavior Selector
    const ytTimelineModePills = document.querySelectorAll('.yt-timeline-mode-pill');
    const ytTimelineBehaviorLabel = document.getElementById('yt-timeline-behavior-label');
    const ytTimelineLabelMap = {
      'fullscreen_only': 'Fullscreen Only',
      'both': 'Both Layouts',
      'vertical_only': 'Vertical Only',
      'manual_only': 'Off (Manual)'
    };
    let currentYtTimelineBehavior = localStorage.getItem('yt_timeline_default_behavior') || 'fullscreen_only';

    function updateYtTimelinePillsUI(behavior) {
      ytTimelineModePills.forEach(pill => {
        const isActive = pill.dataset.behavior === behavior;
        pill.classList.toggle('active', isActive);
        pill.classList.toggle('secondary', !isActive);
      });
      if (ytTimelineBehaviorLabel) {
        ytTimelineBehaviorLabel.textContent = ytTimelineLabelMap[behavior] || 'Fullscreen Only';
      }
    }
    updateYtTimelinePillsUI(currentYtTimelineBehavior);

    ytTimelineModePills.forEach(pill => {
      pill.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        const behavior = pill.dataset.behavior;
        currentYtTimelineBehavior = behavior;
        localStorage.setItem('yt_timeline_default_behavior', behavior);
        updateYtTimelinePillsUI(behavior);
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('yt_timeline_default_behavior', behavior);
          }
          if (typeof window.CaspianBridge.applyTimelineDefaultBehavior === 'function') {
            window.CaspianBridge.applyTimelineDefaultBehavior(behavior);
          }
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(`Timeline Default: ${ytTimelineLabelMap[behavior] || behavior}`);
          }
        }
      });
    });

    // ChatGPT Toolbar handlers
    if (chatgptToolbarLaunchBtn) {
      chatgptToolbarLaunchBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        localStorage.setItem('chatgpt_dock_enabled', 'true');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('chatgpt_dock_enabled', 'true');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleChatGPTDock === 'function') {
          window.CaspianBridge.toggleChatGPTDock(true);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("🚀 ChatGPT Toolbar Opened!");
        }
        updateToolbarsMasterUI();
      });
    }
    if (toggleChatgptDockBtn) {
      toggleChatgptDockBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('chatgpt_dock_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('chatgpt_dock_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('chatgpt_dock_enabled', next ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.toggleChatGPTDock === 'function') {
            window.CaspianBridge.toggleChatGPTDock(next);
          }
        }
        updateToolbarsMasterUI();
      });
    }
    if (chkChatgptDockAutoCollapse) {
      let isAuto = localStorage.getItem('chatgpt_dock_autocollapse') === 'true';
      chkChatgptDockAutoCollapse.checked = isAuto;
      chkChatgptDockAutoCollapse.addEventListener('change', () => {
        playSFX('tb_clicks');
        let val = chkChatgptDockAutoCollapse.checked;
        localStorage.setItem('chatgpt_dock_autocollapse', val ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('chatgpt_dock_autocollapse', val ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.setChatgptDockAutoCollapse === 'function') {
            window.CaspianBridge.setChatgptDockAutoCollapse(val);
          }
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(val ? "ChatGPT Toolbar Auto-Collapse: ON" : "ChatGPT Toolbar Auto-Collapse: OFF");
          }
        }
      });
    }

    // Gemini Toolbar handlers
    if (geminiToolbarLaunchBtn) {
      geminiToolbarLaunchBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        localStorage.setItem('gemini_dock_enabled', 'true');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting('gemini_dock_enabled', 'true');
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.toggleGeminiDock === 'function') {
          window.CaspianBridge.toggleGeminiDock(true);
        }
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast("🚀 Gemini Toolbar Opened!");
        }
        updateToolbarsMasterUI();
      });
    }
    if (toggleGeminiDockBtn) {
      toggleGeminiDockBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_clicks');
        let current = localStorage.getItem('gemini_dock_enabled') !== 'false';
        let next = !current;
        localStorage.setItem('gemini_dock_enabled', next ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('gemini_dock_enabled', next ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.toggleGeminiDock === 'function') {
            window.CaspianBridge.toggleGeminiDock(next);
          }
        }
        updateToolbarsMasterUI();
      });
    }
    if (chkGeminiDockAutoCollapse) {
      let isAuto = localStorage.getItem('gemini_dock_autocollapse') === 'true';
      chkGeminiDockAutoCollapse.checked = isAuto;
      chkGeminiDockAutoCollapse.addEventListener('change', () => {
        playSFX('tb_clicks');
        let val = chkGeminiDockAutoCollapse.checked;
        localStorage.setItem('gemini_dock_autocollapse', val ? 'true' : 'false');
        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting('gemini_dock_autocollapse', val ? 'true' : 'false');
          }
          if (typeof window.CaspianBridge.setGeminiDockAutoCollapse === 'function') {
            window.CaspianBridge.setGeminiDockAutoCollapse(val);
          }
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(val ? "Gemini Toolbar Auto-Collapse: ON" : "Gemini Toolbar Auto-Collapse: OFF");
          }
        }
      });
    }

    window.syncGoogleDockState = function(enabled) {
      localStorage.setItem('google_dock_enabled', enabled ? 'true' : 'false');
      updateToolbarsMasterUI();
    };

    // 6. Widget Scale Controls (Action Button, YouTube Float Pod, Google Search Toolbar, ChatGPT Toolbar, Gemini Toolbar)
    function setupScaleButtonGroup(selector, widgetName, settingKey) {
      const savedScale = localStorage.getItem(settingKey) || '1.0';
      document.querySelectorAll(selector).forEach(b => b.classList.toggle('active', (b.dataset.scale || '1.0') === savedScale));

      document.querySelectorAll(selector).forEach(btn => {
        btn.addEventListener('click', () => {
          playSFX('tb_clicks');
          const scale = btn.dataset.scale || '1.0';
          document.querySelectorAll(selector).forEach(b => b.classList.toggle('active', b.dataset.scale === scale));
          localStorage.setItem(settingKey, scale);
          if (window.CaspianBridge) {
            if (typeof window.CaspianBridge.setWidgetScale === 'function') {
              window.CaspianBridge.setWidgetScale(widgetName, parseFloat(scale));
            }
            if (typeof window.CaspianBridge.saveSetting === 'function') {
              window.CaspianBridge.saveSetting(settingKey, scale);
            }
          }
        });
      });
    }

    setupScaleButtonGroup('.btn-action-btn-scale', 'action_button', 'action_button_scale');
    setupScaleButtonGroup('.btn-yt-pod-scale', 'yt_pod', 'yt_pod_scale');
    setupScaleButtonGroup('.btn-google-dock-scale', 'google_dock', 'google_dock_scale');
    setupScaleButtonGroup('.btn-chatgpt-dock-scale', 'chatgpt_dock', 'chatgpt_dock_scale');
    setupScaleButtonGroup('.btn-gemini-dock-scale', 'gemini_dock', 'gemini_dock_scale');

    // 7. Developer & External Links Handler (Always open in Caspian Browser Tabs)
    document.querySelectorAll('a[href^="http"]').forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const url = link.getAttribute('href');
        if (url) {
          if (window.CaspianBridge && typeof window.CaspianBridge.openTab === 'function') {
            window.CaspianBridge.openTab(url);
          } else {
            window.open(url, '_blank');
          }
        }
      });
    });

    // 8. Global Top-Right Master Engine Power Toggle Button (#power-toggle-btn / #btn-power-off)
    const masterPowerBtn = document.getElementById('power-toggle-btn') || document.getElementById('btn-power-off');
    if (masterPowerBtn) {
      masterPowerBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        playSFX('tb_power');
        let anyOn = (localStorage.getItem('temp_saver_enabled') !== 'false' ||
          localStorage.getItem('chat_limit_enabled') !== 'false' ||
          localStorage.getItem('caspian_current_enabled') !== 'false' ||
          localStorage.getItem('adblock_enabled') !== 'false' ||
          localStorage.getItem('yt_engine_enabled') !== 'false' ||
          localStorage.getItem('google_dock_enabled') === 'true' ||
          localStorage.getItem('yt_dock_enabled') !== 'false' ||
          localStorage.getItem('chatgpt_dock_enabled') !== 'false' ||
          localStorage.getItem('gemini_dock_enabled') !== 'false');
        let targetState = !anyOn;

        ['temp_saver_enabled', 'chat_limit_enabled', 'caspian_current_enabled', 'adblock_enabled', 'yt_engine_enabled', 'google_dock_enabled', 'yt_dock_enabled', 'chatgpt_dock_enabled', 'gemini_dock_enabled'].forEach(key => {
          localStorage.setItem(key, targetState ? 'true' : 'false');
          if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
            window.CaspianBridge.saveSetting(key, targetState ? 'true' : 'false');
          }
        });

        updateEngineCardUI(cardTS, toggleTSBtn, null, document.getElementById('ts-status-dot'), 'temp_saver_enabled');
        updateEngineCardUI(cardCL, toggleCLBtn, chatLimitBody, document.getElementById('status-dot'), 'chat_limit_enabled');
        updateEngineCardUI(cardCC, toggleCCBtn, ccBody, document.getElementById('cc-status-dot'), 'caspian_current_enabled');
        updateEngineCardUI(cardAB, toggleAdblockBtn, adblockBody, document.getElementById('adblock-dot'), 'adblock_enabled');
        updateEngineCardUI(cardYT, toggleYTBtn, ytControlBody, ytStatusDot, 'yt_engine_enabled');
        updateToolbarsMasterUI();

        if (window.CaspianBridge) {
          if (typeof window.CaspianBridge.toggleGoogleSearchDock === 'function') window.CaspianBridge.toggleGoogleSearchDock(targetState);
          if (typeof window.CaspianBridge.toggleFloatingYouTubeRemote === 'function') window.CaspianBridge.toggleFloatingYouTubeRemote(targetState);
          if (typeof window.CaspianBridge.toggleChatGPTDock === 'function') window.CaspianBridge.toggleChatGPTDock(targetState);
          if (typeof window.CaspianBridge.toggleGeminiDock === 'function') window.CaspianBridge.toggleGeminiDock(targetState);
          if (typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(targetState ? '⚡ All Caspian Engines Activated!' : '🔌 All Engines Disabled');
          }
        }
      });
    }

    // Floating Multi-Select Group Toolbar Event Listeners
    let selectedGroupEmoji = '📁';
    let selectedGroupColor = '#ef4444';
    const toolbarGroupBtn = document.getElementById('toolbar-group-btn');
    const toolbarDeselectBtn = document.getElementById('toolbar-deselect-btn');
    const toolbarDeleteBtn = document.getElementById('toolbar-delete-btn');
    const modalCreateGroup = document.getElementById('modal-create-group');
    const inputGroupTitle = document.getElementById('input-group-title');

    if (toolbarGroupBtn) {
      toolbarGroupBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (modalCreateGroup && inputGroupTitle) {
          editingGroupId = null;
          inputGroupTitle.value = `Tab Group ${tabGroups.length + 1}`;
          selectedGroupEmoji = '📁';
          selectedGroupColor = '#ef4444';
          document.querySelectorAll('.group-emoji-dot').forEach(d => {
            d.classList.toggle('active', d.dataset.emoji === '📁');
          });
          document.querySelectorAll('.group-color-dot').forEach(d => {
            d.classList.toggle('active', d.dataset.color === '#ef4444');
          });
          modalCreateGroup.style.display = 'flex';
        }
      });
    }

    const toolbarFavoriteBtn = document.getElementById('toolbar-favorite-btn');
    if (toolbarFavoriteBtn) {
      toolbarFavoriteBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (selectedTabIds.size === 0) return;

        const selTabs = (cachedOpenTabs || []).filter(t => selectedTabIds.has(t.id));
        if (selTabs.length === 0) return;

        const anyNotFav = selTabs.some(t => !t.isFavorite);
        const targetFavState = anyNotFav;

        const idsArray = selTabs.map(t => t.id);
        if (window.CaspianBridge && typeof window.CaspianBridge.setTabsFavorite === 'function') {
          window.CaspianBridge.setTabsFavorite(JSON.stringify(idsArray), targetFavState);
        } else if (window.CaspianBridge && typeof window.CaspianBridge.setGroupTabsFavorite === 'function') {
          window.CaspianBridge.setGroupTabsFavorite(JSON.stringify(idsArray), targetFavState);
        }

        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(targetFavState ? `⭐ ${idsArray.length} Tabs Favorited!` : `★ ${idsArray.length} Tabs Unfavorited`);
        }

        selTabs.forEach(t => t.isFavorite = targetFavState);
        renderOpenTabs();
        setTimeout(() => {
          if (typeof window.renderOpenTabs === 'function') {
            window.renderOpenTabs();
          }
        }, 150);
      });
    }

    if (toolbarDeselectBtn) {
      toolbarDeselectBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        isMultiSelectMode = false;
        selectedTabIds.clear();
        renderOpenTabs();
      });
    }

    if (toolbarDeleteBtn) {
      toolbarDeleteBtn.addEventListener('click', () => {
        try { playSFX('tb_close'); } catch (e) {}
        const idsArray = Array.from(selectedTabIds);
        isMultiSelectMode = false;
        selectedTabIds.clear();
        if (idsArray.length > 0) {
          if (window.CaspianBridge && typeof window.CaspianBridge.closeMultipleTabs === 'function') {
            window.CaspianBridge.closeMultipleTabs(JSON.stringify(idsArray));
          } else {
            idsArray.forEach(id => triggerCloseTab(id));
          }
        }
        setTimeout(() => {
          renderOpenTabs();
          updateUndoButtonState();
        }, 150);
      });
    }

    // Emoji Palette Dots
    document.querySelectorAll('.group-emoji-dot').forEach(dot => {
      dot.addEventListener('click', () => {
        playSFX('tb_clicks');
        document.querySelectorAll('.group-emoji-dot').forEach(d => d.classList.remove('active'));
        dot.classList.add('active');
        selectedGroupEmoji = dot.dataset.emoji || '📁';
      });
    });

    // Color Palette Dots
    document.querySelectorAll('.group-color-dot').forEach(dot => {
      dot.addEventListener('click', () => {
        playSFX('tb_clicks');
        document.querySelectorAll('.group-color-dot').forEach(d => d.classList.remove('active'));
        dot.classList.add('active');
        selectedGroupColor = dot.dataset.color || '#ef4444';
      });
    });

    // Modal Confirm & Cancel
    const btnConfirmCreateGroup = document.getElementById('btn-confirm-create-group');
    const btnCancelCreateGroup = document.getElementById('btn-cancel-create-group');
    const modalCloseGroupBtn = document.getElementById('modal-close-group-btn');

    const closeModal = () => {
      playSFX('tb_modal');
      if (modalCreateGroup) modalCreateGroup.style.display = 'none';
    };

    if (btnCancelCreateGroup) btnCancelCreateGroup.addEventListener('click', closeModal);
    if (modalCloseGroupBtn) modalCloseGroupBtn.addEventListener('click', closeModal);

    if (btnConfirmCreateGroup) {
      btnConfirmCreateGroup.addEventListener('click', () => {
        playSFX('tb_modal');
        const title = inputGroupTitle ? (inputGroupTitle.value.trim() || 'Tab Group') : 'Tab Group';
        const groupEmoji = (selectedGroupEmoji && selectedGroupEmoji.trim()) ? selectedGroupEmoji.trim() : '📁';
        const groupColor = selectedGroupColor || '#ef4444';

        if (editingGroupId) {
          const group = tabGroups.find(g => g.id === editingGroupId);
          if (group) {
            group.title = title;
            group.color = groupColor;
            group.icon = groupEmoji;
          }
        } else {
          const newGroup = {
            id: `group_${Date.now()}`,
            title: title,
            color: groupColor,
            icon: groupEmoji,
            tabIds: Array.from(selectedTabIds)
          };
          tabGroups.push(newGroup);
        }

        saveTabGroups();
        isMultiSelectMode = false;
        selectedTabIds.clear();
        closeModal();
        renderOpenTabs();
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`📁 Saved Group "${title}"!`);
        }
      });
    }

    // Inside Group Banner Action Buttons
    const btnCloseGroupView = document.getElementById('btn-close-group-view');
    const btnEditGroup = document.getElementById('btn-edit-group');
    const btnLeaveGroup = document.getElementById('btn-leave-group');
    const btnDeleteGroup = document.getElementById('btn-delete-group');

    if (btnCloseGroupView) {
      btnCloseGroupView.addEventListener('click', () => {
        playSFX('tb_clicks');
        activeGroupId = null;
        renderOpenTabs();
      });
    }

    if (btnEditGroup) {
      btnEditGroup.addEventListener('click', () => {
        playSFX('tb_clicks');
        const group = tabGroups.find(g => g.id === activeGroupId);
        if (group && modalCreateGroup && inputGroupTitle) {
          editingGroupId = group.id;
          inputGroupTitle.value = group.title;
          selectedGroupColor = group.color || '#ef4444';
          selectedGroupEmoji = group.icon || '📁';
          document.querySelectorAll('.group-color-dot').forEach(d => {
            d.classList.toggle('active', d.dataset.color === selectedGroupColor);
          });
          document.querySelectorAll('.group-emoji-dot').forEach(d => {
            d.classList.toggle('active', d.dataset.emoji === selectedGroupEmoji);
          });
          modalCreateGroup.style.display = 'flex';
        }
      });
    }

    if (btnLeaveGroup) {
      btnLeaveGroup.addEventListener('click', () => {
        playSFX('tb_clicks');
        const group = tabGroups.find(g => g.id === activeGroupId);
        const title = group ? group.title : 'Group';
        tabGroups = tabGroups.filter(g => g.id !== activeGroupId);
        saveTabGroups();
        activeGroupId = null;
        renderOpenTabs();
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast(`📂 Dissolved "${title}". Tabs separated to main view.`);
        }
      });
    }

    // Tab Filter Pills (All / Groups / Single - Fix #8)
    document.querySelectorAll('.tab-filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        playSFX('tb_clicks');
        document.querySelectorAll('.tab-filter-pill').forEach(p => {
          p.classList.remove('active');
          p.style.background = 'transparent';
          p.style.color = 'var(--text-sub)';
        });
        pill.classList.add('active');
        pill.style.background = 'var(--accent)';
        pill.style.color = '#fff';
        activeTabFilter = pill.dataset.filter || 'all';
        renderOpenTabs();
      });
    });

    // Modal Group Color Dots Binding
    document.querySelectorAll('.modal-group-color-dot').forEach(dot => {
      dot.addEventListener('click', () => {
        playSFX('tb_clicks');
        document.querySelectorAll('.modal-group-color-dot').forEach(d => d.classList.remove('active'));
        dot.classList.add('active');
        selectedGroupColor = dot.dataset.color || '#ef4444';
        const colorDot = document.getElementById('group-modal-color-dot');
        if (colorDot) colorDot.style.background = selectedGroupColor;
      });
    });

    // Toast Undo Handler for Group & Single Tab Restoration (Fix #5)
    const undoToastBtn = document.getElementById('undo-toast-btn');
    if (undoToastBtn) {
      undoToastBtn.addEventListener('click', () => {
        playSFX('tb_clicks');
        if (lastDeletedGroup) {
          if (window.CaspianBridge && typeof window.CaspianBridge.restoreLastClosedGroupTabs === 'function') {
            window.CaspianBridge.restoreLastClosedGroupTabs();
          }
          tabGroups.push(lastDeletedGroup.group);
          saveTabGroups();
          lastDeletedGroup = null;
          if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast(`Restored group "${lastDeletedGroup ? lastDeletedGroup.group.title : 'Group'}"!`);
          }
        } else {
          if (window.CaspianBridge && typeof window.CaspianBridge.restoreLastClosedTab === 'function') {
            window.CaspianBridge.restoreLastClosedTab();
          }
        }
        const toastContainer = document.getElementById('undo-toast-container');
        if (toastContainer) toastContainer.style.display = 'none';
        setTimeout(renderOpenTabs, 200);
      });
    }

    if (btnDeleteGroup) {
      btnDeleteGroup.addEventListener('click', () => {
        playSFX('tb_close');
        const group = tabGroups.find(g => g.id === activeGroupId);
        if (group) {
          lastDeletedGroup = { group: Object.assign({}, group), tabIds: [...group.tabIds] };
          if (window.CaspianBridge && typeof window.CaspianBridge.closeMultipleTabs === 'function') {
            window.CaspianBridge.closeMultipleTabs(JSON.stringify(group.tabIds));
          }
          tabGroups = tabGroups.filter(g => g.id !== activeGroupId);
          saveTabGroups();
          showUndoToast(`Group "${group.title}" deleted`);
        }
        activeGroupId = null;
        setTimeout(renderOpenTabs, 150);
      });
    }

    // ==========================================
    // CASPIAN CASKS (MULTI-ACCOUNT CONTAINERS) LOGIC
    // ==========================================
    let controlCasksData = {
      activeCaskId: 'cask_caspian',
      activeCaskName: 'Caspian Cask',
      activeCaskIcon: '🌊',
      activeCaskColor: '#1B4264',
      casks: []
    };

    function loadControlCasks() {
      try {
        if (window.CaspianBridge && typeof window.CaspianBridge.getCaspianCasks === 'function') {
          const jsonStr = window.CaspianBridge.getCaspianCasks();
          controlCasksData = JSON.parse(jsonStr);
          renderControlCaskUI();
        }
      } catch (e) {}
    }

    function renderControlCaskUI() {
      const headerIconEl = document.getElementById('header-cask-icon');
      const headerPillEl = document.getElementById('header-cask-pill');
      const caskSubEl = document.getElementById('cask-active-sub');

      const icon = controlCasksData.activeCaskIcon || '🌊';
      const name = controlCasksData.activeCaskName || 'Caspian Cask';

      if (headerIconEl) headerIconEl.textContent = icon;
      if (headerPillEl) headerPillEl.title = `Active: ${icon} ${name} - Tap to switch`;
      if (caskSubEl) caskSubEl.textContent = `Active: ${icon} ${name} • Isolated login sessions.`;
    }

    function openControlCasksModal() {
      try { playSFX('tb_clicks'); } catch (e) {}
      loadControlCasks();
      renderControlCasksList();
      const modal = document.getElementById('control-casks-modal');
      if (modal) modal.style.display = 'flex';
    }

    function closeControlCasksModal() {
      try { playSFX('tb_clicks'); } catch (e) {}
      const modal = document.getElementById('control-casks-modal');
      if (modal) modal.style.display = 'none';
      hideControlNewCaskForm();
    }

    function renderControlCasksList() {
      const container = document.getElementById('control-casks-list');
      if (!container) return;
      container.innerHTML = '';

      const casks = controlCasksData.casks || [];
      casks.forEach(cask => {
        const isActive = cask.id === controlCasksData.activeCaskId;
        const row = document.createElement('div');
        row.className = 'cask-item-row' + (isActive ? ' active' : '');

        row.innerHTML = `
          <div style="display:flex; align-items:center; gap:10px;">
            <div style="width:34px; height:34px; border-radius:10px; background:var(--input-bg, rgba(128,128,128,0.08)); border:1.5px solid ${cask.color || 'var(--accent, #00E5FF)'}; display:flex; align-items:center; justify-content:center; font-size:16px;">
              ${cask.icon || '🌊'}
            </div>
            <div>
              <div style="font-size:12px; font-weight:700; color:var(--text-main);">${cask.name}</div>
              <div style="font-size:9px; color:var(--text-muted);">${cask.isDefault ? 'Default Cask' : 'Custom Vault'}</div>
            </div>
          </div>
          <div style="display:flex; align-items:center; gap:6px;">
            ${isActive ? '<span style="font-size:10px; font-weight:700; color:#fff; background:var(--accent-gradient, #00E5FF); padding:4px 10px; border-radius:8px; box-shadow:0 2px 8px var(--accent-glow);">Active</span>' : '<button class="btn-ctrl-switch" style="padding:5px 12px; border-radius:8px; background:var(--accent-gradient, #00E5FF); border:none; color:#fff; font-size:11px; font-weight:700; cursor:pointer; box-shadow:0 2px 8px var(--accent-glow);">Switch</button>'}
            ${(!cask.isDefault && !isActive) ? `<button class="btn-ctrl-del" style="background:none; border:none; color:#F43F5E; font-size:15px; cursor:pointer; padding:4px;" title="Delete Cask">🗑️</button>` : ''}
          </div>
        `;

        if (!isActive) {
          const switchBtn = row.querySelector('.btn-ctrl-switch');
          if (switchBtn) {
            switchBtn.addEventListener('click', (e) => {
              e.stopPropagation();
              switchControlCask(cask.id);
            });
          }
          row.addEventListener('click', () => switchControlCask(cask.id));
        }

        const delBtn = row.querySelector('.btn-ctrl-del');
        if (delBtn) {
          delBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            deleteControlCask(cask.id);
          });
        }

        container.appendChild(row);
      });
    }

    function switchControlCask(caskId) {
      try { playSFX('tb_clicks'); } catch (e) {}
      if (window.CaspianBridge && typeof window.CaspianBridge.switchCaspianCask === 'function') {
        window.CaspianBridge.switchCaspianCask(caskId);
      }
      closeControlCasksModal();
      setTimeout(loadControlCasks, 300);
    }

    function deleteControlCask(caskId) {
      try { playSFX('tb_clicks'); } catch (e) {}
      if (window.CaspianBridge && typeof window.CaspianBridge.deleteCaspianCask === 'function') {
        window.CaspianBridge.deleteCaspianCask(caskId);
      }
      setTimeout(() => {
        loadControlCasks();
        renderControlCasksList();
      }, 200);
    }

    function showControlNewCaskForm() {
      try { playSFX('tb_clicks'); } catch (e) {}
      const form = document.getElementById('control-new-cask-form');
      const btn = document.getElementById('btn-control-new-cask');
      if (form) form.style.display = 'flex';
      if (btn) btn.style.display = 'none';
      selectedNewCaskEmoji = '🌊';
      const emojiDisplayEl = document.getElementById('control-cask-emoji-display');
      if (emojiDisplayEl) emojiDisplayEl.textContent = '🌊';
      resetControlPresetDropdown();
      const nameInput = document.getElementById('control-new-cask-name');
      if (nameInput) {
        nameInput.value = '';
        setTimeout(() => nameInput.focus(), 80);
      }
    }

    function hideControlNewCaskForm() {
      const form = document.getElementById('control-new-cask-form');
      const btn = document.getElementById('btn-control-new-cask');
      if (form) form.style.display = 'none';
      if (btn) btn.style.display = 'flex';
      const nameInput = document.getElementById('control-new-cask-name');
      if (nameInput) nameInput.value = '';
      selectedNewCaskEmoji = '🌊';
      const emojiDisplayEl = document.getElementById('control-cask-emoji-display');
      if (emojiDisplayEl) emojiDisplayEl.textContent = '🌊';
      resetControlPresetDropdown();
    }

    function confirmControlCreateCask() {
      try { playSFX('tb_clicks'); } catch (e) {}
      const nameInput = document.getElementById('control-new-cask-name');
      const name = nameInput ? nameInput.value.trim() : '';
      const icon = selectedNewCaskEmoji || '🌊';

      if (!name) {
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast('Please enter a Cask name or pick a preset');
        }
        return;
      }

      if (window.CaspianBridge && typeof window.CaspianBridge.createCaspianCask === 'function') {
        window.CaspianBridge.createCaspianCask(name, icon, 'var(--accent, #00E5FF)');
      }

      hideControlNewCaskForm();
      setTimeout(() => {
        loadControlCasks();
        renderControlCasksList();
      }, 200);
    }

    window.onCaspianCasksUpdated = function (jsonStr) {
      try {
        if (jsonStr) {
          controlCasksData = JSON.parse(jsonStr);
          renderControlCaskUI();
          renderControlCasksList();
        }
      } catch (e) {}
    };

    // Modal & Card Event Listeners with Dynamic Delegation
    document.addEventListener('click', (e) => {
      if (e.target.closest('#header-cask-pill')) {
        e.stopPropagation();
        openControlCasksModal();
        return;
      } else if (e.target.closest('#menu-switch-cask-btn') || e.target.closest('#card-casks-manager')) {
        openControlCasksModal();
      } else if (e.target.closest('#close-control-casks-modal-btn')) {
        closeControlCasksModal();
      } else if (e.target.closest('#btn-control-new-cask')) {
        showControlNewCaskForm();
      } else if (e.target.closest('#btn-control-create-cancel')) {
        hideControlNewCaskForm();
      } else if (e.target.closest('#btn-control-create-confirm')) {
        confirmControlCreateCask();
      } else if (e.target.closest('#close-uptodate-modal-btn')) {
        hideUpToDateModal();
      } else if (e.target.closest('#close-update-modal-btn') || e.target.closest('#update-modal-later-btn')) {
        hideUpdateModal();
      } else if (e.target.closest('.sheet-brand-tag') || e.target.closest('#card-app-updater')) {
        if (latestUpdateInfo && latestUpdateInfo.hasUpdate) {
          showUpdateModal(latestUpdateInfo);
        } else if (latestUpdateInfo && !latestUpdateInfo.hasUpdate) {
          showUpToDateModal(latestUpdateInfo);
        }
      }
    });

    window.openControlCasksModal = openControlCasksModal;
    window.closeControlCasksModal = closeControlCasksModal;

    let selectedNewCaskEmoji = '🌊';
    const emojiInputEl = document.getElementById('control-cask-emoji-input');
    const emojiDisplayEl = document.getElementById('control-cask-emoji-display');
    const emojiBtnEl = document.getElementById('control-cask-emoji-btn');
    const emojiRegex = /\p{Extended_Pictographic}/u;

    const CASPIAN_CASK_PRESETS = [
      { name: 'Work Cask', emoji: '💼', tag: 'Productivity' },
      { name: 'Personal Vault', emoji: '🏠', tag: 'Personal' },
      { name: 'Research & Study', emoji: '🔬', tag: 'Education' },
      { name: 'Coding & Dev', emoji: '💻', tag: 'Development' },
      { name: 'Social & Media', emoji: '🌐', tag: 'Networking' },
      { name: 'Entertainment & Video', emoji: '🎬', tag: 'Streaming' },
      { name: 'Finance & Crypto', emoji: '📈', tag: 'Finance' },
      { name: 'Pacific Cask', emoji: '⚓', tag: 'Ocean' },
      { name: 'Coral Reef', emoji: '🪸', tag: 'Marine' },
      { name: 'Iceberg Vault', emoji: '🧊', tag: 'Cold Storage' },
      { name: 'Trident Vault', emoji: '🔱', tag: 'Security' },
      { name: 'Deep Ocean', emoji: '🌊', tag: 'Default' },
      { name: 'Travel & Navigation', emoji: '⛵', tag: 'Travel' }
    ];

    function resetControlPresetDropdown() {
      const triggerIcon = document.getElementById('control-cask-preset-trigger-icon');
      const triggerLabel = document.getElementById('control-cask-preset-trigger-label');
      const chevron = document.getElementById('control-cask-preset-chevron');
      const menu = document.getElementById('control-cask-preset-menu');
      if (triggerIcon) triggerIcon.textContent = '✨';
      if (triggerLabel) {
        triggerLabel.textContent = 'Choose a Cask Preset (Optional)...';
        triggerLabel.style.color = 'var(--text-muted)';
      }
      if (chevron) chevron.style.transform = 'rotate(0deg)';
      if (menu) {
        menu.style.display = 'none';
        const items = menu.querySelectorAll('.preset-dropdown-item');
        items.forEach(el => el.classList.remove('selected'));
      }
    }

    function initControlPresetDropdown() {
      const trigger = document.getElementById('control-cask-preset-trigger');
      const menu = document.getElementById('control-cask-preset-menu');
      const chevron = document.getElementById('control-cask-preset-chevron');
      if (!trigger || !menu) return;

      menu.innerHTML = CASPIAN_CASK_PRESETS.map(p => `
        <div class="preset-dropdown-item" data-name="${p.name}" data-emoji="${p.emoji}" style="display:flex; align-items:center; justify-content:space-between; padding:8px 10px; border-radius:10px; cursor:pointer; transition:all 0.15s ease;">
          <div style="display:flex; align-items:center; gap:9px;">
            <div style="width:28px; height:28px; border-radius:8px; background:var(--input-bg, rgba(128,128,128,0.08)); border:1px solid var(--border-glass); display:flex; align-items:center; justify-content:center; font-size:14px;">
              ${p.emoji}
            </div>
            <span style="font-size:12px; font-weight:700; color:var(--text-main);">${p.name}</span>
          </div>
          <span style="font-size:9.5px; font-weight:700; color:var(--text-muted); background:rgba(128,128,128,0.12); padding:2px 7px; border-radius:6px;">${p.tag}</span>
        </div>
      `).join('');

      trigger.addEventListener('click', (e) => {
        e.stopPropagation();
        try { playSFX('tb_clicks'); } catch (e) {}
        const isOpen = menu.style.display === 'flex';
        menu.style.display = isOpen ? 'none' : 'flex';
        if (chevron) chevron.style.transform = isOpen ? 'rotate(0deg)' : 'rotate(180deg)';
      });

      menu.querySelectorAll('.preset-dropdown-item').forEach(item => {
        item.addEventListener('click', (e) => {
          e.stopPropagation();
          try { playSFX('tb_clicks'); } catch (e) {}
          const name = item.getAttribute('data-name');
          const emoji = item.getAttribute('data-emoji');

          const nameInput = document.getElementById('control-new-cask-name');
          if (nameInput) nameInput.value = name;

          selectedNewCaskEmoji = emoji;
          const emojiDisp = document.getElementById('control-cask-emoji-display');
          if (emojiDisp) emojiDisp.textContent = emoji;

          const triggerIcon = document.getElementById('control-cask-preset-trigger-icon');
          const triggerLabel = document.getElementById('control-cask-preset-trigger-label');
          if (triggerIcon) triggerIcon.textContent = emoji;
          if (triggerLabel) {
            triggerLabel.textContent = name;
            triggerLabel.style.color = 'var(--text-main)';
          }

          menu.querySelectorAll('.preset-dropdown-item').forEach(el => el.classList.remove('selected'));
          item.classList.add('selected');

          menu.style.display = 'none';
          if (chevron) chevron.style.transform = 'rotate(0deg)';
        });
      });
    }

    initControlPresetDropdown();

    let lastEmojiToastTime = 0;
    function notifyEmojiInput() {
      const now = Date.now();
      if (now - lastEmojiToastTime > 1500) {
        lastEmojiToastTime = now;
        if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
          window.CaspianBridge.showToast('Enter an emoji for this Cask ✨');
        }
      }
    }

    if (emojiBtnEl && emojiInputEl) {
      emojiBtnEl.addEventListener('click', () => {
        try { playSFX('tb_clicks'); } catch (e) {}
        notifyEmojiInput();
        emojiInputEl.focus();
      });
      emojiInputEl.addEventListener('focus', () => {
        notifyEmojiInput();
      });
    }

    if (emojiInputEl) {
      emojiInputEl.addEventListener('input', () => {
        const val = emojiInputEl.value.trim();
        if (!val) return;
        const match = val.match(emojiRegex);
        if (match) {
          selectedNewCaskEmoji = match[0];
          if (emojiDisplayEl) emojiDisplayEl.textContent = selectedNewCaskEmoji;
          try { playSFX('tb_clicks'); } catch (e) {}
        } else {
          // Reject normal text
          if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
            window.CaspianBridge.showToast('Please enter an emoji icon ✨');
          }
        }
        emojiInputEl.value = '';
      });
    }

    // Initial Load
    setTimeout(loadControlCasks, 100);

    // Force clean state & render retries on startup so tabs are never locked after app restart
    isMultiSelectMode = false;
    selectedTabIds.clear();
    renderOpenTabs();
    setTimeout(renderOpenTabs, 150);
    setTimeout(renderOpenTabs, 400);
    setTimeout(renderOpenTabs, 1000);
  });
})();




  // Live Favicon Receiver from Native Android WebChromeClient
  window.onTabFaviconReceived = function(tabId, faviconB64) {
    if (!tabId || !faviconB64) return;
    const tab = (cachedOpenTabs || []).find(t => t.id === tabId);
    if (tab) {
      tab.faviconB64 = faviconB64;
      const card = document.querySelector(`.chrome-tab-card[data-tabid="${tabId}"]`);
      if (card) {
        const headerDiv = card.querySelector('.chrome-tab-header > div:first-child');
        if (headerDiv) {
          let img = headerDiv.querySelector('img');
          if (!img) {
            img = document.createElement('img');
            img.style.cssText = 'width: 16px; height: 16px; border-radius: 4px; object-fit: cover;';
            const titleSpan = headerDiv.querySelector('.chrome-tab-title');
            if (titleSpan) headerDiv.insertBefore(img, titleSpan);
            else headerDiv.appendChild(img);
          }
          img.src = faviconB64;
        }
      }
    }
  };


// ==========================================================================
// CASPIAN DOWNLOAD MANAGER JAVASCRIPT CONTROLLER
// ==========================================================================

(function() {
  let currentDownloadsTab = 'active'; // 'active' | 'completed'
  let cachedDownloads = [];
  let expandedDownloadIds = new Set();

  function formatDownloadBytes(bytes) {
    if (!bytes || bytes <= 0) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }

  function getFileCategoryInfo(fileName, mimeType) {
    const ext = fileName ? fileName.split('.').pop().toLowerCase() : '';
    if (ext === 'apk' || (mimeType && mimeType.includes('vnd.android.package-archive'))) {
      return { icon: '📦', class: 'apk', label: 'APK', title: 'Android App Package (APK)' };
    }
    if (ext === 'pdf' || (mimeType && mimeType.includes('pdf'))) {
      return { icon: '📄', class: 'pdf', label: 'PDF', title: 'PDF Document' };
    }
    if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext) || (mimeType && mimeType.startsWith('image/'))) {
      return { icon: '🖼️', class: 'media', label: 'IMG', title: 'Image Graphic' };
    }
    if (['mp4', 'mkv', 'webm', 'mov', 'avi'].includes(ext) || (mimeType && mimeType.startsWith('video/'))) {
      return { icon: '🎬', class: 'media', label: 'VID', title: 'Video Recording' };
    }
    if (['mp3', 'wav', 'ogg', 'm4a', 'flac'].includes(ext) || (mimeType && mimeType.startsWith('audio/'))) {
      return { icon: '🎵', class: 'media', label: 'AUD', title: 'Audio Music Track' };
    }
    if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext) || (mimeType && (mimeType.includes('zip') || mimeType.includes('compressed')))) {
      return { icon: '🗜️', class: 'archive', label: 'ZIP', title: 'Compressed Zip Archive' };
    }
    if (['html', 'js', 'json', 'css', 'py', 'java', 'xml', 'txt', 'md'].includes(ext)) {
      return { icon: '💻', class: 'code', label: ext.toUpperCase(), title: ext.toUpperCase() + ' Source File' };
    }
    return { icon: '📁', class: 'doc', label: ext ? ext.toUpperCase() : 'FILE', title: (ext ? ext.toUpperCase() + ' ' : '') + 'File' };
  }

  window.openDownloadsModalStandalone = function() {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    document.body.classList.add('downloads-standalone-mode');
    document.body.classList.remove('menu-revealed');
    const modal = document.getElementById('caspian-downloads-modal');
    if (modal) modal.style.display = 'flex';
    refreshDownloadsList();
  };

  window.revealCaspianMenu = function() {
    document.body.classList.add('menu-revealed');
    if (typeof renderOpenTabs === 'function') renderOpenTabs();
    if (typeof syncAppVersion === 'function') syncAppVersion();
    if (typeof restoreSavedSettings === 'function') restoreSavedSettings();
    if (typeof updateDevHudCounters === 'function') updateDevHudCounters();
  };

  window.unrevealCaspianMenu = function() {
    document.body.classList.remove('menu-revealed');
  };

  window.onDownloadsStandaloneClosed = function() {
    document.body.classList.remove('downloads-standalone-mode');
    document.body.classList.remove('menu-revealed');
    const modal = document.getElementById('caspian-downloads-modal');
    if (modal) modal.style.display = 'none';
  };

  window.openDownloadsModal = function(fromOmnibox) {
    if (fromOmnibox) {
      window.openDownloadsModalStandalone();
      return;
    }
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    const modal = document.getElementById('caspian-downloads-modal');
    if (!modal) return;
    refreshDownloadsList();
    modal.style.display = 'flex';
  };

  window.closeDownloadsModal = function() {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (document.body.classList.contains('downloads-standalone-mode')) {
      if (window.CaspianBridge && typeof window.CaspianBridge.hideDownloadsManagerModal === 'function') {
        window.CaspianBridge.hideDownloadsManagerModal();
        return;
      }
    }
    const modal = document.getElementById('caspian-downloads-modal');
    if (modal) modal.style.display = 'none';
  };

  function switchDownloadsTab(tab) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    currentDownloadsTab = tab;
    const activeBtn = document.getElementById('downloads-tab-active-btn');
    const completedBtn = document.getElementById('downloads-tab-completed-btn');
    const activeList = document.getElementById('downloads-active-list');
    const completedList = document.getElementById('downloads-completed-list');

    if (tab === 'active') {
      if (activeBtn) activeBtn.className = 'oneui-pill-btn primary';
      if (completedBtn) completedBtn.className = 'oneui-pill-btn secondary';
      if (activeList) activeList.style.display = 'flex';
      if (completedList) completedList.style.display = 'none';
    } else {
      if (activeBtn) activeBtn.className = 'oneui-pill-btn secondary';
      if (completedBtn) completedBtn.className = 'oneui-pill-btn primary';
      if (activeList) activeList.style.display = 'none';
      if (completedList) completedList.style.display = 'flex';
    }
    renderDownloadsUI();
  }

  function refreshDownloadsList() {
    if (window.CaspianBridge && typeof window.CaspianBridge.getDownloadsJson === 'function') {
      try {
        const jsonStr = window.CaspianBridge.getDownloadsJson();
        cachedDownloads = JSON.parse(jsonStr || '[]');
      } catch (e) {
        console.error('Failed to parse downloads JSON', e);
      }
    }
    renderDownloadsUI();
  }

  function updateDownloadsBadge(activeCount) {
    const badge = document.getElementById('downloads-badge');
    if (!badge) return;
    if (activeCount > 0) {
      badge.textContent = activeCount;
      badge.style.display = 'flex';
    } else {
      badge.style.display = 'none';
    }
  }

  window.toggleExpandDownloadCard = function(id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (expandedDownloadIds.has(id)) {
      expandedDownloadIds.delete(id);
    } else {
      expandedDownloadIds.add(id);
    }
    renderDownloadsUI();
  };

  function renderDownloadsUI() {
    const activeListEl = document.getElementById('downloads-active-list');
    const completedListEl = document.getElementById('downloads-completed-list');
    const activeEmptyEl = document.getElementById('downloads-active-empty');
    const completedEmptyEl = document.getElementById('downloads-completed-empty');
    const activeCountEl = document.getElementById('downloads-tab-active-count');
    const completedCountEl = document.getElementById('downloads-tab-completed-count');

    if (!activeListEl || !completedListEl) return;

    const activeItems = cachedDownloads.filter(d => d.status === 'DOWNLOADING' || d.status === 'PENDING' || d.status === 'PAUSED');
    const completedItems = cachedDownloads.filter(d => d.status === 'COMPLETED' || d.status === 'FAILED' || d.status === 'CANCELLED');

    if (activeCountEl) activeCountEl.textContent = '(' + activeItems.length + ')';
    if (completedCountEl) completedCountEl.textContent = '(' + completedItems.length + ')';
    updateDownloadsBadge(activeItems.length);

    // Render Active
    activeListEl.innerHTML = '';
    if (activeItems.length === 0) {
      activeListEl.innerHTML = `
        <div id="downloads-active-empty" style="padding: 30px 10px; text-align: center; color: var(--text-muted); font-size: 12px;">
          <div style="font-size: 28px; margin-bottom: 8px;">⚡</div>
          No active downloads right now
        </div>`;
    } else {
      activeItems.forEach(item => {
        const cat = getFileCategoryInfo(item.fileName, item.mimeType);
        const percent = item.totalBytes > 0 ? Math.min(100, Math.round((item.downloadedBytes * 100) / item.totalBytes)) : 0;
        const isIndeterminate = item.totalBytes <= 0;
        const isPaused = item.status === 'PAUSED';
        const isExpanded = expandedDownloadIds.has(item.id);

        let statsText = formatDownloadBytes(item.downloadedBytes);
        if (item.totalBytes > 0) statsText += ' / ' + formatDownloadBytes(item.totalBytes) + ' (' + percent + '%)';
        if (item.speedBytesPerSec > 0 && !isPaused) statsText += ' • ' + (item.speedBytesPerSec / (1024 * 1024)).toFixed(1) + ' MB/s';
        if (item.etaSeconds > 0 && !isPaused) statsText += ' • ' + Math.round(item.etaSeconds) + 's left';
        if (isPaused) statsText += ' • Paused';

        const card = document.createElement('div');
        card.className = 'download-item-card' + (isExpanded ? ' expanded' : '');
        card.onclick = function() { window.toggleExpandDownloadCard(item.id); };

        if (!isExpanded) {
          card.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 12.5px; font-weight: 700; color: var(--text-main); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">` + (item.fileName || 'Download') + `</div>
                <div style="font-size: 10.5px; color: var(--text-muted); margin-top: 2px;">` + statsText + `</div>
              </div>
              <div style="display: flex; gap: 4px; align-items: center;">
                <button class="download-action-btn ` + (isPaused ? 'primary' : '') + `" onclick="event.stopPropagation(); window.toggleDownloadPause('` + item.id + `', ` + isPaused + `);" title="` + (isPaused ? 'Resume' : 'Pause') + `">
                  ` + (isPaused ? '▶️' : '⏸️') + `
                </button>
                <button class="download-action-btn danger" onclick="event.stopPropagation(); window.cancelDownloadItem('` + item.id + `');" title="Cancel">
                  ✕
                </button>
              </div>
            </div>
            <div class="download-progress-track">
              <div class="download-progress-fill ` + (isIndeterminate ? 'indeterminate' : '') + `" style="width: ` + percent + `%;"></div>
            </div>`;
        } else {
          // Expanded view: Full filename visible without truncation, emoji + text explanation, expanded buttons!
          card.innerHTML = `
            <div style="display: flex; align-items: flex-start; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `" style="margin-top: 2px;">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 13.5px; font-weight: 800; color: var(--text-main); word-break: break-all; line-height: 1.35;">` + (item.fileName || 'Download') + `</div>
                <div style="margin-top: 6px;">
                  <div class="download-type-pill ` + cat.class + `">
                    <span>` + cat.icon + `</span> <span>` + cat.title + `</span>
                  </div>
                </div>
                <div style="font-size: 11px; color: var(--text-muted); margin-top: 6px;">` + statsText + `</div>
              </div>
            </div>
            <div class="download-progress-track" style="margin-top: 6px;">
              <div class="download-progress-fill ` + (isIndeterminate ? 'indeterminate' : '') + `" style="width: ` + percent + `%;"></div>
            </div>
            <div class="download-expanded-actions">
              <button class="download-action-btn ` + (isPaused ? 'primary' : '') + `" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.toggleDownloadPause('` + item.id + `', ` + isPaused + `);">
                ` + (isPaused ? '▶️ Resume Download' : '⏸️ Pause Download') + `
              </button>
              <button class="download-action-btn danger" style="padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.cancelDownloadItem('` + item.id + `');">
                ✕ Cancel
              </button>
            </div>`;
        }
        activeListEl.appendChild(card);
      });
    }

    // Render Completed
    completedListEl.innerHTML = '';
    if (completedItems.length === 0) {
      completedListEl.innerHTML = `
        <div id="downloads-completed-empty" style="padding: 30px 10px; text-align: center; color: var(--text-muted); font-size: 12px;">
          <div style="font-size: 28px; margin-bottom: 8px;">📂</div>
          No downloaded files yet
        </div>`;
    } else {
      completedItems.forEach(item => {
        const cat = getFileCategoryInfo(item.fileName, item.mimeType);
        const isFailed = item.status === 'FAILED';
        const isCancelled = item.status === 'CANCELLED';
        const isApk = cat.class === 'apk';
        const isExpanded = expandedDownloadIds.has(item.id);

        let subText = formatDownloadBytes(item.totalBytes > 0 ? item.totalBytes : item.downloadedBytes);
        let statusBadge = '';
        if (isFailed) {
          statusBadge = '<span style="color: #ef4444; font-weight: 700; margin-left: 4px;">Failed</span>';
        } else if (isCancelled) {
          statusBadge = '<span style="color: #f59e0b; font-weight: 700; margin-left: 4px;">Cancelled</span>';
        }

        const card = document.createElement('div');
        card.className = 'download-item-card' + (isExpanded ? ' expanded' : '');
        card.onclick = function() { window.toggleExpandDownloadCard(item.id); };

        if (!isExpanded) {
          card.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 12.5px; font-weight: 700; color: var(--text-main); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">` + (item.fileName || 'File') + `</div>
                <div style="font-size: 10.5px; color: var(--text-muted); margin-top: 2px;">
                  ` + subText + ` ` + statusBadge + `
                </div>
              </div>
              <div style="display: flex; gap: 4px; align-items: center;">
                ` + (!isFailed && !isCancelled ? `
                  <button class="download-action-btn primary" onclick="event.stopPropagation(); window.openDownloadedItem('` + item.id + `');" title="` + (isApk ? 'Install' : 'Open') + `">
                    ` + (isApk ? '🚀 Install' : '📂 Open') + `
                  </button>
                  <button class="download-action-btn" onclick="event.stopPropagation(); window.shareDownloadedItem('` + item.id + `');" title="Share">
                    📤
                  </button>
                ` : `
                  <button class="download-action-btn primary" onclick="event.stopPropagation(); window.retryDownloadItem('` + item.id + `');" title="Retry">
                    🔄
                  </button>
                `) + `
                <button class="download-action-btn danger" onclick="event.stopPropagation(); window.deleteDownloadItem('` + item.id + `');" title="Delete">
                  🗑️
                </button>
              </div>
            </div>`;
        } else {
          // Expanded view: Full filename visible without truncation, emoji + text explanation, expanded buttons!
          card.innerHTML = `
            <div style="display: flex; align-items: flex-start; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `" style="margin-top: 2px;">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 13.5px; font-weight: 800; color: var(--text-main); word-break: break-all; line-height: 1.35;">` + (item.fileName || 'File') + `</div>
                <div style="margin-top: 6px;">
                  <div class="download-type-pill ` + cat.class + `">
                    <span>` + cat.icon + `</span> <span>` + cat.title + `</span>
                  </div>
                </div>
                <div style="font-size: 11px; color: var(--text-muted); margin-top: 6px;">
                  Size: ` + subText + ` ` + statusBadge + ` • Saved in Downloads
                </div>
              </div>
            </div>
            <div class="download-expanded-actions">
              ` + (!isFailed && !isCancelled ? `
                <button class="download-action-btn primary" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.openDownloadedItem('` + item.id + `');">
                  ` + (isApk ? '🚀 Install APK' : '📂 Open File') + `
                </button>
                <button class="download-action-btn" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.shareDownloadedItem('` + item.id + `');">
                  📤 Share File
                </button>
              ` : `
                <button class="download-action-btn primary" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.retryDownloadItem('` + item.id + `');">
                  🔄 Retry Download
                </button>
              `) + `
              <button class="download-action-btn danger" style="padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.deleteDownloadItem('` + item.id + `');">
                🗑️ Delete
              </button>
            </div>`;
        }
        completedListEl.appendChild(card);
      });
    }
  }

  window.toggleDownloadPause = function(id, isPaused) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (window.CaspianBridge) {
      if (isPaused) {
        window.CaspianBridge.resumeDownload(id);
      } else {
        window.CaspianBridge.pauseDownload(id);
      }
      setTimeout(refreshDownloadsList, 150);
    }
  };

  window.cancelDownloadItem = function(id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (window.CaspianBridge) {
      if (typeof window.CaspianBridge.deleteDownload === 'function') {
        window.CaspianBridge.deleteDownload(id, true);
      } else {
        window.CaspianBridge.cancelDownload(id);
      }
      setTimeout(refreshDownloadsList, 150);
    }
  };

  window.openDownloadedItem = function(id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (window.CaspianBridge) {
      window.CaspianBridge.openDownloadedFile(id);
    }
  };

  window.shareDownloadedItem = function(id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (window.CaspianBridge) {
      window.CaspianBridge.shareDownloadedFile(id);
    }
  };

  window.retryDownloadItem = function(id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (window.CaspianBridge) {
      window.CaspianBridge.resumeDownload(id);
      setTimeout(refreshDownloadsList, 150);
    }
  };

  window.deleteDownloadItem = function(id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    if (window.CaspianBridge) {
      window.CaspianBridge.deleteDownload(id, true);
      setTimeout(refreshDownloadsList, 150);
    }
  };

  // Android callbacks
  window.onDownloadStarted = function(item) {
    console.log('Download started:', item);
    refreshDownloadsList();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('📥 Downloading ' + (item.fileName || 'file') + '...');
    }
  };

  window.onDownloadProgress = function(item) {
    let found = false;
    for (let i = 0; i < cachedDownloads.length; i++) {
      if (cachedDownloads[i].id === item.id) {
        cachedDownloads[i] = item;
        found = true;
        break;
      }
    }
    if (!found) cachedDownloads.unshift(item);

    const modal = document.getElementById('caspian-downloads-modal');
    if (modal && modal.style.display === 'flex') {
      renderDownloadsUI();
    } else {
      const activeItems = cachedDownloads.filter(d => d.status === 'DOWNLOADING' || d.status === 'PENDING' || d.status === 'PAUSED');
      updateDownloadsBadge(activeItems.length);
    }
  };

  window.onDownloadCompleted = function(item) {
    console.log('Download completed:', item);
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) {}
    refreshDownloadsList();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('✅ Download complete: ' + item.fileName);
    }
  };

  window.onDownloadFailed = function(item, error) {
    console.error('Download failed:', item, error);
    refreshDownloadsList();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('❌ Download failed: ' + item.fileName);
    }
  };

  window.onDownloadCancelled = function(item) {
    refreshDownloadsList();
  };

  // Setup UI Listeners when DOM is ready
  function initDownloadManagerUI() {
    const modal = document.getElementById('caspian-downloads-modal');
    if (modal) {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          window.closeDownloadsModal();
        }
      });
    }

    const closeDownloadsBtn = document.getElementById('downloads-modal-close-btn');
    if (closeDownloadsBtn) {
      closeDownloadsBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        window.closeDownloadsModal();
      });
    }

    const tabActiveBtn = document.getElementById('downloads-tab-active-btn');
    if (tabActiveBtn) {
      tabActiveBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        switchDownloadsTab('active');
      });
    }

    const tabCompletedBtn = document.getElementById('downloads-tab-completed-btn');
    if (tabCompletedBtn) {
      tabCompletedBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        switchDownloadsTab('completed');
      });
    }

    const openFolderBtn = document.getElementById('downloads-open-folder-btn');
    if (openFolderBtn) {
      openFolderBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (err) {}
        if (window.CaspianBridge && typeof window.CaspianBridge.openDownloadsFolder === 'function') {
          window.CaspianBridge.openDownloadsFolder();
        }
      });
    }

    const clearHistoryBtn = document.getElementById('downloads-clear-btn');
    if (clearHistoryBtn) {
      clearHistoryBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (err) {}
        if (window.CaspianBridge && typeof window.CaspianBridge.clearCompletedDownloads === 'function') {
          window.CaspianBridge.clearCompletedDownloads();
          refreshDownloadsList();
        }
      });
    }

    refreshDownloadsList();
    setTimeout(refreshDownloadsList, 250);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDownloadManagerUI);
  } else {
    initDownloadManagerUI();
  }
})();
