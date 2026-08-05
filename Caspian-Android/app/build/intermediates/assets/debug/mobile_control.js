// ======================================================
// CASPIAN ANDROID - MOBILE CONTROL SHEET JS
// ======================================================

document.addEventListener('DOMContentLoaded', () => {
  const floatingBtn = document.getElementById('caspian-floating-btn');
  const sheetBackdrop = document.getElementById('sheet-backdrop');
  const bottomSheet = document.getElementById('bottom-sheet');
  const themeToggleBtn = document.getElementById('theme-toggle-btn');
  const powerToggleBtn = document.getElementById('power-toggle-btn');
  const convertBtn = document.getElementById('convert-btn');
  const copyBtn = document.getElementById('copy-btn');
  const exportDropdownTrigger = document.getElementById('export-dropdown-trigger');
  const exportMenu = document.getElementById('export-menu');

  let activeTheme = 'light';
  let limitVal = 5;
  let globalActive = true;

  // Toggle Bottom Sheet
  function openSheet() {
    sheetBackdrop.classList.add('active');
    bottomSheet.classList.add('active');
  }

  function closeSheet() {
    sheetBackdrop.classList.remove('active');
    bottomSheet.classList.remove('active');
    exportMenu.classList.remove('active');
  }

  if (floatingBtn) floatingBtn.addEventListener('click', openSheet);
  if (sheetBackdrop) sheetBackdrop.addEventListener('click', closeSheet);

  // Theme Toggle
  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', () => {
      activeTheme = activeTheme === 'light' ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', activeTheme);
    });
  }

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

      // Send setting to Android Native Bridge
      if (window.CaspianBridge && typeof window.CaspianBridge.saveSetting === 'function') {
        window.CaspianBridge.saveSetting('globalActive', JSON.stringify(globalActive));
      }

      window.postMessage({ type: 'CASPIAN_SYNC_SETTINGS', payload: { globalActive } }, '*');
    });
  }

  // Limit Pills Selection
  document.querySelectorAll('.limit-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.limit-pill').forEach(p => p.classList.remove('active'));
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

  // Export Dropdown Trigger
  if (exportDropdownTrigger && exportMenu) {
    exportDropdownTrigger.addEventListener('click', (e) => {
      e.stopPropagation();
      exportMenu.classList.toggle('active');
    });
  }

  // Export Option Handlers
  document.querySelectorAll('.export-opt-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const fmt = btn.dataset.fmt;
      exportMenu.classList.remove('active');
      
      let title = document.title || 'ChatGPT Conversation';
      let safeTitle = title.replace(/[^a-z0-9_-]/gi, '_');
      let dateStr = new Date().toLocaleString();

      if (fmt === 'md') {
        let content = `# ${title}\n\n*Exported via Caspian Mobile on ${dateStr}*\n\n---\n\n`;
        const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group');
        turns.forEach((t, i) => {
          content += `### Turn ${i + 1}\n\n${t.innerText || t.textContent}\n\n---\n\n`;
        });
        triggerDownload(`${safeTitle}_Caspian_Exported.md`, content, 'text/markdown');
      } else if (fmt === 'txt') {
        let content = `======================================\n${title.toUpperCase()}\nExported via Caspian Mobile on ${dateStr}\n======================================\n\n`;
        const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group');
        turns.forEach((t, i) => {
          content += `[TURN ${i + 1}]\n${t.innerText || t.textContent}\n\n--------------------------------------\n\n`;
        });
        triggerDownload(`${safeTitle}_Caspian_Exported.txt`, content, 'text/plain');
      } else if (fmt === 'doc') {
        let content = `<html><body><h1>${title}</h1><p>Exported via Caspian Mobile on ${dateStr}</p>`;
        const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group');
        turns.forEach((t, i) => {
          content += `<h3>Turn ${i + 1}</h3><div>${t.innerHTML}</div><hr>`;
        });
        content += `</body></html>`;
        triggerDownload(`${safeTitle}_Caspian_Exported.doc`, content, 'application/msword');
      } else if (fmt === 'nativepdf' || fmt === 'styledpdf') {
        window.print();
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
      const turns = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group');
      turns.forEach((t) => {
        content += `${t.innerText || t.textContent}\n\n---\n\n`;
      });
      navigator.clipboard.writeText(content).then(() => {
        alert('Copied Caspian transcript to clipboard!');
      });
    });
  }
});
