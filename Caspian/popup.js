const DEFAULTS = {
  mode: 'light',
  accent: '#A2A9A9',
  secondary: '#1B4264',
  limit: 5,
  pruningEnabled: true,
  vaultEnabled: true,
  disabledSites: [],
  pinnedPresets: []
};

const DEFAULT_PRESETS = [
  { id: 'caspian', name: 'Caspian', accent: '#A2A9A9', secondary: '#1B4264' },
  { id: 'cyan', name: 'Cyan', accent: '#00f2fe', secondary: '#4facfe' },
  { id: 'violet', name: 'Violet', accent: '#a855f7', secondary: '#ec4899' },
  { id: 'azure', name: 'Azure', accent: '#38bdf8', secondary: '#0284c7' },
  { id: 'emerald', name: 'Emerald', accent: '#10b981', secondary: '#34d399' }
];

const DEV_FACTS = [
  "Legend has it Nigel spent his time building Caspian instead of studying for his End-Sem exams or preparing for company placement interviews tomorrow... Absolute madman! 💀",
  "Nigel's favorite music genre is 'whatever he likes at the moment'. Down for NEFFEX anytime!",
  "Nigel makes extensions and web tools that actually solve real problems.",
  "Did you know? Nigel built Lsync, Caspian, and Scrobby all with custom aesthetic UIs!"
];

let currentFactIndex = 0;
let userPinnedPresets = [];

