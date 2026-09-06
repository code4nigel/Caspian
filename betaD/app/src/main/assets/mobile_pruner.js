/**
 * Caspian Universal Mobile DOM Pruner (v1.1.4-BetaC)
 * Ultra-Smooth Viewport-Synchronized Sliding Window & Tail Window Pruner.
 * Native Chromium CSS content-visibility with automatic scroll tracking.
 */
(function() {
  'use strict';

  if (window.__CASPIAN_PRUNER_INITIALIZED) {
    if (typeof window.__CASPIAN_PRUNER_UPDATE === 'function') {
      window.__CASPIAN_PRUNER_UPDATE();
    }
    return;
  }
  window.__CASPIAN_PRUNER_INITIALIZED = true;

  let state = {
    globalActive: true,
    pruningEnabled: true,
    limit: 6,
    mode: 'sliding_window' // 'sliding_window' | 'tail'
  };

  let isTyping = false;
  let isScrolling = false;
  let scrollDebounceTimer = null;
  let currentWindowStart = -1;
  let currentStepTurnIndex = -1;
  let lastKnownTurnsCount = 0;
  let isPruningScheduled = false;
  let intersectionObserver = null;

  function ensurePrunerStyles() {
    if (document.getElementById('caspian-pruner-styles')) return;
    const style = document.createElement('style');
    style.id = 'caspian-pruner-styles';
    style.textContent = `
      .caspian-turn-pruned {
        content-visibility: hidden !important;
        contain: layout style paint !important;
        contain-intrinsic-size: 0 120px !important;
        opacity: 0 !important;
        pointer-events: none !important;
        user-select: none !important;
      }
      .caspian-turn-active {
        content-visibility: auto !important;
        contain-intrinsic-size: 0 250px;
      }
    `;
    const target = document.head || document.documentElement;
    if (target) target.appendChild(style);
  }

  function loadState() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
        const jsonStr = window.CaspianBridge.getSettings();
        if (jsonStr) {
          const parsed = JSON.parse(jsonStr);
          if (parsed.chat_limit_enabled !== undefined) {
            state.pruningEnabled = (parsed.chat_limit_enabled === true || parsed.chat_limit_enabled === 'true');
          }
          if (parsed.chat_message_limit !== undefined) {
            const lim = parseInt(parsed.chat_message_limit, 10);
            state.limit = isNaN(lim) ? 6 : lim;
          }
          if (parsed.chat_pruning_mode !== undefined) {
            state.mode = parsed.chat_pruning_mode;
          }
        }
      }
    } catch(e) {}
  }

  window.__CASPIAN_PRUNER_UPDATE = function(limit, mode, enabled) {
    if (limit !== undefined) state.limit = parseInt(limit, 10) || 6;
    if (mode !== undefined) state.mode = mode;
    if (enabled !== undefined) state.pruningEnabled = enabled;
    currentWindowStart = -1;
    currentStepTurnIndex = -1;
    schedulePruning(true);
  };

  function scrollToTurn(turnElement, blockPosition) {
    if (!turnElement) return;
    try {
      turnElement.scrollIntoView({
        behavior: 'smooth',
        block: blockPosition || 'start',
        inline: 'nearest'
      });
    } catch (e) {
      try {
        turnElement.scrollIntoView(true);
      } catch (ignored) {}
    }
  }

  window.__CASPIAN_PRUNER_STEP = function(direction) {
    const turns = getTopLevelTurns();
    if (turns.length === 0) return;
    const limit = Math.max(1, state.limit || 6);

    if (currentStepTurnIndex < 0 || currentStepTurnIndex >= turns.length) {
      currentStepTurnIndex = turns.length - 1;
    }

    if (direction < 0) {
      currentStepTurnIndex = Math.max(0, currentStepTurnIndex - 1);
    } else {
      currentStepTurnIndex = Math.min(turns.length - 1, currentStepTurnIndex + 1);
    }

    if (!state.globalActive || !state.pruningEnabled || state.limit >= 9999 || state.mode !== 'sliding_window') {
      const targetTurn = turns[currentStepTurnIndex];
      if (targetTurn) scrollToTurn(targetTurn, direction < 0 ? 'start' : 'nearest');
      return;
    }

    // Center sliding window around currentStepTurnIndex
    currentWindowStart = Math.max(0, Math.min(turns.length - limit, currentStepTurnIndex - Math.floor(limit / 2)));
    applyPruningDirect(turns);

    const targetTurn = turns[currentStepTurnIndex];
    if (targetTurn) {
      setTimeout(() => {
        scrollToTurn(targetTurn, direction < 0 ? 'start' : 'nearest');
      }, 30);
    }
  };

  window.__CASPIAN_PRUNER_JUMP = function(target) {
    const turns = getTopLevelTurns();
    if (turns.length === 0) return;
    const limit = Math.max(1, state.limit || 6);

    if (target === 'top') {
      currentStepTurnIndex = 0;
      currentWindowStart = 0;
    } else {
      currentStepTurnIndex = turns.length - 1;
      currentWindowStart = Math.max(0, turns.length - limit);
    }

    applyPruningDirect(turns);

    const targetTurn = turns[currentStepTurnIndex];
    if (targetTurn) {
      setTimeout(() => {
        scrollToTurn(targetTurn, target === 'top' ? 'start' : 'end');
      }, 40);
    }
  };

  // Linear O(N) Turn Discovery
  function getTopLevelTurns() {
    const host = (location && location.hostname) ? location.hostname.toLowerCase() : '';
    const isGemini = host.includes('gemini.google.com');

    if (!isGemini) {
      // 1. ChatGPT primary selector: conversation turns
      let turns = Array.from(document.querySelectorAll('[data-testid^="conversation-turn"]'));
      if (turns.length > 0) return turns;

      // 2. Standard article tag fallback
      turns = Array.from(document.querySelectorAll('main article, article'));
      if (turns.length > 0) return turns;

      // 3. Message role container fallback
      turns = Array.from(document.querySelectorAll('[data-message-author-role]'));
      if (turns.length > 0) return turns;

      // 4. Generic turn container fallback
      turns = Array.from(document.querySelectorAll('div[class*="group/conversation-turn"]'));
      if (turns.length > 0) return turns;

      return [];
    }

    // Gemini
    let elements = Array.from(document.querySelectorAll('user-query, model-response, chat-turn'));
    try {
      const customHosts = document.querySelectorAll('user-query, model-response, chat-turn, gds-theme-provider');
      for (let i = 0; i < customHosts.length; i++) {
        const sr = customHosts[i].shadowRoot;
        if (sr) {
          elements = elements.concat(Array.from(sr.querySelectorAll('user-query, model-response, chat-turn')));
        }
      }
    } catch(e) {}

    return elements;
  }

  function clearAllPruning(turns) {
    for (let i = 0; i < turns.length; i++) {
      const t = turns[i];
      t.classList.remove('caspian-turn-pruned');
      t.classList.remove('caspian-turn-active');
      t.style.removeProperty('display');
    }
  }

  // Find index of the turn currently closest to viewport center
  function findVisibleCenterTurnIndex(turns) {
    if (!turns || turns.length === 0) return -1;
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight || 800;
    const centerY = viewportHeight / 2;

    let closestIdx = -1;
    let minDistance = Infinity;

    for (let i = 0; i < turns.length; i++) {
      const rect = turns[i].getBoundingClientRect();
      // If turn is in or near the viewport
      if (rect.bottom >= 0 && rect.top <= viewportHeight) {
        const turnCenter = (rect.top + rect.bottom) / 2;
        const dist = Math.abs(turnCenter - centerY);
        if (dist < minDistance) {
          minDistance = dist;
          closestIdx = i;
        }
      }
    }
    return closestIdx;
  }

  function applyPruningDirect(turns) {
    ensurePrunerStyles();
    loadState();

    if (!turns || turns.length === 0) return;

    if (turns.length !== lastKnownTurnsCount) {
      if (currentStepTurnIndex === lastKnownTurnsCount - 1 || currentStepTurnIndex < 0) {
        currentStepTurnIndex = turns.length - 1;
      }
      lastKnownTurnsCount = turns.length;
    }

    if (!state.globalActive || !state.pruningEnabled || state.limit >= 9999) {
      clearAllPruning(turns);
      return;
    }

    const limit = Math.max(1, state.limit || 6);

    if (turns.length <= limit) {
      clearAllPruning(turns);
      return;
    }

    let startIdx = 0;
    let endIdx = turns.length - 1;

    if (state.mode === 'tail') {
      startIdx = turns.length - limit;
      endIdx = turns.length - 1;
    } else {
      if (currentWindowStart < 0 || currentWindowStart > turns.length - limit) {
        currentWindowStart = Math.max(0, turns.length - limit);
      }
      startIdx = currentWindowStart;
      endIdx = Math.min(turns.length - 1, currentWindowStart + limit - 1);
    }

    // Fast Batch CSS Class Switching
    for (let i = 0; i < turns.length; i++) {
      const t = turns[i];
      const isVisible = (i >= startIdx && i <= endIdx);
      if (isVisible) {
        if (t.classList.contains('caspian-turn-pruned')) {
          t.classList.remove('caspian-turn-pruned');
          t.classList.add('caspian-turn-active');
        }
      } else {
        if (!t.classList.contains('caspian-turn-pruned')) {
          t.classList.add('caspian-turn-pruned');
          t.classList.remove('caspian-turn-active');
        }
      }
    }
  }

  function schedulePruning(immediate) {
    if (isTyping) return;
    if (isPruningScheduled && !immediate) return;

    isPruningScheduled = true;
    const runner = () => {
      isPruningScheduled = false;
      if (isTyping) return;
      const turns = getTopLevelTurns();
      applyPruningDirect(turns);
    };

    if (window.requestIdleCallback && !immediate) {
      window.requestIdleCallback(runner, { timeout: 250 });
    } else {
      window.requestAnimationFrame(runner);
    }
  }

  // Capture-phase Scroll Listener: Dynamically tracks manual scrolling across any nested container
  function onScrollHandler() {
    if (isTyping) return;
    isScrolling = true;
    if (scrollDebounceTimer) clearTimeout(scrollDebounceTimer);

    scrollDebounceTimer = setTimeout(() => {
      isScrolling = false;
      if (state.mode === 'sliding_window' && state.pruningEnabled && state.globalActive) {
        const turns = getTopLevelTurns();
        const visibleIdx = findVisibleCenterTurnIndex(turns);
        if (visibleIdx >= 0) {
          const limit = Math.max(1, state.limit || 6);
          currentStepTurnIndex = visibleIdx;
          currentWindowStart = Math.max(0, Math.min(turns.length - limit, visibleIdx - Math.floor(limit / 2)));
          applyPruningDirect(turns);
        }
      }
    }, 200);
  }

  window.addEventListener('scroll', onScrollHandler, { capture: true, passive: true });

  // Debounced MutationObserver for newly arriving messages
  let mutationDebounceTimer = null;
  let observer = new MutationObserver(() => {
    if (isTyping || isScrolling) return;
    if (!mutationDebounceTimer) {
      mutationDebounceTimer = setTimeout(() => {
        mutationDebounceTimer = null;
        schedulePruning(false);
      }, 300);
    }
  });

  function initObserver() {
    ensurePrunerStyles();
    const target = document.body || document.documentElement;
    if (target) {
      observer.observe(target, { childList: true, subtree: true });
    }
  }

  document.addEventListener('focusin', (e) => {
    const tag = e.target && e.target.tagName ? e.target.tagName.toLowerCase() : '';
    if (tag === 'textarea' || tag === 'input' || (e.target && e.target.isContentEditable)) {
      isTyping = true;
    }
  });

  document.addEventListener('focusout', () => {
    isTyping = false;
    setTimeout(() => schedulePruning(false), 300);
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initObserver();
      schedulePruning(true);
    });
  } else {
    initObserver();
    schedulePruning(true);
  }
})();
