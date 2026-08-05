// ======================================================
// CASPIAN ANDROID - MOBILE CONTROL SHEET JS
// ======================================================

(function() {
  const sheetBackdrop = document.getElementById('sheet-backdrop');
  const bottomSheet = document.getElementById('bottom-sheet');
  const dragArea = document.getElementById('sheet-drag-area');
  const themeToggleBtn = document.getElementById('theme-toggle-btn');
  const themeBtnDark = document.getElementById('theme-btn-dark');
  const themeBtnLight = document.getElementById('theme-btn-light');
  const resetThemeBtn = document.getElementById('reset-theme-btn');
  const powerToggleBtn = document.getElementById('power-toggle-btn');
  const convertBtn = document.getElementById('convert-btn');
  const copyBtn = document.getElementById('copy-btn');
  const exportDropdownTrigger = document.getElementById('export-dropdown-trigger');
  const exportMenu = document.getElementById('export-menu');
  const switchHubBtn = document.getElementById('switch-hub-btn');
  const switchGptBtn = document.getElementById('switch-chatgpt-btn');
  const switchGeminiBtn = document.getElementById('switch-gemini-btn');

  const startPicker = document.getElementById('gradient-start-picker');
  const endPicker = document.getElementById('gradient-end-picker');
  const startHex = document.getElementById('gradient-start-hex');
  const endHex = document.getElementById('gradient-end-hex');

  const bgColorPicker = document.getElementById('bg-color-picker');
  const bgColorHex = document.getElementById('bg-color-hex');
  const nigelFactCard = document.getElementById('nigel-fact-card');
  const nigelFactText = document.getElementById('nigel-fact-text');

  let activeTheme = 'dark';
  let selectedDarkBg = '#050811';
  let limitVal = 5;
  let globalActive = true;

  // Restore saved limit and power switch state on load
  function restoreSavedSettings() {
    try {
      if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {
        const jsonStr = window.CaspianBridge.getSettings();
        if (jsonStr) {
          const prefs = JSON.parse(jsonStr);
          if (prefs.limit !== undefined) {
            limitVal = parseInt(prefs.limit);
            document.querySelectorAll('.limit-pill').forEach(p => {
              const val = parseInt(p.dataset.val);
              p.classList.toggle('active', val === limitVal);
            });
            const activeBadge = document.getElementById('active-limit-badge');
            if (activeBadge) {
              activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Turn' : 'Turns'}`;
            }
          }
          if (prefs.globalActive !== undefined) {
            globalActive = (prefs.globalActive === true || prefs.globalActive === 'true');
            const statusDot = document.getElementById('status-dot');
            const statusTitle = document.getElementById('status-title');
            const statusSub = document.getElementById('status-sub');

            if (statusDot) statusDot.classList.toggle('active', globalActive);
            if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Pruning Active' : 'Chat Pruning Disabled';
            if (statusSub) statusSub.textContent = globalActive ? 'Lag Fixer & DOM Limit Active' : 'Pruning paused via Master Power Switch';
          }
        }
      }
    } catch(e) {}
  }

  // Nigel Facts List
  const DEV_FACTS = [
    "Legend has it Nigel spent his time building Caspian instead of studying for his End-Sem exams or preparing for company placement interviews tomorrow... Absolute madman! 💀",
    "Nigel's favorite music genre is 'whatever he likes at the moment'. Down for NEFFEX anytime!",
    "Nigel makes extensions and web tools that actually solve real problems.",
    "Did you know? Nigel built Lsync, Caspian, and Scrobby all with custom aesthetic UIs!"
  ];
  let currentFactIdx = 0;

  if (nigelFactCard && nigelFactText) {
    nigelFactCard.addEventListener('click', () => {
      currentFactIdx = (currentFactIdx + 1) % DEV_FACTS.length;
      nigelFactText.style.opacity = '0';
      setTimeout(() => {
        nigelFactText.textContent = DEV_FACTS[currentFactIdx];
        nigelFactText.style.opacity = '1';
      }, 150);
    });
  }

  // Theme Presets Map
  const presets = {
    caspian: { start: '#A2A9A9', end: '#1B4264' },
    cyan: { start: '#06b6d4', end: '#0891b2' },
    violet: { start: '#a855f7', end: '#7c3aed' },
    azure: { start: '#3b82f6', end: '#1d4ed8' },
    emerald: { start: '#10b981', end: '#047857' }
  };

  function applyCustomGradient(start, end) {
    document.documentElement.style.setProperty('--accent', start);
    document.documentElement.style.setProperty('--secondary', end);
    document.documentElement.style.setProperty('--accent-glow', `${start}55`);
    document.documentElement.style.setProperty('--accent-gradient', `linear-gradient(135deg, ${start}, ${end})`);

    if (startPicker) startPicker.value = start;
    if (endPicker) endPicker.value = end;
    if (startHex) startHex.value = start.toUpperCase();
    if (endHex) endHex.value = end.toUpperCase();
  }

  function applyCustomBg(colorHex) {
    if (activeTheme === 'dark') {
      selectedDarkBg = colorHex;
      document.documentElement.style.setProperty('--sheet-bg', colorHex);
    } else {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff');
    }
    if (bgColorPicker) bgColorPicker.value = colorHex;
    if (bgColorHex) bgColorHex.value = colorHex.toUpperCase();
  }

  // Bind Background Tone Presets
  document.querySelectorAll('.bg-preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.bg-preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const bgHex = btn.dataset.bg;
      if (bgHex) applyCustomBg(bgHex);
    });
  });

  if (bgColorPicker && bgColorHex) {
    bgColorPicker.addEventListener('input', (e) => {
      bgColorHex.value = e.target.value.toUpperCase();
      applyCustomBg(e.target.value);
    });
    bgColorHex.addEventListener('change', (e) => {
      let val = e.target.value;
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        applyCustomBg(val);
      }
    });
  }

  // Bind Quick Presets
  document.querySelectorAll('.preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.preset-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const pKey = btn.dataset.preset;
      if (presets[pKey]) {
        applyCustomGradient(presets[pKey].start, presets[pKey].end);
      }
    });
  });

  // Color Pickers
  if (startPicker && startHex) {
    startPicker.addEventListener('input', (e) => {
      startHex.value = e.target.value.toUpperCase();
      applyCustomGradient(e.target.value, endPicker ? endPicker.value : '#1B4264');
    });
    startHex.addEventListener('change', (e) => {
      let val = e.target.value;
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        startPicker.value = val;
        applyCustomGradient(val, endPicker ? endPicker.value : '#1B4264');
      }
    });
  }

  if (endPicker && endHex) {
    endPicker.addEventListener('input', (e) => {
      endHex.value = e.target.value.toUpperCase();
      applyCustomGradient(startPicker ? startPicker.value : '#A2A9A9', e.target.value);
    });
    endHex.addEventListener('change', (e) => {
      let val = e.target.value;
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        endPicker.value = val;
        applyCustomGradient(startPicker ? startPicker.value : '#A2A9A9', val);
      }
    });
  }

  // Reset Defaults
  if (resetThemeBtn) {
    resetThemeBtn.addEventListener('click', () => {
      applyCustomGradient('#A2A9A9', '#1B4264');
      applyCustomBg('#050811');
      setTheme('dark');
    });
  }

  // Sync Theme with Host Web Page
  function syncHostPageTheme(t) {
    const isDark = (t === 'dark');
    try {
      if (isDark) {
        document.documentElement.classList.add('dark');
        document.documentElement.classList.remove('light');
        document.documentElement.setAttribute('data-theme', 'dark');
        document.documentElement.style.colorScheme = 'dark';
      } else {
        document.documentElement.classList.add('light');
        document.documentElement.classList.remove('dark');
        document.documentElement.setAttribute('data-theme', 'light');
        document.documentElement.style.colorScheme = 'light';
      }
      localStorage.setItem('theme', isDark ? 'dark' : 'light');
      localStorage.setItem('colorMode', isDark ? 'dark' : 'light');
    } catch(e) {}
  }

  // Theme Toggles
  function setTheme(t) {
    activeTheme = t;
    document.documentElement.setAttribute('data-theme', activeTheme);
    if (t === 'light') {
      document.documentElement.style.setProperty('--sheet-bg', '#ffffff');
    } else {
      document.documentElement.style.setProperty('--sheet-bg', selectedDarkBg);
    }
    if (themeBtnDark) themeBtnDark.classList.toggle('active', t === 'dark');
    if (themeBtnLight) themeBtnLight.classList.toggle('active', t === 'light');

    syncHostPageTheme(t);
  }

  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      setTheme(activeTheme === 'light' ? 'dark' : 'light');
    });
  }
  if (themeBtnDark) themeBtnDark.addEventListener('click', () => setTheme('dark'));
  if (themeBtnLight) themeBtnLight.addEventListener('click', () => setTheme('light'));

  // Resizable Drag Area Hitbox
  const targetDragArea = dragArea || document.querySelector('.sheet-drag-area') || document.querySelector('.sheet-drag-handle');
  if (targetDragArea && bottomSheet) {
    let startY, startHeight;

    targetDragArea.addEventListener('touchstart', (e) => {
      try { e.preventDefault(); } catch(err) {}
      const touch = e.touches[0];
      startY = touch.clientY;
      startHeight = bottomSheet.offsetHeight;
      bottomSheet.style.transition = 'none';
    }, { passive: false });

    targetDragArea.addEventListener('touchmove', (e) => {
      try { e.preventDefault(); } catch(err) {}
      const touch = e.touches[0];
      const deltaY = startY - touch.clientY;
      const newHeight = startHeight + deltaY;
      const vhHeight = Math.max(25, Math.min(95, (newHeight / window.innerHeight) * 100));
      bottomSheet.style.height = vhHeight + 'vh';
      bottomSheet.style.maxHeight = '95vh';
    }, { passive: false });

    targetDragArea.addEventListener('touchend', () => {
      bottomSheet.style.transition = 'transform 0.35s cubic-bezier(0.16, 1, 0.3, 1), height 0.3s ease';
    });
  }

  // Navigation (Hub, ChatGPT, Gemini)
  if (switchHubBtn) {
    switchHubBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('hub');
      }
    });
  }

  if (switchGptBtn) {
    switchGptBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('chatgpt');
      }
    });
  }

  if (switchGeminiBtn) {
    switchGeminiBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('gemini');
      }
    });
  }

  // Mobile Tab Navigation
  document.querySelectorAll('.tab-nav-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-nav-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.style.display = 'none');

      btn.classList.add('active');
      const targetTab = btn.dataset.tab;
      const pane = document.getElementById(`tab-pane-${targetTab}`);
      if (pane) pane.style.display = 'block';
    });
  });

  // Master Power Switch
  if (powerToggleBtn) {
    powerToggleBtn.addEventListener('click', () => {
      globalActive = !globalActive;
      const statusDot = document.getElementById('status-dot');
      const statusTitle = document.getElementById('status-title');
      const statusSub = document.getElementById('status-sub');

      if (statusDot) statusDot.classList.toggle('active', globalActive);
      if (statusTitle) statusTitle.textContent = globalActive ? 'Chat Pruning Active' : 'Chat Pruning Disabled';
      if (statusSub) statusSub.textContent = globalActive ? 'Lag Fixer & DOM Limit Active' : 'Pruning paused via Master Power Switch';

      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('globalActive', JSON.stringify(globalActive));
      }
    });
  }

  // Limit Pills Selection
  document.querySelectorAll('.limit-pill[data-val]').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.limit-pill[data-val]').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      limitVal = parseInt(pill.dataset.val);

      const activeBadge = document.getElementById('active-limit-badge');
      if (activeBadge) {
        activeBadge.textContent = limitVal >= 9999 ? '∞ All' : `${limitVal} ${limitVal === 1 ? 'Turn' : 'Turns'}`;
      }

      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('limit', limitVal);
      }
    });
  });

  // Convert Chat Handler
  if (convertBtn) {
    convertBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation('convert');
      }
    });
  }

  // Export Dropdown Trigger
  if (exportDropdownTrigger && exportMenu) {
    exportDropdownTrigger.addEventListener('click', (e) => {
      e.stopPropagation();
      exportMenu.classList.toggle('active');
    });
  }

  // Export Options Handler -> Direct Bridge Call to mainWebView
  document.querySelectorAll('.export-opt-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const fmt = btn.dataset.fmt;
      if (exportMenu) exportMenu.classList.remove('active');
      
      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation(fmt);
      }
    });
  });

  // Copy Button
  if (copyBtn) {
    copyBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.exportConversation === 'function') {
        window.CaspianBridge.exportConversation('copy');
      }
    });
  }

  // Initialize saved settings on load
  restoreSavedSettings();
})();
