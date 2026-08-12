// ======================================================
// CASPIAN ANDROID - MOBILE CONTROL SHEET JS
// ======================================================

(function() {
  let sfxVolume = 0.5;
  let masterSFXMuted = false;

  function setMasterMute(muted) {
    masterSFXMuted = !!muted;
    try {
      localStorage.setItem('master_sfx_muted', masterSFXMuted ? 'true' : 'false');
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('master_sfx_muted', masterSFXMuted ? 'true' : 'false');
      }
    } catch(e) {}

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
  } catch(e) {}

  const sfxAssets = {
    tm: null,
    tb: null,
    ta: null
  };

  setTimeout(() => {
    try {
      sfxAssets.tm = new Audio('sfx/tap_main.wav');
      sfxAssets.tb = new Audio('sfx/tap_button.wav');
      sfxAssets.ta = new Audio('sfx/tap_alternate.wav');
      sfxAssets.tm.load();
      sfxAssets.tb.load();
      sfxAssets.ta.load();
    } catch (e) {}
  }, 1000);

  function playSFX(type) {
    try {
      if (masterSFXMuted) return;
      let enabled = true;
      if (type === 'tm_tabs') enabled = localStorage.getItem('sfx_enabled_tm_tabs') !== 'false';
      else if (type === 'tb_clicks') enabled = localStorage.getItem('sfx_enabled_tb_clicks') !== 'false';
      else if (type === 'tm_header') enabled = localStorage.getItem('sfx_enabled_tm_header') !== 'false';
      else if (type === 'tb_close') enabled = localStorage.getItem('sfx_enabled_tb_close') !== 'false';
      else if (type === 'tb_modal') enabled = localStorage.getItem('sfx_enabled_tb_modal') !== 'false';

      if (!enabled) return;

      let audio = null;
      if (type === 'tm_tabs' || type === 'tm_header') audio = sfxAssets.tm;
      else if (type === 'tb_clicks' || type === 'tb_close' || type === 'tb_modal') audio = sfxAssets.tb;

      if (!audio) return;
      audio.currentTime = 0;
      // Exponential curve for soft/subtle volume scaling (e.g. 5% = 0.05^2.5 = 0.0005)
      const effectiveVol = Math.pow(sfxVolume, 2.5);
      audio.volume = effectiveVol;
      audio.play().catch(() => {});
    } catch (e) {}
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

  const appCardHub = document.getElementById('app-card-hub');
  const appCardGpt = document.getElementById('app-card-chatgpt');
  const appCardGemini = document.getElementById('app-card-gemini');
  const appCardGoogle = document.getElementById('app-card-google');
  const appCardYoutube = document.getElementById('app-card-youtube');
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
      } else if (tab.service === 'google') {
        iconB64 = window.GOOGLE_ICON_B64 || '';
      } else if (tab.service === 'youtube') {
        iconB64 = window.YOUTUBE_ICON_B64 || '';
      }

      const activeClass = tab.active ? 'active' : '';
      const activeBadge = tab.active ? '<span style="font-size: 9px; font-weight: 800; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 6px; border-radius: 6px;">ACTIVE</span>' : '';

      const shouldShowAudio = (tab.isPlayingAudio === true || tab.isMuted === true);
      const muteIcon = tab.isMuted ? '🔇' : '🔊';
      const muteText = tab.isMuted ? 'Muted' : 'Playing';
      const audioBadge = shouldShowAudio ? `
        <button class="chrome-tab-mute-btn ${tab.isMuted ? 'muted' : 'playing'}" data-muteid="${tab.id}" title="Toggle Tab Audio Mute" style="display: flex; align-items: center; gap: 4px; font-size: 9px; font-weight: 700; color: ${tab.isMuted ? '#f43f5e' : '#3b82f6'}; background: ${tab.isMuted ? 'rgba(244,63,94,0.15)' : 'rgba(59,130,246,0.15)'}; border: 1px solid ${tab.isMuted ? 'rgba(244,63,94,0.3)' : 'rgba(59,130,246,0.3)'}; border-radius: 6px; padding: 2px 6px; cursor: pointer;">
          <span>${muteIcon}</span>
          <span>${muteText}</span>
        </button>
      ` : '';

      const favStarBadge = tab.isFavorite ? '<span style="color: #eab308; font-size: 11px; margin-right: 2px;" title="Favorited Tab (Protected from Close All)">⭐</span>' : '';

      html += `
        <div class="chrome-tab-card ${activeClass}" data-tabid="${tab.id}">
          <div class="chrome-tab-header">
            <div style="display: flex; align-items: center; gap: 6px; overflow: hidden;">
              ${favStarBadge}
              ${iconB64 ? `<img src="${iconB64}" style="width: 16px; height: 16px; border-radius: 4px;" />` : ''}
              <span class="chrome-tab-title">${tab.title || 'Browser Tab'}</span>
            </div>
            <button class="chrome-tab-close" data-closeid="${tab.id}" title="Close Tab">&times;</button>
          </div>
          <div class="chrome-tab-url" style="font-size: 10px; color: var(--text-sub); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-top: 4px; display: flex; align-items: center; gap: 4px;">
            ${tab.nickname ? `🏷️ <strong style="color: #10b981;">${tab.nickname}</strong>` : tab.url || ''}
          </div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 6px;">
            <div>${audioBadge}</div>
            <div>${activeBadge}</div>
          </div>
        </div>
      `;
    });
    html += '</div>';

    container.innerHTML = html;

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

    // Bind Tab Card touch drag reordering, swipe triggers, and click navigation
    container.querySelectorAll('.chrome-tab-card').forEach(card => {
      let touchStartX = 0;
      let touchStartY = 0;
      let lastMoveX = 0;
      let lastMoveY = 0;
      let diffX = 0;
      let diffY = 0;
      let isSwipe = false;
      let isDrag = false;
      let pressTimer = null;
      let dIdx = -1;
      let cardHeight = 0;
      let ignoreClickDueToSwipe = false;

      const onTouchStart = (e) => {
        const touch = e.touches[0];
        touchStartX = touch.clientX;
        touchStartY = touch.clientY;
        lastMoveX = touch.clientX;
        lastMoveY = touch.clientY;
        diffX = 0;
        diffY = 0;
        isSwipe = false;
        isDrag = false;

        const cardsList = Array.from(container.querySelectorAll('.chrome-tab-card'));
        dIdx = cardsList.indexOf(card);
        cardHeight = card.offsetHeight + 12; // Card height + grid gap

        // 350ms touch and hold to trigger dragging
        pressTimer = setTimeout(() => {
          if (!isSwipe) {
            isDrag = true;
            card.classList.add('dragging');
            card.style.zIndex = '1000';
            card.style.transition = 'none';
            // Snap to current move positions to prevent jumping
            touchStartX = lastMoveX;
            touchStartY = lastMoveY;
            diffX = 0;
            diffY = 0;
            if (navigator.vibrate) navigator.vibrate(40);
          }
        }, 350);
      };

      const onTouchMove = (e) => {
        const touch = e.touches[0];
        lastMoveX = touch.clientX;
        lastMoveY = touch.clientY;
        diffX = touch.clientX - touchStartX;
        diffY = touch.clientY - touchStartY;

        if (isDrag) {
          e.preventDefault();
          // Allow free 2D dragging in all directions
          card.style.transform = `translate(${diffX}px, ${diffY}px)`;

          // Highlight target card under the dragging card's center
          const dragRect = card.getBoundingClientRect();
          const dragCenterX = dragRect.left + dragRect.width / 2;
          const dragCenterY = dragRect.top + dragRect.height / 2;

          container.querySelectorAll('.chrome-tab-card').forEach(other => {
            if (other === card) return;
            const rect = other.getBoundingClientRect();
            if (dragCenterX > rect.left && dragCenterX < rect.right &&
                dragCenterY > rect.top && dragCenterY < rect.bottom) {
              other.classList.add('drop-target');
            } else {
              other.classList.remove('drop-target');
            }
          });
          return;
        }

        if (!isSwipe && Math.abs(diffX) > 25 && Math.abs(diffX) > Math.abs(diffY) * 1.5) {
          isSwipe = true;
          clearTimeout(pressTimer);
        }

        if (isSwipe) {
          e.preventDefault();
          card.style.transform = `translateX(${diffX}px)`;
        }
      };

      const onTouchEnd = () => {
        clearTimeout(pressTimer);

        if (isDrag) {
          card.classList.remove('dragging');
          card.style.zIndex = '';
          card.style.transform = '';
          isDrag = false;

          // Find target drop index
          const activeDropTarget = container.querySelector('.chrome-tab-card.drop-target');
          let targetIdx = dIdx;
          if (activeDropTarget) {
            const cardsList = Array.from(container.querySelectorAll('.chrome-tab-card'));
            targetIdx = cardsList.indexOf(activeDropTarget);
            activeDropTarget.classList.remove('drop-target');
          }

          // Clear any leftovers
          container.querySelectorAll('.chrome-tab-card').forEach(other => {
            other.classList.remove('drop-target');
          });

          if (targetIdx !== dIdx && targetIdx >= 0 && targetIdx < tabs.length) {
            // Reorder in JS array
            const [moved] = tabs.splice(dIdx, 1);
            tabs.splice(targetIdx, 0, moved);

            const newIds = tabs.map(t => t.id);
            if (window.CaspianBridge && typeof window.CaspianBridge.reorderTabs === 'function') {
              window.CaspianBridge.reorderTabs(JSON.stringify(newIds));
            }
            setTimeout(renderOpenTabs, 50);
          } else {
            card.style.transform = '';
          }
          return;
        }

        if (isSwipe) {
          card.style.transform = '';
          if (diffX > 80) {
            const tabId = parseInt(card.dataset.tabid);
            const tab = tabs.find(t => t.id === tabId);
            if (tab) openTabOptionsMenu(tab);
          } else if (diffX < -80) {
            const tabId = parseInt(card.dataset.tabid);
            card.style.transition = 'transform 0.2s ease, opacity 0.2s ease';
            card.style.transform = 'translateX(-120%)';
            card.style.opacity = '0';
            setTimeout(() => {
              triggerCloseTab(tabId);
            }, 200);
          }
          ignoreClickDueToSwipe = true;
          setTimeout(() => { ignoreClickDueToSwipe = false; }, 200);
          isSwipe = false;
        }
      };

      card.addEventListener('touchstart', onTouchStart, { passive: true });
      card.addEventListener('touchmove', onTouchMove, { passive: false });
      card.addEventListener('touchend', onTouchEnd);
      card.addEventListener('touchcancel', onTouchEnd);

      // Support Click Event for switching tab
      card.addEventListener('click', (e) => {
        if (e.target.classList.contains('chrome-tab-close')) return;
        if (isSwipe || isDrag || ignoreClickDueToSwipe) return;
        const tabId = parseInt(card.dataset.tabid);

        container.querySelectorAll('.chrome-tab-card').forEach(c => {
          c.classList.remove('active');
          const badge = c.querySelector('span[style*="color: #10b981"]');
          if (badge) badge.remove();
        });
        card.classList.add('active');
        playSFX('tb_clicks');

        const badgeContainer = card.querySelector('div[style*="justify-content: flex-end"]');
        if (badgeContainer) {
          badgeContainer.innerHTML = '<span style="font-size: 9px; font-weight: 800; color: #10b981; background: rgba(16,185,129,0.15); padding: 2px 6px; border-radius: 6px;">ACTIVE</span>';
        }

        if (window.CaspianBridge && typeof window.CaspianBridge.switchTab === 'function') {
          window.CaspianBridge.switchTab(tabId);
          setTimeout(renderOpenTabs, 400);
        }
      });
    });

    // Close buttons binding
    container.querySelectorAll('.chrome-tab-close').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const tabId = parseInt(btn.dataset.closeid);
        const card = container.querySelector(`.chrome-tab-card[data-tabid="${tabId}"]`);
        if (card) {
          card.style.opacity = '0';
          card.style.transform = 'scale(0.9)';
          card.style.transition = 'all 0.25s cubic-bezier(0.16, 1, 0.3, 1)';
          setTimeout(() => {
            card.remove();
            const remaining = container.querySelectorAll('.chrome-tab-card').length;
            if (countBadge) {
              countBadge.textContent = remaining === 1 ? '1 Tab' : `${remaining} Tabs`;
            }
          }, 200);
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
    } catch(e) {}

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
              activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Message' : 'Messages'}`;
            }
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

          // Check active tab to toggle YouTube Control Card
          try {
            if (window.CaspianBridge && typeof window.CaspianBridge.getOpenTabs === 'function') {
              const openTabsStr = window.CaspianBridge.getOpenTabs();
              if (openTabsStr) {
                const tabs = JSON.parse(openTabsStr);
                const activeTab = tabs.find(t => t.active);
                const ytCard = document.getElementById('youtube-control-card');
                if (ytCard) {
                  const isYT = activeTab && ((activeTab.service && activeTab.service.toLowerCase().includes('youtube')) || (activeTab.url && activeTab.url.toLowerCase().includes('youtube.com')));
                  ytCard.style.display = isYT ? 'block' : 'none';
                }
              }
            }
          } catch(e) {}
          if (prefs.theme_start_color && prefs.theme_end_color) {
            applyCustomGradient(prefs.theme_start_color, prefs.theme_end_color);
          }
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
          updateIconPreview(prefs.theme_start_color || '#A2A9A9', prefs.theme_end_color || '#1B4264', selectedShapeVal);

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
    } catch(e){}
  }

  // Custom Dropdown Handling
  var customShapeSelect = document.getElementById('custom-shape-select');
  var selectedShapeText = document.getElementById('selected-shape-text');
  
  if (customShapeSelect && selectedShapeText) {
    customShapeSelect.querySelector('.caspian-select-trigger').addEventListener('click', (e) => {
      e.stopPropagation();
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

    document.addEventListener('click', () => {
      customShapeSelect.classList.remove('open');
    });
  }

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
    } catch(e) {}
  }

  // Theme Toggles (Default Light)
  function setTheme(t) {
    activeTheme = t || 'dark';
    document.documentElement.setAttribute('data-theme', activeTheme);
    document.documentElement.classList.toggle('dark', activeTheme === 'dark');
    document.documentElement.classList.toggle('light', activeTheme === 'light');
    if (activeTheme === 'light') {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff');
    } else {
      document.documentElement.style.setProperty('--sheet-bg', selectedDarkBg);
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
      try { e.preventDefault(); } catch(err) {}
      const touch = e.touches[0];
      startY = touch.clientY;
      lastClientY = touch.clientY;
      startHeight = bottomSheet.offsetHeight;
      startTime = Date.now();
      bottomSheet.style.transition = 'none';
    }, { passive: false });

    targetDragArea.addEventListener('touchmove', (e) => {
      try { e.preventDefault(); } catch(err) {}
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
          bottomSheet.style.height = '65vh';
        }, 350);
      } else {
        // Snap to closest stable layout position (e.g. 45vh, 65vh, 85vh)
        let snapVh = 65;
        if (currentHeightVh < 55) {
          snapVh = 45;
        } else if (currentHeightVh > 75) {
          snapVh = 85;
        }
        bottomSheet.style.height = snapVh + 'vh';
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

  // App Icon Cards: Open new tab for selected platform (Hub, ChatGPT, Gemini)
  if (appCardHub) {
    appCardHub.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('hub');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (appCardGpt) {
    appCardGpt.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('chatgpt');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (appCardGemini) {
    appCardGemini.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('gemini');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (appCardGoogle) {
    appCardGoogle.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('google');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (appCardYoutube) {
    appCardYoutube.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('youtube');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (newTabBtn) {
    newTabBtn.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.createNewTab === 'function') {
        window.CaspianBridge.createNewTab('chatgpt');
        setTimeout(renderOpenTabs, 100);
      }
    });
  }

  if (closeAllTabsBtn) {
    closeAllTabsBtn.addEventListener('click', () => {
      playSFX('tb_close');
      if (window.CaspianBridge && typeof window.CaspianBridge.closeAllTabs === 'function') {
        window.CaspianBridge.closeAllTabs();
        setTimeout(renderOpenTabs, 150);
      }
    });
  }

  // Mobile Tab Navigation
  document.querySelectorAll('.tab-nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      playSFX('tm_tabs');
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
      playSFX('tm_header');
      globalActive = !globalActive;
      const statusDot = document.getElementById('status-dot');
      const statusTitle = document.getElementById('status-title');
      const statusSub = document.getElementById('status-sub');
 
      if (statusDot) statusDot.classList.toggle('active', globalActive);
      if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Message Limit: ON' : 'Chat Message Limit: OFF';
      if (statusSub) statusSub.textContent = globalActive ? 'it limites the amout of message shown from below so all message above it will get prune or cut out this is done to improve performance and reduce lagging.' : 'Message Limit paused via Master Power Switch';

      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
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

  // Limit Pills Selection
  document.querySelectorAll('.limit-pill[data-val]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.limit-pill[data-val]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      limitVal = parseInt(pill.dataset.val);

      const activeBadge = document.getElementById('active-limit-badge');
      if (activeBadge) {
        activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Message' : 'Messages'}`;
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
      try { localStorage.setItem('pdfExportMode', mode); } catch(e){}
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
        } catch(err){}
      });
    });
  }

  initCustomSelect('select-export-chatgpt-normal', 'export_chatgpt_normal');
  initCustomSelect('select-export-chatgpt-temp', 'export_chatgpt_temp');
  initCustomSelect('select-export-gemini-normal', 'export_gemini_normal');
  initCustomSelect('select-export-gemini-temp', 'export_gemini_temp');
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
    });
  }

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
          if (item.key === 'sfx_enabled_ta') {
            if (window.CaspianBridge && typeof window.CaspianBridge.playAssetSound === 'function') {
              window.CaspianBridge.playAssetSound("sfx/tap_alternate.wav");
            }
          } else {
            playSFX(item.key.replace('sfx_enabled_', ''));
          }
        }
      });
    }
  });

  // Initialize saved settings on load
  restoreSavedSettings();

