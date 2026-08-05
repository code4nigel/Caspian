// ======================================================
// CASPIAN ANDROID - MOBILE CONTROL SHEET JS
// ======================================================

(function() {
  const floatingBtn = document.getElementById('caspian-floating-btn');
  const sheetBackdrop = document.getElementById('sheet-backdrop');
  const bottomSheet = document.getElementById('bottom-sheet');
  const dragArea = document.getElementById('sheet-drag-area');
  const themeToggleBtn = document.getElementById('theme-toggle-btn');
  const themeBtnDark = document.getElementById('theme-btn-dark');
  const themeBtnLight = document.getElementById('theme-btn-light');
  const glassToggleBtn = document.getElementById('glass-toggle-btn');
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
  let isGlassOn = true;
  let limitVal = 5;
  let globalActive = true;
  let lastToggleTime = 0;

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
      setGlass(true);
    });
  }

  // Glassmorphism Toggle
  function setGlass(on) {
    isGlassOn = on;
    document.documentElement.setAttribute('data-glass', on ? 'on' : 'off');
    if (glassToggleBtn) {
      glassToggleBtn.classList.toggle('active', on);
      glassToggleBtn.textContent = on ? 'ON' : 'OFF';
    }
  }

  if (glassToggleBtn) {
    glassToggleBtn.addEventListener('click', () => {
      setGlass(!isGlassOn);
    });
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
  }

  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      setTheme(activeTheme === 'light' ? 'dark' : 'light');
    });
  }
  if (themeBtnDark) themeBtnDark.addEventListener('click', () => setTheme('dark'));
  if (themeBtnLight) themeBtnLight.addEventListener('click', () => setTheme('light'));

  // Single Tap Open Sheet Handler
  function safeToggleSheet(e) {
    if (e && e.type === 'touchend') {
      try { e.preventDefault(); } catch(err) {}
    }
    const now = Date.now();
    if (now - lastToggleTime < 400) return;
    lastToggleTime = now;

    const sheet = document.getElementById('bottom-sheet');
    if (sheet && sheet.classList.contains('active')) {
      closeSheet();
    } else {
      openSheet();
    }
  }

  // Floating Button Drag & Touch Handling
  if (floatingBtn) {
    let touchMoved = false;
    let startX, startY, initialLeft, initialTop;

    floatingBtn.addEventListener('touchstart', (e) => {
      touchMoved = false;
      const touch = e.touches[0];
      startX = touch.clientX;
      startY = touch.clientY;

      const rect = floatingBtn.getBoundingClientRect();
      initialLeft = rect.left;
      initialTop = rect.top;
    }, { passive: true });

    floatingBtn.addEventListener('touchmove', (e) => {
      const touch = e.touches[0];
      const deltaX = touch.clientX - startX;
      const deltaY = touch.clientY - startY;
      const dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

      if (dist > 12) {
        touchMoved = true;
        let newLeft = initialLeft + deltaX;
        let newTop = initialTop + deltaY;

        newLeft = Math.max(10, Math.min(window.innerWidth - 60, newLeft));
        newTop = Math.max(10, Math.min(window.innerHeight - 60, newTop));

        floatingBtn.style.left = newLeft + 'px';
        floatingBtn.style.top = newTop + 'px';
        floatingBtn.style.right = 'auto';
        floatingBtn.style.bottom = 'auto';
      }
    }, { passive: true });

    floatingBtn.addEventListener('touchend', (e) => {
      if (!touchMoved) {
        safeToggleSheet(e);
      }
    });

    floatingBtn.addEventListener('click', (e) => {
      if (!touchMoved) {
        safeToggleSheet(e);
      }
    });
  }

  // Resizable Drag Area Hitbox Fix (Prevents Sheet Content Scrolling)
  const targetDragArea = dragArea || document.querySelector('.sheet-drag-area') || document.querySelector('.sheet-drag-handle');
  if (targetDragArea && bottomSheet) {
    let startY, startHeight;

    targetDragArea.addEventListener('touchstart', (e) => {
      try { e.preventDefault(); } catch(err) {}
      const touch = e.touches[0];
      startX = touch.clientX;
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
      } else {
        window.location.href = 'file:///android_asset/launch_hub.html';
      }
    });
  }

  if (switchGptBtn) {
    switchGptBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('chatgpt');
      } else {
        window.location.href = 'https://chatgpt.com/';
      }
    });
  }

  if (switchGeminiBtn) {
    switchGeminiBtn.addEventListener('click', () => {
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {
        window.CaspianBridge.switchService('gemini');
      } else {
        window.location.href = 'https://gemini.google.com/';
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

  // Sync initial service pill
  if (window.location.href.includes('gemini.google.com')) {
    if (switchGptBtn) switchGptBtn.classList.remove('active');
    if (switchGeminiBtn) switchGeminiBtn.classList.add('active');
  } else {
    if (switchGptBtn) switchGptBtn.classList.add('active');
    if (switchGeminiBtn) switchGeminiBtn.classList.remove('active');
  }

  // Sheet Open / Close
  function openSheet() {
    const backdrop = document.getElementById('sheet-backdrop');
    const sheet = document.getElementById('bottom-sheet');

    if (sheet) {
      sheet.classList.add('active');
      sheet.style.setProperty('display', 'block', 'important');
      sheet.style.setProperty('visibility', 'visible', 'important');
      sheet.style.setProperty('opacity', '1', 'important');
      sheet.style.setProperty('transform', 'translateY(0)', 'important');
      sheet.style.setProperty('z-index', '2147483647', 'important');
    }
    if (backdrop) {
      backdrop.classList.add('active');
      backdrop.style.setProperty('display', 'block', 'important');
      backdrop.style.setProperty('visibility', 'visible', 'important');
      backdrop.style.setProperty('opacity', '1', 'important');
      backdrop.style.setProperty('pointer-events', 'auto', 'important');
      backdrop.style.setProperty('z-index', '2147483640', 'important');
    }
  }

  function closeSheet() {
    const backdrop = document.getElementById('sheet-backdrop');
    const sheet = document.getElementById('bottom-sheet');

    if (sheet) {
      sheet.classList.remove('active');
      sheet.style.setProperty('transform', 'translateY(100%)', 'important');
      sheet.style.setProperty('opacity', '0', 'important');
      setTimeout(() => {
        if (!sheet.classList.contains('active')) {
          sheet.style.setProperty('display', 'none', 'important');
        }
      }, 350);
    }
    if (backdrop) {
      backdrop.classList.remove('active');
      backdrop.style.setProperty('opacity', '0', 'important');
      backdrop.style.setProperty('pointer-events', 'none', 'important');
      setTimeout(() => {
        if (!backdrop.classList.contains('active')) {
          backdrop.style.setProperty('display', 'none', 'important');
        }
      }, 350);
    }
    if (exportMenu) exportMenu.classList.remove('active');
  }

  if (sheetBackdrop) sheetBackdrop.addEventListener('click', closeSheet);

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

      window.postMessage({ type: 'CASPIAN_SYNC_SETTINGS', payload: { globalActive } }, '*');
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

      window.postMessage({ type: 'CASPIAN_SYNC_SETTINGS', payload: { limit: limitVal } }, '*');
    });
  });

  // Convert Chat Handler
  if (convertBtn) {
    convertBtn.addEventListener('click', () => {
      closeSheet();
      let turnsText = '';
      const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, user-query, model-response');
      turns.forEach((t, i) => {
        turnsText += `[Turn ${i+1}]\n${t.innerText || t.textContent}\n\n`;
      });

      if (turnsText.trim()) {
        if (window.CaspianBridge && typeof window.CaspianBridge.convertAndLaunchTab === 'function') {
          window.CaspianBridge.convertAndLaunchTab(turnsText);
        } else {
          navigator.clipboard.writeText(turnsText);
          window.location.href = 'https://chatgpt.com/';
        }
      } else {
        alert('Open a temporary chat session to convert!');
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

  // Build Full HTML Document for Isolated PDF Printing
  function generateFormattedHtmlDocument(isCaspianTheme) {
    let title = document.title || 'AI Conversation';
    let dateStr = new Date().toLocaleString();

    let turnsHtml = '';
    const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group, user-query, model-response');
    turns.forEach((t, i) => {
      turnsHtml += `
        <div style="margin-bottom: 20px; padding: 16px; border: 1px solid ${isCaspianTheme ? '#1B4264' : '#cbd5e1'}; border-radius: 12px; background: ${isCaspianTheme ? '#050811' : '#f8fafc'}; color: ${isCaspianTheme ? '#f8fafc' : '#0f172a'};">
          <div style="font-weight: 800; color: ${isCaspianTheme ? '#A2A9A9' : '#1B4264'}; font-size: 14px; margin-bottom: 10px;">[ Turn ${i + 1} ]</div>
          <div style="white-space: pre-wrap; font-size: 13px; line-height: 1.6;">${t.innerHTML || t.innerText}</div>
        </div>
      `;
    });

    return `<!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>${title}</title>
      <style>
        body { font-family: system-ui, -apple-system, sans-serif; padding: 24px; background: ${isCaspianTheme ? '#000000' : '#ffffff'}; color: ${isCaspianTheme ? '#ffffff' : '#000000'}; }
        h1 { font-size: 22px; font-weight: 800; color: ${isCaspianTheme ? '#A2A9A9' : '#1B4264'}; margin-bottom: 4px; }
        .meta { font-size: 11px; color: #94a3b8; margin-bottom: 24px; border-bottom: 2px solid ${isCaspianTheme ? '#1B4264' : '#e2e8f0'}; padding-bottom: 12px; }
      </style>
    </head>
    <body>
      <h1>${title}</h1>
      <div class="meta">Full Conversation Transcript &bull; Exported via Caspian Mobile on ${dateStr}</div>
      ${turnsHtml}
    </body>
    </html>`;
  }

  // Export Options Handler
  document.querySelectorAll('.export-opt-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const fmt = btn.dataset.fmt;
      if (exportMenu) exportMenu.classList.remove('active');
      
      let title = document.title || 'AI Conversation';
      let safeTitle = title.replace(/[^a-z0-9_-]/gi, '_');
      let dateStr = new Date().toLocaleString();

      if (fmt === 'md') {
        let content = `# ${title}\n\n*Exported via Caspian Mobile on ${dateStr}*\n\n---\n\n`;
        const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group, user-query, model-response');
        turns.forEach((t, i) => {
          content += `### Turn ${i + 1}\n\n${t.innerText || t.textContent}\n\n---\n\n`;
        });
        triggerDownload(`${safeTitle}_Caspian_Exported.md`, content, 'text/markdown');
      } else if (fmt === 'txt') {
        let content = `======================================\n${title.toUpperCase()}\nExported via Caspian Mobile on ${dateStr}\n======================================\n\n`;
        const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group, user-query, model-response');
        turns.forEach((t, i) => {
          content += `[TURN ${i + 1}]\n${t.innerText || t.textContent}\n\n--------------------------------------\n\n`;
        });
        triggerDownload(`${safeTitle}_Caspian_Exported.txt`, content, 'text/plain');
      } else if (fmt === 'doc') {
        let content = `<html><body><h1>${title}</h1><p>Exported via Caspian Mobile on ${dateStr}</p>`;
        const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group, user-query, model-response');
        turns.forEach((t, i) => {
          content += `### Turn ${i + 1}</h3><div>${t.innerHTML}</div><hr>`;
        });
        content += `</body></html>`;
        triggerDownload(`${safeTitle}_Caspian_Exported.doc`, content, 'application/msword');
      } else if (fmt === 'nativepdf' || fmt === 'styledpdf') {
        closeSheet();
        const isCaspianTheme = (fmt === 'styledpdf');
        const htmlDoc = generateFormattedHtmlDocument(isCaspianTheme);
        const fileName = `${safeTitle}_${isCaspianTheme ? 'Caspian' : 'Document'}.html`;
        
        // Also save standalone HTML copy to Downloads/Caspian/ for 100% reliable viewing
        triggerDownload(fileName, htmlDoc, 'text/html');

        if (window.CaspianBridge && typeof window.CaspianBridge.printHtml === 'function') {
          window.CaspianBridge.printHtml(`Caspian_${isCaspianTheme ? 'Styled' : 'Native'}_PDF`, htmlDoc);
        } else if (window.CaspianBridge && typeof window.CaspianBridge.printPage === 'function') {
          window.CaspianBridge.printPage();
        } else {
          window.print();
        }
      }
    });
  });

  function triggerDownload(fileName, content, mimeType) {
    if (window.CaspianBridge && typeof window.CaspianBridge.downloadFile === 'function') {
      window.CaspianBridge.downloadFile(fileName, content, mimeType);
    } else {
      const blob = new Blob([content], { type: mimeType });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = fileName;
      a.click();
    }
  }

  // Copy Button
  if (copyBtn) {
    copyBtn.addEventListener('click', () => {
      let content = '';
      const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group, user-query, model-response');
      turns.forEach((t) => {
        content += `${t.innerText || t.textContent}\n\n---\n\n`;
      });

      if (window.CaspianBridge && typeof window.CaspianBridge.copyToClipboard === 'function') {
        window.CaspianBridge.copyToClipboard(content);
      } else {
        navigator.clipboard.writeText(content).then(() => {
          alert('Copied Caspian transcript to clipboard!');
        });
      }
    });
  }
})();