function escapeHtml(text) {
  if (!text) return '';
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function parseMarkdownAndLaTeX(mdText) {
  if (!mdText) return '';
  let text = mdText;

  // 1. Convert code blocks
  const codeBlocks = [];
  text = text.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    const langName = lang || 'code';
    const placeholder = `___CODE_BLOCK_${codeBlocks.length}___`;
    codeBlocks.push(`
      <div class="code-block">
        <div class="code-header">
          <span>${escapeHtml(langName)}</span>
          <span>Copy code</span>
        </div>
        <pre><code>${escapeHtml(code.trim())}</code></pre>
      </div>
    `);
    return placeholder;
  });

  // 2. Escape HTML symbols outside code blocks
  text = escapeHtml(text);

  // 3. Convert LaTeX bracket blocks: [ \text{...} ] or [ ... ] to KaTeX math block \[ ... \]
  text = text.replace(/\[\s*(\\text\{[\s\S]*?)\s*\]/g, '\\[ $1 \\]');
  text = text.replace(/\[\s*(\\boxed[\s\S]*?)\s*\]/g, '\\[ $1 \\]');
  text = text.replace(/\[\s*(\\times[\s\S]*?)\s*\]/g, '\\[ $1 \\]');

  // 4. Convert Headings: # , ## , ### , ####
  text = text.replace(/^#### (.*$)/gim, '<h4>$1</h4>');
  text = text.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  text = text.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  text = text.replace(/^# (.*$)/gim, '<h1>$1</h1>');

  // 5. Horizontal rules
  text = text.replace(/^---$/gim, '<hr>');

  // 6. Bold & Italic
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');

  // 7. Inline code
  text = text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

  // 8. Bullet lists
  text = text.replace(/^\s*[\-\*]\s+(.*$)/gim, '<li>$1</li>');
  text = text.replace(/(<li>.*<\/li>)/gis, '<ul>$1</ul>');

  // 9. Numbered lists
  text = text.replace(/^\s*(\d+)\.\s+(.*$)/gim, '<li><span class="list-num">$1.</span> $2</li>');

  // 10. Restore code blocks
  codeBlocks.forEach((block, idx) => {
    text = text.replace(`___CODE_BLOCK_${idx}___`, block);
  });

  // 11. Newlines to breaks
  text = text.replace(/\n\n/g, '<br><br>').replace(/\n/g, '<br>');

  return text;
}

function hexToRgba(hex, alpha = 0.35) {
  let c = hex.replace('#', '');
  if (c.length === 3) {
    c = c.split('').map(x => x + x).join('');
  }
  const num = parseInt(c, 16);
  if (isNaN(num)) return `rgba(162, 169, 169, ${alpha})`;
  return `rgba(${(num >> 16) & 255}, ${(num >> 8) & 255}, ${num & 255}, ${alpha})`;
}

function updateThemeMode(mode) {
  const activeMode = mode || 'light';
  document.documentElement.setAttribute('data-theme', activeMode);

  const sunIcon = document.getElementById('mode-icon-sun');
  const moonIcon = document.getElementById('mode-icon-moon');
  if (sunIcon && moonIcon) {
    sunIcon.style.display = activeMode === 'dark' ? 'block' : 'none';
    moonIcon.style.display = activeMode === 'light' ? 'block' : 'none';
  }

  document.querySelectorAll('.mode-opt').forEach(opt => {
    opt.classList.toggle('active', opt.dataset.mode === activeMode);
  });
}

function updateThemeColors(theme) {
  const accent = theme.accent || DEFAULTS.accent;
  const secondary = theme.secondary || DEFAULTS.secondary;

  document.documentElement.style.setProperty('--accent', accent);
  document.documentElement.style.setProperty('--secondary', secondary);
  document.documentElement.style.setProperty('--accent-glow', hexToRgba(accent, 0.35));
}

function renderPresets(pinnedList = []) {
  userPinnedPresets = pinnedList;
  const grid = document.getElementById('preset-grid');
  if (!grid) return;

  grid.innerHTML = '';

  const allPresets = [
    ...DEFAULT_PRESETS.map(p => ({ ...p, isPinned: false })),
    ...pinnedList.map(p => ({ ...p, isPinned: true }))
  ];

  allPresets.forEach(preset => {
    const btn = document.createElement('button');
    btn.className = `preset-btn ${preset.isPinned ? 'pinned' : ''}`;
    btn.title = preset.name;

    btn.innerHTML = `
      <span class="preset-swatch" style="background: linear-gradient(135deg, ${preset.accent}, ${preset.secondary});"></span>
      <span class="preset-title-text">${preset.name}</span>
      ${preset.isPinned ? `<span class="unpin-btn" data-id="${preset.id}" title="Unpin preset">✕</span>` : ''}
    `;

    btn.addEventListener('click', (e) => {
      if (e.target.classList.contains('unpin-btn')) {
        e.stopPropagation();
        unpinPreset(preset.id);
        return;
      }

      const palette = {
        accent: preset.accent,
        secondary: preset.secondary
      };
      chrome.storage.local.set(palette, loadSettings);
    });

    grid.appendChild(btn);
  });
}

function unpinPreset(idToUnpin) {
  const updatedList = userPinnedPresets.filter(p => p.id !== idToUnpin);
  chrome.storage.local.set({ pinnedPresets: updatedList }, () => {
    renderPresets(updatedList);
  });
}

function pinCurrentGradient() {
  const accentVal = (document.getElementById('primary-hex')?.value || DEFAULTS.accent).trim();
  const secondaryVal = (document.getElementById('secondary-hex')?.value || DEFAULTS.secondary).trim();

  const exists = userPinnedPresets.some(p => p.accent.toUpperCase() === accentVal.toUpperCase() && p.secondary.toUpperCase() === secondaryVal.toUpperCase());
  const pinBtn = document.getElementById('pin-current-btn');

  if (exists) {
    if (pinBtn) {
      const origText = pinBtn.innerHTML;
      pinBtn.innerHTML = `<span>Already Pinned!</span>`;
      setTimeout(() => { pinBtn.innerHTML = origText; }, 1400);
    }
    return;
  }

  const newPreset = {
    id: 'custom_' + Date.now(),
    name: `Custom ${userPinnedPresets.length + 1}`,
    accent: accentVal,
    secondary: secondaryVal
  };

  const updatedList = [...userPinnedPresets, newPreset];
  chrome.storage.local.set({ pinnedPresets: updatedList }, () => {
    renderPresets(updatedList);

    if (pinBtn) {
      const origText = pinBtn.innerHTML;
      pinBtn.innerHTML = `<span>✨ Pinned!</span>`;
      setTimeout(() => { pinBtn.innerHTML = origText; }, 1400);
    }
  });
}

// ------------------------------------------
// BULLETPROOF DIRECT DOM EXTRACTION IN POPUP
// ------------------------------------------
function extractPageConversationPayload() {
  try {
    const isUrlTemp = window.location.href.includes('temporary-chat=true');
    const isDomTemp = !!document.querySelector('[data-testid="temporary-chat-indicator"]') ||
                      !!document.querySelector('button[aria-label*="Temporary"]') ||
                      (document.body && document.body.innerText && document.body.innerText.toLowerCase().includes('temporary chat'));
    const isTemporary = isUrlTemp || isDomTemp;

    const turns = [];
    const processedTexts = new Set();

    let turnNodes = Array.from(document.querySelectorAll('[data-message-author-role]'));
    
    if (!turnNodes || turnNodes.length === 0) {
      turnNodes = Array.from(document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group'));
    }

    turnNodes.forEach((el, idx) => {
      const roleAttr = el.getAttribute('data-message-author-role');
      const isUser = roleAttr === 'user' ||
                     !!el.querySelector('[data-message-author-role="user"]') ||
                     (el.innerText && el.innerText.includes('You said:'));
      const role = isUser ? 'User' : 'ChatGPT';

      const bodyEl = el.querySelector('.markdown') || el;
      const text = bodyEl && bodyEl.innerText ? bodyEl.innerText.trim() : '';
      const html = bodyEl ? bodyEl.innerHTML : '';

      if (text && text.length > 1 && !processedTexts.has(text)) {
        processedTexts.add(text);
        turns.push({ role, content: text, htmlContent: html, index: turns.length + 1 });
      }
    });

    if (turns.length === 0) {
      const main = document.querySelector('main');
      if (main) {
        const textBlocks = main.querySelectorAll('.whitespace-pre-wrap, .markdown');
        textBlocks.forEach((tb, i) => {
          const t = tb.innerText ? tb.innerText.trim() : '';
          const html = tb ? tb.innerHTML : '';
          if (t && !processedTexts.has(t)) {
            processedTexts.add(t);
            turns.push({ role: i % 2 === 0 ? 'User' : 'ChatGPT', content: t, htmlContent: html, index: turns.length + 1 });
          }
        });
      }
    }

    const titleEl = document.querySelector('title');
    let title = titleEl ? titleEl.textContent.replace(/ - (ChatGPT|Gemini)$/i, '').trim() : 'Saved Conversation';

    const isGemini = window.location.hostname.includes('gemini');
    const site = isGemini ? 'gemini' : 'chatgpt';
    const url = window.location.href;

    const dateStr = new Date().toLocaleString();
    let markdown = `# ${title}\n\n`;
    markdown += `*Exported via Caspian on ${dateStr}*\n`;
    markdown += `*Session Mode: ${isTemporary ? 'Temporary Chat Session' : 'Standard Session'}*\n\n---\n\n`;

    turns.forEach((t) => {
      const icon = t.role === 'User' ? '👤 **User**' : '🤖 **AI**';
      markdown += `### ${icon}\n\n${t.content}\n\n---\n\n`;
    });

    return {
      title,
      isTemporary,
      turnCount: turns.length,
      turns,
      markdown,
      site,
      url
    };
  } catch (err) {
    return { title: 'Error', isTemporary: false, turnCount: 0, turns: [], markdown: '', site: 'chatgpt', url: '' };
  }
}

function fetchCurrentTabData(callback) {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    if (!tabs || !tabs[0] || !tabs[0].id) {
      callback(null);
      return;
    }
    const tab = tabs[0];

    const url = tab.url || '';
    const isSupported = url.includes('chatgpt.com') || url.includes('gemini.google.com');

    if (!isSupported) {
      callback(null);
      return;
    }

    if (chrome.scripting && typeof chrome.scripting.executeScript === 'function') {
      chrome.scripting.executeScript({
        target: { tabId: tab.id },
        func: extractPageConversationPayload
      }, (results) => {
        if (chrome.runtime.lastError || !results || !results[0] || !results[0].result) {
          chrome.tabs.sendMessage(tab.id, { action: 'GET_CHAT_INFO' }, (res) => {
            if (!chrome.runtime.lastError && res) {
              callback(res);
            } else {
              callback(null);
            }
          });
          return;
        }
        callback(results[0].result);
      });
    } else {
      chrome.tabs.sendMessage(tab.id, { action: 'GET_CHAT_INFO' }, (res) => {
        if (!chrome.runtime.lastError && res) {
          callback(res);
        } else {
          callback(null);
        }
      });
    }
  });
}

function initTempChatVault() {
  fetchCurrentTabData((data) => {
    const countBadge = document.getElementById('temp-turn-count');
    const vaultTitle = document.getElementById('temp-vault-title');
    const vaultDesc = document.getElementById('temp-vault-desc');
    const card = document.getElementById('temp-vault-card');
    const dot = document.getElementById('temp-indicator-dot');

    chrome.storage.local.get('vaultEnabled', (storageData) => {
      const isVaultActive = storageData.vaultEnabled ?? true;

      if (!data || data.turnCount === 0) {
        if (countBadge) countBadge.textContent = '0 Messages';
        if (vaultTitle) vaultTitle.textContent = 'Temporary Chat Saver';
        if (vaultDesc && isVaultActive) vaultDesc.textContent = 'Open an active AI chat page to save or convert sessions.';
        return;
      }

      if (countBadge) countBadge.textContent = `${data.turnCount} ${data.turnCount === 1 ? 'Message' : 'Messages'}`;
      if (card) card.classList.toggle('is-temp-session', data.isTemporary);

      if (isVaultActive) {
        if (data.isTemporary) {
          if (vaultTitle) vaultTitle.textContent = 'Temporary Chat Detected';
          if (vaultDesc) vaultDesc.textContent = 'Convert this temporary session into a permanent saved chat in your history.';
          if (dot) {
            dot.style.background = 'var(--accent)';
            dot.style.boxShadow = '0 0 10px var(--accent-glow)';
          }
        } else {
          if (vaultTitle) vaultTitle.textContent = 'Temporary Chat Saver';
          if (vaultDesc) vaultDesc.textContent = 'Export or copy this conversation transcript anytime.';
          if (dot) {
            dot.style.background = 'var(--accent)';
            dot.style.boxShadow = '0 0 10px var(--accent-glow)';
          }
        }
      }
    });
  });

  // 1. Convert Chat Action
  const convertBtn = document.getElementById('convert-chat-btn');
  if (convertBtn) {
    convertBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      const origText = convertBtn.innerHTML;
      convertBtn.innerHTML = `<span>🚀 Converting...</span>`;

      fetchCurrentTabData((data) => {
        if (!data || !data.markdown || data.turnCount === 0) {
          alert('No messages found on this page. Please open an active AI conversation tab.');
          convertBtn.innerHTML = origText;
          return;
        }

        const targetUrl = (data.site === 'gemini' || (data.url && data.url.includes('gemini')))
          ? 'https://gemini.google.com/app'
          : 'https://chatgpt.com/';

        chrome.storage.local.set({ pendingTransferContext: data }, () => {
          convertBtn.innerHTML = `<span>Converted!</span>`;
          chrome.tabs.create({ url: targetUrl });
          setTimeout(() => { convertBtn.innerHTML = origText; }, 1500);
        });
      });
    });
  }

  // 2. Export Dropdown Menu Toggle
  const exportMenuBtn = document.getElementById('export-menu-btn');
  const exportDropdownMenu = document.getElementById('export-dropdown-menu');

  if (exportMenuBtn && exportDropdownMenu) {
    exportMenuBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      exportDropdownMenu.classList.toggle('show');
    });

    document.addEventListener('click', (e) => {
      if (!e.target.closest('.export-dropdown-wrapper')) {
        exportDropdownMenu.classList.remove('show');
      }
    });
  }

  // 3. Export Formats (.MD, .TXT, .PDF, Full Page Print PDF)
  document.querySelectorAll('.export-opt-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      if (exportDropdownMenu) exportDropdownMenu.classList.remove('show');

      const format = btn.dataset.format;
      const origBtnHtml = exportMenuBtn.innerHTML;
      exportMenuBtn.innerHTML = `<span>Exporting...</span>`;

      fetchCurrentTabData((data) => {
        if (!data || !data.markdown || data.turnCount === 0) {
          alert('No conversation messages found to export on this page.');
          exportMenuBtn.innerHTML = origBtnHtml;
          return;
        }

        if (format === 'md') {
          exportMarkdownFile(data);
        } else if (format === 'txt') {
          exportPlainTextFile(data);
        } else if (format === 'doc') {
          exportGoogleDocFile(data);
        } else if (format === 'pdf') {
          exportStyledPdfDocument(data);
        } else if (format === 'webpdf') {
          exportFullPagePrintPdf(data);
        }

        exportMenuBtn.innerHTML = `<span>Exported!</span>`;
        setTimeout(() => { exportMenuBtn.innerHTML = origBtnHtml; }, 1500);
      });
    });
  });

  // 4. Copy Transcript Action
  const copyBtn = document.getElementById('copy-chat-btn');
  if (copyBtn) {
    copyBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      const origText = copyBtn.innerHTML;
      copyBtn.innerHTML = `<span>Copying...</span>`;

      fetchCurrentTabData((data) => {
        if (!data || !data.markdown || data.turnCount === 0) {
          alert('No conversation messages found to copy on this page.');
          copyBtn.innerHTML = origText;
          return;
        }

        navigator.clipboard.writeText(data.markdown).then(() => {
          copyBtn.innerHTML = `<span>Copied!</span>`;
          setTimeout(() => { copyBtn.innerHTML = origText; }, 1500);
        }).catch(() => {
          const ta = document.createElement('textarea');
          ta.value = data.markdown;
          document.body.appendChild(ta);
          ta.select();
          document.execCommand('copy');
          document.body.removeChild(ta);
          copyBtn.innerHTML = `<span>Copied!</span>`;
          setTimeout(() => { copyBtn.innerHTML = origText; }, 1500);
        });
      });
    });
  }
}

