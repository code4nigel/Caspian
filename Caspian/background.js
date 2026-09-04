// Dynamic Extension Icon Speed Badge Manager
function updateSpeedBadge() {
  chrome.storage.local.get(['flow_speed_enabled', 'flow_speed_badge_enabled', 'flow_speed_val', 'accent'], (data) => {
    const enabled = data.flow_speed_enabled ?? true;
    const badgeEnabled = data.flow_speed_badge_enabled ?? true;
    const speed = parseFloat(data.flow_speed_val) || 1.0;

    if (!enabled || !badgeEnabled) {
      chrome.action.setBadgeText({ text: '' });
      return;
    }

    const badgeText = speed % 1 === 0 ? `${speed.toFixed(1)}` : `${parseFloat(speed.toFixed(2))}`;
    chrome.action.setBadgeText({ text: badgeText });
    chrome.action.setBadgeBackgroundColor({ color: '#0284c7' });
    if (chrome.action.setBadgeTextColor) {
      chrome.action.setBadgeTextColor({ color: '#ffffff' });
    }
  });
}

// Initial setup and startup listeners
chrome.runtime.onInstalled.addListener(updateSpeedBadge);
chrome.runtime.onStartup.addListener(updateSpeedBadge);
updateSpeedBadge();

chrome.storage.onChanged.addListener((changes, area) => {
  if (area === 'local' && (changes.flow_speed_enabled || changes.flow_speed_badge_enabled || changes.flow_speed_val || changes.accent)) {
    updateSpeedBadge();
  }
});

chrome.commands.onCommand.addListener((command) => {
  if (command === "toggle-feature") {
    chrome.storage.local.get(['pruningEnabled', 'vaultEnabled', 'enabled', 'yt_feed_limit_enabled', 'flow_speed_enabled', 'rf_enabled'], (data) => {
      const isCurrentlyOn = (data.pruningEnabled ?? true) || 
                            (data.vaultEnabled ?? true) || 
                            (data.yt_feed_limit_enabled ?? true) || 
                            (data.flow_speed_enabled ?? true) || 
                            (data.rf_enabled ?? true);
      const next = !isCurrentlyOn;
      chrome.storage.local.set({
        pruningEnabled: next,
        vaultEnabled: next,
        enabled: next,
        yt_feed_limit_enabled: next,
        flow_speed_enabled: next,
        rf_enabled: next
      });
    });
  }
  if (command === "reset-colors") {
    chrome.storage.local.set({
      mode: 'light',
      accent: '#A2A9A9',
      secondary: '#1B4264',
      limit: 5
    });
  }
});

// IndexedDB Storage Helper for High-Res PNG Blobs
function saveCaptureToDB(captureData) {
  return new Promise((resolve) => {
    try {
      const req = indexedDB.open('caspian_rippleframe_db', 1);
      req.onupgradeneeded = (e) => {
        const db = e.target.result;
        if (!db.objectStoreNames.contains('captures')) {
          db.createObjectStore('captures', { keyPath: 'id' });
        }
      };
      req.onsuccess = () => {
        const db = req.result;
        const tx = db.transaction('captures', 'readwrite');
        const store = tx.objectStore('captures');
        store.put({ id: 'latest_capture', ...captureData });
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      };
      req.onerror = () => resolve(false);
    } catch (e) {
      resolve(false);
    }
  });
}

// Robust Tab Capture Helper with Quota Rate Limit Backoff
async function captureTabWithQuotaRetry(windowId, maxRetries = 5) {
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const dataUrl = await new Promise((resolve, reject) => {
        chrome.tabs.captureVisibleTab(windowId, { format: 'png' }, (resUrl) => {
          if (chrome.runtime.lastError) {
            return reject(new Error(chrome.runtime.lastError.message));
          }
          if (!resUrl) {
            return reject(new Error('captureVisibleTab returned empty url'));
          }
          resolve(resUrl);
        });
      });
      return dataUrl;
    } catch (err) {
      console.warn(`[RippleFrame] Capture attempt ${attempt + 1} notice: ${err.message}`);
      // Wait for Chromium quota window to clear (minimum 600ms)
      await new Promise(r => setTimeout(r, 650));
      if (attempt === maxRetries - 1) throw err;
    }
  }
  throw new Error('Failed to capture tab after retries');
}

