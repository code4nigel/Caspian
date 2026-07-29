chrome.commands.onCommand.addListener((command) => {
  if (command === "toggle-feature") {
    chrome.storage.local.get('enabled', (data) => {
      const current = data.enabled ?? true;
      chrome.storage.local.set({ enabled: !current });
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