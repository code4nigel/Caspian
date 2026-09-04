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
    chrome.storage.local.get(['pruningEnabled', 'enabled'], (data) => {
      const current = data.pruningEnabled ?? (data.enabled ?? true);
      const next = !current;
      chrome.storage.local.set({ pruningEnabled: next, enabled: next });
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

// Runtime Messages for RippleFrame Capture & Studio Tab
chrome.runtime.onMessage.addListener((req, sender, sendResponse) => {
  if (req.action === 'capture_visible_tab') {
    chrome.tabs.captureVisibleTab(null, { format: 'png' }, (dataUrl) => {
      if (chrome.runtime.lastError) {
        sendResponse({ error: chrome.runtime.lastError.message });
      } else {
        sendResponse({ dataUrl });
      }
    });
    return true; // Keep message channel open for async response
  }

  if (req.action === 'open_rippleframe_studio') {
    const studioUrl = chrome.runtime.getURL('rippleframe.html');
    chrome.tabs.create({ url: studioUrl });
    sendResponse({ status: 'opened' });
  }
});