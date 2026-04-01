const primaryInput = document.getElementById('primary-hex');
const paletteInput = document.getElementById('palette-import');
const powerBtn = document.getElementById('power-toggle');
const pills = document.querySelectorAll('.pill');

// Default setup
chrome.storage.local.get(['enabled', 'limit', 'accent'], (data) => {
  const isEnabled = data.enabled ?? true;
  const accent = data.accent || '#F3BE7A';
  const limit = data.limit || 5;

  updateAccent(accent);
  primaryInput.value = accent;
  if (isEnabled) powerBtn.classList.add('active');
  highlightPill(limit);
});

// Extract Hex codes from a string or link
paletteInput.addEventListener('input', (e) => {
  const input = e.target.value;
  const hexMatch = input.match(/#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})/g);
  
  if (hexMatch && hexMatch.length > 0) {
    const newAccent = hexMatch[0]; // Takes the first hex found
    updateAccent(newAccent);
    primaryInput.value = newAccent;
    chrome.storage.local.set({ accent: newAccent });
    paletteInput.style.borderColor = 'var(--accent)';
  }
});

primaryInput.addEventListener('input', (e) => {
  if (/^#[0-9A-F]{6}$/i.test(e.target.value)) {
    updateAccent(e.target.value);
    chrome.storage.local.set({ accent: e.target.value });
  }
});

function updateAccent(hex) {
  document.documentElement.style.setProperty('--accent', hex);
}

function highlightPill(val) {
  pills.forEach(p => p.classList.toggle('active', parseInt(p.dataset.val) === val));
}

pills.forEach(p => {
  p.addEventListener('click', () => {
    const val = parseInt(p.dataset.val);
    chrome.storage.local.set({ limit: val });
    highlightPill(val);
  });
});

powerBtn.addEventListener('click', () => {
  const isActive = powerBtn.classList.toggle('active');
  chrome.storage.local.set({ enabled: isActive });
});

document.getElementById('open-shortcuts').addEventListener('click', () => {
  chrome.tabs.create({ url: 'chrome://extensions/shortcuts' });
});