// Progressive RippleFrame Capture Controller (GoFullPage Engine)
async function performRippleFrameCapture(tabId, options = {}) {
  const isFullPage = options.scope !== 'viewport';
  // Minimum 550ms to strictly respect Chromium's MAX_CAPTURE_VISIBLE_TAB_CALLS_PER_SECOND quota
  const delay = Math.max(550, options.delay || 550);

  try {
    // 1. Inject content script helper into active tab
    await chrome.scripting.executeScript({
      target: { tabId },
      files: ['rippleframe_capture.js']
    });

    const tab = await chrome.tabs.get(tabId);
    
    // 2. Prepare tab and retrieve metrics
    const metrics = await new Promise((resolve, reject) => {
      chrome.tabs.sendMessage(tabId, { action: 'rf_prepare_capture', isFullPage }, (res) => {
        if (chrome.runtime.lastError) {
          return reject(new Error(chrome.runtime.lastError.message));
        }
        resolve(res);
      });
    });

    if (!metrics || !metrics.totalHeight) {
      throw new Error('Failed to retrieve page dimensions');
    }

    let totalHeight = metrics.totalHeight;
    const { viewportHeight, viewportWidth, dpr, title, url } = metrics;
    const slices = [];

    console.log(`[RippleFrame Background] Metrics: Total Height = ${totalHeight}px, Viewport = ${viewportWidth}x${viewportHeight}px, DPR = ${dpr}, Title = "${title}"`);

    if (!isFullPage) {
      console.log('[RippleFrame Background] Capturing single viewport slice...');
      // Single Viewport Capture (100% Lossless Crystal-Clear PNG)
      const dataUrl = await captureTabWithQuotaRetry(tab.windowId);
      slices.push({ dataUrl, scrollY: 0 });
    } else {
      // Multi-step Progressive Scrolling (GoFullPage Algorithm)
      let y = 0;
      let sliceIndex = 0;
      const maxSlices = 100; // Safety cap for very long 50,000px pages

      console.log(`[RippleFrame Background] Starting full-page capture across ${Math.ceil(totalHeight / viewportHeight)} expected slices (delay: ${delay}ms)...`);

      while (y < totalHeight && sliceIndex < maxSlices) {
        // Calculate exact scroll target for this step
        const targetY = (y + viewportHeight > totalHeight)
          ? Math.max(0, totalHeight - viewportHeight)
          : y;

        console.log(`[RippleFrame Background] Step ${sliceIndex + 1}: Scrolling to target Y = ${targetY}px (y = ${y}, totalHeight = ${totalHeight}px)`);

        // 1. Scroll page to target position
        const progressPct = Math.min(99, Math.round(((sliceIndex + 1) / Math.max(1, Math.ceil(totalHeight / viewportHeight))) * 100));
        chrome.action.setBadgeText({ text: `${progressPct}%` });
        chrome.action.setBadgeBackgroundColor({ color: '#0284c7' });

        const scrollRes = await new Promise((resolve) => {
          chrome.tabs.sendMessage(tabId, {
            action: 'rf_scroll_to',
            y: targetY,
            sliceIndex
          }, (res) => {
            if (chrome.runtime.lastError) resolve(null);
            else resolve(res);
          });
        });

        if (scrollRes && scrollRes.totalHeight && scrollRes.totalHeight > totalHeight) {
          console.log(`[RippleFrame Background] Dynamic height expansion detected: ${totalHeight}px -> ${scrollRes.totalHeight}px`);
          totalHeight = scrollRes.totalHeight;
        }

        // 2. Wait for paint & rate limit cooldown
        await new Promise(r => setTimeout(r, delay));

        // 3. Capture Lossless PNG with rate-limit retry
        const dataUrl = await captureTabWithQuotaRetry(tab.windowId);

        slices.push({ dataUrl, scrollY: targetY });
        console.log(`[RippleFrame Background] Slice ${sliceIndex + 1} captured successfully at scrollY = ${targetY}px`);
        sliceIndex++;

        // 4. Stop when bottom reached
        if (targetY >= totalHeight - viewportHeight || y + viewportHeight >= totalHeight) {
          console.log(`[RippleFrame Background] Reached bottom boundary at targetY = ${targetY}px. Slicing complete (${slices.length} total slices).`);
          break;
        }

        y += viewportHeight;
      }
    }

    // 3. Restore page & badge
    try {
      await new Promise((resolve) => {
        chrome.tabs.sendMessage(tabId, { action: 'rf_restore_page' }, () => resolve());
      });
    } catch (e) {}

    updateSpeedBadge();

    const capturePayload = {
      slices,
      totalHeight: isFullPage ? totalHeight : viewportHeight,
      viewportWidth,
      viewportHeight,
      dpr: dpr || 1,
      title: title || tab.title || 'Screen Capture',
      url: url || tab.url || '',
      fullPage: isFullPage,
      timestamp: Date.now()
    };

    // 4. Save to IndexedDB and chrome.storage.local
    await saveCaptureToDB(capturePayload);

    try {
      await new Promise((resolve) => {
        chrome.storage.local.set({ latest_rippleframe_capture: capturePayload }, () => resolve());
      });
    } catch (storageErr) {
      console.warn('[RippleFrame storage warn]', storageErr);
    }

    // 5. Open studio tab and track tab ID for memory cleanup on close
    chrome.tabs.create({ url: chrome.runtime.getURL('rippleframe.html') }, (newTab) => {
      if (newTab?.id) studioTabIds.add(newTab.id);
    });
  } catch (err) {
    console.error('[RippleFrame Background Error]', err);
    updateSpeedBadge();
    try {
      chrome.tabs.sendMessage(tabId, { action: 'rf_restore_page' }, () => {});
    } catch (e) {}

    // Fallback: If we collected at least one slice, still open studio
    if (typeof slices !== 'undefined' && slices && slices.length > 0) {
      chrome.tabs.create({ url: chrome.runtime.getURL('rippleframe.html') }, (newTab) => {
        if (newTab?.id) studioTabIds.add(newTab.id);
      });
    }
  }
}