// ------------------------------------------
// EXPORT FORMAT GENERATORS (.MD, .TXT, .PDF)
// ------------------------------------------
function exportMarkdownFile(data) {
  const blob = new Blob([data.markdown], { type: 'text/markdown;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const safeTitle = (data.title || 'ChatGPT_Export').replace(/[^a-z0-9_-]/gi, '_');

  const a = document.createElement('a');
  a.href = url;
  a.download = `${safeTitle}.md`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

function exportPlainTextFile(data) {
  const title = data.title || 'ChatGPT Conversation';
  const dateStr = new Date().toLocaleString();
  let text = `==================================================\n`;
  text += `${title.toUpperCase()}\n`;
  text += `Exported via Caspian on ${dateStr}\n`;
  text += `Session Mode: ${data.isTemporary ? 'Temporary Chat Session' : 'Standard Session'}\n`;
  text += `==================================================\n\n`;

  (data.turns || []).forEach((t) => {
    text += `[${t.role.toUpperCase()}]\n`;
    text += `${t.content}\n\n`;
    text += `--------------------------------------------------\n\n`;
  });

  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const safeTitle = (title || 'ChatGPT_Export').replace(/[^a-z0-9_-]/gi, '_');

  const a = document.createElement('a');
  a.href = url;
  a.download = `${safeTitle}.txt`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

function exportGoogleDocFile(data) {
  const title = data.title || 'Saved Conversation';
  const safeTitle = (title || 'Caspian_Export').replace(/[^a-z0-9_-]/gi, '_');
  const turns = data.turns || [];
  const dateStr = new Date().toLocaleString();

  let turnsHtml = '';
  turns.forEach((t, i) => {
    const isUser = t.role === 'User';
    const roleName = isUser ? 'User' : 'ChatGPT';
    const roleIcon = isUser ? '👤' : '🤖';
    const turnHeader = `${roleIcon} ${roleName} (Turn ${i + 1})`;

    const formattedContent = t.htmlContent && t.htmlContent.length > 5 ? t.htmlContent : parseMarkdownAndLaTeX(t.content);

    turnsHtml += `
      <div class="turn-container ${isUser ? 'user-turn' : 'assistant-turn'}" style="margin-bottom: 24px; padding: 14px 18px; border-radius: 8px; ${isUser ? 'background: #f8fafc; border-left: 4px solid #3b82f6;' : 'background: #ffffff; border-left: 4px solid #10b981;'}">
        <h2 id="turn-${i + 1}" style="font-size: 16pt; font-weight: bold; color: ${isUser ? '#1d4ed8' : '#047857'}; margin-top: 0; margin-bottom: 10pt; font-family: 'Arial', sans-serif;">
          ${turnHeader}
        </h2>
        <div class="turn-content" style="font-size: 11pt; line-height: 1.6; color: #1e293b;">
          ${formattedContent}
        </div>
      </div>
    `;
  });

  const docHtml = `
    <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
    <head>
      <meta charset='utf-8'>
      <title>${escapeHtml(title)}</title>
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
      <style>
        @page { size: A4; margin: 1in; }
        body { font-family: 'Arial', 'Calibri', sans-serif; font-size: 11pt; line-height: 1.5; color: #1e293b; background: #ffffff; margin: 0; padding: 0; }
        h1.doc-title { font-size: 22pt; font-weight: bold; color: #0f172a; margin-top: 0; margin-bottom: 6pt; border-bottom: 2px solid #cbd5e1; padding-bottom: 8pt; font-family: 'Arial', sans-serif; }
        p.doc-meta { font-size: 9.5pt; font-style: italic; color: #64748b; margin-top: 0; margin-bottom: 20pt; }
        h1, h2, h3, h4, h5, h6 { font-family: 'Arial', sans-serif; page-break-after: avoid; }
        h2 { font-size: 16pt; font-weight: bold; color: #0f172a; margin-top: 14pt; margin-bottom: 6pt; }
        h3 { font-size: 13pt; font-weight: bold; color: #334155; margin-top: 12pt; margin-bottom: 4pt; }
        p { margin-top: 0; margin-bottom: 8pt; }
        b, strong { font-weight: bold; color: #0f172a; }
        pre { background: #f1f5f9; padding: 10px; border-radius: 6px; font-family: 'Courier New', monospace; font-size: 9.5pt; overflow-x: auto; white-space: pre-wrap; border: 1px solid #cbd5e1; margin-bottom: 10pt; }
        code { font-family: 'Courier New', monospace; background: #f1f5f9; padding: 2px 5px; border-radius: 4px; font-size: 9.5pt; color: #0f172a; }
        blockquote { border-left: 3px solid #94a3b8; padding-left: 12px; color: #475569; margin-left: 0; font-style: italic; }
        table { border-collapse: collapse; width: 100%; margin-bottom: 12pt; }
        th, td { border: 1px solid #cbd5e1; padding: 8px 12px; text-align: left; }
        th { background-color: #f8fafc; font-weight: bold; color: #0f172a; }
        .katex { font-size: 1.1em; }
        .katex-display { display: block; margin: 1em 0; text-align: center; }
      </style>
    </head>
    <body>
      <h1 class="doc-title">${escapeHtml(title)}</h1>
      <p class="doc-meta">Exported via Caspian on ${dateStr} &bull; ${data.isTemporary ? 'Temporary Chat Session' : 'Standard Session'}</p>
      ${turnsHtml}
    </body>
    </html>
  `;

  const blob = new Blob(['\ufeff' + docHtml], { type: 'application/msword;charset=utf-8' });
  const filename = `${safeTitle}_GoogleDoc.doc`;

  if (chrome.downloads && typeof chrome.downloads.download === 'function') {
    const url = URL.createObjectURL(blob);
    chrome.downloads.download({
      url: url,
      filename: filename,
      saveAs: true
    }, () => {
      URL.revokeObjectURL(url);
    });
  } else {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function exportFullPagePrintPdf(data) {
  const title = data.title || 'Saved Conversation';
  const turns = data.turns || [];
  const dateStr = new Date().toLocaleString();

  let turnsHtml = '';
  turns.forEach((t, i) => {
    const isUser = t.role === 'User';
    const roleName = isUser ? 'User' : 'ChatGPT';
    const roleIcon = isUser ? '👤' : '🤖';
    const formattedContent = t.htmlContent && t.htmlContent.length > 5 ? t.htmlContent : parseMarkdownAndLaTeX(t.content);

    turnsHtml += `
      <div class="print-turn ${isUser ? 'user-turn' : 'assistant-turn'}">
        <div class="turn-header">
          <span class="role-icon">${roleIcon}</span>
          <span class="role-name">${roleName}</span>
          <span class="turn-num">Turn #${i + 1}</span>
        </div>
        <div class="turn-body">
          ${formattedContent}
        </div>
      </div>
    `;
  });

  const fullPrintHtml = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <title>${escapeHtml(title)}</title>
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
      <style>
        @page { size: A4; margin: 12mm; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; color: #0f172a; background: #ffffff; margin: 0; padding: 0; font-size: 13px; line-height: 1.6; }
        .print-header { border-bottom: 2px solid #cbd5e1; padding-bottom: 12px; margin-bottom: 20px; }
        .print-title { font-size: 20px; font-weight: 700; color: #0f172a; margin: 0 0 6px 0; }
        .print-meta { font-size: 11px; color: #64748b; margin: 0; font-style: italic; }
        .print-turn { margin-bottom: 18px; padding: 14px 16px; border-radius: 8px; page-break-inside: avoid; }
        .user-turn { background: #f8fafc; border: 1px solid #cbd5e1; border-left: 4px solid #3b82f6; }
        .assistant-turn { background: #ffffff; border: 1px solid #cbd5e1; border-left: 4px solid #10b981; }
        .turn-header { display: flex; align-items: center; gap: 8px; font-weight: 700; font-size: 13px; margin-bottom: 8px; color: #1e293b; border-bottom: 1px solid #f1f5f9; padding-bottom: 6px; }
        .turn-num { margin-left: auto; font-size: 11px; color: #64748b; font-weight: 600; }
        .turn-body { font-size: 13px; color: #1e293b; overflow-wrap: break-word; }
        pre { background: #0f172a !important; color: #f8fafc !important; padding: 12px !important; border-radius: 6px !important; font-family: "Courier New", monospace !important; font-size: 11px !important; overflow-x: auto !important; white-space: pre-wrap !important; margin: 10px 0 !important; }
        code { font-family: "Courier New", monospace !important; background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 11px; color: #0f172a; }
        table { border-collapse: collapse; width: 100%; margin: 12px 0; }
        th, td { border: 1px solid #cbd5e1; padding: 8px 10px; text-align: left; }
        th { background: #f8fafc; font-weight: 700; }
        .katex { font-size: 1.1em; }
        .katex-display { display: block; margin: 1em 0; text-align: center; }
      </style>
    </head>
    <body>
      <div class="print-header">
        <h1 class="print-title">${escapeHtml(title)}</h1>
        <p class="print-meta">Exported via Caspian on ${dateStr} &bull; ${data.isTemporary ? 'Temporary Chat Session' : 'Standard Session'}</p>
      </div>
      ${turnsHtml}
    </body>
    </html>
  `;

  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    if (tabs && tabs[0] && tabs[0].id) {
      chrome.scripting.executeScript({
        target: { tabId: tabs[0].id },
        func: (htmlContent) => {
          let iframe = document.getElementById('caspian-print-iframe');
          if (iframe) iframe.remove();

          iframe = document.createElement('iframe');
          iframe.id = 'caspian-print-iframe';
          iframe.style.position = 'fixed';
          iframe.style.right = '0';
          iframe.style.bottom = '0';
          iframe.style.width = '0';
          iframe.style.height = '0';
          iframe.style.border = '0';
          document.body.appendChild(iframe);

          const doc = iframe.contentWindow.document;
          doc.open();
          doc.write(htmlContent);
          doc.close();

          iframe.contentWindow.focus();
          setTimeout(() => {
            iframe.contentWindow.print();
          }, 300);
        },
        args: [fullPrintHtml]
      });
    }
  });
}

function exportStyledPdfDocument(data) {
  const title = data.title || 'ChatGPT Conversation';
  const safeTitle = (title || 'ChatGPT_Export').replace(/[^a-z0-9_-]/gi, '_');
  const turns = data.turns || [];
  const dateStr = new Date().toLocaleString();

  let turnsHtml = '';
  turns.forEach((t) => {
    const isUser = t.role === 'User';
    const roleName = isUser ? 'User' : 'ChatGPT';
    const roleIcon = isUser ? '👤' : '🤖';

    const formattedContent = t.htmlContent && t.htmlContent.length > 5 ? t.htmlContent : parseMarkdownAndLaTeX(t.content);

    turnsHtml += `
      <div class="turn-container ${isUser ? 'user-turn' : 'assistant-turn'}">
        <div class="turn-header">
          <div class="avatar ${isUser ? 'user-avatar' : 'ai-avatar'}">${roleIcon}</div>
          <span class="role-name">${roleName}</span>
        </div>
        <div class="turn-body">${formattedContent}</div>
      </div>
    `;
  });

  const fullHtml = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <title>${escapeHtml(title)}</title>
      
      <link rel="preconnect" href="https://fonts.googleapis.com">
      <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
      <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">

      <style>
        * { box-sizing: border-box; }
        body {
          font-family: 'Inter', system-ui, -apple-system, sans-serif;
          background-color: #ffffff;
          color: #0d0d0d;
          margin: 0;
          padding: 32px;
          line-height: 1.7;
          font-size: 14px;
        }

        .doc-header {
          border-bottom: 2px solid #ececf1;
          padding-bottom: 18px;
          margin-bottom: 28px;
        }
        .doc-title {
          font-size: 24px;
          font-weight: 700;
          color: #111827;
          margin: 0 0 8px 0;
        }
        .doc-meta {
          font-size: 12px;
          color: #6b7280;
          display: flex;
          gap: 16px;
        }

        .turn-container {
          margin-bottom: 22px;
          padding: 20px 24px;
          border-radius: 12px;
          border: 1px solid #e5e7eb;
          page-break-inside: avoid;
        }
        .user-turn {
          background-color: #f9fafb;
          border-color: #f3f4f6;
        }
        .assistant-turn {
          background-color: #ffffff;
          box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }

        .turn-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 12px;
          font-weight: 600;
          font-size: 13.5px;
        }
        .avatar {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
        }
        .user-avatar { background: #e5e7eb; }
        .ai-avatar { background: #10a37f; color: white; }

        .role-name { color: #374151; }

        .turn-body {
          color: #1f2937;
          font-size: 14px;
          word-break: break-word;
        }

        h1 { font-size: 22px; font-weight: 700; color: #111827; margin: 18px 0 10px 0; }
        h2 { font-size: 18px; font-weight: 700; color: #1f2937; margin: 16px 0 8px 0; border-bottom: 1px solid #f3f4f6; padding-bottom: 4px; }
        h3 { font-size: 15px; font-weight: 600; color: #374151; margin: 14px 0 6px 0; }
        h4 { font-size: 14px; font-weight: 600; color: #4b5563; margin: 12px 0 4px 0; }

        hr {
          border: none;
          border-top: 1px solid #e5e7eb;
          margin: 20px 0;
        }

        ul, ol {
          padding-left: 22px;
          margin: 10px 0;
        }

        li { margin-bottom: 6px; }

        .katex-display {
          background: #ffffff;
          padding: 12px 18px;
          border-radius: 8px;
          border: 1px solid #d1d5db;
          margin: 16px 0;
          overflow-x: auto;
          box-shadow: 0 1px 3px rgba(0,0,0,0.04);
          text-align: center;
        }

        .inline-code {
          background: #f3f4f6;
          color: #1f2937;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'JetBrains Mono', monospace;
          font-size: 12.5px;
        }

        .code-block {
          background: #1e1e1e;
          color: #d4d4d4;
          border-radius: 8px;
          overflow: hidden;
          margin: 14px 0;
          font-family: 'JetBrains Mono', monospace;
        }
        .code-header {
          background: #2d2d2d;
          padding: 6px 14px;
          font-size: 11px;
          color: #9cdcfe;
          display: flex;
          justify-content: space-between;
          border-bottom: 1px solid #3c3c3c;
          text-transform: uppercase;
        }
        pre {
          margin: 0;
          padding: 14px;
          overflow-x: auto;
          font-size: 12.5px;
          line-height: 1.5;
        }
        code { font-family: 'JetBrains Mono', monospace; }

        @media print {
          body { padding: 15px !important; background: #ffffff !important; }
          .turn-container { page-break-inside: avoid; }
        }
      </style>
    </head>
    <body>
      <div id="pdf-export-container">
        <div class="doc-header">
          <h1 class="doc-title">${escapeHtml(title)}</h1>
          <div class="doc-meta">
            <span>Exported via Caspian</span>
            <span>Date: ${dateStr}</span>
            <span>Mode: ${data.isTemporary ? 'Temporary Chat Session' : 'Standard Session'}</span>
          </div>
        </div>

        ${turnsHtml}
      </div>

      <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
      <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js"></script>
      <script>
        document.addEventListener("DOMContentLoaded", function() {
          if (window.renderMathInElement) {
            renderMathInElement(document.body, {
              delimiters: [
                {left: '$$', right: '$$', display: true},
                {left: '$', right: '$', display: false},
                {left: '\\(', right: '\\)', display: false},
                {left: '\\[', right: '\\]', display: true},
                {left: '[', right: ']', display: true}
              ],
              throwOnError: false
            });
          }
        });
      </script>
    </body>
    </html>
  `;

  // 1. Direct formatted document file download via Chrome Extension API
  const blob = new Blob([fullHtml], { type: 'text/html;charset=utf-8' });
  const reader = new FileReader();
  reader.onloadend = function() {
    chrome.downloads.download({
      url: reader.result,
      filename: `${safeTitle}_Formatted.html`,
      saveAs: true
    });
  };
  reader.readAsDataURL(blob);

  // 2. Inject print iframe directly on active tab (where Caspian HAS 100% HOST PERMISSION!)
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    if (tabs && tabs[0]) {
      chrome.scripting.executeScript({
        target: { tabId: tabs[0].id },
        func: (htmlData) => {
          const iframe = document.createElement('iframe');
          iframe.style.position = 'fixed';
          iframe.style.right = '0';
          iframe.style.bottom = '0';
          iframe.style.width = '0';
          iframe.style.height = '0';
          iframe.style.border = '0';
          document.body.appendChild(iframe);

          const doc = iframe.contentWindow.document;
          doc.open();
          doc.write(htmlData);
          doc.close();

          setTimeout(() => {
            iframe.contentWindow.focus();
            iframe.contentWindow.print();
            setTimeout(() => iframe.remove(), 2500);
          }, 450);
        },
        args: [fullHtml]
      });
    }
  });
}

// Interactive Card Toggle Handlers
function setupCardToggleHandlers() {
  const pruningCard = document.getElementById('pruning-status-card');
  if (pruningCard) {
    pruningCard.addEventListener('click', () => {
      chrome.storage.local.get('pruningEnabled', (data) => {
        const next = !(data.pruningEnabled ?? true);
        chrome.storage.local.set({ pruningEnabled: next }, loadSettings);
      });
    });
  }

  const vaultCard = document.getElementById('temp-vault-card');
  if (vaultCard) {
    vaultCard.addEventListener('click', (e) => {
      if (e.target.closest('.temp-act-btn') || e.target.closest('.export-dropdown-wrapper')) return;
      chrome.storage.local.get('vaultEnabled', (data) => {
        const next = !(data.vaultEnabled ?? true);
        chrome.storage.local.set({ vaultEnabled: next }, loadSettings);
      });
    });
  }
}

// Sites Toggles Setup (ChatGPT & Gemini)
function setupSiteToggles(disabledSites = []) {
  const domains = ['chatgpt.com', 'gemini.google.com'];
  
  domains.forEach(domain => {
    const input = document.querySelector(`input[data-domain="${domain}"]`);
    if (!input) return;

    input.checked = !disabledSites.includes(domain);

    input.onchange = () => {
      chrome.storage.local.get('disabledSites', (data) => {
        let list = data.disabledSites || [];
        if (!input.checked) {
          if (!list.includes(domain)) list = [...list, domain];
        } else {
          list = list.filter(d => d !== domain);
        }
        chrome.storage.local.set({ disabledSites: list });
      });
    };
  });
}

function loadSettings() {
  chrome.storage.local.get(['pruningEnabled', 'vaultEnabled', 'enabled', 'limit', 'mode', 'accent', 'secondary', 'pinnedPresets', 'disabledSites'], (data) => {
    const currentMode = data.mode || DEFAULTS.mode;
    updateThemeMode(currentMode);
    updateThemeColors(data);

    renderPresets(data.pinnedPresets || []);

    const accentVal = data.accent || DEFAULTS.accent;
    const secondaryVal = data.secondary || DEFAULTS.secondary;

    const primaryHexInput = document.getElementById('primary-hex');
    const accentPicker = document.getElementById('accent-picker');
    if (primaryHexInput) primaryHexInput.value = accentVal.toUpperCase();
    if (accentPicker) accentPicker.value = accentVal.length === 7 ? accentVal : DEFAULTS.accent;

    const secondaryHexInput = document.getElementById('secondary-hex');
    const secondaryPicker = document.getElementById('secondary-picker');
    if (secondaryHexInput) secondaryHexInput.value = secondaryVal.toUpperCase();
    if (secondaryPicker) secondaryPicker.value = secondaryVal.length === 7 ? secondaryVal : DEFAULTS.secondary;

    // Pruning Card State
    const pruningEnabled = data.pruningEnabled ?? (data.enabled ?? true);
    const pruningCard = document.getElementById('pruning-status-card');
    const statusDot = document.getElementById('status-indicator');
    const statusText = document.getElementById('status-state-text');
    const statusSub = document.getElementById('status-sub-text');

    if (pruningCard) pruningCard.classList.toggle('is-disabled', !pruningEnabled);
    if (statusDot) {
      statusDot.classList.toggle('active', pruningEnabled);
      statusDot.classList.toggle('inactive', !pruningEnabled);
    }
    if (statusText) {
      statusText.textContent = pruningEnabled ? 'Chat Pruning Active' : 'Chat Pruning Disabled';
    }
    if (statusSub) {
      statusSub.textContent = pruningEnabled ? 'Lag Fixer & DOM Limit Active' : 'Click card to activate Chat Pruning';
    }

    // Vault Card State
    const vaultEnabled = data.vaultEnabled ?? true;
    const vaultCard = document.getElementById('temp-vault-card');
    const tempDot = document.getElementById('temp-indicator-dot');
    const tempDesc = document.getElementById('temp-vault-desc');

    if (vaultCard) vaultCard.classList.toggle('is-disabled', !vaultEnabled);
    if (tempDot) {
      tempDot.classList.toggle('active', vaultEnabled);
      tempDot.classList.toggle('inactive', !vaultEnabled);
    }
    if (tempDesc) {
      if (!vaultEnabled) {
        tempDesc.textContent = 'Temporary Chat Vault Disabled (Click card to enable)';
      } else {
        fetchCurrentTabData((tabData) => {
          if (tabData && tabData.isTemporary) {
            tempDesc.textContent = 'Convert this temporary session into a permanent saved chat in your history.';
          } else {
            tempDesc.textContent = 'Export or copy this conversation transcript anytime.';
          }
        });
      }
    }

    // Master Power Ring State
    const isMasterOn = pruningEnabled || vaultEnabled;
    const powerToggle = document.getElementById('power-toggle');
    if (powerToggle) powerToggle.classList.toggle('active', isMasterOn);

    // Limit badge
    const limit = data.limit || DEFAULTS.limit;
    const limitBadge = document.getElementById('active-limit-badge');
    if (limitBadge) {
      limitBadge.textContent = limit >= 9999 ? '∞ All' : `${limit} ${limit === 1 ? 'Turn' : 'Turns'}`;
    }

    document.querySelectorAll('.pill').forEach(p => {
      p.classList.toggle('active', parseInt(p.dataset.val) === limit);
    });

    setupSiteToggles(data.disabledSites || []);
  });
}

// Floating Dock Slider & Navigation Handler (3 Tabs)
function updateDockIndicator(targetTab) {
  const activeBtn = document.querySelector(`.dock-tab-btn[data-target="${targetTab}"]`);
  const indicator = document.getElementById('dock-indicator');
  const dock = document.getElementById('floating-dock');

  if (activeBtn && indicator && dock) {
    const btnRect = activeBtn.getBoundingClientRect();
    const dockRect = dock.getBoundingClientRect();

    indicator.style.left = `${btnRect.left - dockRect.left}px`;
    indicator.style.width = `${btnRect.width}px`;
  }
}

function switchTab(targetTab) {
  document.querySelectorAll('.dock-tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.target === targetTab);
  });

  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.toggle('active', pane.id === `tab-${targetTab}`);
  });

  updateDockIndicator(targetTab);
}

document.querySelectorAll('.dock-tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    switchTab(btn.dataset.target);
  });
});

// Light/Dark Mode Toggle
const modeToggleHeaderBtn = document.getElementById('mode-toggle-btn');
if (modeToggleHeaderBtn) {
  modeToggleHeaderBtn.addEventListener('click', () => {
    chrome.storage.local.get('mode', (data) => {
      const currentMode = data.mode || DEFAULTS.mode;
      const newMode = currentMode === 'light' ? 'dark' : 'light';
      chrome.storage.local.set({ mode: newMode }, () => {
        updateThemeMode(newMode);
        loadSettings();
      });
    });
  });
}

document.querySelectorAll('.mode-opt').forEach(opt => {
  opt.addEventListener('click', () => {
    const newMode = opt.dataset.mode;
    chrome.storage.local.set({ mode: newMode }, () => {
      updateThemeMode(newMode);
      loadSettings();
    });
  });
});

// Pin Current Gradient Button
const pinCurrentBtn = document.getElementById('pin-current-btn');
if (pinCurrentBtn) {
  pinCurrentBtn.addEventListener('click', pinCurrentGradient);
}

// Gradient Start Color Picker & Hex Sync
const accentPicker = document.getElementById('accent-picker');
if (accentPicker) {
  accentPicker.addEventListener('input', (e) => {
    const val = e.target.value;
    chrome.storage.local.set({ accent: val }, loadSettings);
  });
}

const primaryHexInput = document.getElementById('primary-hex');
if (primaryHexInput) {
  primaryHexInput.addEventListener('input', (e) => {
    let val = e.target.value.trim();
    if (!val.startsWith('#')) val = '#' + val;
    if (/^#[0-9A-F]{6}$/i.test(val)) {
      chrome.storage.local.set({ accent: val }, loadSettings);
    }
  });
}

// Gradient End Color Picker & Hex Sync
const secondaryPicker = document.getElementById('secondary-picker');
if (secondaryPicker) {
  secondaryPicker.addEventListener('input', (e) => {
    const val = e.target.value;
    chrome.storage.local.set({ secondary: val }, loadSettings);
  });
}

const secondaryHexInput = document.getElementById('secondary-hex');
if (secondaryHexInput) {
  secondaryHexInput.addEventListener('input', (e) => {
    let val = e.target.value.trim();
    if (!val.startsWith('#')) val = '#' + val;
    if (/^#[0-9A-F]{6}$/i.test(val)) {
      chrome.storage.local.set({ secondary: val }, loadSettings);
    }
  });
}

// Smart Palette Import
const paletteImportInput = document.getElementById('palette-import');
if (paletteImportInput) {
  paletteImportInput.addEventListener('input', (e) => {
    const input = e.target.value;
    const hexMatch = input.match(/[A-Fa-f0-9]{6}/g);
    
    if (hexMatch && hexMatch.length >= 2) {
      const palette = {
        accent: '#' + hexMatch[0],
        secondary: '#' + hexMatch[1]
      };
      chrome.storage.local.set(palette, loadSettings);
      e.target.value = "Palette Applied!";
      setTimeout(() => { e.target.value = ""; }, 1500);
    } else if (hexMatch && hexMatch.length >= 1) {
      chrome.storage.local.set({ accent: '#' + hexMatch[0] }, loadSettings);
    }
  });
}

// Message Limit Pills Selection
document.querySelectorAll('.pill').forEach(p => {
  p.addEventListener('click', () => {
    chrome.storage.local.set({ limit: parseInt(p.dataset.val) }, loadSettings);
  });
});

// Master Power Toggle Switch
const powerToggleBtn = document.getElementById('power-toggle');
if (powerToggleBtn) {
  powerToggleBtn.addEventListener('click', () => {
    chrome.storage.local.get(['pruningEnabled', 'vaultEnabled'], (data) => {
      const isCurrentlyOn = (data.pruningEnabled ?? true) || (data.vaultEnabled ?? true);
      const nextState = !isCurrentlyOn;
      chrome.storage.local.set({
        pruningEnabled: nextState,
        vaultEnabled: nextState,
        enabled: nextState
      }, loadSettings);
    });
  });
}

// Reset Defaults
const resetBtn = document.getElementById('reset-link');
if (resetBtn) {
  resetBtn.addEventListener('click', () => {
    chrome.storage.local.set(DEFAULTS, loadSettings);
  });
}

// Interactive Nigel Facts Bubble
const factBubble = document.getElementById('dev-fact-bubble');
const factText = document.getElementById('fact-text');
if (factBubble && factText) {
  factBubble.addEventListener('click', () => {
    factText.style.opacity = '0';
    setTimeout(() => {
      let nextIndex;
      do {
        nextIndex = Math.floor(Math.random() * DEV_FACTS.length);
      } while (nextIndex === currentFactIndex && DEV_FACTS.length > 1);
      
      currentFactIndex = nextIndex;
      factText.textContent = DEV_FACTS[currentFactIndex];
      factText.style.opacity = '1';
    }, 180);
  });
}

// Initialize Position, Settings & Temp Vault
window.addEventListener('DOMContentLoaded', () => {
  loadSettings();
  initTempChatVault();
  setupCardToggleHandlers();
  setTimeout(() => updateDockIndicator('engine'), 50);
});