// =========================================================================
// CASPIAN ANDROID - YOUTUBE UBLOCK DEFUSER & BACKGROUND HELPER ENGINE
// =========================================================================
(function () {
  if (window.__CASPIAN_YT_DEFUSER_INITIALIZED__) return;
  window.__CASPIAN_YT_DEFUSER_INITIALIZED__ = true;

  // -------------------------------------------------------------
  // 1. Comprehensive Page Visibility & Background Play Engine
  // -------------------------------------------------------------
  window.__caspian_explicit_pause = false;
  window.__caspian_pip_active = false;

  try {
    Object.defineProperty(document, 'hidden', { get: () => false, configurable: true });
    Object.defineProperty(document, 'visibilityState', { get: () => 'visible', configurable: true });
    Object.defineProperty(document, 'webkitVisibilityState', { get: () => 'visible', configurable: true });
    document.hasFocus = () => true;
    window.hasFocus = () => true;
  } catch (e) { }

  // Intercept and drop listeners that YouTube uses to pause videos (visibility, blur, freeze, pagehide)
  try {
    const origDocAddEventListener = document.addEventListener;
    document.addEventListener = function (type, listener, options) {
      if (type === 'visibilitychange' || type === 'webkitvisibilitychange' || type === 'blur' || type === 'focusout' || type === 'pagehide' || type === 'freeze') {
        return;
      }
      return origDocAddEventListener.apply(this, arguments);
    };

    const origWinAddEventListener = window.addEventListener;
    window.addEventListener = function (type, listener, options) {
      if (type === 'visibilitychange' || type === 'webkitvisibilitychange' || type === 'pagehide' || type === 'blur' || type === 'focusout' || type === 'freeze') {
        return;
      }
      return origWinAddEventListener.apply(this, arguments);
    };

    try {
      window.onblur = null;
      document.onblur = null;
      window.onpagehide = null;
    } catch(e){}
  } catch (e) { }

  // Block automatic pausing triggered by backgrounding
  const originalPause = HTMLVideoElement.prototype.pause;
  HTMLVideoElement.prototype.pause = function () {
    if (window.__caspian_explicit_pause) {
      window.__caspian_explicit_pause = false;
      return originalPause.apply(this, arguments);
    }
    const err = new Error();
    const stack = (err.stack || '').toLowerCase();
    // Only intercept genuine backgrounding event triggers (visibilitychange, pagehide)
    if (stack.includes('visibilitychange') || stack.includes('pagehide')) {
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
      // 1. Primary: watch player video element on m.youtube.com and youtube.com
      const mainVid = document.querySelector(
        '#movie_player video, .html5-video-player video, #player video, #player-container-id video, .player-container video, ytm-watch video, video.html5-main-video, video[src*="blob:"]'
      );
      if (mainVid) return mainVid;

      // 2. Filter out feed preview overlays only
      const allVideos = Array.from(document.querySelectorAll('video')).filter(v => {
        return !v.closest('ytm-thumbnail-overlay-preview-video-renderer, ytd-thumbnail-overlay-preview-video-renderer, .ytp-inline-preview-ui, #inline-preview-player, .inline-preview-player');
      });
      if (allVideos.length > 0) {
        return allVideos.find(v => !v.paused && v.currentTime > 0) || allVideos[0];
      }

      // 3. Fallback to any video element on page
      return document.querySelector('video');
    },
    notifyState: function (force) {
      try {
        const v = this.getVideo();
        if (v) {
          const isPlaying = !v.paused && !v.ended;
          const isMuted = !!v.muted;
          const tabId = window.__caspian_tab_id || 0;
          if (force || this._lastPlaying !== isPlaying || this._lastMuted !== isMuted) {
            this._lastPlaying = isPlaying;
            this._lastMuted = isMuted;
            if (window.CaspianBridge) {
              if (typeof window.CaspianBridge.updateTabYouTubeState === 'function') {
                window.CaspianBridge.updateTabYouTubeState(tabId, isPlaying, isMuted);
              } else if (typeof window.CaspianBridge.updateYouTubeState === 'function') {
                window.CaspianBridge.updateYouTubeState(isPlaying, isMuted);
              }
            }
          }
        }
      } catch (e) { }
    },
    enterPipMode: function () {
      window.__caspian_pip_active = true;
      try {
        document.documentElement.classList.add('caspian-pip-active');
        document.body.classList.add('caspian-pip-active');
      } catch(e){}
      const v = this.getVideo();
      if (v && v.paused) {
        v.play().catch(() => {});
      }
    },
    exitPipMode: function () {
      window.__caspian_pip_active = false;
      try {
        document.documentElement.classList.remove('caspian-pip-active');
        document.body.classList.remove('caspian-pip-active');
      } catch(e){}
    },
    togglePlay: function () {
      const v = this.getVideo();
      if (v) {
        if (v.paused) {
          v.play().then(() => {
            this.notifyState(true);
          }).catch(() => {});
        } else {
          window.__caspian_explicit_pause = true;
          v.pause();
          this.notifyState(true);
        }
        this.notifyState(true);
        setTimeout(() => this.notifyState(true), 150);
      }
    },
    toggleMute: function () {
      const v = this.getVideo();
      if (v) {
        v.muted = !v.muted;
        this.notifyState(true);
        setTimeout(() => this.notifyState(true), 100);
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
    getAvailableQualities: function () {
      try {
        const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player');
        if (player) {
          if (typeof player.getAvailableQualityData === 'function') {
            const data = player.getAvailableQualityData();
            if (Array.isArray(data) && data.length > 0) {
              return JSON.stringify(data.map(function (d) {
                return {
                  code: d.quality || d.formatId || '',
                  label: d.qualityLabel || d.label || d.quality || ''
                };
              }));
            }
          }
          if (typeof player.getAvailableQualityLevels === 'function') {
            const levels = player.getAvailableQualityLevels();
            if (Array.isArray(levels) && levels.length > 0) {
              const qualityMap = {
                'hd2160': '2160p (4K)',
                'hd1440': '1440p (2K)',
                'hd1080': '1080p (HD)',
                'hd720': '720p (HD)',
                'large': '480p',
                'medium': '360p',
                'small': '240p',
                'tiny': '144p',
                'auto': 'Auto'
              };
              return JSON.stringify(levels.map(function (q) {
                return {
                  code: q,
                  label: qualityMap[q] || q.toUpperCase()
                };
              }));
            }
          }
        }
      } catch (e) { }
      return "";
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
          'hd2160': '2160p',
          'hd1440': '1440p',
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
    },
    toggleCaptions: function () {
      try {
        const ccBtn = document.querySelector('.ytp-subtitles-button, button.ytp-subtitles-button, button[aria-label*="Subtitles"], button[aria-label*="Captions"], [aria-label*="subtitles"], [aria-label*="captions"]');
        if (ccBtn) {
          ccBtn.click();
          return true;
        }
        const v = this.getVideo();
        if (v && v.textTracks && v.textTracks.length > 0) {
          let showing = false;
          for (let i = 0; i < v.textTracks.length; i++) {
            if (v.textTracks[i].mode === 'showing') showing = true;
          }
          for (let i = 0; i < v.textTracks.length; i++) {
            v.textTracks[i].mode = showing ? 'hidden' : 'showing';
          }
          return !showing;
        }
      } catch(e){}
      return false;
    },
    toggleLoop: function () {
      try {
        const v = this.getVideo();
        if (v) {
          v.loop = !v.loop;
          return v.loop;
        }
      } catch(e){}
      return false;
    },
    isLooping: function () {
      try {
        const v = this.getVideo();
        return !!(v && v.loop);
      } catch(e){ return false; }
    },
    toggleAmbient: function () {
      try {
        const sizeBtn = document.querySelector('.ytp-size-button');
        if (sizeBtn) sizeBtn.click();
        const app = document.querySelector('ytm-app, ytd-app, body');
        if (app) {
          app.classList.toggle('caspian-ambient-mode');
          return app.classList.contains('caspian-ambient-mode');
        }
      } catch(e){}
      return false;
    },
    toggleAutoplay: function () {
      try {
        const apBtn = document.querySelector('.ytp-autonav-toggle-button, [aria-label*="Autoplay"], button[data-tooltip-target-id*="autoplay"]');
        if (apBtn) {
          apBtn.click();
          return true;
        }
      } catch(e){}
      return false;
    },
    togglePip: function () {
      try {
        const v = this.getVideo();
        if (v) {
          if (document.pictureInPictureElement) {
            document.exitPictureInPicture().catch(()=>{});
          } else if (v.requestPictureInPicture) {
            v.requestPictureInPicture().catch(()=>{});
          }
        }
      } catch(e){}
    }
  };

  function attachVideoListeners() {
    try {
      const v = window.__CaspianYouTube ? window.__CaspianYouTube.getVideo() : document.querySelector('video');
      if (v && !v.__caspian_attached) {
        v.__caspian_attached = true;
        ['play', 'playing', 'pause', 'ended', 'volumechange', 'ratechange'].forEach(evt => {
          v.addEventListener(evt, () => {
            if (evt === 'ended') {
              const tabId = window.__caspian_tab_id || 0;
              if (window.CaspianBridge && typeof window.CaspianBridge.onYouTubeVideoEnded === 'function') {
                window.CaspianBridge.onYouTubeVideoEnded(tabId);
              }
            }
            if (window.__CaspianYouTube) window.__CaspianYouTube.notifyState();
          });
        });
        v.addEventListener('timeupdate', () => {
          const tabId = window.__caspian_tab_id || 0;
          if (v.ended || (v.duration > 0 && Math.abs((v.currentTime || 0) - v.duration) < 0.5)) {
            if (window.CaspianBridge && typeof window.CaspianBridge.onYouTubeVideoEnded === 'function') {
              window.CaspianBridge.onYouTubeVideoEnded(tabId);
            }
          }
          if (window.CaspianBridge && typeof window.CaspianBridge.updateTabYouTubeTime === 'function') {
            window.CaspianBridge.updateTabYouTubeTime(tabId, v.currentTime || 0, v.duration || 0);
          } else if (window.CaspianBridge && typeof window.CaspianBridge.updateYouTubeTime === 'function') {
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
  // 4.5 Per-Tab YouTube Home Feed & Recommendation State Preservation
  // -------------------------------------------------------------
  // Memory-only state scoped strictly to this specific tab's window
  // When this tab is closed, this WebView window is destroyed and this RAM is immediately freed.
  window.__caspian_yt_home_state = {
    browseResponse: null,
    scrollY: 0,
    isReturningHome: false,
    hasLeftHome: false
  };

  // Track user scrolling on the home page
  window.addEventListener('scroll', () => {
    try {
      const path = window.location.pathname;
      if (path === '/' || path === '') {
        window.__caspian_yt_home_state.scrollY = window.scrollY || window.pageYOffset || 0;
      }
    } catch (e) { }
  }, { passive: true });

  // Listen to navigation changes (popstate)
  window.addEventListener('popstate', () => {
    try {
      const path = window.location.pathname;
      if ((path === '/' || path === '') && window.__caspian_yt_home_state.hasLeftHome && window.__caspian_yt_home_state.browseResponse) {
        window.__caspian_yt_home_state.isReturningHome = true;
      }
    } catch (e) { }
  });

  // Observe route changes between home and watch
  let _lastYtPath = window.location.pathname;
  setInterval(() => {
    try {
      const curPath = window.location.pathname;
      if (curPath !== _lastYtPath) {
        if (_lastYtPath === '/' && curPath.includes('/watch')) {
          window.__caspian_yt_home_state.hasLeftHome = true;
        } else if ((curPath === '/' || curPath === '') && _lastYtPath.includes('/watch')) {
          if (window.__caspian_yt_home_state.browseResponse) {
            window.__caspian_yt_home_state.isReturningHome = true;
          }
        }
        _lastYtPath = curPath;
      }
    } catch (e) { }
  }, 150);

  // Hook window.fetch for /youtubei/v1/browse
  if (window.fetch) {
    const origFetch = window.fetch;
    window.fetch = async function (resource, init) {
      const url = typeof resource === 'string' ? resource : (resource && resource.url ? resource.url : '');
      if (url.includes('/youtubei/v1/browse')) {
        // If returning home via back navigation, serve our in-memory cached browse response!
        if (window.__caspian_yt_home_state.isReturningHome && window.__caspian_yt_home_state.browseResponse) {
          window.__caspian_yt_home_state.isReturningHome = false;
          window.__caspian_yt_home_state.hasLeftHome = false;
          const cachedJson = window.__caspian_yt_home_state.browseResponse;

          // Restore scroll position after YouTube mounts the home feed
          setTimeout(() => {
            try {
              if (window.__caspian_yt_home_state.scrollY > 0) {
                window.scrollTo({ top: window.__caspian_yt_home_state.scrollY, behavior: 'instant' });
              }
            } catch (e) { }
          }, 80);
          setTimeout(() => {
            try {
              if (window.__caspian_yt_home_state.scrollY > 0) {
                window.scrollTo({ top: window.__caspian_yt_home_state.scrollY, behavior: 'instant' });
              }
            } catch (e) { }
          }, 300);

          return new Response(JSON.stringify(cachedJson), {
            status: 200,
            statusText: 'OK',
            headers: { 'Content-Type': 'application/json' }
          });
        }

        // Live fetch for home feed: cache the initial recommendation payload in memory
        const response = await origFetch.apply(this, arguments);
        try {
          // Only cache initial feed, not infinite scroll continuations
          let isContinuation = false;
          if (init && init.body && typeof init.body === 'string' && init.body.includes('continuation')) {
            isContinuation = true;
          }
          if (!isContinuation && (window.location.pathname === '/' || window.location.pathname === '')) {
            const clone = response.clone();
            clone.json().then(data => {
              if (data && !data.error) {
                window.__caspian_yt_home_state.browseResponse = data;
              }
            }).catch(() => {});
          }
        } catch (e) { }
        return response;
      }
      return origFetch.apply(this, arguments);
    };
  }

  // -------------------------------------------------------------
  // 5. Accurate 0ms Auto-Skip & GSI Codec-Safe Fallback Engine
  // -------------------------------------------------------------
  let _adHangStartTime = null;

  function executeFastForwardSkip() {
    try {
      const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player');
      const isAdActive = !!(player && (player.classList.contains('ad-showing') || player.classList.contains('ad-interrupting')));
      const adText = document.querySelector('.ytp-ad-text, .ytp-ad-preview-text, .ytp-ad-duration-remaining');
      const video = document.querySelector('video');

      // Native YouTube Player API skip (cleanest and doesn't rely on brute-force seeking)
      if (player && typeof player.skipAd === 'function') {
        try { player.skipAd(); } catch (e) { }
      }

      // Safe fast-forward: ONLY if video media is ready and buffered (readyState >= 2)
      // Never set 16x or seek on unbuffered video as it crashes generic AOSP MediaCodec on GSI ROMs!
      if ((isAdActive || adText) && video) {
        video.muted = true;
        if (video.readyState >= 2) {
          try {
            if (video.playbackRate < 8.0) video.playbackRate = 8.0;
          } catch (e) { }
          if (isFinite(video.duration) && video.duration > 0 && video.duration < 120 && video.currentTime < video.duration - 0.5) {
            try { video.currentTime = video.duration - 0.1; } catch (e) { }
          }
        }

        // Ad Freeze / Hang Watchdog for GSI ROMs:
        // If an ad is stalled for > 1.5 seconds without advancing, force clear ad state
        if (!_adHangStartTime) {
          _adHangStartTime = Date.now();
        } else if (Date.now() - _adHangStartTime > 1500) {
          if (player) {
            try { if (typeof player.skipAd === 'function') player.skipAd(); } catch (e) { }
            player.classList.remove('ad-showing', 'ad-interrupting');
          }
          if (video) {
            video.muted = false;
            video.playbackRate = 1.0;
            video.play().catch(() => {});
          }
          _adHangStartTime = null;
        }
      } else {
        _adHangStartTime = null;
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
    /* Only scope settings gear button and menus inside fullscreen */
    :fullscreen .ytp-settings-button,
    :fullscreen .ytp-menu-button,
    :fullscreen .ytm-settings-button,
    :fullscreen .ytp-settings-menu,
    :fullscreen .ytp-popup,
    :fullscreen .ytp-panel {
      z-index: 2147483647 !important;
      pointer-events: auto !important;
    }
    /* Guarantee YouTube mobile bottom navigation bar (Home, Shorts, Subscriptions, You) is always on top and interactive */
    ytm-pivot-bar-renderer,
    .pivot-bar,
    #pivot-bar {
      z-index: 99999 !important;
      pointer-events: auto !important;
      visibility: visible !important;
      opacity: 1 !important;
    }
    /* Caspian PiP Active: forces video element to occupy 100vw/100vh and hides everything else */
    html.caspian-pip-active, body.caspian-pip-active {
      overflow: hidden !important;
      background: #000 !important;
      width: 100vw !important;
      height: 100vh !important;
      margin: 0 !important;
      padding: 0 !important;
    }
    html.caspian-pip-active #player,
    html.caspian-pip-active #player-container-id,
    html.caspian-pip-active .player-container,
    html.caspian-pip-active .html5-video-player,
    html.caspian-pip-active video.html5-main-video,
    html.caspian-pip-active video {
      position: fixed !important;
      top: 0 !important;
      left: 0 !important;
      width: 100vw !important;
      height: 100vh !important;
      max-width: 100vw !important;
      max-height: 100vh !important;
      z-index: 2147483647 !important;
      object-fit: cover !important;
      background: #000 !important;
    }
    html.caspian-pip-active ytm-mobile-topbar-renderer,
    html.caspian-pip-active header,
    html.caspian-pip-active .header-bar,
    html.caspian-pip-active ytm-pivot-bar-renderer,
    html.caspian-pip-active #page-manager,
    html.caspian-pip-active ytm-single-column-watch-next-results-renderer-header-view-model,
    html.caspian-pip-active .related-items,
    html.caspian-pip-active ytm-item-section-renderer,
    html.caspian-pip-active ytm-comments-entry-point-header-renderer {
      display: none !important;
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
      const v = window.__CaspianYouTube ? window.__CaspianYouTube.getVideo() : document.querySelector('video');
      const isPlaying = !!(v && !v.paused && v.currentTime > 0 && !v.ended && v.readyState > 1);
      const isMuted = !!(v && v.muted);
      const tabId = window.__caspian_tab_id || 0;

      if (window.__CaspianYouTube) {
        window.__CaspianYouTube.notifyState();
      }

      if (window.CaspianBridge) {
        if (typeof window.CaspianBridge.updateTabYouTubeState === 'function') {
          window.CaspianBridge.updateTabYouTubeState(tabId, isPlaying, isMuted);
        } else if (typeof window.CaspianBridge.updateYouTubeState === 'function') {
          window.CaspianBridge.updateYouTubeState(isPlaying, isMuted);
        }
      }

      if (isPlaying) {
        var title = '';
        var titleEl = document.querySelector('h1.title, .slim-video-metadata-title, ytm-slim-video-metadata-renderer .title, meta[name="title"]');
        if (titleEl) title = titleEl.textContent || titleEl.getAttribute('content') || '';
        if (!title) title = (document.title || '').replace(' - YouTube', '').trim();
        var thumbUrl = '';
        var videoId = '';
        var vMatch = location.search.match(/[?&]v=([^&]+)/);
        if (vMatch && vMatch[1]) {
          videoId = vMatch[1];
        } else {
          var pMatch = location.pathname.match(/\/(?:shorts|embed|v)\/([^/?]+)/);
          if (pMatch && pMatch[1]) videoId = pMatch[1];
        }
        if (videoId) {
          thumbUrl = 'https://i.ytimg.com/vi/' + videoId + '/maxresdefault.jpg';
        } else {
          var ogImage = document.querySelector('meta[property="og:image"]');
          if (ogImage) thumbUrl = ogImage.getAttribute('content') || '';
        }
        if (title && window.CaspianBridge) {
          if (typeof window.CaspianBridge.updateTabMediaMetadata === 'function') {
            window.CaspianBridge.updateTabMediaMetadata(tabId, title, thumbUrl);
          } else if (typeof window.CaspianBridge.updateMediaMetadata === 'function') {
            window.CaspianBridge.updateMediaMetadata(title, thumbUrl);
          }
        }
      }
    } catch (e) { }
  }, 1000);
})();
