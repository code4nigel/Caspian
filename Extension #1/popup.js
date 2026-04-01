const DEFAULTS = { accent: '#F3BE7A', limit: 5 };

function updateTheme(hex) {
  document.documentElement.style.setProperty('--accent', hex);
}

function loadSettings() {
  chrome.storage.local.get(['enabled', 'limit', 'accent'], (data) => {
    const isEnabled = data.enabled ?? true;
    const accent = data.accent || DEFAULTS.accent;
    const limit = data.limit || DEFAULTS.limit;

    updateTheme(accent);
    document.getElementById('primary-hex').value = accent;
    
    const powerBtn = document.getElementById('power-toggle');
    powerBtn.classList.toggle('active', isEnabled);

    document.querySelectorAll('.pill').forEach(p => {
      p.classList.toggle('active', parseInt(p.dataset.val) === limit);
    });
  });
}

document.getElementById('palette-import').addEventListener('input', (e) => {
  const hexMatch = e.target.value.match(/#([A-Fa-f0-9]{6})/g);
  if (hexMatch) {
    const newColor = hexMatch[0];
    chrome.storage.local.set({ accent: newColor }, loadSettings);
  }
});

document.getElementById('primary-hex').addEventListener('input', (e) => {
  if (/^#[0-9A-F]{6}$/i.test(e.target.value)) {
    chrome.storage.local.set({ accent: e.target.value }, loadSettings);
  }
});

document.querySelectorAll('.pill').forEach(p => {
  p.addEventListener('click', () => {
    chrome.storage.local.set({ limit: parseInt(p.dataset.val) }, loadSettings);
  });
});

document.getElementById('power-toggle').addEventListener('click', () => {
  chrome.storage.local.get('enabled', (data) => {
    chrome.storage.local.set({ enabled: !(data.enabled ?? true) }, loadSettings);
  });
});

document.getElementById('reset-link').addEventListener('click', () => {
  chrome.storage.local.set(DEFAULTS, loadSettings);
});

loadSettings();