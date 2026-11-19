// ==========================================================
// CASPIAN - FLOW SPEED UNIVERSAL VIDEO/AUDIO SPEED ENGINE
// Supports: YouTube, Reddit, Twitter/X, TikTok, Netflix,
// Web Components & Shadow DOM Players, IFrames, Dynamic Feeds
// ==========================================================

(function () {
  'use strict';

  if (globalThis.caspianFlowSpeedLoaded) return;
  globalThis.caspianFlowSpeedLoaded = true;
  const { cycleDefault, fromEvent, normalize } = globalThis.caspianSpeedKeys;

  let config = {
    enabled: false,
    speed: 1.0,
    cycleList: [1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0],
    shortcutReset: 'alt+s',
    shortcutCycle: cycleDefault,
    shortcutUp: ']',
    shortcutDown: '[',
    showHud: true,
    hudPosition: 'top-left'
  };
  const hudThemeKeys = ['mode', 'accent', 'secondary', 'customBgColor'];
  const hudTheme = {};

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
      'flow_speed_show_hud',
      'flow_speed_hud_position',
      ...hudThemeKeys
    ], (data) => {
      config.enabled = data.flow_speed_enabled ?? true;
      config.speed = parseFloat(data.flow_speed_val) || 1.0;
      lastCustomSpeed = parseFloat(data.flow_speed_last_custom) || (Math.abs(config.speed - 1.0) > 0.01 ? config.speed : 1.5);
      config.cycleList = parseCycleList(data.flow_speed_cycle_list);
      config.shortcutReset = normalize(data.flow_speed_shortcut_reset, 'alt+s');
      config.shortcutCycle = normalize(data.flow_speed_shortcut_cycle, cycleDefault);
      config.shortcutUp = normalize(data.flow_speed_shortcut_up, ']');
      config.shortcutDown = normalize(data.flow_speed_shortcut_down, '[');
      config.showHud = data.flow_speed_show_hud ?? true;
      config.hudPosition = data.flow_speed_hud_position;
      for (const key of hudThemeKeys) hudTheme[key] = data[key];
      if (callback) callback();
    });
  }

  // Listen for real-time config updates from popup
  chrome.storage.onChanged.addListener((changes, area) => {
    if (area === 'local') {
      let shouldApply = false;
      if (changes.flow_speed_enabled !== undefined) {
        config.enabled = changes.flow_speed_enabled.newValue ?? true;
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
        config.shortcutReset = normalize(changes.flow_speed_shortcut_reset.newValue, 'alt+s');
      }
      if (changes.flow_speed_shortcut_cycle !== undefined) {
        config.shortcutCycle = normalize(changes.flow_speed_shortcut_cycle.newValue, cycleDefault);
      }
      if (changes.flow_speed_shortcut_up !== undefined) {
        config.shortcutUp = normalize(changes.flow_speed_shortcut_up.newValue, ']');
      }
      if (changes.flow_speed_shortcut_down !== undefined) {
        config.shortcutDown = normalize(changes.flow_speed_shortcut_down.newValue, '[');
      }
      if (changes.flow_speed_show_hud !== undefined) {
        config.showHud = changes.flow_speed_show_hud.newValue ?? true;
        if (!config.showHud) clearSpeedHud();
      }
      if (changes.flow_speed_hud_position) config.hudPosition = changes.flow_speed_hud_position.newValue;
      for (const key of hudThemeKeys) {
        if (key in changes) hudTheme[key] = changes[key].newValue;
      }

      if (shouldApply) {
        if (config.enabled) {
          if (engineRunning) applySpeedToAllMedia(config.speed);
          else startEngine();
        } else if (engineRunning) {
          stopEngine();
        }
      }
    }
  });

  // Install the HUD stylesheet only the first time it is needed in this frame.
  function injectHudStyles() {
    if (document.getElementById('caspian-flow-speed-styles')) return;
    const style = document.createElement('style');
    style.id = 'caspian-flow-speed-styles';
    style.textContent = `
      .caspian-speed-hud {
        --hud-gutter: clamp(12px, 3vw, 26px);
        --hud-x: 0%;
        --hud-entry: -10px;
        position: fixed !important;
        inset: auto !important;
        width: max-content !important;
        height: auto !important;
        margin: 0 !important;
        box-sizing: border-box !important;
        z-index: 2147483647 !important;
        background: linear-gradient(145deg, color-mix(in srgb, var(--hud-accent) 18%, var(--hud-panel)), var(--hud-panel) 70%) !important;
        border: 1px solid color-mix(in srgb, var(--hud-accent) 65%, transparent) !important;
        color: var(--hud-ink) !important;
        font: 600 11px/1.1 system-ui, sans-serif !important;
        padding: 7px 8px !important;
        border-radius: 16px !important;
        box-shadow: 0 7px 24px rgb(0 0 0 / 26%), 0 0 16px color-mix(in srgb, var(--hud-accent) 32%, transparent) !important;
        pointer-events: none !important;
        display: flex !important;
        align-items: center !important;
        gap: 8px !important;
        transform: translateX(var(--hud-x)) !important;
        animation: caspianSpeedHudAnim 1.7s cubic-bezier(.18,.82,.22,1) forwards;
      }
      .caspian-speed-hud[data-position^="top"] { top: var(--hud-gutter) !important; }
      .caspian-speed-hud[data-position^="bottom"] {
        bottom: var(--hud-gutter) !important;
        --hud-entry: 10px;
      }
      .caspian-speed-hud[data-position$="left"] { left: var(--hud-gutter) !important; }
      .caspian-speed-hud[data-position$="right"] { right: var(--hud-gutter) !important; }
      .caspian-speed-hud[data-position$="center"] {
        left: 50% !important;
        --hud-x: -50%;
      }
      .caspian-speed-hud .caspian-hud-icon {
        display: grid;
        place-items: center;
        width: 33px;
        height: 33px;
        border-radius: 11px;
        background: linear-gradient(135deg, var(--hud-accent), var(--hud-secondary));
        color: white;
      }
      .caspian-speed-hud .caspian-hud-reading { display: grid; gap: 2px; }
      .caspian-speed-hud strong { font: 750 20px/1 system-ui, sans-serif; font-variant-numeric: tabular-nums; }
      .caspian-speed-hud .caspian-hud-beats { display: flex; gap: 2px; align-items: end; padding: 0 3px; }
      .caspian-speed-hud .caspian-hud-beats i {
        width: 3px;
        height: 13px;
        border-radius: 2px;
        background: var(--hud-secondary);
        transform-origin: bottom;
        animation: caspianHudBeat 440ms ease-in-out 3 alternate;
      }
      .caspian-speed-hud .caspian-hud-beats i:first-child { height: 7px; animation-delay: 70ms; }
      .caspian-speed-hud .caspian-hud-beats i:last-child { height: 9px; animation-delay: 130ms; }
      @keyframes caspianHudBeat { from { scale: 1 .45; } to { scale: 1 1; } }
      @keyframes caspianSpeedHudAnim {
        0% { opacity: 0; translate: 0 var(--hud-entry); scale: .9; }
        17% { opacity: 1; translate: 0 0; scale: 1.04; }
        27%, 80% { opacity: 1; translate: 0 0; scale: 1; }
        100% { opacity: 0; translate: 0 calc(var(--hud-entry) / 2); scale: .97; }
      }
      @media (prefers-reduced-motion: reduce) {
        .caspian-speed-hud, .caspian-speed-hud .caspian-hud-beats i { animation: none !important; }
      }
    `;
    (document.head || document.documentElement).appendChild(style);
  }

  // Display On-Screen Speed HUD with Caspian wave icon
  let currentHudTimer = null;
  let currentHud = null;
  function clearSpeedHud() {
    clearTimeout(currentHudTimer);
    currentHudTimer = null;
    currentHud?.remove();
    currentHud = null;
  }
  function showSpeedHud(speedVal) {
    if (!config.showHud) return;
    injectHudStyles();

    clearSpeedHud();

    const hud = document.createElement('div');
    hud.className = 'caspian-speed-hud';
    hud.setAttribute('role', 'status');
    hud.dataset.position = /^(top|bottom)-(left|center|right)$/.test(config.hudPosition) ? config.hudPosition : 'top-left';
    const safeColor = (value, fallback) => /^#(?:[\da-f]{3}|[\da-f]{6})$/i.test(value) ? value : fallback;
    const dark = hudTheme.mode === 'dark';
    hud.style.setProperty('--hud-accent', safeColor(hudTheme.accent, '#a2a9a9'));
    hud.style.setProperty('--hud-secondary', safeColor(hudTheme.secondary, '#1b4264'));
    hud.style.setProperty('--hud-panel', dark ? safeColor(hudTheme.customBgColor, '#141110') : '#ffffff');
    hud.style.setProperty('--hud-ink', dark ? '#f8fafc' : '#0f172a');
    hud.innerHTML = `
      <span class="caspian-hud-icon" aria-hidden="true"><svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round">
        <path d="M5 8 C6 9 7 9.5 9 9.5 C12 9.5 12 7.5 15 7.5 C17 7.5 18 8 19 9"></path>
        <path d="M5 12 C6 13 7 13.5 9 13.5 C12 13.5 12 11.5 15 11.5 C17 11.5 18 12 19 13"></path>
        <path d="M5 16 C6 17 7 17.5 9 17.5 C12 17.5 12 15.5 15 15.5 C17 15.5 18 16 19 17"></path>
      </svg></span>
      <span class="caspian-hud-reading"><span>Flow Speed</span><strong>${speedVal}×</strong></span>
      <span class="caspian-hud-beats" aria-hidden="true"><i></i><i></i><i></i></span>
    `;

    const fullscreen = document.fullscreenElement;
    (fullscreen && fullscreen.tagName !== 'VIDEO' ? fullscreen : document.documentElement).appendChild(hud);
    if (hud.showPopover) {
      hud.setAttribute('popover', 'manual');
      hud.showPopover();
    }
    currentHud = hud;
    currentHudTimer = setTimeout(clearSpeedHud, 1700);
  }

  // ==========================================================
  // SHADOW DOM PIERCING MEDIA DISCOVERY ENGINE
  // ==========================================================
  const mediaEvents = ['play', 'playing', 'loadedmetadata', 'canplay', 'ratechange', 'seeked'];
  let trackedMedia = new WeakSet();
  const activeMediaList = new Set();
  const rateEnforcingElements = new WeakSet();
  let engineRunning = false;
  let watchdogId = null;
  let observedRoots = new WeakSet();
  let engineController;
  const rootObservers = new Set();

  // Recursively search DOM and all open Shadow Roots for video/audio elements
  function findAllMediaElements(root = document, seenRoots = new Set()) {
    const results = [];
    if (!root || seenRoots.has(root)) return results;
    seenRoots.add(root);

    try {
      if (root.querySelectorAll) {
        // 1. Direct media elements in this root
        const media = root.querySelectorAll('video, audio');
        for (let i = 0; i < media.length; i++) {
          results.push(media[i]);
        }

        // 2. Discover all shadow hosts (like <shreddit-player> on Reddit)
        const hosts = root.querySelectorAll('*');
        for (let i = 0; i < hosts.length; i++) {
          const host = hosts[i];
          if (host.shadowRoot && !seenRoots.has(host.shadowRoot)) {
            observeRoot(host.shadowRoot);
            const shadowMedia = findAllMediaElements(host.shadowRoot, seenRoots);
            for (let j = 0; j < shadowMedia.length; j++) {
              results.push(shadowMedia[j]);
            }
          }
        }
      }
    } catch (e) {}

    return results;
  }

  // Safely enforce target speed without infinite event recursion
  function enforceRate(media, targetRate) {
    if (!media) return;
    const rate = targetRate !== undefined ? targetRate : (config.enabled ? config.speed : 1.0);
    if (Math.abs(media.playbackRate - rate) < 0.01 && Math.abs(media.defaultPlaybackRate - rate) < 0.01) return;
    if (rateEnforcingElements.has(media)) return;

    try {
      rateEnforcingElements.add(media);
      media.playbackRate = rate;
      if (media.defaultPlaybackRate !== undefined) {
        media.defaultPlaybackRate = rate;
      }
    } catch (e) {
    } finally {
      setTimeout(() => rateEnforcingElements.delete(media), 0);
    }
  }

  // Register and bind listeners to a discovered media element
  function registerAndEnforceMedia(media, targetRate) {
    if (!config.enabled) return;
    if (!media || !(media.tagName === 'VIDEO' || media.tagName === 'AUDIO')) return;
    activeMediaList.add(media);

    if (!trackedMedia.has(media)) {
      trackedMedia.add(media);

      const onEnforce = () => {
        if (config.enabled) {
          enforceRate(media, config.speed);
        }
      };

      for (const event of mediaEvents) {
        media.addEventListener(event, onEnforce, { passive: true, signal: engineController.signal });
      }
    }

    enforceRate(media, targetRate);
  }

  // Apply speed across all media (light DOM, shadow DOM, active set)
  function applySpeedToAllMedia(targetSpeed) {
    // 1. Update elements currently in our active tracker
    for (const media of activeMediaList) {
      if (!media.isConnected) {
        activeMediaList.delete(media);
        continue;
      }
      enforceRate(media, targetSpeed);
    }

    // 2. Perform deep shadow-piercing discovery scan
    const allFound = findAllMediaElements(document);
    for (let i = 0; i < allFound.length; i++) {
      registerAndEnforceMedia(allFound[i], targetSpeed);
    }
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
    if (target.isContentEditable || target.getAttribute?.('contenteditable') === 'true') return true;
    if (target.closest && target.closest('input, textarea, [contenteditable="true"]')) return true;
    return false;
  }

  function onShortcut(e) {
    if (isEditingText(e.composedPath()[0])) return;
    const shortcut = fromEvent(e);
    if (![config.shortcutReset, config.shortcutCycle, config.shortcutUp, config.shortcutDown].includes(shortcut)) return;
    e.preventDefault();
    e.stopPropagation();

    // 1. Toggle between 1.0x and previous speed (Default Alt+S)
    if (shortcut === config.shortcutReset) {
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

    // 2. Cycle speeds
    if (shortcut === config.shortcutCycle) {
      cycleToNextSpeed();
      return;
    }

    // 3. Step Up (+0.25x)
    if (shortcut === config.shortcutUp) {
      setSpeed(config.speed + 0.25, true);
      return;
    }

    // 4. Step Down (-0.25x)
    if (shortcut === config.shortcutDown) {
      setSpeed(config.speed - 0.25, true);
      return;
    }
  }

  // ==========================================================
  // CAPTURE-PHASE EVENT LISTENER (PIERCES ANY SHADOW ROOT)
  // When ANY video plays anywhere on the page, catch it immediately
  // ==========================================================
  function onMediaEvent(event) {
    registerAndEnforceMedia(event.composedPath()[0]);
  }

  // Observe a DOM node or ShadowRoot for dynamic media insertions
  function observeRoot(root) {
    if (!engineRunning || !root || observedRoots.has(root)) return;
    observedRoots.add(root);

    try {
      const observer = new MutationObserver((mutations) => {
        for (let i = 0; i < mutations.length; i++) {
          const added = mutations[i].addedNodes;
          for (let j = 0; j < added.length; j++) {
            const node = added[j];
            if (node.nodeType === 1) {
              if (node.tagName === 'VIDEO' || node.tagName === 'AUDIO') {
                registerAndEnforceMedia(node);
              }
              if (node.shadowRoot) {
                observeRoot(node.shadowRoot);
                findAllMediaElements(node.shadowRoot).forEach(media => registerAndEnforceMedia(media));
              }
              if (node.querySelectorAll) {
                const nested = node.querySelectorAll('video, audio');
                for (let k = 0; k < nested.length; k++) {
                  registerAndEnforceMedia(nested[k]);
                }
                const potentialHosts = node.querySelectorAll('*');
                for (let k = 0; k < potentialHosts.length; k++) {
                  if (potentialHosts[k].shadowRoot) {
                    observeRoot(potentialHosts[k].shadowRoot);
                    findAllMediaElements(potentialHosts[k].shadowRoot).forEach(media => registerAndEnforceMedia(media));
                  }
                }
              }
            }
          }
        }
      });

      observer.observe(root, { childList: true, subtree: true });
      rootObservers.add(observer);
    } catch (e) {}
  }

  // Keep only a lightweight watchdog while the feature is actually enabled.
  function startWatchdog() {
    if (watchdogId) return;
    watchdogId = window.setInterval(() => {
      if (!engineRunning || !config.enabled) return;
      for (const media of activeMediaList) {
        if (!media.isConnected) {
          activeMediaList.delete(media);
          continue;
        }
        if (!media.paused && Math.abs(media.playbackRate - config.speed) > 0.01) {
          enforceRate(media, config.speed);
        }
      }
    }, 1200);
  }

  function stopEngine() {
    engineRunning = false;
    engineController.abort();
    clearSpeedHud();
    document.getElementById('caspian-flow-speed-styles')?.remove();
    if (watchdogId) {
      clearInterval(watchdogId);
      watchdogId = null;
    }
    rootObservers.forEach(observer => observer.disconnect());
    rootObservers.clear();
    observedRoots = new WeakSet();
    for (const media of activeMediaList) {
      rateEnforcingElements.delete(media);
      if (media.isConnected) enforceRate(media, 1);
    }
    activeMediaList.clear();
    trackedMedia = new WeakSet();
  }

  // Monitor DOM for dynamically added video/audio elements
  function startEngine() {
    if (!config.enabled || engineRunning) return;
    engineRunning = true;
    engineController = new AbortController();
    const options = { capture: true, signal: engineController.signal };
    window.addEventListener('keydown', onShortcut, options);
    for (const event of mediaEvents) window.addEventListener(event, onMediaEvent, options);
    applySpeedToAllMedia(config.speed);
    observeRoot(document);
    startWatchdog();
  }

  // Initialize
  loadConfig(startEngine);

})();
