// =========================================================================
// CASPIAN ANDROID - YOUTUBE UBLOCK DEFUSER & BACKGROUND HELPER ENGINE
// =========================================================================
(function () {
  if (window.__CASPIAN_YT_DEFUSER_INITIALIZED__) return;
  window.__CASPIAN_YT_DEFUSER_INITIALIZED__ = true;

  // -------------------------------------------------------------
  // 1. Page Visibility API Override (Prevents YouTube background pause)
  // -------------------------------------------------------------
  try {
    Object.defineProperty(document, 'hidden', { get: () => false, configurable: true });
    Object.defineProperty(document, 'visibilityState', { get: () => 'visible', configurable: true });
  } catch (e) { }

  window.addEventListener('visibilitychange', function (e) {
    e.stopImmediatePropagation();
  }, true);

  const originalPause = HTMLVideoElement.prototype.pause;
  HTMLVideoElement.prototype.pause = function () {
    if (document.hidden || document.visibilityState === 'hidden') {
      return;
    }
    return originalPause.apply(this, arguments);
  };

  // -------------------------------------------------------------
  // 1.5 YouTube Player Controls API (Seek, Speed, Quality, Play/Pause, Fullscreen, Mute)
  // -------------------------------------------------------------
  window.__CaspianYouTube = {
    _lastPlaying: null,
    _lastMuted: null,
    getVideo: function () {
      // 1. Prioritize the main player video element
      const mainVid = document.querySelector('#movie_player video.html5-main-video, .html5-video-player video.html5-main-video, .html5-main-video, #player video, ytm-custom-control video');
      if (mainVid) return mainVid;
      // 2. Filter out thumbnail preview / feed preview videos
      const allVideos = Array.from(document.querySelectorAll('video')).filter(v => {
        return !v.closest('ytm-thumbnail-overlay-preview-video-renderer, ytd-thumbnail-overlay-preview-video-renderer, .ytp-inline-preview-ui');
      });
      if (allVideos.length > 0) {
        return allVideos.find(v => !v.paused && v.currentTime > 0) || allVideos[0];
      }
      return document.querySelector('video');
    },
    notifyState: function () {
      try {
        const v = this.getVideo();
        if (v && window.CaspianBridge && typeof window.CaspianBridge.updateYouTubeState === 'function') {
          const isPlaying = !v.paused && !v.ended;
          const isMuted = !!v.muted;
          if (this._lastPlaying !== isPlaying || this._lastMuted !== isMuted) {
            this._lastPlaying = isPlaying;
            this._lastMuted = isMuted;
            window.CaspianBridge.updateYouTubeState(isPlaying, isMuted);
          }
        }
      } catch (e) { }
    },
    togglePlay: function () {
      const v = this.getVideo();
      if (v) {
        if (v.paused) {
          v.play();
        } else {
          v.pause();
        }
        this.notifyState();
      }
    },
    toggleMute: function () {
      const v = this.getVideo();
      if (v) {
        v.muted = !v.muted;
        this.notifyState();
      }
    },
    seekBy: function (seconds) {
      const v = this.getVideo();
      if (v) {
        v.currentTime = Math.max(0, Math.min(v.duration || Infinity, v.currentTime + seconds));
      }
    },
    setSpeed: function (rate) {
      const v = this.getVideo();
      if (v) {
        v.playbackRate = parseFloat(rate);
      }
    },
    setQuality: function (qualityStr) {
      try {
        const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player');
        if (player) {
          if (typeof player.setPlaybackQualityRange === 'function') {
            player.setPlaybackQualityRange(qualityStr, qualityStr);
          }
          if (typeof player.setPlaybackQuality === 'function') {
            player.setPlaybackQuality(qualityStr);
          }
        }
        const qualityMap = {
          'hd1080': '1080p',
          'hd720': '720p',
          'large': '480p',
          'medium': '360p',
          'small': '240p',
          'tiny': '144p',
          'auto': 'Auto'
        };
        const targetLabel = qualityMap[qualityStr] || qualityStr;
        const items = document.querySelectorAll('.ytp-menuitem, .ytm-menu-item, ytm-menu-item');
        items.forEach(el => {
          if (el.textContent && el.textContent.toLowerCase().includes(targetLabel.toLowerCase())) {
            el.click();
          }
        });
      } catch (e) { }
    },
    previousVideo: function () {
      try {
        const prevBtn = document.querySelector('.ytp-prev-button, button.ytp-prev-button');
        if (prevBtn && typeof prevBtn.click === 'function') {
          prevBtn.click();
        } else if (window.history.length > 1) {
          window.history.back();
        }
      } catch (e) { }
    },
    nextVideo: function () {
      try {
        const nextBtn = document.querySelector('.ytp-next-button, button.ytp-next-button, ytm-next-button');
        if (nextBtn && typeof nextBtn.click === 'function') {
          nextBtn.click();
        } else if (window.history.length > 1) {
          window.history.forward();
        }
      } catch (e) { }
    },
    seekTo: function (sec) {
      try {
        const v = this.getVideo();
        if (v && Number.isFinite(sec)) {
          v.currentTime = sec;
        }
      } catch (e) { }
    },
    setVolume: function (vol) {
      try {
        const v = this.getVideo();
        if (v && Number.isFinite(vol)) {
          v.volume = Math.max(0, Math.min(1, vol));
          if (v.volume > 0 && v.muted) v.muted = false;
        }
      } catch (e) { }
    },
    showPlayerControls: function () {
      try {
        const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player') || document.querySelector('.player-container') || document.querySelector('ytm-media-item');
        if (player) {
          player.classList.remove('ytp-autohide');
          player.setAttribute('aria-hidden', 'false');
          ['mousemove', 'pointermove', 'touchstart', 'touchend'].forEach(type => {
            try { player.dispatchEvent(new Event(type, { bubbles: true })); } catch(e){}
          });
        }
        const overlay = document.querySelector('.player-control-overlay, .ytm-custom-control, .ytp-chrome-bottom, ytm-player-control-overlay');
        if (overlay) {
          overlay.style.display = '';
          overlay.style.opacity = '1';
        }
      } catch (e) { }
    },
    toggleFullscreen: function () {
      try {
        if (document.fullscreenElement || document.webkitFullscreenElement) {
          if (document.exitFullscreen) {
            document.exitFullscreen().catch(() => {});
          } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
          }
          return;
        }

        const v = this.getVideo();
        if (v && v.paused) {
          v.play().catch(() => {});
        }

        // 1. First prioritize YouTube's official player button if visible
        const fsBtn = document.querySelector(
          '.ytp-fullscreen-button, button.ytp-fullscreen-button, .fullscreen-icon, ytm-fullscreen-button, button[aria-label*="Fullscreen"], button[aria-label*="fullscreen"], button[title*="Full screen"], [aria-label*="full screen"]'
        );
        if (fsBtn && (fsBtn.offsetWidth > 0 || fsBtn.offsetHeight > 0 || fsBtn.getClientRects().length > 0)) {
          fsBtn.click();
          return;
        }

        // 2. Direct native HTML5 video fullscreen (triggers Android WebChromeClient onShowCustomView)
        if (v) {
          if (typeof v.webkitEnterFullscreen === 'function') {
            try {
              v.webkitEnterFullscreen();
              return;
            } catch (e) {}
          }
          if (typeof v.requestFullscreen === 'function') {
            try {
              v.requestFullscreen().catch(() => {});
              return;
            } catch (e) {}
          }
        }

        // 3. Fallback to player container requestFullscreen
        const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player') || document.querySelector('ytm-media-item') || document.querySelector('.player-container');
        if (player) {
          if (player.requestFullscreen) {
            player.requestFullscreen().catch(() => {});
            return;
          } else if (player.webkitRequestFullscreen) {
            player.webkitRequestFullscreen();
            return;
          }
        }

        // 4. Final fallback: click button if present
        if (fsBtn) {
          fsBtn.click();
        }
      } catch (e) { }
    }
  };

  function attachVideoListeners() {
    try {
      const v = window.__CaspianYouTube ? window.__CaspianYouTube.getVideo() : document.querySelector('video');
      if (v && !v.__caspian_attached) {
        v.__caspian_attached = true;
        ['play', 'playing', 'pause', 'ended', 'volumechange', 'ratechange'].forEach(evt => {
          v.addEventListener(evt, () => {
            if (window.__CaspianYouTube) window.__CaspianYouTube.notifyState();
          });
        });
        v.addEventListener('timeupdate', () => {
          if (window.CaspianBridge && typeof window.CaspianBridge.updateYouTubeTime === 'function') {
            window.CaspianBridge.updateYouTubeTime(v.currentTime || 0, v.duration || 0);
          }
        });
      }
      if (window.__CaspianYouTube) window.__CaspianYouTube.notifyState();
    } catch (e) { }
  }

  try {
    const videoObserver = new MutationObserver(() => {
      attachVideoListeners();
    });
    if (document.body) {
      videoObserver.observe(document.body, { childList: true, subtree: true });
    } else {
      document.addEventListener('DOMContentLoaded', () => {
        if (document.body) videoObserver.observe(document.body, { childList: true, subtree: true });
      });
    }
  } catch (e) { }
  attachVideoListeners();
  setInterval(attachVideoListeners, 1000);

  // 1.6 Intercept in-page YouTube Fullscreen button to trigger native WebView full-screen
  try {
    document.addEventListener('click', function (e) {
      try {
        const target = e.target;
        if (target && target.closest) {
          const fs = target.closest(
            '.ytp-fullscreen-button, button.ytp-fullscreen-button, .fullscreen-icon, ytm-fullscreen-button, button[aria-label*="Fullscreen"], button[aria-label*="fullscreen"], button[title*="Full screen"]'
          );
          if (fs) {
            const v = window.__CaspianYouTube ? window.__CaspianYouTube.getVideo() : document.querySelector('video');
            if (v && typeof v.webkitEnterFullscreen === 'function') {
              e.preventDefault();
              e.stopPropagation();
              if (v.paused) v.play().catch(() => {});
              v.webkitEnterFullscreen();
              return;
            }
          }

          // 1.7 Intercept Settings (Gear) button to show Caspian's high-elevation settings popup in fullscreen
          const settingsBtn = target.closest(
            '.ytp-settings-button, button[aria-label*="Settings"], button[aria-label*="settings"], .yt-spec-button-shape-next[aria-label*="Settings"], button.icon-button[aria-label*="Settings"], button[title*="Settings"]'
          );
          if (settingsBtn) {
            const isFs = !!(document.fullscreenElement || document.webkitFullscreenElement);
            if (isFs && window.CaspianBridge && typeof window.CaspianBridge.showYouTubeSettingsMenu === 'function') {
              e.preventDefault();
              e.stopPropagation();
              window.CaspianBridge.showYouTubeSettingsMenu();
              return;
            }
          }
        }
      } catch (err) { }
    }, true);

    // 1.8 Support for GSI ROMs / AOSP WebViews: if tap on video occurs while controls are hidden, reveal controls instead of pausing
    document.addEventListener('click', function (e) {
      try {
        const isFs = !!(document.fullscreenElement || document.webkitFullscreenElement);
        if (isFs && e.target && (e.target.tagName === 'VIDEO' || e.target.closest('.html5-main-video'))) {
          const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player') || document.querySelector('.player-container');
          const isHidden = player && (player.classList.contains('ytp-autohide') || player.getAttribute('aria-hidden') === 'true');
          if (isHidden) {
            e.preventDefault();
            e.stopPropagation();
            if (window.__CaspianYouTube) window.__CaspianYouTube.showPlayerControls();
          }
        }
      } catch (err) { }
    }, true);
  } catch (e) { }

  // -------------------------------------------------------------
  // 2. Data Cleaning Utility (uBlock Origin JSON Prune)
  // -------------------------------------------------------------
  function cleanYouTubeData(data) {
    if (!data || typeof data !== 'object') return data;
    try {
      if (data.adPlacements) delete data.adPlacements;
      if (data.adSlots) delete data.adSlots;
      if (data.playerAds) delete data.playerAds;
      if (data.adBreakParams) delete data.adBreakParams;
      if (data.adPlacementConfig) delete data.adPlacementConfig;

      if (data.playerConfig) {
        if (data.playerConfig.adPlacementConfig) delete data.playerConfig.adPlacementConfig;
        if (data.playerConfig.adBreakHeartbeatParams) delete data.playerConfig.adBreakHeartbeatParams;
      }

      if (data.args) {
        if (data.args.raw_player_response) {
          try {
            let raw = typeof data.args.raw_player_response === 'string'
              ? JSON.parse(data.args.raw_player_response)
              : data.args.raw_player_response;
            cleanYouTubeData(raw);
            data.args.raw_player_response = typeof data.args.raw_player_response === 'string'
              ? JSON.stringify(raw)
              : raw;
          } catch (e) { }
        }
        if (data.args.ad_flags) delete data.args.ad_flags;
        if (data.args.ad_tag) delete data.args.ad_tag;
        if (data.args.ad_logging_flag) delete data.args.ad_logging_flag;
      }
    } catch (e) { }
    return data;
  }

  // -------------------------------------------------------------
  // 3. Property Trapping (ytInitialPlayerResponse & ytInitialData)
  // -------------------------------------------------------------
  let _ytInitialPlayerResponse = window.ytInitialPlayerResponse;
  try {
    Object.defineProperty(window, 'ytInitialPlayerResponse', {
      get() { return _ytInitialPlayerResponse; },
      set(val) { _ytInitialPlayerResponse = cleanYouTubeData(val); },
      configurable: true
    });
    if (_ytInitialPlayerResponse) {
      _ytInitialPlayerResponse = cleanYouTubeData(_ytInitialPlayerResponse);
    }
  } catch (e) { }

  let _ytInitialData = window.ytInitialData;
  try {
    Object.defineProperty(window, 'ytInitialData', {
      get() { return _ytInitialData; },
      set(val) { _ytInitialData = cleanYouTubeData(val); },
      configurable: true
    });
    if (_ytInitialData) {
      _ytInitialData = cleanYouTubeData(_ytInitialData);
    }
  } catch (e) { }

  // -------------------------------------------------------------
  // 4. Safe Stream Interception via Response.prototype.json
  // -------------------------------------------------------------
  if (window.Response && Response.prototype.json) {
    const origJson = Response.prototype.json;
    Response.prototype.json = async function () {
      const data = await origJson.apply(this, arguments);
      try {
        if (this.url && this.url.includes('/youtubei/v1/player')) {
          cleanYouTubeData(data);
        }
      } catch (e) { }
      return data;
    };
  }

  // Hook JSON.parse as secondary safety net
  const origJsonParse = JSON.parse;
  JSON.parse = function () {
    const parsed = origJsonParse.apply(this, arguments);
    if (parsed && typeof parsed === 'object' && (parsed.adPlacements || parsed.playerAds || parsed.adSlots)) {
      cleanYouTubeData(parsed);
    }
    return parsed;
  };

  // -------------------------------------------------------------
  // 5. Accurate 0ms Auto-Skip & Fast-Forward Fallback Engine
  // -------------------------------------------------------------
  function executeFastForwardSkip() {
    try {
      const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player');
      const isAdActive = !!(player && (player.classList.contains('ad-showing') || player.classList.contains('ad-interrupting')));
      const adText = document.querySelector('.ytp-ad-text, .ytp-ad-preview-text, .ytp-ad-duration-remaining');

      const video = document.querySelector('video');

      // ONLY fast-forward if an actual ad is confirmed active (never on real video)
      if ((isAdActive || adText) && video) {
        video.muted = true;
        video.playbackRate = 16.0;
        // Most video ads are short (< 120s). Fast-forward to end of ad stream
        if (isFinite(video.duration) && video.duration > 0 && video.duration < 120) {
          video.currentTime = video.duration;
        }
      }

      // Click any skip button instantly
      const skipSelectors = [
        '.ytp-ad-skip-button',
        '.ytp-ad-skip-button-modern',
        '.ytp-skip-ad-button',
        '.ytp-ad-skip-button-text',
        '.ytp-ad-overlay-close-button',
        'button.ytp-ad-skip-button-modern',
        'button.ytp-ad-skip-button'
      ];

      for (let i = 0; i < skipSelectors.length; i++) {
        const btn = document.querySelector(skipSelectors[i]);
        if (btn && typeof btn.click === 'function') {
          btn.click();
          break;
        }
      }
    } catch (e) { }
  }

  // 50ms High-frequency tick
  setInterval(executeFastForwardSkip, 50);

  // MutationObserver for instant trigger on DOM ad class changes
  try {
    const observer = new MutationObserver(() => {
      executeFastForwardSkip();
    });
    if (document.body) {
      observer.observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['class'] });
    } else {
      document.addEventListener('DOMContentLoaded', () => {
        if (document.body) {
          observer.observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['class'] });
        }
      });
    }
  } catch (e) { }

  // -------------------------------------------------------------
  // 6. Visual Element Ad Suppression & Player Control Fixes
  // -------------------------------------------------------------
  const style = document.createElement('style');
  style.id = 'caspian-yt-ublock-rules';
  style.textContent = `
    .ytp-ad-module, .ytp-ad-overlay-container, #player-ads,
    ytd-promoted-sparkles-web-renderer, ytd-promoted-video-renderer,
    ytd-compact-promoted-video-renderer, ytd-in-feed-ad-layout-renderer,
    ytd-banner-promo-renderer, ytd-ad-slot-renderer, ytm-promoted-sparkles-web-renderer,
    ytm-promoted-video-renderer, .ad-container, .ad-slot, .ad-banner {
      display: none !important;
      visibility: hidden !important;
      height: 0 !important;
      pointer-events: none !important;
    }
    /* Ensure settings gear button and menus are interactive in fullscreen landscape (Fix for Chromium #57449) */
    button[aria-label*="Settings" i],
    button[aria-label*="More options" i],
    button[aria-label*="Playback settings" i],
    button[aria-label*="Quality" i],
    button[aria-label*="Speed" i],
    button[title*="Settings" i],
    button[title*="More" i],
    .ytp-settings-button,
    .ytp-menu-button,
    .ytm-settings-button,
    button.icon-button[aria-label*="Settings" i],
    button.icon-button[aria-label*="More options" i] {
      z-index: 2147483647 !important;
      pointer-events: auto !important;
      visibility: visible !important;
      opacity: 1 !important;
    }
    .ytp-settings-menu,
    .ytp-popup,
    .ytp-panel,
    ytm-menu-renderer,
    ytm-bottom-sheet-renderer,
    ytm-settings-dialog,
    ytm-popup-container,
    .ytm-sheet,
    dialog.ytm-dialog,
    div.dialog-container,
    .bottom-sheet-container {
      position: fixed !important;
      bottom: 0 !important;
      left: 0 !important;
      width: 100% !important;
      max-height: 85vh !important;
      overflow-y: auto !important;
      z-index: 2147483647 !important;
      pointer-events: auto !important;
      visibility: visible !important;
    }
  `;
  (document.head || document.documentElement).appendChild(style);

  // -------------------------------------------------------------
  // 6.5 YouTube Fullscreen Settings Gear & Menu Bug Fix
  // -------------------------------------------------------------
  function reparentFsMenus() {
    const fsElem = document.fullscreenElement || document.webkitFullscreenElement;
    if (!fsElem) return;
    const menus = document.querySelectorAll(
      'body > ytm-menu-renderer, body > .ytm-bottom-sheet-renderer, body > ytm-settings-dialog, body > dialog, body > .dialog-container, body > .bottom-sheet-container, body > ytm-popup-container, ' +
      'ytm-app > ytm-menu-renderer, ytm-app > .ytm-bottom-sheet-renderer, ytm-app > ytm-settings-dialog, ytm-app > dialog, ytm-app > .dialog-container, ytm-app > .bottom-sheet-container, ytm-app > ytm-popup-container'
    );
    menus.forEach(menu => {
      if (menu && menu.parentNode && menu.parentNode !== fsElem) {
        fsElem.appendChild(menu);
      }
    });
  }

  function handleSettingsInteraction(e) {
    const target = e.target;
    if (!target) return;
    const btn = target.closest(
      'button[aria-label*="Settings" i], button[aria-label*="More options" i], button[aria-label*="Playback settings" i], .ytp-settings-button, .ytm-settings-button, .ytp-menu-button'
    );
    if (btn) {
      setTimeout(reparentFsMenus, 20);
      setTimeout(reparentFsMenus, 80);
      setTimeout(reparentFsMenus, 200);
      setTimeout(reparentFsMenus, 500);
    }
  }
  document.addEventListener('touchstart', handleSettingsInteraction, true);
  document.addEventListener('click', handleSettingsInteraction, true);

  // In HTML5 fullscreen, any element appended to document.body is hidden behind the fullscreen top-layer
  // Reparent YouTube bottom sheets / menus into document.fullscreenElement so they display properly
  try {
    const fsMenuObserver = new MutationObserver(() => {
      reparentFsMenus();
    });
    if (document.body) {
      fsMenuObserver.observe(document.body, { childList: true, subtree: true });
    } else {
      document.addEventListener('DOMContentLoaded', () => {
        if (document.body) fsMenuObserver.observe(document.body, { childList: true, subtree: true });
      });
    }
  } catch (e) { }

  // -------------------------------------------------------------
  // 7. Playback State Synchronization with Caspian Android
  // -------------------------------------------------------------
  setInterval(function () {
    try {
      const v = document.querySelector('video');
      const isPlaying = !!(v && !v.paused && v.currentTime > 0 && !v.ended && v.readyState > 2);
      const tabId = window.__caspian_tab_id || 0;
      if (window.CaspianBridge && typeof window.CaspianBridge.updateTabMediaPlaybackState === 'function') {
        window.CaspianBridge.updateTabMediaPlaybackState(tabId, isPlaying);
      } else if (window.CaspianBridge && typeof window.CaspianBridge.updateMediaPlaybackState === 'function') {
        window.CaspianBridge.updateMediaPlaybackState(isPlaying);
      }
    } catch (e) { }
  }, 1000);
})();
