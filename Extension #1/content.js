function applyFix() {
  chrome.storage.local.get(['enabled', 'limit'], (data) => {
    const isEnabled = data.enabled ?? true;
    const limit = data.limit ?? 15;

    // ChatGPT uses data-testid="conversation-turn-[number]" for messages
    const messages = document.querySelectorAll('[data-testid^="conversation-turn"]');
    
    console.log(`[TurboUI] Found ${messages.length} messages. Fix Enabled: ${isEnabled}`);

    if (!isEnabled) {
      messages.forEach(msg => msg.style.display = 'block');
      return;
    }

    messages.forEach((msg, index) => {
      // We hide messages from the start of the array (the oldest ones)
      if (index < messages.length - limit) {
        msg.style.setProperty('display', 'none', 'important');
      } else {
        msg.style.setProperty('display', 'block', 'important');
      }
    });
  });
}

// Throttled observer to prevent infinite loops
let timeout = null;
const observer = new MutationObserver(() => {
  if (timeout) clearTimeout(timeout);
  timeout = setTimeout(applyFix, 100); 
});

observer.observe(document.body, { childList: true, subtree: true });
chrome.storage.onChanged.addListener(applyFix);
applyFix();