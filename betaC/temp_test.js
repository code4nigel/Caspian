
    function syncTheme() {
      try {
        if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
          const prefs = JSON.parse(window.CaspianBridge.getSettings());
          const isDark = prefs.themeMode === 'dark';
          document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
        }
      } catch(e) {}
    }
    syncTheme();
    setInterval(syncTheme, 1000);

    document.getElementById('launch-chatgpt').addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('chatgpt');
      } else {
        window.location.href = 'https://chatgpt.com/';
      }
    });

    document.getElementById('launch-gemini').addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('gemini');
      } else {
        window.location.href = 'https://gemini.google.com/';
      }
    });

    document.getElementById('launch-google').addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('google');
      } else {
        window.location.href = 'https://www.google.com/';
      }
    });

    document.getElementById('launch-youtube').addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('youtube');
      } else {
        window.location.href = 'https://www.youtube.com/';
      }
    });
  