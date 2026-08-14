// ======================================================
// CASPIAN ANDROID - MOBILE DOM PRUNER (Ultra-Fast Zero-Lag Architecture)
// ======================================================

(function() {
  if (window.__CASPIAN_MOBILE_PRUNER_LOADED__) return;
  window.__CASPIAN_MOBILE_PRUNER_LOADED__ = true;

  let state = {
    globalActive: true,
    pruningEnabled: true,
    vaultEnabled: true,
    limit: 5
  };

  let isTyping = false;
  let typingTimer = null;
  let isPruningScheduled = false;

  // Track active input/textarea typing to freeze pruning during typing
  document.addEventListener('keydown', () => {
    isTyping = true;
    if (typingTimer) clearTimeout(typingTimer);
    typingTimer = setTimeout(() => { isTyping = false; }, 1200);
  }, { passive: true });

  document.addEventListener('input', () => {
    isTyping = true;
    if (typingTimer) clearTimeout(typingTimer);
    typingTimer = setTimeout(() => { isTyping = false; }, 1200);
  }, { passive: true });

  function loadState() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
        const jsonStr = window.CaspianBridge.getSettings();
        if (jsonStr) {
          const parsed = JSON.parse(jsonStr);
          state = { ...state, ...parsed };

          // Map Caspian mobile control settings keys
          if (parsed.chat_limit_enabled !== undefined) {
            state.pruningEnabled = (parsed.chat_limit_enabled === true || parsed.chat_limit_enabled === 'true');
          }
          if (parsed.chat_message_limit !== undefined) {
            const lim = parseInt(parsed.chat_message_limit, 10);
            state.limit = isNaN(lim) ? 5 : lim;
          }
        }
      }
    } catch(e) {}
  }

  // Fast-Path Direct Selector (0.05ms execution vs 50ms recursive scan)
  function getTopLevelTurns() {
    const host = (location && location.hostname) ? location.hostname.toLowerCase() : '';
    const isGemini = host.includes('gemini.google.com');

    if (!isGemini) {
      // ChatGPT & general web: Direct DOM query without touching shadow roots
      const rawTurns = document.querySelectorAll('[data-testid^="conversation-turn"], article');
      if (rawTurns.length > 0) return Array.from(rawTurns);
      return [];
    }

    // Gemini-specific targeted custom elements
    let elements = Array.from(document.querySelectorAll('user-query, model-response, chat-turn, article'));
    try {
      const customHosts = document.querySelectorAll('user-query, model-response, chat-turn, gds-theme-provider');
      for (let i = 0; i < customHosts.length; i++) {
        const sr = customHosts[i].shadowRoot;
        if (sr) {
          elements = elements.concat(Array.from(sr.querySelectorAll('user-query, model-response, chat-turn, article')));
        }
      }
    } catch(e) {}

    return elements.filter((turn, i, arr) => {
      return !arr.some(other => other !== turn && other.contains(turn));
    });
  }

  function applyPruning() {
    if (isTyping) return; // Do not manipulate DOM during active typing
    loadState();
    
    const turns = getTopLevelTurns();

    if (!state.globalActive || !state.pruningEnabled || state.limit >= 9999) {
      turns.forEach(t => t.style.removeProperty('display'));
      return;
    }

    const limit = state.limit || 5;
    turns.forEach((t, idx) => {
      if (idx < turns.length - limit) {
        t.style.setProperty('display', 'none', 'important');
      } else {
        t.style.removeProperty('display');
      }
    });
  }

  // Idle-Callback Scheduled Pruning: Never blocks UI thread touch events or sheet switches
  let debounceTimer = null;
  function debouncedApplyPruning() {
    if (isTyping) return;
    if (debounceTimer) clearTimeout(debounceTimer);
    
    debounceTimer = setTimeout(() => {
      if (isPruningScheduled) return;
      isPruningScheduled = true;
      
      const scheduleIdle = window.requestIdleCallback || ((cb) => setTimeout(cb, 50));
      scheduleIdle(() => {
        isPruningScheduled = false;
        applyPruning();
      }, { timeout: 800 });
    }, 350);
  }

  function startObserver() {
    const targetNode = document.documentElement || document.body;
    if (!targetNode) {
      setTimeout(startObserver, 100);
      return;
    }
    const observer = new MutationObserver(() => {
      debouncedApplyPruning();
    });
    observer.observe(targetNode, { childList: true, subtree: true });
  }

  window.addEventListener('message', (e) => {
    if (e.data && e.data.type === 'CASPIAN_SYNC_SETTINGS') {
      state = { ...state, ...e.data.payload };
      applyPruning();
    }
  });

  startObserver();
  applyPruning();
})();
