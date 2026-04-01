function applyTurbo() {
  chrome.storage.local.get(['enabled', 'limit'], (data) => {
    const isEnabled = data.enabled ?? true;
    const limit = data.limit ?? 5;
    const messages = document.querySelectorAll('[data-testid^="conversation-turn"]');

    messages.forEach((msg, index) => {
      if (isEnabled && index < messages.length - limit) {
        msg.style.setProperty('display', 'none', 'important');
      } else {
        msg.style.setProperty('display', 'block', 'important');
      }
    });
  });
}

const observer = new MutationObserver(applyTurbo);
observer.observe(document.body, { childList: true, subtree: true });
chrome.storage.onChanged.addListener(applyTurbo);
applyTurbo();