function restoreSavedSettings() {
  const pdfModeSelect = document.getElementById('pdf-export-mode-select');
  if (pdfModeSelect) {
    try {
      const savedMode = localStorage.getItem('pdfExportMode') || 'html';
      pdfModeSelect.value = savedMode;
      if (window.CaspianBridge && typeof window.CaspianBridge.setPdfExportMode === 'function') {
        window.CaspianBridge.setPdfExportMode(savedMode);
      }
    } catch(e){}
  }

  // Restore Custom Dropdown Export Settings from preferences
  if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
    try {
      const prefs = JSON.parse(window.CaspianBridge.getSettings());
      if (prefs) {
        const exportKeys = {
          'export_chatgpt_normal': 'select-export-chatgpt-normal',
          'export_chatgpt_temp': 'select-export-chatgpt-temp',
          'export_gemini_normal': 'select-export-gemini-normal',
          'export_gemini_temp': 'select-export-gemini-temp',
          'active_refresh_rate': 'select-refresh-rate',
          'sheetAnimationStyle': 'select-anim-style'
        };
        
        for (const [prefKey, elementId] of Object.entries(exportKeys)) {
          const val = prefs[prefKey] || localStorage.getItem(prefKey);
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
    } catch(e){}
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

  const clearNicknameBtn = document.getElementById('modal-clear-nickname-btn');
  if (clearNicknameBtn) {
    clearNicknameBtn.onclick = () => {
      playSFX('tb_modal');
      nicknameInput.blur();
      urlDisplay.blur();
      nicknameInput.value = '';
      const url = urlDisplay.value.trim();
      if (window.CaspianBridge && typeof window.CaspianBridge.updateTabDetails === 'function') {
        window.CaspianBridge.updateTabDetails(tab.id, '', url);
      }
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
      if (window.CaspianBridge && typeof window.CaspianBridge.updateTabDetails === 'function') {
        window.CaspianBridge.updateTabDetails(tab.id, nick, url);
      }
      modal.style.display = 'none';
      setTimeout(() => {
        if (typeof window.renderOpenTabs === 'function') {
          window.renderOpenTabs();
        }
      }, 100);
    };
  }
}

function triggerCloseTab(tabId) {
  playSFX('tb_close');
  showUndoToast();
  if (window.CaspianBridge && typeof window.CaspianBridge.closeTab === 'function') {
    window.CaspianBridge.closeTab(tabId);
    setTimeout(() => {
      if (typeof window.renderOpenTabs === 'function') {
        window.renderOpenTabs();
      }
    }, 300);
  }
}

let undoTimeout = null;

function showUndoToast() {
  const toast = document.getElementById('undo-toast-container');
  if (!toast) return;

  clearTimeout(undoTimeout);
  toast.style.display = 'flex';
  toast.style.transform = '';

  // Swipe to dismiss undo toast touch listeners
  let startX = 0;
  toast.ontouchstart = (e) => {
    startX = e.touches[0].clientX;
  };
  toast.ontouchmove = (e) => {
    let diff = e.touches[0].clientX - startX;
    if (Math.abs(diff) > 10) {
      toast.style.transform = `translateX(${diff}px)`;
    }
  };
  toast.ontouchend = (e) => {
    let diff = e.changedTouches[0].clientX - startX;
    if (Math.abs(diff) > 100) {
      toast.style.display = 'none';
    } else {
      toast.style.transform = '';
    }
  };

  const undoBtn = document.getElementById('undo-toast-btn');
  undoBtn.onclick = () => {
    playSFX('tb_clicks');
    if (window.CaspianBridge && typeof window.CaspianBridge.restoreLastClosedTab === 'function') {
      window.CaspianBridge.restoreLastClosedTab();
      setTimeout(() => {
        if (typeof window.renderOpenTabs === 'function') {
          window.renderOpenTabs();
        }
      }, 300);
    }
    toast.style.display = 'none';
  };

  const closeBtn = document.getElementById('close-undo-btn');
  closeBtn.onclick = () => {
    playSFX('tb_clicks');
    toast.style.display = 'none';
  };

  undoTimeout = setTimeout(() => {
    toast.style.display = 'none';
  }, 6000); // Allow 6 seconds to undo
}

// YouTube Controls & AdBlocker Event Listeners
document.addEventListener('DOMContentLoaded', () => {
  const ytSeekBackBtn = document.getElementById('yt-seek-back-btn');
  if (ytSeekBackBtn) {
    ytSeekBackBtn.addEventListener('click', () => {
      playSFX('tb_clicks');
      if (window.CaspianBridge && typeof window.CaspianBridge.seekYouTube === 'function') {
        window.CaspianBridge.seekYouTube(-5);
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

  const toggleTSBtn = document.getElementById('toggle-temp-saver-btn');
  const toggleCLBtn = document.getElementById('toggle-chat-limit-btn');
  const toggleCCBtn = document.getElementById('toggle-caspian-current-btn');
  const toggleAdblockBtn = document.getElementById('toggle-adblock-btn');

  const chatLimitHeader = document.getElementById('chat-limit-header');
  const chatLimitBody = document.getElementById('chat-limit-body');
  const ccHeader = document.getElementById('caspian-current-header');
  const ccBody = document.getElementById('caspian-current-body');
  const adblockHeader = document.getElementById('adblock-header');
  const adblockBody = document.getElementById('adblock-body');

  // Helper to update card states
  function updateEngineCardUI(card, toggleBtn, body, dotEl, key) {
    const isEnabled = localStorage.getItem(key) !== 'false';
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

  // Initial Sync
  updateEngineCardUI(cardTS, toggleTSBtn, null, document.getElementById('ts-status-dot'), 'temp_saver_enabled');
  updateEngineCardUI(cardCL, toggleCLBtn, chatLimitBody, document.getElementById('status-dot'), 'chat_limit_enabled');
  updateEngineCardUI(cardCC, toggleCCBtn, ccBody, document.getElementById('cc-status-dot'), 'caspian_current_enabled');
  updateEngineCardUI(cardAB, toggleAdblockBtn, adblockBody, document.getElementById('adblock-dot'), 'adblock_enabled');

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
    });
  }

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

  // 4. AdBlocker Accordion & Toggle
  if (adblockHeader && adblockBody) {
    adblockHeader.addEventListener('click', (e) => {
      if (e.target === toggleAdblockBtn || (toggleAdblockBtn && toggleAdblockBtn.contains(e.target))) return;
      const isOpen = adblockBody.style.display !== 'none';
      adblockBody.style.display = isOpen ? 'none' : 'block';
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
      updateEngineCardUI(cardAB, toggleAdblockBtn, adblockBody, document.getElementById('adblock-dot'), 'adblock_enabled');
    });
  }

  // 5. Global Top-Right Master Engine Power Toggle Button (#power-toggle-btn / #btn-power-off)
  const masterPowerBtn = document.getElementById('power-toggle-btn') || document.getElementById('btn-power-off');
  if (masterPowerBtn) {
    masterPowerBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      playSFX('tb_power');
      let anyOn = (localStorage.getItem('temp_saver_enabled') !== 'false' ||
                    localStorage.getItem('chat_limit_enabled') !== 'false' ||
                    localStorage.getItem('caspian_current_enabled') !== 'false' ||
                    localStorage.getItem('adblock_enabled') !== 'false');
      let targetState = !anyOn;

      ['temp_saver_enabled', 'chat_limit_enabled', 'caspian_current_enabled', 'adblock_enabled'].forEach(key => {
        localStorage.setItem(key, targetState ? 'true' : 'false');
        if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
          window.CaspianBridge.saveSetting(key, targetState ? 'true' : 'false');
        }
      });

      updateEngineCardUI(cardTS, toggleTSBtn, null, document.getElementById('ts-status-dot'), 'temp_saver_enabled');
      updateEngineCardUI(cardCL, toggleCLBtn, chatLimitBody, document.getElementById('status-dot'), 'chat_limit_enabled');
      updateEngineCardUI(cardCC, toggleCCBtn, ccBody, document.getElementById('cc-status-dot'), 'caspian_current_enabled');
      updateEngineCardUI(cardAB, toggleAdblockBtn, adblockBody, document.getElementById('adblock-dot'), 'adblock_enabled');

      if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
        window.CaspianBridge.showToast(targetState ? '⚡ All Caspian Engines Activated!' : '🔌 All Engines Disabled');
      }
    });
  }
});
})();
