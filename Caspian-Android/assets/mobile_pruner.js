// ======================================================
// CASPIAN ANDROID - MOBILE DOM PRUNER (ChatGPT + Gemini Shadow DOM)
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

  // Track active input/textarea typing to freeze pruning during typing
  document.addEventListener('keydown', () => {
    isTyping = true;
    if (typingTimer) clearTimeout(typingTimer);
    typingTimer = setTimeout(() => { isTyping = false; }, 1000);
  }, { passive: true });

  document.addEventListener('input', () => {
    isTyping = true;
    if (typingTimer) clearTimeout(typingTimer);
    typingTimer = setTimeout(() => { isTyping = false; }, 1000);
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

  // Recursive Shadow DOM Selector (Pierces through Google Gemini Web Components)
  function queryShadowSelectorAll(selector, root) {
    root = root || document;
    let elements = [];
    try {
      elements = Array.from(root.querySelectorAll(selector));
      const shadowRoots = Array.from(root.querySelectorAll('*'))
        .map(el => el.shadowRoot)
        .filter(Boolean);
      for (const shadowRoot of shadowRoots) {
        elements = elements.concat(queryShadowSelectorAll(selector, shadowRoot));
      }
    } catch(e) {}
    return elements;
  }

  function getTopLevelTurns() {
    const turnSelector = '[data-testid^="conversation-turn"], article, user-query, model-response, chat-turn';
    const rawTurns = queryShadowSelectorAll(turnSelector, document);
    return rawTurns.filter(turn => {
      return !rawTurns.some(other => other !== turn && other.contains(turn));
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

  // 250ms Debounced Observer for zero lag during message generation
  let debounceTimer = null;
  function debouncedApplyPruning() {
    if (isTyping) return;
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(applyPruning, 250);
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
