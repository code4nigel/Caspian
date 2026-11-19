// Feature script lifecycle -------------------------------------------------
// Scripts are registered only while their corresponding feature is on. This
// keeps disabled tools out of pages entirely, rather than merely idling there.
const CHAT_MATCHES = ['*://chatgpt.com/*', '*://*.chatgpt.com/*', '*://gemini.google.com/*'];
const YOUTUBE_MATCHES = ['*://*.youtube.com/*', '*://youtube.com/*'];
const SPEED_SCRIPT_ID = 'caspian-flow-speed-v2';
const SPEED_FILES = ['speed_shortcuts.js', 'speed_content.js'];
const FEATURE_SCRIPT_IDS = ['caspian-chat-tools', 'caspian-youtube-tools', 'caspian-flow-speed', SPEED_SCRIPT_ID];
let featureScriptSync = Promise.resolve();

async function attachFlowSpeed(tab) {
  if (!tab || !/^https?:/.test(tab.url || '')) return;
  try {
    await browser.scripting.executeScript({ target: { tabId: tab.id, allFrames: true }, files: SPEED_FILES });
  } catch {
    // A restricted embedded frame should not prevent the top-level player from working.
    await browser.scripting.executeScript({ target: { tabId: tab.id }, files: SPEED_FILES });
  }
}

async function syncFeatureScripts(attachSpeedToOpenTabs) {
  const data = await browser.storage.local.get([
    'globalActive', 'pruningEnabled', 'enabled', 'vaultEnabled',
    'yt_feed_limit_enabled', 'yt_not_interested_enabled', 'flow_speed_enabled'
  ]);

  const globalActive = data.globalActive ?? true;
  const chatEnabled = globalActive && (
    (data.pruningEnabled ?? (data.enabled ?? true)) ||
    (data.vaultEnabled ?? true)
  );
  const youtubeEnabled = (data.yt_feed_limit_enabled ?? false) ||
    (data.yt_not_interested_enabled ?? false);
  const speedEnabled = data.flow_speed_enabled ?? true;

  const scripts = [];
  if (chatEnabled) {
    scripts.push({
      id: 'caspian-chat-tools', matches: CHAT_MATCHES, js: ['content.js'],
      runAt: 'document_end', persistAcrossSessions: true
    });
  }
  if (youtubeEnabled) {
    scripts.push({
      id: 'caspian-youtube-tools', matches: YOUTUBE_MATCHES, js: ['youtube_content.js'],
      runAt: 'document_end', persistAcrossSessions: true
    });
  }
  if (speedEnabled) {
    scripts.push({
      id: SPEED_SCRIPT_ID, matches: ['<all_urls>'], js: SPEED_FILES,
      allFrames: true, matchOriginAsFallback: true, runAt: 'document_start', persistAcrossSessions: true
    });
  }

  const registered = await browser.scripting.getRegisteredContentScripts({ ids: FEATURE_SCRIPT_IDS });
  const desiredIds = new Set(scripts.map(script => script.id));
  const registeredIds = new Set(registered.map(script => script.id));
  const obsolete = registered.filter(script => !desiredIds.has(script.id)).map(script => script.id);
  if (obsolete.length) await browser.scripting.unregisterContentScripts({ ids: obsolete });

  for (const script of scripts) {
    if (registeredIds.has(script.id)) continue;
    try {
      await browser.scripting.registerContentScripts([script]);
    } catch (error) {
      if (script.id !== SPEED_SCRIPT_ID) {
        console.warn('[Caspian] Script registration failed:', script.id, error);
        continue;
      }
      // Retry without matching opaque embedded frames if the option is unavailable.
      const { matchOriginAsFallback, ...compatibleScript } = script;
      await browser.scripting.registerContentScripts([compatibleScript]);
    }
  }

  if (speedEnabled && attachSpeedToOpenTabs) {
    const tabs = await browser.tabs.query({ active: true });
    await Promise.allSettled(tabs.map(attachFlowSpeed));
  }
}

function refreshFeatureScripts(attachSpeedToOpenTabs = false) {
  featureScriptSync = featureScriptSync.then(() => syncFeatureScripts(attachSpeedToOpenTabs))
    .catch(error => console.warn('[Caspian] Script registration failed:', error));
}

