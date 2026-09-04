// ==========================================================================
// CASPIAN - RIPPLEFRAME FULL-PAGE SCROLLING SCREENSHOT ENGINE
// ==========================================================================

(function () {
  'use strict';

  // Prevent multiple injections
  if (window.__CASPIAN_RIPPLEFRAME_ACTIVE) return;
  window.__CASPIAN_RIPPLEFRAME_ACTIVE = true;

  // Open / Init IndexedDB for high-capacity image storage
  function openDatabase() {
    return new Promise((resolve, reject) => {
      const req = indexedDB.open('caspian_rippleframe_db', 1);
      req.onupgradeneeded = (e) => {
        const db = e.target.result;
        if (!db.objectStoreNames.contains('captures')) {
          db.createObjectStore('captures', { keyPath: 'id' });
        }
      };
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
  }

  async function saveCaptureToDB(captureData) {
    const db = await openDatabase();
    return new Promise((resolve, reject) => {
      const tx = db.transaction('captures', 'readwrite');
      const store = tx.objectStore('captures');
      store.put({ id: 'latest_capture', ...captureData, timestamp: Date.now() });
      tx.oncomplete = () => resolve();
      tx.onerror = () => reject(tx.error);
    });
  }

  // Create progress HUD overlay
  function createProgressHud() {
    const existing = document.getElementById('caspian-rf-hud');
    if (existing) existing.remove();

    const hud = document.createElement('div');
    hud.id = 'caspian-rf-hud';
    hud.innerHTML = `
      <div style="
        position: fixed !important;
        bottom: 28px !important;
        right: 28px !important;
        z-index: 2147483647 !important;
        background: rgba(15, 23, 42, 0.92) !important;
        backdrop-filter: blur(16px) !important;
        -webkit-backdrop-filter: blur(16px) !important;
        border: 1.5px solid rgba(56, 189, 248, 0.5) !important;
        color: #ffffff !important;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
        padding: 12px 20px !important;
        border-radius: 16px !important;
        box-shadow: 0 12px 36px rgba(0,0,0,0.6), 0 0 24px rgba(56, 189, 248, 0.35) !important;
        display: flex !important;
        align-items: center !important;
        gap: 12px !important;
        min-width: 240px !important;
        animation: caspianFadeIn 0.25s ease forwards !important;
      ">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#38bdf8" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="animation: caspianSpin 1.8s linear infinite;">
          <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
        </svg>
        <div style="display: flex; flex-direction: column; gap: 2px;">
          <span style="font-weight: 700; font-size: 13px; color: #f8fafc; letter-spacing: 0.3px;">RippleFrame Capturing</span>
          <span id="caspian-rf-progress-text" style="font-size: 11px; color: #94a3b8; font-family: monospace;">Scanning page (0%)...</span>
        </div>
      </div>
      <style>
        @keyframes caspianSpin { 100% { transform: rotate(360deg); } }
        @keyframes caspianFadeIn { 0% { opacity: 0; transform: translateY(10px); } 100% { opacity: 1; transform: translateY(0); } }
      </style>
    `;
    document.documentElement.appendChild(hud);
    return hud;
  }

  function updateProgress(percent, text) {
    const el = document.getElementById('caspian-rf-progress-text');
    if (el) el.textContent = text || `${Math.round(percent)}% completed`;
  }

  function removeProgressHud() {
    const hud = document.getElementById('caspian-rf-hud');
    if (hud) hud.remove();
  }

  // Sleep helper
  const sleep = (ms) => new Promise(r => setTimeout(r, ms));

  // Request single tab screenshot from background service worker
  function captureViewport() {
    return new Promise((resolve, reject) => {
      chrome.runtime.sendMessage({ action: 'capture_visible_tab' }, (response) => {
        if (chrome.runtime.lastError) {
          return reject(new Error(chrome.runtime.lastError.message));
        }
        if (response && response.dataUrl) {
          resolve(response.dataUrl);
        } else {
          reject(new Error(response?.error || 'Failed to capture tab viewport'));
        }
      });
    });
  }

  // Load image object from dataURL
  function loadImage(dataUrl) {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = () => reject(new Error('Image decode error'));
      img.src = dataUrl;
    });
  }

  // Hide sticky headers / fixed bars temporarily to prevent ghost repetitions in long screenshots
  function hideStickyElements() {
    const hiddenNodes = [];
    const elements = document.querySelectorAll('*');
    for (let i = 0; i < elements.length; i++) {
      const el = elements[i];
      if (el.id === 'caspian-rf-hud') continue;
      const style = window.getComputedStyle(el);
      if (style.position === 'fixed' || style.position === 'sticky') {
        const prevVisibility = el.style.visibility;
        el.style.visibility = 'hidden';
        hiddenNodes.push({ el, prevVisibility });
      }
    }
    return () => {
      hiddenNodes.forEach(item => {
        item.el.style.visibility = item.prevVisibility;
      });
    };
  }

  // Main capture sequence
  async function startCapture(options = {}) {
    const isFullPage = options.scope !== 'viewport';
    const delay = options.delay || 150;
    const hud = createProgressHud();

    const originalScrollX = window.scrollX || document.documentElement.scrollLeft || 0;
    const originalScrollY = window.scrollY || document.documentElement.scrollTop || 0;

    // Viewport-only capture mode
    if (!isFullPage) {
      updateProgress(50, 'Capturing viewport...');
      try {
        const dataUrl = await captureViewport();
        await saveCaptureToDB({
          dataUrl,
          title: document.title || 'Screen Capture',
          url: window.location.href,
          width: window.innerWidth,
          height: window.innerHeight,
          fullPage: false
        });
        removeProgressHud();
        chrome.runtime.sendMessage({ action: 'open_rippleframe_studio' });
      } catch (err) {
        removeProgressHud();
        alert('❌ RippleFrame Capture Failed: ' + err.message);
      } finally {
        window.__CASPIAN_RIPPLEFRAME_ACTIVE = false;
      }
      return;
    }

    // Full-page scrolling capture mode
    const restoreSticky = hideStickyElements();
    const totalHeight = Math.max(
      document.body.scrollHeight,
      document.documentElement.scrollHeight,
      document.body.offsetHeight,
      document.documentElement.offsetHeight,
      document.body.clientHeight,
      document.documentElement.clientHeight
    );
    const viewportHeight = window.innerHeight;
    const viewportWidth = window.innerWidth;
    const dpr = window.devicePixelRatio || 1;

    const slices = [];
    let currentScroll = 0;

    try {
      window.scrollTo(0, 0);
      await sleep(delay);

      while (currentScroll < totalHeight) {
        window.scrollTo(0, currentScroll);
        await sleep(delay);

        const actualScrollY = window.scrollY || document.documentElement.scrollTop || currentScroll;
        const percent = Math.min(95, (actualScrollY / (totalHeight - viewportHeight || 1)) * 100);
        updateProgress(percent, `Capturing section (${Math.round(percent)}%)...`);

        const dataUrl = await captureViewport();
        slices.push({
          dataUrl,
          scrollY: actualScrollY
        });

        // If remaining height is smaller than viewport, advance exactly to the bottom
        if (currentScroll + viewportHeight >= totalHeight) {
          break;
        }

        currentScroll += viewportHeight - 20; // 20px overlap for seamless registration
        if (currentScroll + viewportHeight > totalHeight) {
          currentScroll = totalHeight - viewportHeight;
        }
      }

      updateProgress(98, 'Stitching ultra-res canvas...');
      restoreSticky();

      // Stitch slices together onto single high-res canvas
      const canvas = document.createElement('canvas');
      canvas.width = viewportWidth * dpr;
      canvas.height = totalHeight * dpr;
      const ctx = canvas.getContext('2d');

      for (let i = 0; i < slices.length; i++) {
        const slice = slices[i];
        const img = await loadImage(slice.dataUrl);
        const destY = slice.scrollY * dpr;
        ctx.drawImage(img, 0, destY);
      }

      // Convert to high-quality PNG data URL
      const finalDataUrl = canvas.toDataURL('image/png');

      await saveCaptureToDB({
        dataUrl: finalDataUrl,
        title: document.title || 'Full Page Capture',
        url: window.location.href,
        width: canvas.width,
        height: canvas.height,
        fullPage: true
      });

      removeProgressHud();
      // Restore original scroll
      window.scrollTo(originalScrollX, originalScrollY);

      // Launch RippleFrame Studio tab
      chrome.runtime.sendMessage({ action: 'open_rippleframe_studio' });
    } catch (err) {
      restoreSticky();
      removeProgressHud();
      window.scrollTo(originalScrollX, originalScrollY);
      console.error('[RippleFrame Error]', err);
      alert('❌ RippleFrame Capture Failed: ' + err.message);
    } finally {
      window.__CASPIAN_RIPPLEFRAME_ACTIVE = false;
    }
  }

  // Listen for trigger command from popup
  chrome.runtime.onMessage.addListener((req, sender, sendResponse) => {
    if (req.action === 'start_rippleframe_capture') {
      startCapture(req.options || {});
      sendResponse({ status: 'started' });
    }
  });

})();
