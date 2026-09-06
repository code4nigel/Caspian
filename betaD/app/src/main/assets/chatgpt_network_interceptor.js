/**
 * Caspian ChatGPT High-Performance Network Stream Interceptor (v1.0.0)
 * Intercepts ChatGPT conversation API responses to prevent React VDOM overload.
 * Trims heavy historical message trees at the network boundary.
 */
(function() {
  'use strict';

  if (window.__CASPIAN_INTERCEPTOR_INITIALIZED) return;
  window.__CASPIAN_INTERCEPTOR_INITIALIZED = true;

  const MAX_ACTIVE_TURNS = 12; // Number of latest conversation turns to feed to React

  // Intercept window.fetch
  const originalFetch = window.fetch;
  window.__CASPIAN_NATIVE_FETCH = originalFetch;
  window.__CASPIAN_RAW_CONVERSATION_CACHE = window.__CASPIAN_RAW_CONVERSATION_CACHE || {};

  window.fetch = async function(...args) {
    const url = typeof args[0] === 'string' ? args[0] : (args[0] && args[0].url ? args[0].url : '');

    // Allow Caspian Export to bypass network truncation completely
    const options = args[1] || {};
    const headers = options.headers || {};
    let isExportBypass = false;
    if (headers) {
      if (typeof headers.get === 'function') {
        isExportBypass = !!headers.get('X-Caspian-Export');
      } else if (typeof headers === 'object') {
        isExportBypass = !!headers['X-Caspian-Export'] || !!headers['x-caspian-export'];
      }
    }
    if (isExportBypass) {
      return originalFetch.apply(this, args);
    }

    // Check if this is a ChatGPT conversation detail request
    if (url.includes('/backend-api/conversation/') && !url.includes('/backend-api/conversations')) {
      try {
        const response = await originalFetch.apply(this, args);
        if (!response.ok) return response;

        const clone = response.clone();
        const data = await clone.json();

        if (data && data.mapping && data.current_node) {
          // Cache the full, unpruned raw conversation in memory for Caspian Export
          try {
            const match = url.match(/\/backend-api\/conversation\/([a-f0-9-]+)/i);
            if (match && match[1]) {
              window.__CASPIAN_RAW_CONVERSATION_CACHE[match[1]] = data;
            }
          } catch (cacheErr) {}

          const optimizedData = optimizeConversationGraph(data, MAX_ACTIVE_TURNS);
          const optimizedBlob = new Blob([JSON.stringify(optimizedData)], { type: 'application/json' });
          return new Response(optimizedBlob, {
            status: response.status,
            statusText: response.statusText,
            headers: response.headers
          });
        }
        return response;
      } catch (err) {
        console.warn('Caspian Network Interceptor fallback:', err);
        return originalFetch.apply(this, args);
      }
    }

    return originalFetch.apply(this, args);
  };

  // Optimize ChatGPT's node graph to keep only the active linear chain of length N
  function optimizeConversationGraph(convData, maxTurns) {
    try {
      const mapping = convData.mapping;
      let currentNodeId = convData.current_node;
      if (!mapping || !currentNodeId) return convData;

      // 1. Build linear active chain from current_node up to root
      const activeChain = [];
      let cursor = currentNodeId;
      const visited = new Set();

      while (cursor && mapping[cursor] && !visited.has(cursor)) {
        visited.add(cursor);
        activeChain.unshift(cursor);
        cursor = mapping[cursor].parent;
      }

      // If conversation is already short, keep untouched
      if (activeChain.length <= maxTurns) {
        return convData;
      }

      // 2. Keep the root node and the latest `maxTurns` nodes
      const rootId = activeChain[0];
      const keptNodes = activeChain.slice(-maxTurns);
      const firstKeptNodeId = keptNodes[0];

      const newMapping = {};

      // Preserve root node structure
      if (mapping[rootId]) {
        newMapping[rootId] = {
          ...mapping[rootId],
          children: [firstKeptNodeId]
        };
      }

      // Add kept nodes with proper parent linkage
      for (let i = 0; i < keptNodes.length; i++) {
        const id = keptNodes[i];
        const node = { ...mapping[id] };
        if (i === 0) {
          node.parent = rootId; // Link first kept node directly to root
        }
        newMapping[id] = node;
      }

      return {
        ...convData,
        mapping: newMapping,
        current_node: convData.current_node
      };
    } catch (e) {
      console.error('Error optimizing conversation graph:', e);
      return convData;
    }
  }
})();