chrome.runtime.onInstalled.addListener(() => {
  refreshFeatureScripts(true);
  updateSpeedBadge();
});
chrome.runtime.onStartup.addListener(() => {
  refreshFeatureScripts(true);
  updateSpeedBadge();
});

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
updateSpeedBadge();

chrome.storage.onChanged.addListener((changes, area) => {
  if (area === 'local' && (
    changes.globalActive || changes.pruningEnabled || changes.enabled || changes.vaultEnabled ||
    changes.yt_feed_limit_enabled || changes.yt_not_interested_enabled || changes.flow_speed_enabled
  )) {
    refreshFeatureScripts(Boolean(changes.flow_speed_enabled?.newValue));
  }
  if (area === 'local' && (changes.flow_speed_enabled || changes.flow_speed_badge_enabled || changes.flow_speed_val || changes.accent)) {
    updateSpeedBadge();
  }
});

chrome.commands.onCommand.addListener((command) => {
  if (command === "toggle-feature") {
    chrome.storage.local.get(['pruningEnabled', 'vaultEnabled', 'enabled', 'yt_feed_limit_enabled', 'yt_not_interested_enabled', 'flow_speed_enabled', 'rf_enabled'], (data) => {
      const isCurrentlyOn = (data.pruningEnabled ?? true) ||
                            (data.vaultEnabled ?? true) ||
                            (data.yt_feed_limit_enabled ?? false) ||
                            (data.yt_not_interested_enabled ?? false) ||
                            (data.flow_speed_enabled ?? true) ||
                            (data.rf_enabled ?? true);
      const next = !isCurrentlyOn;
      chrome.storage.local.set({
        pruningEnabled: next,
        vaultEnabled: next,
        enabled: next,
        yt_feed_limit_enabled: next,
        yt_not_interested_enabled: next,
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

// Firefox can capture a specific tab even if the user switches tabs mid-capture.
async function captureRippleFrameTab(tab) {
  const options = { format: 'png' };
  if (browser.tabs.captureTab) {
    try {
      return await browser.tabs.captureTab(tab.id, options);
    } catch (error) {
      if (!(await browser.tabs.get(tab.id)).active) throw error;
    }
  }
  if (!(await browser.tabs.get(tab.id)).active) {
    throw new Error('Switch back to the page you started capturing.');
  }
  return browser.tabs.captureVisibleTab(tab.windowId, options);
}

// Progressive RippleFrame Capture Controller (GoFullPage Engine)
async function performRippleFrameCapture(tabId, options = {}) {
  const isFullPage = options.scope !== 'viewport';
  // Keep the page's paint and lazy-load settling delay between slices.
  const delay = Math.max(550, options.delay || 550);

  try {
    // 1. Inject content script helper into active tab
    await browser.scripting.executeScript({
      target: { tabId },
      files: ['rippleframe_capture.js']
    });

    const tab = await browser.tabs.get(tabId);
    
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
      const dataUrl = await captureRippleFrameTab(tab);
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

        // 2. Wait for paint
        await new Promise(r => setTimeout(r, delay));

        // 3. Capture the current viewport as a lossless PNG
        const dataUrl = await captureRippleFrameTab(tab);

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
    const newTab = await browser.tabs.create({ url: browser.runtime.getURL('rippleframe.html') });
    if (newTab?.id) studioTabIds.add(newTab.id);
    return { status: 'captured' };
  } catch (err) {
    console.error('[RippleFrame Background Error]', err);
    updateSpeedBadge();
    try {
      chrome.tabs.sendMessage(tabId, { action: 'rf_restore_page' }, () => {});
    } catch (e) {}

    return { status: 'error', message: err.message };
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
browser.runtime.onMessage.addListener((req, sender, sendResponse) => {
  if (req.action === 'attach_flow_speed' && sender.url === chrome.runtime.getURL('popup.html')) {
    return browser.storage.local.get('flow_speed_enabled').then(async ({ flow_speed_enabled }) => {
      if (flow_speed_enabled === false) return false;
      const [tab] = await browser.tabs.query({ active: true, lastFocusedWindow: true });
      await attachFlowSpeed(tab);
      return true;
    }).catch(() => false);
  }
  if (req.action === 'init_rippleframe_capture') {
    return performRippleFrameCapture(req.tabId, req.options || {});
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
