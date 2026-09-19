// ==========================================================================
// CASPIAN - RIPPLEFRAME CONTENT PAGE HELPER (GoFullPage Engine)
// ==========================================================================

(function () {
  'use strict';

  let originalScrollX = 0;
  let originalScrollY = 0;
  let hiddenFixedNodes = [];
  let scrollbarStyleTag = null;

  // 1. Hide Scrollbars during capture
  function suppressScrollbars() {
    if (!scrollbarStyleTag) {
      scrollbarStyleTag = document.createElement('style');
      scrollbarStyleTag.id = 'caspian-rf-hide-scrollbars';
      scrollbarStyleTag.textContent = `
        html, body {
          scrollbar-width: none !important;
          -ms-overflow-style: none !important;
        }
        ::-webkit-scrollbar {
          display: none !important;
          width: 0 !important;
          height: 0 !important;
        }
      `;
      (document.head || document.documentElement).appendChild(scrollbarStyleTag);
    }
  }

  function restoreScrollbars() {
    if (scrollbarStyleTag) {
      scrollbarStyleTag.remove();
      scrollbarStyleTag = null;
    }
  }

  // 2. Freeze sticky/fixed headers after first slice
  function hideStickyElements() {
    hiddenFixedNodes = [];
    const elements = document.querySelectorAll('*');
    for (let i = 0; i < elements.length; i++) {
      const el = elements[i];
      if (el.id === 'caspian-rf-hud') continue;
      const style = window.getComputedStyle(el);
      if (style.position === 'fixed' || style.position === 'sticky') {
        hiddenFixedNodes.push({ el, prev: el.style.visibility });
        el.style.visibility = 'hidden';
      }
    }
  }

  function restoreStickyElements() {
    hiddenFixedNodes.forEach(item => {
      item.el.style.visibility = item.prev;
    });
    hiddenFixedNodes = [];
  }

  // 3. Accurate Document Dimensions
  function getPageMetrics() {
    const doc = document.documentElement;
    const body = document.body;

    const totalHeight = Math.max(
      doc ? doc.scrollHeight : 0,
      doc ? doc.offsetHeight : 0,
      doc ? doc.clientHeight : 0,
      body ? body.scrollHeight : 0,
      body ? body.offsetHeight : 0,
      body ? body.clientHeight : 0,
      window.innerHeight || 0
    );

    const totalWidth = Math.max(
      doc ? doc.scrollWidth : 0,
      doc ? doc.offsetWidth : 0,
      doc ? doc.clientWidth : 0,
      body ? body.scrollWidth : 0,
      body ? body.offsetWidth : 0,
      body ? body.clientWidth : 0,
      window.innerWidth || 0
    );

    return {
      totalHeight,
      totalWidth,
      viewportHeight: window.innerHeight,
      viewportWidth: window.innerWidth,
      dpr: window.devicePixelRatio || 1,
      title: document.title || 'Screen Capture',
      url: window.location.href
    };
  }

  // 4. Message Listener
  chrome.runtime.onMessage.addListener((req, sender, sendResponse) => {
    if (req.action === 'rf_prepare_capture') {
      originalScrollX = window.scrollX || (document.documentElement ? document.documentElement.scrollLeft : 0) || 0;
      originalScrollY = window.scrollY || (document.documentElement ? document.documentElement.scrollTop : 0) || 0;

      suppressScrollbars();

      // Scroll to top
      window.scrollTo(0, 0);
      if (document.scrollingElement) document.scrollingElement.scrollTop = 0;

      const metrics = getPageMetrics();
      console.log(`[RippleFrame Content] Prepared capture metrics: Total Height = ${metrics.totalHeight}px, Viewport = ${metrics.viewportWidth}x${metrics.viewportHeight}px, DPR = ${metrics.dpr}`);
      sendResponse(metrics);
      return true;
    }

    if (req.action === 'rf_scroll_to') {
      // Hide sticky elements on subsequent slices (after top slice is captured)
      if (req.sliceIndex > 0) {
        hideStickyElements();
      }

      window.scrollTo({ top: req.y, left: 0, behavior: 'instant' });
      if (document.scrollingElement) document.scrollingElement.scrollTop = req.y;
      if (document.documentElement) document.documentElement.scrollTop = req.y;
      if (document.body) document.body.scrollTop = req.y;

      const actualY = window.scrollY || (document.scrollingElement ? document.scrollingElement.scrollTop : 0) || (document.documentElement ? document.documentElement.scrollTop : 0) || req.y;
      const metrics = getPageMetrics();
      console.log(`[RippleFrame Content] Scroll command target = ${req.y}px (actual window.scrollY = ${actualY}px, totalHeight = ${metrics.totalHeight}px)`);

      sendResponse({
        scrolled: true,
        actualY,
        totalHeight: metrics.totalHeight
      });
      return true;
    }

    if (req.action === 'rf_restore_page') {
      restoreStickyElements();
      restoreScrollbars();
      window.scrollTo(originalScrollX, originalScrollY);
      if (document.scrollingElement) document.scrollingElement.scrollTop = originalScrollY;
      console.log(`[RippleFrame Content] Restored original scroll position (${originalScrollX}, ${originalScrollY})`);
      sendResponse({ restored: true });
      return true;
    }
  });

})();
