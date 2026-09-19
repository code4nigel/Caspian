// ==========================================================
// CASPIAN - FLOW SPEED UNIVERSAL VIDEO/AUDIO SPEED ENGINE
// ==========================================================

(function () {
  'use strict';

  let config = {
    enabled: true,
    speed: 1.0,
    cycleList: [1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0],
    shortcutReset: 'alt+s',
    shortcutCycle: 'alt+d',
    shortcutUp: ']',
    shortcutDown: '[',
    showHud: true
  };

  // Parse comma-separated cycle string into numeric array
  function parseCycleList(str) {
    if (!str) return [1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0];
    const arr = str.split(',').map(s => parseFloat(s.trim())).filter(n => !isNaN(n) && n > 0);
    return arr.length > 0 ? arr : [1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0];
  }

  let lastCustomSpeed = 1.5;

  // Load config from Chrome Storage
  function loadConfig(callback) {
    chrome.storage.local.get([
      'flow_speed_enabled',
      'flow_speed_val',
      'flow_speed_last_custom',
      'flow_speed_cycle_list',
      'flow_speed_shortcut_reset',
      'flow_speed_shortcut_cycle',
      'flow_speed_shortcut_up',
      'flow_speed_shortcut_down',
      'flow_speed_show_hud'
    ], (data) => {
      config.enabled = data.flow_speed_enabled ?? true;
      config.speed = parseFloat(data.flow_speed_val) || 1.0;
      lastCustomSpeed = parseFloat(data.flow_speed_last_custom) || (Math.abs(config.speed - 1.0) > 0.01 ? config.speed : 1.5);
      config.cycleList = parseCycleList(data.flow_speed_cycle_list);
      config.shortcutReset = (data.flow_speed_shortcut_reset || 'alt+s').toLowerCase();
      config.shortcutCycle = (data.flow_speed_shortcut_cycle || 'alt+d').toLowerCase();
      config.shortcutUp = (data.flow_speed_shortcut_up || ']').toLowerCase();
      config.shortcutDown = (data.flow_speed_shortcut_down || '[').toLowerCase();
      config.showHud = data.flow_speed_show_hud ?? true;
      if (callback) callback();
    });
  }

  // Listen for real-time config updates from popup
  chrome.storage.onChanged.addListener((changes, area) => {
    if (area === 'local') {
      let shouldApply = false;
      if (changes.flow_speed_enabled !== undefined) {
        config.enabled = changes.flow_speed_enabled.newValue;
        shouldApply = true;
      }
      if (changes.flow_speed_val !== undefined) {
        config.speed = parseFloat(changes.flow_speed_val.newValue) || 1.0;
        shouldApply = true;
      }
      if (changes.flow_speed_cycle_list !== undefined) {
        config.cycleList = parseCycleList(changes.flow_speed_cycle_list.newValue);
      }
      if (changes.flow_speed_shortcut_reset !== undefined) {
        config.shortcutReset = (changes.flow_speed_shortcut_reset.newValue || 'alt+s').toLowerCase();
      }
      if (changes.flow_speed_shortcut_cycle !== undefined) {
        config.shortcutCycle = (changes.flow_speed_shortcut_cycle.newValue || 'alt+d').toLowerCase();
      }
      if (changes.flow_speed_shortcut_up !== undefined) {
        config.shortcutUp = (changes.flow_speed_shortcut_up.newValue || ']').toLowerCase();
      }
      if (changes.flow_speed_shortcut_down !== undefined) {
        config.shortcutDown = (changes.flow_speed_shortcut_down.newValue || '[').toLowerCase();
      }
      if (changes.flow_speed_show_hud !== undefined) {
        config.showHud = changes.flow_speed_show_hud.newValue;
      }

      if (shouldApply) {
        applySpeedToAllMedia(config.enabled ? config.speed : 1.0);
      }
    }
  });

  // Inject HUD styles (positioned on the left side with wave aesthetic)
  function injectHudStyles() {
    if (document.getElementById('caspian-flow-speed-styles')) return;
    const style = document.createElement('style');
    style.id = 'caspian-flow-speed-styles';
    style.textContent = `
      .caspian-speed-hud {
        position: fixed !important;
        top: 24px !important;
        left: 24px !important;
        right: auto !important;
        z-index: 2147483647 !important;
        background: rgba(15, 23, 42, 0.90) !important;
        backdrop-filter: blur(16px) !important;
        -webkit-backdrop-filter: blur(16px) !important;
        border: 1.5px solid rgba(56, 189, 248, 0.45) !important;
        color: #ffffff !important;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif !important;
        font-size: 15.5px !important;
        font-weight: 700 !important;
        letter-spacing: 0.5px !important;
        padding: 8px 18px !important;
        border-radius: 9999px !important;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.6), 0 0 20px rgba(56, 189, 248, 0.35) !important;
        pointer-events: none !important;
        display: flex !important;
        align-items: center !important;
        gap: 9px !important;
        animation: caspianSpeedHudAnim 1.4s cubic-bezier(0.16, 1, 0.3, 1) forwards !important;
      }
      @keyframes caspianSpeedHudAnim {
        0% { opacity: 0; transform: translateX(-12px) scale(0.9); }
        15% { opacity: 1; transform: translateX(0) scale(1); }
        75% { opacity: 1; transform: translateX(0) scale(1); }
        100% { opacity: 0; transform: translateX(-8px) scale(0.95); }
      }
    `;
    (document.head || document.documentElement).appendChild(style);
  }

  // Display On-Screen Speed HUD with Caspian wave icon
  let currentHudTimer = null;
  function showSpeedHud(speedVal) {
    if (!config.showHud) return;
    injectHudStyles();

    const existingHud = document.querySelector('.caspian-speed-hud');
    if (existingHud) existingHud.remove();

    const hud = document.createElement('div');
    hud.className = 'caspian-speed-hud';
    hud.innerHTML = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#38bdf8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M2 6c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path>
        <path d="M2 12c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path>
        <path d="M2 18c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path>
      </svg>
      <span>${speedVal.toFixed(2)}x</span>
    `;

    document.documentElement.appendChild(hud);

    if (currentHudTimer) clearTimeout(currentHudTimer);
    currentHudTimer = setTimeout(() => {
      if (hud && hud.parentNode) hud.parentNode.removeChild(hud);
    }, 1400);
  }

  // Apply speed to a specific media element
  const trackedMedia = new WeakSet();
  function attachMediaListeners(media) {
    if (!media || trackedMedia.has(media)) return;
    trackedMedia.add(media);

    const enforceSpeed = () => {
      if (!config.enabled) return;
      if (media.playbackRate !== config.speed) {
        media.playbackRate = config.speed;
      }
    };

    media.addEventListener('play', enforceSpeed);
    media.addEventListener('playing', enforceSpeed);
    media.addEventListener('ratechange', () => {
      if (config.enabled && Math.abs(media.playbackRate - config.speed) > 0.01) {
        media.playbackRate = config.speed;
      }
    });

    if (config.enabled) {
      media.playbackRate = config.speed;
    }
  }

  // Apply target speed across all video/audio tags
  function applySpeedToAllMedia(targetSpeed) {
    const mediaElements = document.querySelectorAll('video, audio');
    mediaElements.forEach(media => {
      attachMediaListeners(media);
      try {
        media.playbackRate = targetSpeed;
      } catch (e) {}
    });
  }

  // Set new speed and sync with storage
  function setSpeed(newSpeed, triggerHud = true) {
    newSpeed = Math.max(0.1, Math.min(16.0, parseFloat(newSpeed.toFixed(2))));
    if (Math.abs(newSpeed - 1.0) > 0.01) {
      lastCustomSpeed = newSpeed;
      chrome.storage.local.set({ flow_speed_last_custom: newSpeed });
    }
    config.speed = newSpeed;
    chrome.storage.local.set({ flow_speed_val: newSpeed });
    applySpeedToAllMedia(newSpeed);
    if (triggerHud) showSpeedHud(newSpeed);
  }

  // Cycle to next speed in cycleList
  function cycleToNextSpeed() {
    const list = config.cycleList;
    if (!list || list.length === 0) return;

    let currentIndex = list.findIndex(s => Math.abs(s - config.speed) < 0.05);
    let nextIndex = 0;
    if (currentIndex >= 0 && currentIndex < list.length - 1) {
      nextIndex = currentIndex + 1;
    } else {
      nextIndex = 0;
    }

    setSpeed(list[nextIndex], true);
  }

  // Helper to check if event originated from editable text inputs
  function isEditingText(target) {
    if (!target) return false;
    const tag = (target.tagName || '').toLowerCase();
    if (tag === 'input' || tag === 'textarea' || tag === 'select') return true;
    if (target.isContentEditable || target.getAttribute('contenteditable') === 'true') return true;
    return false;
  }

  // Helper to match keyboard shortcut string against keyboard event
  function matchShortcut(e, shortcutStr) {
    if (!shortcutStr) return false;
    const parts = shortcutStr.toLowerCase().split('+').map(s => s.trim());
    const key = parts[parts.length - 1];
    const requiresAlt = parts.includes('alt');
    const requiresCtrl = parts.includes('ctrl') || parts.includes('control');
    const requiresShift = parts.includes('shift');

    if (requiresAlt !== e.altKey) return false;
    if (requiresCtrl !== (e.ctrlKey || e.metaKey)) return false;
    if (requiresShift !== e.shiftKey) return false;

    return e.key.toLowerCase() === key;
  }

  // Global Keyboard Shortcuts Listener
  window.addEventListener('keydown', (e) => {
    if (!config.enabled) return;
    if (isEditingText(e.target)) return;

    // 1. Toggle between 1.0x and previous speed (Default Alt+S)
    if (matchShortcut(e, config.shortcutReset)) {
      e.preventDefault();
      if (Math.abs(config.speed - 1.0) < 0.01) {
        // Currently at 1.0x -> Restore last custom speed
        const restoreSpeed = (lastCustomSpeed && Math.abs(lastCustomSpeed - 1.0) > 0.01) ? lastCustomSpeed : 1.5;
        setSpeed(restoreSpeed, true);
      } else {
        // Currently at custom speed (e.g. 1.75x) -> Remember it and set to 1.0x
        lastCustomSpeed = config.speed;
        chrome.storage.local.set({ flow_speed_last_custom: config.speed });
        setSpeed(1.0, true);
      }
      return;
    }

    // 2. Cycle Speeds (Default Alt+D)
    if (matchShortcut(e, config.shortcutCycle)) {
      e.preventDefault();
      cycleToNextSpeed();
      return;
    }

    // 3. Step Up (+0.25x)
    if (matchShortcut(e, config.shortcutUp)) {
      e.preventDefault();
      setSpeed(config.speed + 0.25, true);
      return;
    }

    // 4. Step Down (-0.25x)
    if (matchShortcut(e, config.shortcutDown)) {
      e.preventDefault();
      setSpeed(config.speed - 0.25, true);
      return;
    }
  }, true);

  // Monitor DOM for dynamically added video/audio elements
  function observeMedia() {
    applySpeedToAllMedia(config.enabled ? config.speed : 1.0);

    const observer = new MutationObserver((mutations) => {
      for (const mutation of mutations) {
        for (const node of mutation.addedNodes) {
          if (node.nodeType === 1) {
            if (node.tagName === 'VIDEO' || node.tagName === 'AUDIO') {
              attachMediaListeners(node);
            } else if (node.querySelectorAll) {
              const nested = node.querySelectorAll('video, audio');
              nested.forEach(m => attachMediaListeners(m));
            }
          }
        }
      }
    });

    if (document.body) {
      observer.observe(document.body, { childList: true, subtree: true });
    } else {
      window.addEventListener('DOMContentLoaded', () => {
        observer.observe(document.body, { childList: true, subtree: true });
      });
    }
  }

  // Initialize
  loadConfig(() => {
    if (document.readyState === 'complete' || document.readyState === 'interactive') {
      observeMedia();
    } else {
      window.addEventListener('DOMContentLoaded', observeMedia);
    }
  });

})();
