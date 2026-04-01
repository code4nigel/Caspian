chrome.commands.onCommand.addListener((command) => {
  if (command === "toggle-feature") {
    chrome.storage.local.get('enabled', (data) => {
      chrome.storage.local.set({ enabled: !data.enabled });
    });
  }
  if (command === "reset-colors") {
    chrome.storage.local.set({ accent: '#F3BE7A', limit: 5 });
  }
});