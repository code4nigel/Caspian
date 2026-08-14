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
    toggleFullscreen: function () {
      try {
        const fsBtn = document.querySelector('.ytp-fullscreen-button, button.ytp-fullscreen-button, .fullscreen-icon, ytm-fullscreen-button');
        if (fsBtn && typeof fsBtn.click === 'function') {
          fsBtn.click();
        } else {
          const v = this.getVideo();
          if (v) {
            if (document.fullscreenElement || document.webkitFullscreenElement) {
              if (document.exitFullscreen) document.exitFullscreen();
              else if (document.webkitExitFullscreen) document.webkitExitFullscreen();
            } else {
              if (v.webkitEnterFullscreen) {
                v.webkitEnterFullscreen();
              } else if (v.requestFullscreen) {
                v.requestFullscreen();
              } else if (v.webkitRequestFullscreen) {
                v.webkitRequestFullscreen();
              }
            }
          }
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
  // 6. Visual Element Ad Suppression
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
    .ytp-settings-menu, .ytp-popup, .ytp-panel {
      z-index: 2147483647 !important;
      pointer-events: auto !important;
    }
  `;
  (document.head || document.documentElement).appendChild(style);

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