// Track open Studio Editor tabs for auto-clearing memory
const studioTabIds = new Set();

function clearCaptureStorage() {
  try {
    const req = indexedDB.open('caspian_rippleframe_db', 1);
    req.onsuccess = () => {
      const db = req.result;
      if (db.objectStoreNames.contains('captures')) {
        const tx = db.transaction('captures', 'readwrite');
        tx.objectStore('captures').clear();
      }
    };
  } catch (e) {}
  try {
    chrome.storage.local.remove('latest_rippleframe_capture');
  } catch (e) {}
}

// Clear stored capture when user closes a Studio tab
chrome.tabs.onRemoved.addListener((tabId) => {
  if (studioTabIds.has(tabId)) {
    studioTabIds.delete(tabId);
    if (studioTabIds.size === 0) {
      clearCaptureStorage();
      console.log('[RippleFrame Background] Studio tab closed: Cleared temporary capture cache & IndexedDB storage.');
    }
  }
});

// Runtime Messages for RippleFrame Capture & Studio Tab
chrome.runtime.onMessage.addListener((req, sender, sendResponse) => {
  if (req.action === 'init_rippleframe_capture') {
    performRippleFrameCapture(req.tabId, req.options || {});
    sendResponse({ status: 'initiated' });
    return false;
  }

  if (req.action === 'open_rippleframe_studio') {
    const studioUrl = chrome.runtime.getURL('rippleframe.html');
    chrome.tabs.create({ url: studioUrl }, (newTab) => {
      if (newTab?.id) studioTabIds.add(newTab.id);
    });
    sendResponse({ status: 'opened' });
    return false;
  }

  if (req.action === 'clear_rippleframe_storage') {
    clearCaptureStorage();
    sendResponse({ status: 'cleared' });
    return false;
  }
});