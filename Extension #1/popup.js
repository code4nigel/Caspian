const DEFAULTS = {
  bgDeep: '#3852B4',
  bgCard: '#5E7AC4',
  accent: '#F3BE7A',
  secondary: '#F08D39',
  limit: 5
};

function updateTheme(theme) {
  document.documentElement.style.setProperty('--bg-deep', theme.bgDeep || DEFAULTS.bgDeep);
  document.documentElement.style.setProperty('--bg-card', theme.bgCard || DEFAULTS.bgCard);
  document.documentElement.style.setProperty('--accent', theme.accent || DEFAULTS.accent);
  document.documentElement.style.setProperty('--secondary', theme.secondary || DEFAULTS.secondary);
}

function loadSettings() {
  chrome.storage.local.get(['enabled', 'limit', 'bgDeep', 'bgCard', 'accent', 'secondary'], (data) => {
    updateTheme(data);
    document.getElementById('primary-hex').value = data.accent || DEFAULTS.accent;
    document.getElementById('power-toggle').classList.toggle('active', data.enabled ?? true);
    
    const limit = data.limit || DEFAULTS.limit;
    document.querySelectorAll('.pill').forEach(p => {
      p.classList.toggle('active', parseInt(p.dataset.val) === limit);
    });
  });
}

// SMART IMPORT LOGIC
document.getElementById('palette-import').addEventListener('input', (e) => {
  const input = e.target.value;
  const hexMatch = input.match(/[A-Fa-f0-9]{6}/g); // Finds all hex codes
  
  if (hexMatch && hexMatch.length >= 4) {
    const palette = {
      bgDeep: '#' + hexMatch[0],
      bgCard: '#' + hexMatch[1],
      accent: '#' + hexMatch[2],
      secondary: '#' + hexMatch[3]
    };
    chrome.storage.local.set(palette, loadSettings);
    e.target.value = "Palette Applied!";
    setTimeout(() => { e.target.value = ""; }, 1500);
  } else if (hexMatch && hexMatch.length >= 1) {
    chrome.storage.local.set({ accent: '#' + hexMatch[0] }, loadSettings);
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