// ======================================================
// CASPIAN ANDROID - MOBILE DOM PRUNER (ChatGPT + Gemini)
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

  function applyPruning() {
    loadState();
    
    // Support ChatGPT & Google Gemini DOM turn selectors
    const turns = document.querySelectorAll(
      '[data-testid^="conversation-turn"], article, main div.group, user-query, model-response, div.conversation-container > div, .query-content, .model-response-text'
    );

    if (!state.globalActive || !state.pruningEnabled || state.limit >= 9999) {
      turns.forEach(t => t.style.setProperty('display', 'block', 'important'));
      return;
    }

    const limit = state.limit || 5;
    turns.forEach((t, idx) => {
      if (idx < turns.length - limit) {
        t.style.setProperty('display', 'none', 'important');
      } else {
        t.style.setProperty('display', 'block', 'important');
      }
    });
  }

  // Live Observer for DOM changes
  const observer = new MutationObserver(() => {
    applyPruning();
  });
  observer.observe(document.body, { childList: true, subtree: true });

  window.addEventListener('message', (e) => {
    if (e.data && e.data.type === 'CASPIAN_SYNC_SETTINGS') {
      state = { ...state, ...e.data.payload };
      applyPruning();
    }
  });

  applyPruning();
})();
