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

  function loadState() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
        const jsonStr = window.CaspianBridge.getSettings();
        if (jsonStr) {
          const parsed = JSON.parse(jsonStr);
          state = { ...state, ...parsed };
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
    // Only target top-level turn containers (ChatGPT & Gemini)
    const turnSelector = '[data-testid^="conversation-turn"], article, user-query, model-response, chat-turn';
    const rawTurns = queryShadowSelectorAll(turnSelector, document);
    
    // Filter out nested turns so each turn is strictly counted once
    return rawTurns.filter(turn => {
      return !rawTurns.some(other => other !== turn && other.contains(turn));
    });
  }

  function applyPruning() {
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

  // Safe Observer initialization for dynamic Gemini & ChatGPT DOMs
  function startObserver() {
    const targetNode = document.documentElement || document.body;
    if (!targetNode) {
      setTimeout(startObserver, 100);
      return;
    }
    const observer = new MutationObserver(() => {
      applyPruning();
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
