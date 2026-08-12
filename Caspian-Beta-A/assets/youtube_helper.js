// ======================================================
// CASPIAN ANDROID - YOUTUBE UTILITY & BACKGROUND HELPER
// ======================================================
(function() {
  if (window.__CASPIAN_YOUTUBE_HELPER_LOADED__) return;
  window.__CASPIAN_YOUTUBE_HELPER_LOADED__ = true;

  // 1. Page Visibility API Override (Prevents YouTube from pausing when tab is hidden or minimized)
  try {
    Object.defineProperty(document, 'hidden', { get: () => false, configurable: true });
    Object.defineProperty(document, 'visibilityState', { get: () => 'visible', configurable: true });
  } catch(e) {}

  window.addEventListener('visibilitychange', function(e) {
    e.stopImmediatePropagation();
  }, true);

  // 2. Prevent YouTube pause on visibility blur
  const originalPause = HTMLVideoElement.prototype.pause;
  HTMLVideoElement.prototype.pause = function() {
    if (document.hidden || document.visibilityState === 'hidden') {
      return;
    }
    return originalPause.apply(this, arguments);
  };

  // 3. YouTube Player Controls Helper Functions (Seek, Speed, Quality, Ad Hiding)
  window.__CaspianYouTube = {
    getVideo: function() {
      return document.querySelector('video');
    },
    seekBy: function(seconds) {
      const v = this.getVideo();
      if (v) {
        v.currentTime = Math.max(0, Math.min(v.duration || Infinity, v.currentTime + seconds));
      }
    },
    setSpeed: function(rate) {
      const v = this.getVideo();
      if (v) {
        v.playbackRate = parseFloat(rate);
      }
    },
    setQuality: function(qualityStr) {
      try {
        const player = document.getElementById('movie_player') || document.querySelector('.html5-video-player');
        if (player && typeof player.setPlaybackQualityRange === 'function') {
          player.setPlaybackQualityRange(qualityStr, qualityStr);
        } else if (player && typeof player.setPlaybackQuality === 'function') {
          player.setPlaybackQuality(qualityStr);
        }
      } catch(e) {}
    }
  };

  // 4. Mobile Gear Settings Touch Pass-through & Ad Element Hiding
  const style = document.createElement('style');
  style.id = 'caspian-yt-fixes';
  style.textContent = `
    .video-ads, .ytp-ad-module, .ytp-ad-overlay-container, #player-ads, ytd-promoted-sparkles-web-renderer, .ad-slot, .ad-banner {
      display: none !important;
    }
    .ytp-settings-menu, .ytp-popup, .ytp-panel {
      z-index: 2147483647 !important;
      pointer-events: auto !important;
    }
  `;
  (document.head || document.documentElement).appendChild(style);

  // Auto skip YouTube video ads & report active playback state for specific tab
  setInterval(function() {
    const skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .ytp-skip-ad-button');
    if (skipBtn) {
      skipBtn.click();
    }

    try {
      const v = document.querySelector('video');
      const isPlaying = !!(v && !v.paused && v.currentTime > 0 && !v.ended && v.readyState > 2);
      const tabId = window.__caspian_tab_id || 0;
      if (window.CaspianBridge && typeof window.CaspianBridge.updateTabMediaPlaybackState === 'function') {
        window.CaspianBridge.updateTabMediaPlaybackState(tabId, isPlaying);
      } else if (window.CaspianBridge && typeof window.CaspianBridge.updateMediaPlaybackState === 'function') {
        window.CaspianBridge.updateMediaPlaybackState(isPlaying);
      }
    } catch(e) {}
  }, 1000);
})();
