const DEFAULTS = {
  mode: 'light',
  accent: '#A2A9A9',
  secondary: '#1B4264',
  customBgColor: '',
  ui_zoom: 95,
  font_scale: 100,
  limit: 5,
  pruningEnabled: true,
  vaultEnabled: true,
  chat_pruning_mode: 'sliding_window',
  yt_feed_limit_enabled: true,
  yt_feed_limit: 12,
  yt_not_interested_enabled: true,
  flow_speed_enabled: true,
  flow_speed_val: 1.0,
  flow_speed_cycle_list: '1, 1.25, 1.5, 1.75, 2, 2.5, 3',
  flow_speed_shortcut_reset: 'alt+s',
  flow_speed_shortcut_cycle: 'alt+d',
  flow_speed_shortcut_up: ']',
  flow_speed_shortcut_down: '[',
  flow_speed_show_hud: true,
  flow_speed_badge_enabled: true,
  flow_speed_card_collapsed: false,
  rf_capture_scope: 'full',
  rf_scroll_delay: 150,
  rf_card_collapsed: false,
  disabledSites: [],
  pinnedPresets: []
};

const DEFAULT_PRESETS = [
  { id: 'caspian', name: 'Caspian', accent: '#A2A9A9', secondary: '#1B4264' },
  { id: 'sunset', name: 'Sunset', accent: '#ff512f', secondary: '#dd2476' },
  { id: 'cyan', name: 'Neon', accent: '#00f2fe', secondary: '#4facfe' },
  { id: 'violet', name: 'Violet', accent: '#a855f7', secondary: '#ec4899' },
  { id: 'emerald', name: 'Emerald', accent: '#10b981', secondary: '#047857' },
  { id: 'crimson', name: 'Crimson', accent: '#e11d48', secondary: '#881337' },
  { id: 'azure', name: 'Azure', accent: '#38bdf8', secondary: '#0284c7' }
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

function renderTableHtml(tableLines) {
  if (tableLines.length < 2) return tableLines.join('\n');

  const formatCell = (cStr) => {
    let c = escapeHtml(cStr);
    c = c.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    c = c.replace(/\*(.*?)\*/g, '<em>$1</em>');
    c = c.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');
    return c;
  };

  const parseRow = (rowStr) => {
    return rowStr.split('|').slice(1, -1).map(cell => cell.trim());
  };

  const headers = parseRow(tableLines[0]);
  let startIdx = 1;
  if (tableLines.length > 1 && tableLines[1].includes('---')) {
    startIdx = 2;
  }

  let html = `<div class="table-wrapper" style="overflow-x: auto; margin: 16px 0; page-break-inside: avoid; break-inside: avoid;"><table style="width: 100%; border-collapse: collapse; margin: 12px 0; font-size: 13.5px; border: 1px solid #cbd5e1; page-break-inside: avoid;">`;
  
  html += `<thead style="background-color: #f8fafc; border-bottom: 2px solid #cbd5e1;"><tr>`;
  headers.forEach(h => {
    html += `<th style="padding: 10px 14px; text-align: left; font-weight: 700; color: #0f172a; border: 1px solid #cbd5e1;">${formatCell(h)}</th>`;
  });
  html += `</tr></thead>`;

  html += `<tbody>`;
  for (let i = startIdx; i < tableLines.length; i++) {
    const cells = parseRow(tableLines[i]);
    const rowBg = i % 2 === 0 ? '#ffffff' : '#f8fafc';
    html += `<tr style="background-color: ${rowBg}; page-break-inside: avoid; break-inside: avoid;">`;
    cells.forEach(c => {
      html += `<td style="padding: 8px 14px; color: #334155; border: 1px solid #cbd5e1;">${formatCell(c)}</td>`;
    });
    html += `</tr>`;
  }
  html += `</tbody></table></div>`;

  return html;
}

function parseMarkdownTables(text) {
  const lines = text.split('\n');
  let inTable = false;
  let tableLines = [];
  let result = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();
    if (line.startsWith('|') && line.endsWith('|')) {
      inTable = true;
      tableLines.push(line);
    } else {
      if (inTable) {
        result.push(renderTableHtml(tableLines));
        inTable = false;
        tableLines = [];
      }
      result.push(lines[i]);
    }
  }
  if (inTable && tableLines.length > 0) {
    result.push(renderTableHtml(tableLines));
  }
  return result.join('\n');
}

function parseMarkdownAndLaTeX(mdText) {
  if (!mdText) return '';
  let text = mdText;

  // 1. Protect LaTeX math blocks BEFORE escapeHtml runs
  const mathBlocks = [];
  text = text.replace(/(\\\[[\s\S]*?\\\]|\$\$[\s\S]*?\$\$|\\\(.*?\\\))/g, (match) => {
    const placeholder = `___MATH_BLOCK_${mathBlocks.length}___`;
    mathBlocks.push(match);
    return placeholder;
  });

  // 2. Protect Code blocks
  const codeBlocks = [];
  text = text.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    const langName = lang || 'code';
    const placeholder = `___CODE_BLOCK_${codeBlocks.length}___`;
    codeBlocks.push(`
      <div class="code-block" style="page-break-inside: avoid; break-inside: avoid;">
        <div class="code-header">
          <span>${escapeHtml(langName)}</span>
          <span>Copy code</span>
        </div>
        <pre style="page-break-inside: avoid; break-inside: avoid;"><code>${escapeHtml(code.trim())}</code></pre>
      </div>
    `);
    return placeholder;
  });

  // 3. Parse Markdown Tables into HTML <table> elements
  const tableBlocks = [];
  text = text.replace(/(?:^|\n)(\|[^\n]+\|\n\|[-:\s|]+\|\n(?:\|[^\n]+\|\n?)+)/g, (match, tblStr) => {
    const placeholder = `___TABLE_BLOCK_${tableBlocks.length}___`;
    const lines = tblStr.trim().split('\n');
    tableBlocks.push(renderTableHtml(lines));
    return '\n' + placeholder + '\n';
  });

  // 4. Escape HTML symbols outside code blocks & tables
  text = escapeHtml(text);

  // 5. Restore LaTeX math blocks safely
  mathBlocks.forEach((mBlock, idx) => {
    text = text.replace(`___MATH_BLOCK_${idx}___`, mBlock);
  });

  // 6. Convert Headings: # , ## , ### , ####
  text = text.replace(/^#### (.*$)/gim, '<h4>$1</h4>');
  text = text.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  text = text.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  text = text.replace(/^# (.*$)/gim, '<h1>$1</h1>');

  // 7. Horizontal rules
  text = text.replace(/^---$/gim, '<hr>');

  // 8. Bold & Italic
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');

  // 9. Inline code
  text = text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

  // 10. Bullet & Numbered lists
  text = text.replace(/^\s*[\-\*]\s+(.*$)/gim, '<li>$1</li>');
  text = text.replace(/(<li>.*<\/li>)/gis, '<ul>$1</ul>');
  text = text.replace(/^\s*(\d+)\.\s+(.*$)/gim, '<li><span class="list-num">$1.</span> $2</li>');

  // 11. Restore code blocks & table blocks
  codeBlocks.forEach((block, idx) => {
    text = text.replace(`___CODE_BLOCK_${idx}___`, block);
  });
  tableBlocks.forEach((tBlock, idx) => {
    text = text.replace(`___TABLE_BLOCK_${idx}___`, tBlock);
  });

  // 12. Clean spacing
  text = text.replace(/\n\n/g, '<br>').replace(/\n/g, '<br>');

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

function getElevatedToneColor(hexOrRgb, opacity = 0.94) {
  if (!hexOrRgb) return '';
  let hex = hexOrRgb.replace('#', '');
  if (hex.length === 3) hex = hex.split('').map(x => x + x).join('');
  if (hex.length !== 6) return '';

  let r = parseInt(hex.substring(0, 2), 16);
  let g = parseInt(hex.substring(2, 4), 16);
  let b = parseInt(hex.substring(4, 6), 16);

  r = Math.min(255, r + 14);
  g = Math.min(255, g + 16);
  b = Math.min(255, b + 22);

  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
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
  const mode = theme.mode || DEFAULTS.mode;

  document.documentElement.style.setProperty('--accent', accent);
  document.documentElement.style.setProperty('--secondary', secondary);
  document.documentElement.style.setProperty('--accent-glow', hexToRgba(accent, 0.35));

  if (mode === 'dark') {
    if (theme.customBgColor) {
      document.documentElement.style.setProperty('--bg-deep', theme.customBgColor);
      document.body.style.background = theme.customBgColor;
      const dockElevated = getElevatedToneColor(theme.customBgColor, 0.94);
      if (dockElevated) {
        document.documentElement.style.setProperty('--dock-bg', dockElevated);
      }
    } else {
      document.documentElement.style.removeProperty('--bg-deep');
      document.documentElement.style.setProperty('--dock-bg', 'rgba(14, 19, 32, 0.94)');
      document.body.style.background = '';
    }
  } else {
    document.documentElement.style.removeProperty('--bg-deep');
    document.documentElement.style.setProperty('--dock-bg', 'rgba(255, 255, 255, 0.96)');
    document.body.style.background = '';
  }
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
async function extractPageConversationPayload() {
  try {
    console.log('==========================================');
    console.log('🌊 CASPIAN API EXTRACTION DEBUGGER INITIALIZED');
    console.log('==========================================');

    const isUrlTemp = window.location.href.includes('temporary-chat=true');
    const isDomTemp = !!document.querySelector('[data-testid="temporary-chat-indicator"]') ||
      !!document.querySelector('button[aria-label*="Temporary"]') ||
      (document.body && document.body.innerText && document.body.innerText.toLowerCase().includes('temporary chat'));
    const isTemporary = isUrlTemp || isDomTemp;

    const titleEl = document.querySelector('title') || document.querySelector('h1');
    let title = titleEl ? titleEl.textContent.replace(/ - (ChatGPT|Gemini)$/i, '').replace(/^(ChatGPT|Gemini) - /i, '').trim() : 'Saved Conversation';
    if (!title || title === 'ChatGPT') title = 'Saved Conversation';

    const url = window.location.href;
    console.log('Caspian API Debug: Page URL =', url);

    const match = url.match(/\/c\/([a-f0-9-]+)/i);
    const conversationId = match ? match[1] : null;
    console.log('Caspian API Debug: Extracted Conversation ID =', conversationId);

    let turns = [];

    // LAYER A: Fetch directly from ChatGPT internal Backend API
    if (conversationId) {
      let token = null;

      // Token Path 1: Check window.__NEXT_DATA__
      try {
        if (window.__NEXT_DATA__ && window.__NEXT_DATA__.props && window.__NEXT_DATA__.props.pageProps) {
          token = window.__NEXT_DATA__.props.pageProps.accessToken;
          if (token) console.log('Caspian API Debug: Access token retrieved from __NEXT_DATA__');
        }
      } catch (e) {}

      // Token Path 2: Fetch /api/auth/session
      if (!token) {
        try {
          console.log('Caspian API Debug: Fetching /api/auth/session...');
          const sResp = await fetch('https://chatgpt.com/api/auth/session', { credentials: 'include' });
          console.log('Caspian API Debug: /api/auth/session HTTP status =', sResp.status);
          if (sResp.ok) {
            const sJson = await sResp.json();
            if (sJson && sJson.accessToken) {
              token = sJson.accessToken;
              console.log('Caspian API Debug: Access token retrieved from session endpoint');
            }
          }
        } catch (se) {
          console.error('Caspian API Debug: Session fetch exception:', se);
        }
      }

      console.log('Caspian API Debug: Final Bearer Token Present =', !!token);

      const headers = { 'Accept': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;

      console.log(`Caspian API Debug: Requesting https://chatgpt.com/backend-api/conversation/${conversationId}...`);

      try {
        const resp = await fetch(`https://chatgpt.com/backend-api/conversation/${conversationId}`, {
          headers,
          credentials: 'include'
        });

        console.log('Caspian API Debug: Backend API HTTP Status =', resp.status);

        if (resp.ok) {
          const json = await resp.json();
          console.log('Caspian API Debug: API JSON payload received successfully!');
          console.log('Caspian API Debug: current_node =', json.current_node);
          console.log('Caspian API Debug: Total mapping nodes =', json.mapping ? Object.keys(json.mapping).length : 0);

          if (json && json.mapping && json.current_node) {
            const mapping = json.mapping;
            let currId = json.current_node;
            const activeNodes = [];

            while (currId && mapping[currId]) {
              const node = mapping[currId];
              if (node.message && node.message.content && node.message.content.parts) {
                const author = node.message.author ? node.message.author.role : 'assistant';
                if (author === 'user' || author === 'assistant') {
                  const textParts = node.message.content.parts.filter(p => typeof p === 'string').join('\n').trim();
                  if (textParts && textParts.length > 0) {
                    activeNodes.unshift({
                      id: node.id,
                      role: author === 'user' ? 'User' : 'ChatGPT',
                      content: textParts
                    });
                  }
                }
              }
              currId = node.parent;
            }

            console.log('Caspian API Debug: Parent chain traversed! Total active turns =', activeNodes.length);

            const processedTexts = new Set();
            activeNodes.forEach(node => {
              if (!processedTexts.has(node.content)) {
                processedTexts.add(node.content);
                turns.push({
                  role: node.role,
                  content: node.content,
                  htmlContent: '',
                  index: turns.length + 1
                });
              }
            });

            console.log('Caspian API Debug: 🎉 SUCCESS! Extracted', turns.length, 'turns strictly via API method!');
          } else {
            console.warn('Caspian API Debug: JSON mapping or current_node was missing in response!');
          }
        } else {
          const errBody = await resp.text();
          console.error('Caspian API Debug: Backend API failed with status', resp.status, 'Error Body:', errBody.substring(0, 300));
        }
      } catch (fe) {
        console.error('Caspian API Debug: Backend API fetch exception:', fe);
      }
    } else {
      console.warn('Caspian API Debug: No Conversation ID found in URL!');
    }

    if (turns.length === 0) {
      console.log('Caspian API Debug: Falling back to DOM query for temporary/unsaved session...');
      const turnElements = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group');
      turnElements.forEach((el, idx) => {
        const isUser = el.querySelector('[data-message-author-role="user"]') || el.textContent.includes('User') || idx % 2 === 0;
        const role = isUser ? 'User' : 'ChatGPT';

        const contentEl = el.querySelector('.markdown') || el.querySelector('[data-message-author-role]') || el;
        const textContent = (contentEl.innerText || contentEl.textContent || '').trim();

        if (textContent && textContent.length > 0) {
          turns.push({
            role,
            content: textContent,
            htmlContent: contentEl.innerHTML || '',
            index: turns.length + 1
          });
        }
      });
      console.log('Caspian API Debug: DOM Fallback Extracted', turns.length, 'turns!');
    }

    const isGemini = window.location.hostname.includes('gemini');
    const site = isGemini ? 'gemini' : 'chatgpt';

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
    console.error('Caspian API Debug: Global extraction error:', err);
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

      if (format === 'webpdf') {
        fetchCurrentTabData((data) => {
          if (!data || !data.turns || data.turnCount === 0) {
            alert('No conversation messages found to print on this page.');
            exportMenuBtn.innerHTML = origBtnHtml;
            return;
          }
          exportStyledPdfDocument(data);
          exportMenuBtn.innerHTML = `<span>Exported!</span>`;
          setTimeout(() => { exportMenuBtn.innerHTML = origBtnHtml; }, 1500);
        });
        return;
      } else if (format === 'pdf') {
        fetchCurrentTabData((data) => {
          if (!data || !data.turns || data.turnCount === 0) {
            alert('No conversation messages found to print on this page.');
            exportMenuBtn.innerHTML = origBtnHtml;
            return;
          }
          exportNativePdfDocument(data);
          exportMenuBtn.innerHTML = `<span>Exported!</span>`;
          setTimeout(() => { exportMenuBtn.innerHTML = origBtnHtml; }, 1500);
        });
        return;
      }

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
  const safeTitle = (data.title || 'ChatGPT_Export').trim();

  const a = document.createElement('a');
  a.href = url;
  a.download = `${safeTitle} Caspian_Exported.md`;
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
  const safeTitle = (title || 'ChatGPT_Export').trim();

  const a = document.createElement('a');
  a.href = url;
  a.download = `${safeTitle} Caspian_Exported.txt`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

function exportGoogleDocFile(data) {
  const title = data.title || 'Saved Conversation';
  const safeTitle = (title || 'ChatGPT_Export').trim();
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
  const filename = `${safeTitle} Caspian_Exported.doc`;

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
}

function formatMathSymbols(mathStr) {
  return mathStr
    .replace(/\\boxed\{([\s\S]*?)\}/gi, '$1')
    .replace(/\\Sigma/g, 'Σ')
    .replace(/\\delta/g, 'δ')
    .replace(/\\epsilon/g, 'ε')
    .replace(/\\in/g, '∈')
    .replace(/\\notin/g, '∉')
    .replace(/\\cup/g, '∪')
    .replace(/\\cap/g, '∩')
    .replace(/\\rightarrow/g, '→')
    .replace(/\\leftarrow/g, '←')
    .replace(/\\emptyset/g, '∅')
    .replace(/\\times/g, '×')
    .replace(/\\text\{([\s\S]*?)\}/g, '$1')
    .replace(/q_0/g, 'q₀')
    .replace(/2\^n/g, '2ⁿ')
    .replace(/q_(\d+)/g, 'q$1');
}

function parseMarkdownAndLaTeX(mdText) {
  if (!mdText) return '';
  let text = mdText;

  // 1. Process LaTeX boxed & display equations into mathBlocks placeholders
  const mathBlocks = [];

  // Match bracketed display math blocks: [\n M=(Q,\Sigma,\delta,q_0,F) \n] or \[ ... \]
  text = text.replace(/(?:^|\n)\s*\[\s*([\s\S]*?(?:\\Sigma|\\delta|\\epsilon|\\in|\\notin|\\cup|\\cap|\\rightarrow|\\leftarrow|\\emptyset|\\times|\\boxed|q_0|q_\d+)[\s\S]*?)\s*\]\s*(?:\n|$)/gi, (match, inner) => {
    const formatted = formatMathSymbols(inner.trim());
    const placeholder = `___MATH_BLOCK_${mathBlocks.length}___`;
    mathBlocks.push(`<div class="katex-display-box" style="text-align: center; margin: 16px 0; page-break-inside: avoid; break-inside: avoid;"><span style="display: inline-block; border: 1.5px solid #0f172a; padding: 6px 18px; border-radius: 4px; font-family: 'Times New Roman', Times, serif; font-size: 16.5px; font-style: italic; background: #ffffff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); color: #0f172a;">${formatted}</span></div>`);
    return '\n' + placeholder + '\n';
  });

  text = text.replace(/\\\[\s*\\boxed\{([\s\S]*?)\}\s*\\\]/gi, (match, inner) => {
    const formatted = formatMathSymbols(inner);
    const placeholder = `___MATH_BLOCK_${mathBlocks.length}___`;
    mathBlocks.push(`<div class="katex-display-box" style="text-align: center; margin: 18px 0; page-break-inside: avoid; break-inside: avoid;"><span style="display: inline-block; border: 1.5px solid #0f172a; padding: 6px 18px; border-radius: 4px; font-family: 'Times New Roman', Times, serif; font-size: 16.5px; font-style: italic; background: #ffffff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); color: #0f172a;">${formatted}</span></div>`);
    return placeholder;
  });

  text = text.replace(/\\\[\s*([\s\S]*?)\s*\\\]/gi, (match, inner) => {
    const formatted = formatMathSymbols(inner);
    const placeholder = `___MATH_BLOCK_${mathBlocks.length}___`;
    mathBlocks.push(`<div class="katex-display-box" style="text-align: center; margin: 16px 0; font-family: 'Times New Roman', Times, serif; font-size: 16.5px; font-style: italic; color: #0f172a; page-break-inside: avoid; break-inside: avoid;">${formatted}</div>`);
    return placeholder;
  });

  text = text.replace(/\\boxed\{([\s\S]*?)\}/gi, (match, inner) => {
    const formatted = formatMathSymbols(inner);
    const placeholder = `___MATH_BLOCK_${mathBlocks.length}___`;
    mathBlocks.push(`<span style="display: inline-block; border: 1.5px solid #0f172a; padding: 2px 8px; border-radius: 3px; font-family: 'Times New Roman', Times, serif; font-size: 15px; font-style: italic; background: #ffffff;">${formatted}</span>`);
    return placeholder;
  });

  // 2. Protect Code blocks
  const codeBlocks = [];
  text = text.replace(/```(\w+)?\n([\s\S]*?)```/g, (match, lang, code) => {
    const langName = lang || 'code';
    const placeholder = `___CODE_BLOCK_${codeBlocks.length}___`;
    codeBlocks.push(`
      <div class="code-block" style="page-break-inside: avoid; break-inside: avoid;">
        <div class="code-header">
          <span>${escapeHtml(langName)}</span>
          <span>Copy code</span>
        </div>
        <pre style="page-break-inside: avoid; break-inside: avoid;"><code>${escapeHtml(code.trim())}</code></pre>
      </div>
    `);
    return placeholder;
  });

  // 3. Parse Markdown Tables into HTML <table> elements
  const tableBlocks = [];
  text = text.replace(/(?:^|\n)(\|[^\n]+\|\n\|[-:\s|]+\|\n(?:\|[^\n]+\|\n?)+)/g, (match, tblStr) => {
    const placeholder = `___TABLE_BLOCK_${tableBlocks.length}___`;
    const lines = tblStr.trim().split('\n');
    tableBlocks.push(renderTableHtml(lines));
    return '\n' + placeholder + '\n';
  });

  // 4. Escape HTML symbols outside code blocks, math blocks & tables
  text = escapeHtml(text);

  // 5. Restore LaTeX math blocks safely (AFTER escapeHtml)
  mathBlocks.forEach((mBlock, idx) => {
    text = text.replace(`___MATH_BLOCK_${idx}___`, mBlock);
  });

  // 6. Convert Headings: # , ## , ### , ####
  text = text.replace(/^#### (.*$)/gim, '<h4>$1</h4>');
  text = text.replace(/^### (.*$)/gim, '<h3>$1</h3>');
  text = text.replace(/^## (.*$)/gim, '<h2>$1</h2>');
  text = text.replace(/^# (.*$)/gim, '<h1>$1</h1>');

  // 7. Horizontal rules
  text = text.replace(/^---$/gim, '<hr>');

  // 8. Bold & Italic
  text = text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  text = text.replace(/\*(.*?)\*/g, '<em>$1</em>');

  // 9. Inline code
  text = text.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');

  // 10. Bullet & Numbered lists
  text = text.replace(/^\s*[\-\*]\s+(.*$)/gim, '<li>$1</li>');
  text = text.replace(/(<li>.*<\/li>)/gis, '<ul>$1</ul>');
  text = text.replace(/^\s*(\d+)\.\s+(.*$)/gim, '<li><span class="list-num">$1.</span> $2</li>');

  // 11. Restore code blocks & table blocks
  codeBlocks.forEach((block, idx) => {
    text = text.replace(`___CODE_BLOCK_${idx}___`, block);
  });
  tableBlocks.forEach((tBlock, idx) => {
    text = text.replace(`___TABLE_BLOCK_${idx}___`, tBlock);
  });

  // 12. Clean spacing
  text = text.replace(/\n\n/g, '<br>').replace(/\n/g, '<br>');

  return text;
}

function exportNativePdfDocument(data) {
  const title = data.title || 'ChatGPT Conversation';
  const turns = data.turns || [];
  const dateStr = new Date().toLocaleString();

  let turnsHtml = '';
  turns.forEach((t) => {
    const isUser = t.role === 'User';
    const roleName = isUser ? 'User' : 'ChatGPT';

    const formattedContent = t.htmlContent && t.htmlContent.length > 5
      ? t.htmlContent
      : parseMarkdownAndLaTeX(t.content);

    turnsHtml += `
      <div class="native-turn-row" style="padding: 20px 0; border-bottom: 1px solid #f1f5f9; page-break-inside: avoid; break-inside: avoid;">
        <div style="font-weight: 700; font-size: 14px; color: ${isUser ? '#1d4ed8' : '#047857'}; margin-bottom: 8px; font-family: system-ui, -apple-system, sans-serif;">
          ${roleName}
        </div>
        <div class="turn-body" style="color: #0f172a; font-size: 14.5px; line-height: 1.7; word-break: break-word;">
          ${formattedContent}
        </div>
      </div>
    `;
  });

  const fullHtml = `
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="utf-8">
      <title>${escapeHtml(title)} Caspian_Exported</title>
      <link rel="preconnect" href="https://fonts.googleapis.com">
      <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
      <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
      <style>
        * { box-sizing: border-box; }
        body {
          font-family: system-ui, -apple-system, 'Inter', sans-serif;
          background: #ffffff;
          color: #0f172a;
          margin: 0;
          padding: 32px 48px;
          line-height: 1.7;
          font-size: 14.5px;
        }
        .doc-header {
          border-bottom: 2px solid #e2e8f0;
          padding-bottom: 14px;
          margin-bottom: 24px;
        }
        .doc-title {
          font-size: 24px;
          font-weight: 700;
          color: #0f172a;
          margin: 0 0 6px 0;
        }
        .doc-meta {
          font-size: 12px;
          color: #64748b;
          font-style: italic;
        }
        p { margin: 0 0 10px 0; line-height: 1.7; }
        h1, h2, h3, h4 { font-family: system-ui, -apple-system, sans-serif; margin-top: 18px; margin-bottom: 8px; font-weight: 700; color: #0f172a; }
        h1 { font-size: 22px; }
        h2 { font-size: 18px; }
        h3 { font-size: 15.5px; }
        h4 { font-size: 14px; }
        ul, ol { padding-left: 22px; margin: 10px 0; }
        li { margin-bottom: 6px; }
        
        .code-block {
          background: #f8fafc;
          color: #0f172a;
          border: 1px solid #cbd5e1;
          border-radius: 8px;
          overflow: hidden;
          margin: 14px 0;
          font-family: 'JetBrains Mono', monospace;
          page-break-inside: avoid !important;
          break-inside: avoid !important;
        }
        .code-header {
          background: #f1f5f9;
          padding: 6px 14px;
          font-size: 11px;
          font-weight: 600;
          color: #475569;
          display: flex;
          justify-content: space-between;
          border-bottom: 1px solid #cbd5e1;
          text-transform: uppercase;
        }
        pre {
          margin: 0;
          padding: 14px;
          background: #f8fafc;
          color: #0f172a;
          overflow-x: auto;
          font-size: 12.5px;
          line-height: 1.5;
          page-break-inside: avoid !important;
          break-inside: avoid !important;
        }
        code { font-family: 'JetBrains Mono', monospace; }
        .table-wrapper, table { width: 100%; border-collapse: collapse; margin: 14px 0; page-break-inside: avoid !important; break-inside: avoid !important; }
        th, td { border: 1px solid #cbd5e1; padding: 8px 12px; text-align: left; }
        th { background: #f8fafc; font-weight: 700; color: #0f172a; }
        tr { page-break-inside: avoid !important; break-inside: avoid !important; }

        @media print {
          body { padding: 20px !important; background: #ffffff !important; }
          .native-turn-row, .code-block, pre, .table-wrapper, table, tr {
            page-break-inside: avoid !important;
            break-inside: avoid !important;
          }
        }
      </style>
    </head>
    <body>
      <div id="pdf-export-container">
        <div class="doc-header">
          <h1 class="doc-title">${escapeHtml(title)}</h1>
          <div class="doc-meta">Full Conversation Transcript &bull; Exported via <a href="https://github.com/code4nigel/Caspian.git" target="_blank" style="color: #2563eb; text-decoration: underline; font-weight: 600;">Caspian</a> on ${dateStr}</div>
        </div>
        ${turnsHtml}
      </div>
    </body>
    </html>
  `;

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
          }, 400);
        },
        args: [fullHtml]
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
  turns.forEach((t, i) => {
    const isUser = t.role === 'User';
    const roleName = isUser ? 'User' : 'ChatGPT';
    const roleIcon = isUser ? '👤' : '🤖';

    const formattedContent = t.htmlContent && t.htmlContent.length > 5 ? t.htmlContent : parseMarkdownAndLaTeX(t.content);

    turnsHtml += `
      <div class="turn-container ${isUser ? 'user-turn' : 'assistant-turn'}">
        <div class="turn-header">
          <div class="avatar ${isUser ? 'user-avatar' : 'ai-avatar'}">${roleIcon}</div>
          <span class="role-name" style="color: ${isUser ? '#1d4ed8' : '#047857'}; font-weight: 700;">
            ${roleName} <span style="font-size: 11px; color: #94a3b8; font-weight: 500;">(Turn ${i + 1})</span>
          </span>
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
      <title>${escapeHtml(title)} Caspian_Special_Exported</title>
      
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
          padding-bottom: 14px;
          margin-bottom: 24px;
        }
        .doc-title {
          font-size: 24px;
          font-weight: 700;
          color: #111827;
          margin: 0 0 6px 0;
        }
        .doc-meta {
          font-size: 12px;
          color: #64748b;
          font-style: italic;
        }

        .turn-container {
          margin-bottom: 22px;
          padding: 18px 22px;
          border-radius: 10px;
          border: 1px solid #e2e8f0;
          page-break-inside: avoid;
        }
        .user-turn {
          background-color: #f8fafc;
          border-left: 4px solid #2563eb !important;
        }
        .assistant-turn {
          background-color: #ffffff;
          border-left: 4px solid #10b981 !important;
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
          background: #f1f5f9;
          color: #0f172a;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: 'JetBrains Mono', monospace;
          font-size: 12.5px;
        }

        .code-block {
          background: #f8fafc;
          color: #0f172a;
          border: 1px solid #cbd5e1;
          border-radius: 8px;
          overflow: hidden;
          margin: 14px 0;
          font-family: 'JetBrains Mono', monospace;
          page-break-inside: avoid !important;
          break-inside: avoid !important;
        }
        .code-header {
          background: #f1f5f9;
          padding: 6px 14px;
          font-size: 11px;
          font-weight: 600;
          color: #475569;
          display: flex;
          justify-content: space-between;
          border-bottom: 1px solid #cbd5e1;
          text-transform: uppercase;
        }
        pre {
          margin: 0;
          padding: 14px;
          background: #f8fafc;
          color: #0f172a;
          overflow-x: auto;
          font-size: 12.5px;
          line-height: 1.5;
          page-break-inside: avoid !important;
          break-inside: avoid !important;
        }
        code { font-family: 'JetBrains Mono', monospace; }

        @media print {
          body { padding: 15px !important; background: #ffffff !important; }
          .turn-container, .code-block, pre, .table-wrapper, table, tr {
            page-break-inside: avoid !important;
            break-inside: avoid !important;
          }
        }
      </style>
    </head>
    <body>
      <div id="pdf-export-container">
        <div class="doc-header">
          <h1 class="doc-title">${escapeHtml(title)}</h1>
          <div class="doc-meta">Full Conversation Transcript &bull; Exported via <a href="https://github.com/code4nigel/Caspian.git" target="_blank" style="color: #2563eb; text-decoration: underline; font-weight: 600;">Caspian</a> on ${dateStr}</div>
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
                {left: '\\\\(', right: '\\\\)', display: false},
                {left: '\\\\[', right: '\\\\]', display: true},
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
  reader.onloadend = function () {
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

// Interactive Card & Mode Toggle Handlers
function setupCardToggleHandlers() {
  const toggleLimitBtn = document.getElementById('toggle-chat-limit-btn');
  const limitHeader = document.getElementById('chat-limit-header');
  
  function toggleLimit() {
    chrome.storage.local.get(['pruningEnabled', 'enabled'], (data) => {
      const current = data.pruningEnabled ?? (data.enabled ?? true);
      const next = !current;
      chrome.storage.local.set({ pruningEnabled: next, enabled: next }, loadSettings);
    });
  }

  if (toggleLimitBtn) {
    toggleLimitBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleLimit();
    });
  }

  // Collapsible Accordion Header Handler
  if (limitHeader) {
    limitHeader.addEventListener('click', (e) => {
      if (e.target.closest('#toggle-chat-limit-btn')) return;
      const limitBody = document.getElementById('chat-limit-body');
      const statusSub = document.getElementById('status-sub-text');
      if (!limitBody) return;
      const isCurrentlyVisible = limitBody.style.display !== 'none';
      const nextVisible = !isCurrentlyVisible;
      limitBody.style.display = nextVisible ? 'flex' : 'none';
      chrome.storage.local.set({ limit_card_collapsed: !nextVisible });
      if (statusSub) {
        chrome.storage.local.get(['pruningEnabled', 'enabled'], (data) => {
          const isEnabled = data.pruningEnabled ?? (data.enabled ?? true);
          if (isEnabled) {
            statusSub.textContent = nextVisible ? 'Limits message count to improve performance & prevent lag. Tap to collapse.' : 'Limits message count to improve performance & prevent lag. Tap to expand.';
          }
        });
      }
    });
  }

  const toggleTempBtn = document.getElementById('toggle-temp-saver-btn');
  const tempHeader = document.getElementById('temp-vault-header');

  function toggleVault() {
    chrome.storage.local.get('vaultEnabled', (data) => {
      const next = !(data.vaultEnabled ?? true);
      chrome.storage.local.set({ vaultEnabled: next }, loadSettings);
    });
  }

  if (toggleTempBtn) {
    toggleTempBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleVault();
    });
  }

  if (tempHeader) {
    tempHeader.addEventListener('click', toggleVault);
  }

  // Virtualization Mode Pills (Sliding Window / Tail Window)
  const btnSliding = document.getElementById('btn-mode-sliding');
  const btnTail = document.getElementById('btn-mode-tail');

  if (btnSliding) {
    btnSliding.addEventListener('click', () => {
      chrome.storage.local.set({ chat_pruning_mode: 'sliding_window' }, loadSettings);
    });
  }

  if (btnTail) {
    btnTail.addEventListener('click', () => {
      chrome.storage.local.set({ chat_pruning_mode: 'tail' }, loadSettings);
    });
  }

  // Collapsible Aesthetics & Themes Card Handler
  const aestheticsHeader = document.getElementById('aesthetics-header');
  if (aestheticsHeader) {
    aestheticsHeader.addEventListener('click', (e) => {
      if (e.target.closest('#reset-link')) return;
      const aesBody = document.getElementById('aesthetics-body');
      const aesSub = document.getElementById('aesthetics-sub-text');
      if (!aesBody) return;
      const isCurrentlyVisible = aesBody.style.display !== 'none';
      const nextVisible = !isCurrentlyVisible;
      aesBody.style.display = nextVisible ? 'flex' : 'none';
      chrome.storage.local.set({ aesthetics_card_collapsed: !nextVisible });
      if (aesSub) {
        aesSub.textContent = nextVisible ? 'Customize colors, gradients & background tones. Tap to collapse.' : 'Customize colors, gradients & background tones. Tap to expand.';
      }
    });
  }

  // Collapsible Display & Scaling Card Handler
  const displayHeader = document.getElementById('display-header');
  if (displayHeader) {
    displayHeader.addEventListener('click', () => {
      const displayBody = document.getElementById('display-body');
      const displaySub = document.getElementById('display-sub-text');
      if (!displayBody) return;
      const isCurrentlyVisible = displayBody.style.display !== 'none';
      const nextVisible = !isCurrentlyVisible;
      displayBody.style.display = nextVisible ? 'flex' : 'none';
      chrome.storage.local.set({ display_card_collapsed: !nextVisible });
      if (displaySub) {
        displaySub.textContent = nextVisible ? 'Adjust UI window size and font scale independently. Tap to collapse.' : 'Adjust UI window size and font scale independently. Tap to expand.';
      }
    });
  }

  // Collapsible YouTube Home Card Handler
  const ytHeader = document.getElementById('youtube-home-header');
  const toggleYtFeedBtn = document.getElementById('toggle-yt-feed-btn');
  const toggleYtNotInterestedBtn = document.getElementById('toggle-yt-not-interested-btn');

  function toggleYtFeed() {
    chrome.storage.local.get('yt_feed_limit_enabled', (data) => {
      const current = data.yt_feed_limit_enabled ?? DEFAULTS.yt_feed_limit_enabled;
      chrome.storage.local.set({ yt_feed_limit_enabled: !current }, loadSettings);
    });
  }

  if (toggleYtFeedBtn) {
    toggleYtFeedBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggleYtFeed();
    });
  }

  if (ytHeader) {
    ytHeader.addEventListener('click', (e) => {
      if (e.target.closest('#toggle-yt-feed-btn')) return;
      const ytBody = document.getElementById('youtube-home-body');
      const ytSub = document.getElementById('yt-status-sub-text');
      if (!ytBody) return;
      const isCurrentlyVisible = ytBody.style.display !== 'none';
      const nextVisible = !isCurrentlyVisible;
      ytBody.style.display = nextVisible ? 'flex' : 'none';
      chrome.storage.local.set({ yt_card_collapsed: !nextVisible });
      if (ytSub) {
        ytSub.textContent = nextVisible ? 'Feed video limits & quick Not-Interested button. Tap to collapse.' : 'Feed video limits & quick Not-Interested button. Tap to expand.';
      }
    });
  }

  if (toggleYtNotInterestedBtn) {
    toggleYtNotInterestedBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      chrome.storage.local.get('yt_not_interested_enabled', (data) => {
        const current = data.yt_not_interested_enabled ?? DEFAULTS.yt_not_interested_enabled;
        chrome.storage.local.set({ yt_not_interested_enabled: !current }, loadSettings);
      });
    });
  }

  // YouTube Limit numerical pills
  document.querySelectorAll('.yt-pill').forEach(pill => {
    pill.addEventListener('click', (e) => {
      e.stopPropagation();
      const val = parseInt(pill.dataset.yt);
      if (!isNaN(val)) {
        chrome.storage.local.set({ yt_feed_limit: val }, loadSettings);
      }
    });
  });

  // Flow Speed Card Handlers
  const toggleFlowSpeedBtn = document.getElementById('toggle-flow-speed-btn');
  const flowSpeedHeader = document.getElementById('flow-speed-header');
  const flowSpeedBody = document.getElementById('flow-speed-body');
  const flowSpeedSub = document.getElementById('flow-speed-status-sub-text');
  const toggleFlowSpeedHudBtn = document.getElementById('toggle-flow-speed-hud-btn');
  const flowSpeedSlider = document.getElementById('flow-speed-slider');
  const speedDownBtn = document.getElementById('speed-down-btn');
  const speedUpBtn = document.getElementById('speed-up-btn');
  const flowSpeedCycleInput = document.getElementById('flow-speed-cycle-input');
  const shortcutResetInput = document.getElementById('shortcut-reset-input');
  const shortcutCycleInput = document.getElementById('shortcut-cycle-input');

  if (toggleFlowSpeedBtn) {
    toggleFlowSpeedBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      chrome.storage.local.get('flow_speed_enabled', (data) => {
        const current = data.flow_speed_enabled ?? DEFAULTS.flow_speed_enabled;
        chrome.storage.local.set({ flow_speed_enabled: !current }, loadSettings);
      });
    });
  }

  if (flowSpeedHeader && flowSpeedBody) {
    flowSpeedHeader.addEventListener('click', () => {
      const isVisible = flowSpeedBody.style.display !== 'none';
      const nextVisible = !isVisible;
      flowSpeedBody.style.display = nextVisible ? 'flex' : 'none';
      chrome.storage.local.set({ flow_speed_card_collapsed: !nextVisible });
      if (flowSpeedSub) {
        flowSpeedSub.textContent = nextVisible ? 'Universal video & audio speed engine. Tap to collapse.' : 'Universal video & audio speed engine. Tap to expand.';
      }
    });
  }

  if (flowSpeedSlider) {
    flowSpeedSlider.addEventListener('input', (e) => {
      const val = parseFloat(e.target.value);
      if (!isNaN(val)) {
        const display = document.getElementById('flow-speed-val-display');
        const badge = document.getElementById('flow-speed-active-badge');
        if (display) display.textContent = `${val.toFixed(2)}x`;
        if (badge) badge.textContent = `${val.toFixed(2)}x`;
        chrome.storage.local.set({ flow_speed_val: val });
        document.querySelectorAll('.speed-pill').forEach(p => {
          p.classList.toggle('active', Math.abs(parseFloat(p.dataset.speed) - val) < 0.02);
        });
      }
    });
  }

  if (speedDownBtn && flowSpeedSlider) {
    speedDownBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      const current = parseFloat(flowSpeedSlider.value) || 1.0;
      const next = Math.max(0.25, parseFloat((current - 0.25).toFixed(2)));
      flowSpeedSlider.value = next;
      flowSpeedSlider.dispatchEvent(new Event('input'));
    });
  }

  if (speedUpBtn && flowSpeedSlider) {
    speedUpBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      const current = parseFloat(flowSpeedSlider.value) || 1.0;
      const next = Math.min(5.0, parseFloat((current + 0.25).toFixed(2)));
      flowSpeedSlider.value = next;
      flowSpeedSlider.dispatchEvent(new Event('input'));
    });
  }

  document.querySelectorAll('.speed-pill').forEach(pill => {
    pill.addEventListener('click', (e) => {
      e.stopPropagation();
      const speed = parseFloat(pill.dataset.speed);
      if (!isNaN(speed)) {
        if (flowSpeedSlider) flowSpeedSlider.value = speed;
        chrome.storage.local.set({ flow_speed_val: speed }, loadSettings);
      }
    });
  });

  if (flowSpeedCycleInput) {
    flowSpeedCycleInput.addEventListener('change', (e) => {
      chrome.storage.local.set({ flow_speed_cycle_list: e.target.value.trim() });
    });
  }

  if (shortcutResetInput) {
    shortcutResetInput.addEventListener('input', (e) => {
      const val = e.target.value.trim().toLowerCase();
      if (val) chrome.storage.local.set({ flow_speed_shortcut_reset: val });
    });
  }

  if (shortcutCycleInput) {
    shortcutCycleInput.addEventListener('input', (e) => {
      const val = e.target.value.trim().toLowerCase();
      if (val) chrome.storage.local.set({ flow_speed_shortcut_cycle: val });
    });
  }

  if (toggleFlowSpeedHudBtn) {
    toggleFlowSpeedHudBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      chrome.storage.local.get('flow_speed_show_hud', (data) => {
        const current = data.flow_speed_show_hud ?? DEFAULTS.flow_speed_show_hud;
        chrome.storage.local.set({ flow_speed_show_hud: !current }, loadSettings);
      });
    });
  }

  const toggleFlowSpeedBadgeBtn = document.getElementById('toggle-flow-speed-badge-btn');
  const toggleSettingsSpeedBadgeBtn = document.getElementById('toggle-settings-speed-badge-btn');

  const handleBadgeToggle = (e) => {
    e.stopPropagation();
    chrome.storage.local.get('flow_speed_badge_enabled', (data) => {
      const current = data.flow_speed_badge_enabled ?? DEFAULTS.flow_speed_badge_enabled;
      chrome.storage.local.set({ flow_speed_badge_enabled: !current }, loadSettings);
    });
  };

  if (toggleFlowSpeedBadgeBtn) toggleFlowSpeedBadgeBtn.addEventListener('click', handleBadgeToggle);
  if (toggleSettingsSpeedBadgeBtn) toggleSettingsSpeedBadgeBtn.addEventListener('click', handleBadgeToggle);

  // RippleFrame Card Handlers
  const rfHeader = document.getElementById('rippleframe-header');
  const rfBody = document.getElementById('rippleframe-body');
  const rfSub = document.getElementById('rf-status-sub-text');
  const triggerRfCaptureBtn = document.getElementById('trigger-rf-capture-btn');
  const btnRfFullpageDirect = document.getElementById('btn-rf-fullpage-direct');
  const btnRfOpenStudio = document.getElementById('btn-rf-open-studio');
  const rfDelaySlider = document.getElementById('rf-delay-slider');
  const rfDelayVal = document.getElementById('rf-delay-val');

  if (rfHeader && rfBody) {
    rfHeader.addEventListener('click', (e) => {
      if (e.target.closest('#trigger-rf-capture-btn')) return;
      const isCurrentlyHidden = rfBody.style.display === 'none';
      const nextState = isCurrentlyHidden ? 'flex' : 'none';
      rfBody.style.display = nextState;
      if (rfSub) {
        rfSub.textContent = nextState === 'none' 
          ? 'Long screenshot & studio editor. Tap to expand.' 
          : 'Long screenshot & studio editor. Tap to collapse.';
      }
      chrome.storage.local.set({ rf_card_collapsed: nextState === 'none' });
    });
  }

  // Scope pills
  document.querySelectorAll('.rf-scope-pill').forEach(pill => {
    pill.addEventListener('click', (e) => {
      e.stopPropagation();
      const scope = pill.dataset.scope;
      chrome.storage.local.set({ rf_capture_scope: scope }, loadSettings);
    });
  });

  // Delay slider
  if (rfDelaySlider) {
    rfDelaySlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value);
      if (rfDelayVal) rfDelayVal.textContent = `${val}ms`;
      chrome.storage.local.set({ rf_scroll_delay: val });
    });
  }

  // Launch Capture Handler
  const launchRippleFrameCapture = (overrideScope = null) => {
    chrome.storage.local.get(['rf_capture_scope', 'rf_scroll_delay'], (data) => {
      const scope = overrideScope || (data.rf_capture_scope || DEFAULTS.rf_capture_scope);
      const delay = data.rf_scroll_delay || DEFAULTS.rf_scroll_delay;

      chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
        if (!tabs || tabs.length === 0) return;
        const activeTab = tabs[0];

        // Execute capture script in active tab
        chrome.scripting.executeScript({
          target: { tabId: activeTab.id },
          files: ['rippleframe_capture.js']
        }, () => {
          chrome.tabs.sendMessage(activeTab.id, {
            action: 'start_rippleframe_capture',
            options: { scope, delay }
          });
          window.close(); // Close popup so it doesn't obstruct capture
        });
      });
    });
  };

  if (triggerRfCaptureBtn) {
    triggerRfCaptureBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      launchRippleFrameCapture();
    });
  }

  if (btnRfFullpageDirect) {
    btnRfFullpageDirect.addEventListener('click', (e) => {
      e.stopPropagation();
      launchRippleFrameCapture('full');
    });
  }

  if (btnRfOpenStudio) {
    btnRfOpenStudio.addEventListener('click', (e) => {
      e.stopPropagation();
      chrome.tabs.create({ url: chrome.runtime.getURL('rippleframe.html') });
      window.close();
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

// Developer Exclusive Themes Click Handlers
function setupDevExclusiveThemes() {
  document.querySelectorAll('.dev-theme-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const accent = chip.dataset.start;
      const secondary = chip.dataset.end;
      chrome.storage.local.set({ accent, secondary }, loadSettings);
    });
  });
}

function loadSettings() {
  chrome.storage.local.get(['pruningEnabled', 'vaultEnabled', 'enabled', 'limit', 'mode', 'accent', 'secondary', 'customBgColor', 'ui_zoom', 'font_scale', 'pinnedPresets', 'disabledSites', 'chat_pruning_mode', 'limit_card_collapsed', 'aesthetics_card_collapsed', 'display_card_collapsed', 'yt_feed_limit_enabled', 'yt_feed_limit', 'yt_not_interested_enabled', 'yt_card_collapsed', 'flow_speed_enabled', 'flow_speed_badge_enabled', 'flow_speed_val', 'flow_speed_cycle_list', 'flow_speed_shortcut_reset', 'flow_speed_shortcut_cycle', 'flow_speed_shortcut_up', 'flow_speed_shortcut_down', 'flow_speed_show_hud', 'flow_speed_card_collapsed', 'rf_capture_scope', 'rf_scroll_delay', 'rf_card_collapsed'], (data) => {
    const currentMode = data.mode || DEFAULTS.mode;
    updateThemeMode(currentMode);
    updateThemeColors(data);
    applyUiZoom(data.ui_zoom ?? DEFAULTS.ui_zoom);
    applyFontScale(data.font_scale ?? DEFAULTS.font_scale);

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

    // Background Canvas State Sync
    const customBg = data.customBgColor || '';
    document.querySelectorAll('.bg-tone-btn').forEach(btn => {
      btn.classList.toggle('active', (btn.dataset.bg || '').toLowerCase() === customBg.toLowerCase());
    });
    const customBgHexInput = document.getElementById('custom-bg-hex');
    const customBgPicker = document.getElementById('custom-bg-picker');
    if (customBgHexInput) customBgHexInput.value = customBg ? customBg.toUpperCase() : '';
    if (customBgPicker) customBgPicker.value = customBg && customBg.length === 7 ? customBg : '#0a1128';

    // Aesthetics Card Collapsible State
    const isAesCollapsed = data.aesthetics_card_collapsed ?? false;
    const aesBody = document.getElementById('aesthetics-body');
    const aesSub = document.getElementById('aesthetics-sub-text');
    if (aesBody) aesBody.style.display = isAesCollapsed ? 'none' : 'flex';
    if (aesSub) {
      aesSub.textContent = isAesCollapsed ? 'Customize colors, gradients & background tones. Tap to expand.' : 'Customize colors, gradients & background tones. Tap to collapse.';
    }

    // Display Card Collapsible State
    const isDisplayCollapsed = data.display_card_collapsed ?? false;
    const displayBody = document.getElementById('display-body');
    const displaySub = document.getElementById('display-sub-text');
    if (displayBody) displayBody.style.display = isDisplayCollapsed ? 'none' : 'flex';
    if (displaySub) {
      displaySub.textContent = isDisplayCollapsed ? 'Adjust UI window size and font scale independently. Tap to expand.' : 'Adjust UI window size and font scale independently. Tap to collapse.';
    }

    // YouTube Home Card State
    const ytFeedEnabled = data.yt_feed_limit_enabled ?? DEFAULTS.yt_feed_limit_enabled;
    const ytLimit = data.yt_feed_limit ?? DEFAULTS.yt_feed_limit;
    const ytNotInterestedEnabled = data.yt_not_interested_enabled ?? DEFAULTS.yt_not_interested_enabled;
    const isYtCollapsed = data.yt_card_collapsed ?? false;

    const ytCard = document.getElementById('card-youtube-home');
    const ytStatusTitle = document.getElementById('yt-status-title');
    const ytActiveBadge = document.getElementById('yt-active-limit-badge');
    const ytFeedBtn = document.getElementById('toggle-yt-feed-btn');
    const ytNotInterestedBtn = document.getElementById('toggle-yt-not-interested-btn');
    const ytBody = document.getElementById('youtube-home-body');

    if (ytBody) ytBody.style.display = isYtCollapsed ? 'none' : 'flex';
    if (ytCard) ytCard.classList.toggle('is-disabled', !ytFeedEnabled);
    if (ytStatusTitle) ytStatusTitle.textContent = ytFeedEnabled ? 'YouTube Home Feed: ON' : 'YouTube Home Feed: OFF';
    if (ytActiveBadge) ytActiveBadge.textContent = ytLimit >= 9999 ? '∞ All' : `${ytLimit} Videos`;
    if (ytFeedBtn) {
      ytFeedBtn.textContent = ytFeedEnabled ? 'ON' : 'OFF';
      ytFeedBtn.classList.toggle('active', ytFeedEnabled);
    }
    if (ytNotInterestedBtn) {
      ytNotInterestedBtn.textContent = ytNotInterestedEnabled ? 'ON' : 'OFF';
      ytNotInterestedBtn.classList.toggle('active', ytNotInterestedEnabled);
    }

    document.querySelectorAll('.yt-pill').forEach(p => {
      p.classList.toggle('active', parseInt(p.dataset.yt) === ytLimit);
    });

    // Flow Speed Card State & Sync
    const flowSpeedEnabled = data.flow_speed_enabled ?? DEFAULTS.flow_speed_enabled;
    const flowSpeedVal = parseFloat(data.flow_speed_val) || DEFAULTS.flow_speed_val;
    const flowSpeedCycleList = data.flow_speed_cycle_list ?? DEFAULTS.flow_speed_cycle_list;
    const shortcutReset = data.flow_speed_shortcut_reset ?? DEFAULTS.flow_speed_shortcut_reset;
    const shortcutCycle = data.flow_speed_shortcut_cycle ?? DEFAULTS.flow_speed_shortcut_cycle;
    const flowSpeedShowHud = data.flow_speed_show_hud ?? DEFAULTS.flow_speed_show_hud;
    const isFlowSpeedCollapsed = data.flow_speed_card_collapsed ?? false;

    const flowSpeedCard = document.getElementById('card-flow-speed');
    const flowSpeedStatusTitle = document.getElementById('flow-speed-status-title');
    const flowSpeedActiveBadge = document.getElementById('flow-speed-active-badge');
    const flowSpeedValDisplay = document.getElementById('flow-speed-val-display');
    const flowSpeedBtn = document.getElementById('toggle-flow-speed-btn');
    const flowSpeedHudBtn = document.getElementById('toggle-flow-speed-hud-btn');
    const flowSpeedBody = document.getElementById('flow-speed-body');
    const flowSpeedSlider = document.getElementById('flow-speed-slider');
    const flowSpeedCycleInput = document.getElementById('flow-speed-cycle-input');
    const shortcutResetInput = document.getElementById('shortcut-reset-input');
    const shortcutCycleInput = document.getElementById('shortcut-cycle-input');

    if (flowSpeedBody) flowSpeedBody.style.display = isFlowSpeedCollapsed ? 'none' : 'flex';
    if (flowSpeedCard) flowSpeedCard.classList.toggle('is-disabled', !flowSpeedEnabled);
    if (flowSpeedStatusTitle) flowSpeedStatusTitle.textContent = flowSpeedEnabled ? 'Flow Speed: ON' : 'Flow Speed: OFF';
    if (flowSpeedActiveBadge) flowSpeedActiveBadge.textContent = `${flowSpeedVal.toFixed(2)}x`;
    if (flowSpeedValDisplay) flowSpeedValDisplay.textContent = `${flowSpeedVal.toFixed(2)}x`;
    if (flowSpeedSlider) flowSpeedSlider.value = flowSpeedVal;
    if (flowSpeedCycleInput) flowSpeedCycleInput.value = flowSpeedCycleList;
    if (shortcutResetInput) shortcutResetInput.value = shortcutReset.toUpperCase();
    if (shortcutCycleInput) shortcutCycleInput.value = shortcutCycle.toUpperCase();

    if (flowSpeedBtn) {
      flowSpeedBtn.textContent = flowSpeedEnabled ? 'ON' : 'OFF';
      flowSpeedBtn.classList.toggle('active', flowSpeedEnabled);
    }
    if (flowSpeedHudBtn) {
      flowSpeedHudBtn.textContent = flowSpeedShowHud ? 'ON' : 'OFF';
      flowSpeedHudBtn.classList.toggle('active', flowSpeedShowHud);
    }

    const flowSpeedBadgeEnabled = data.flow_speed_badge_enabled ?? DEFAULTS.flow_speed_badge_enabled;
    const flowSpeedBadgeBtn = document.getElementById('toggle-flow-speed-badge-btn');
    const settingsBadgeBtn = document.getElementById('toggle-settings-speed-badge-btn');

    if (flowSpeedBadgeBtn) {
      flowSpeedBadgeBtn.textContent = flowSpeedBadgeEnabled ? 'ON' : 'OFF';
      flowSpeedBadgeBtn.classList.toggle('active', flowSpeedBadgeEnabled);
    }
    if (settingsBadgeBtn) {
      settingsBadgeBtn.textContent = flowSpeedBadgeEnabled ? 'ON' : 'OFF';
      settingsBadgeBtn.classList.toggle('active', flowSpeedBadgeEnabled);
    }

    document.querySelectorAll('.speed-pill').forEach(p => {
      p.classList.toggle('active', Math.abs(parseFloat(p.dataset.speed) - flowSpeedVal) < 0.02);
    });

    // RippleFrame Card State & Sync
    const rfScope = data.rf_capture_scope || DEFAULTS.rf_capture_scope;
    const rfDelay = data.rf_scroll_delay || DEFAULTS.rf_scroll_delay;
    const isRfCollapsed = data.rf_card_collapsed ?? false;

    const rfBody = document.getElementById('rippleframe-body');
    const rfSub = document.getElementById('rf-status-sub-text');
    const rfScopeHint = document.getElementById('rf-scope-hint');
    const rfDelaySlider = document.getElementById('rf-delay-slider');
    const rfDelayVal = document.getElementById('rf-delay-val');

    if (rfBody) rfBody.style.display = isRfCollapsed ? 'none' : 'flex';
    if (rfSub) {
      rfSub.textContent = isRfCollapsed 
        ? 'Long screenshot & studio editor. Tap to expand.' 
        : 'Long screenshot & studio editor. Tap to collapse.';
    }
    if (rfScopeHint) rfScopeHint.textContent = rfScope === 'full' ? 'Full Page' : 'Viewport';
    if (rfDelaySlider) rfDelaySlider.value = rfDelay;
    if (rfDelayVal) rfDelayVal.textContent = `${rfDelay}ms`;

    document.querySelectorAll('.rf-scope-pill').forEach(pill => {
      pill.classList.toggle('active', pill.dataset.scope === rfScope);
    });

    // Chat Message Limit Card State & Collapsible Accordion
    const pruningEnabled = data.pruningEnabled ?? (data.enabled ?? true);
    const limitCard = document.getElementById('card-chat-limit');
    const statusDot = document.getElementById('status-indicator');
    const statusText = document.getElementById('status-state-text');
    const statusSub = document.getElementById('status-sub-text');
    const toggleLimitBtn = document.getElementById('toggle-chat-limit-btn');
    const limitBody = document.getElementById('chat-limit-body');
    const isCollapsed = data.limit_card_collapsed ?? false;

    if (limitBody) limitBody.style.display = isCollapsed ? 'none' : 'flex';
    if (limitCard) limitCard.classList.toggle('is-disabled', !pruningEnabled);
    if (statusDot) {
      statusDot.classList.toggle('active', pruningEnabled);
      statusDot.classList.toggle('inactive', !pruningEnabled);
    }
    if (statusText) {
      statusText.textContent = pruningEnabled ? 'Chat Message Limit: ON' : 'Chat Message Limit: OFF';
    }
    if (statusSub) {
      if (!pruningEnabled) {
        statusSub.textContent = 'Click to activate Chat Message Limit.';
      } else {
        statusSub.textContent = isCollapsed ? 'Limits message count to improve performance & prevent lag. Tap to expand.' : 'Limits message count to improve performance & prevent lag. Tap to collapse.';
      }
    }
    if (toggleLimitBtn) {
      toggleLimitBtn.textContent = pruningEnabled ? 'ON' : 'OFF';
      toggleLimitBtn.classList.toggle('active', pruningEnabled);
    }

    // Pruning Virtualization Mode Pills State
    const currentPruningMode = data.chat_pruning_mode || DEFAULTS.chat_pruning_mode;
    const btnSliding = document.getElementById('btn-mode-sliding');
    const btnTail = document.getElementById('btn-mode-tail');
    if (btnSliding) btnSliding.classList.toggle('active', currentPruningMode === 'sliding_window');
    if (btnTail) btnTail.classList.toggle('active', currentPruningMode === 'tail');

    // Vault Card State
    const vaultEnabled = data.vaultEnabled ?? true;
    const vaultCard = document.getElementById('temp-vault-card');
    const tempDot = document.getElementById('temp-indicator-dot');
    const tempDesc = document.getElementById('temp-vault-desc');
    const toggleTempBtn = document.getElementById('toggle-temp-saver-btn');

    if (vaultCard) vaultCard.classList.toggle('is-disabled', !vaultEnabled);
    if (tempDot) {
      tempDot.classList.toggle('active', vaultEnabled);
      tempDot.classList.toggle('inactive', !vaultEnabled);
    }
    if (toggleTempBtn) {
      toggleTempBtn.textContent = vaultEnabled ? 'ON' : 'OFF';
      toggleTempBtn.classList.toggle('active', vaultEnabled);
    }
    if (tempDesc) {
      if (!vaultEnabled) {
        tempDesc.textContent = 'Temporary Chat Saver Disabled (Click to enable)';
      } else {
        fetchCurrentTabData((tabData) => {
          if (tabData && tabData.isTemporary) {
            tempDesc.textContent = 'Convert this temporary session into permanent history.';
          } else {
            tempDesc.textContent = 'Save or convert temporary sessions into permanent history.';
          }
        });
      }
    }

    // Master Power Ring State
    const isMasterOn = pruningEnabled || vaultEnabled;
    const powerToggle = document.getElementById('power-toggle');
    if (powerToggle) powerToggle.classList.toggle('active', isMasterOn);

    // Limit badge & Pills
    const limit = data.limit || DEFAULTS.limit;
    const limitBadge = document.getElementById('active-limit-badge');
    if (limitBadge) {
      limitBadge.textContent = limit >= 9999 ? '∞ All' : `${limit} ${limit === 1 ? 'Message' : 'Messages'}`;
    }

    document.querySelectorAll('.pill').forEach(p => {
      p.classList.toggle('active', parseInt(p.dataset.val) === limit);
    });

    setupSiteToggles(data.disabledSites || []);
  });
}

// Floating Dock Slider & Navigation Handler (3 Tabs with Persistent Active Tab Memory)
function updateDockIndicator(targetTab) {
  const activeBtn = document.querySelector(`.dock-tab-btn[data-target="${targetTab}"]`);
  const indicator = document.getElementById('dock-indicator');

  if (activeBtn && indicator) {
    indicator.style.left = `${activeBtn.offsetLeft}px`;
    indicator.style.width = `${activeBtn.offsetWidth}px`;
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

  // Save active tab preference so it reopens in the same tab
  try {
    chrome.storage.local.set({ active_popup_tab: targetTab });
  } catch(e) {}
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

// Background Canvas Options Handler
function setupBackgroundOptions() {
  document.querySelectorAll('.bg-tone-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const chosenBg = btn.dataset.bg || '';
      chrome.storage.local.set({ customBgColor: chosenBg }, loadSettings);
    });
  });

  const bgPicker = document.getElementById('custom-bg-picker');
  if (bgPicker) {
    bgPicker.addEventListener('input', (e) => {
      const val = e.target.value;
      chrome.storage.local.set({ customBgColor: val }, loadSettings);
    });
  }

  const bgHexInput = document.getElementById('custom-bg-hex');
  if (bgHexInput) {
    bgHexInput.addEventListener('input', (e) => {
      let val = e.target.value.trim();
      if (!val) {
        chrome.storage.local.set({ customBgColor: '' }, loadSettings);
        return;
      }
      if (!val.startsWith('#')) val = '#' + val;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        chrome.storage.local.set({ customBgColor: val }, loadSettings);
      }
    });
  }
}

// UI Zoom Scale Controller
function applyUiZoom(zoomPercent = 100) {
  const pct = parseInt(zoomPercent) || 100;
  const factor = pct / 100;
  document.documentElement.style.setProperty('--ui-scale', factor);
  const badge = document.getElementById('ui-zoom-badge');
  if (badge) badge.textContent = `${pct}%`;
  const slider = document.getElementById('ui-zoom-slider');
  if (slider) slider.value = pct;

  // Re-align dock indicator seamlessly
  chrome.storage.local.get('active_popup_tab', (data) => {
    updateDockIndicator(data.active_popup_tab || 'engine');
  });
}

// Typography Font Scale Controller
function applyFontScale(fontPercent = 100) {
  const pct = parseInt(fontPercent) || 100;
  const factor = pct / 100;
  document.documentElement.style.setProperty('--font-scale', factor);
  const badge = document.getElementById('font-scale-badge');
  if (badge) badge.textContent = `${pct}%`;
  const slider = document.getElementById('font-size-slider');
  if (slider) slider.value = pct;
}

function setupZoomAndFontControls() {
  // 1. UI Window Zoom Controls
  const uiSlider = document.getElementById('ui-zoom-slider');
  const uiZoomIn = document.getElementById('ui-zoom-in-btn');
  const uiZoomOut = document.getElementById('ui-zoom-out-btn');

  if (uiSlider) {
    uiSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value);
      applyUiZoom(val);
      chrome.storage.local.set({ ui_zoom: val });
    });
  }

  if (uiZoomIn) {
    uiZoomIn.addEventListener('click', () => {
      const slider = document.getElementById('ui-zoom-slider');
      const current = parseInt(slider?.value || 100);
      const next = Math.min(115, current + 5);
      applyUiZoom(next);
      chrome.storage.local.set({ ui_zoom: next });
    });
  }

  if (uiZoomOut) {
    uiZoomOut.addEventListener('click', () => {
      const slider = document.getElementById('ui-zoom-slider');
      const current = parseInt(slider?.value || 100);
      const next = Math.max(75, current - 5);
      applyUiZoom(next);
      chrome.storage.local.set({ ui_zoom: next });
    });
  }

  // 2. Typography Font Scale Controls
  const fontSlider = document.getElementById('font-size-slider');
  const fontIn = document.getElementById('font-size-in-btn');
  const fontOut = document.getElementById('font-size-out-btn');

  if (fontSlider) {
    fontSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value);
      applyFontScale(val);
      chrome.storage.local.set({ font_scale: val });
    });
  }

  if (fontIn) {
    fontIn.addEventListener('click', () => {
      const slider = document.getElementById('font-size-slider');
      const current = parseInt(slider?.value || 100);
      const next = Math.min(125, current + 5);
      applyFontScale(next);
      chrome.storage.local.set({ font_scale: next });
    });
  }

  if (fontOut) {
    fontOut.addEventListener('click', () => {
      const slider = document.getElementById('font-size-slider');
      const current = parseInt(slider?.value || 100);
      const next = Math.max(80, current - 5);
      applyFontScale(next);
      chrome.storage.local.set({ font_scale: next });
    });
  }
}

// Settings Backup & Sync (Export / Import JSON)
function setupSettingsBackupSync() {
  const exportBtn = document.getElementById('export-settings-btn');
  const importBtn = document.getElementById('import-settings-btn');
  const fileInput = document.getElementById('import-file-input');

  if (exportBtn) {
    exportBtn.addEventListener('click', () => {
      chrome.storage.local.get(null, (data) => {
        const exportObj = {
          app: 'Caspian',
          version: chrome.runtime?.getManifest?.()?.version || '6.1.1',
          exportDate: new Date().toISOString(),
          settings: data
        };
        const blob = new Blob([JSON.stringify(exportObj, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `caspian_settings_backup_${new Date().toISOString().slice(0, 10)}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      });
    });
  }

  if (importBtn && fileInput) {
    importBtn.addEventListener('click', () => {
      fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
      const file = e.target.files?.[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const parsed = JSON.parse(event.target.result);
          const settingsToRestore = parsed.settings || parsed;
          if (typeof settingsToRestore === 'object' && settingsToRestore !== null) {
            chrome.storage.local.set(settingsToRestore, () => {
              loadSettings();
              syncVersionTag();
              alert('✅ Caspian Settings successfully imported!');
            });
          } else {
            alert('❌ Invalid settings format in JSON file.');
          }
        } catch (err) {
          alert('❌ Failed to parse JSON settings file.');
        }
      };
      reader.readAsText(file);
      fileInput.value = '';
    });
  }
}

// Quick Launch Site Bookmarks Click Handler
function setupSiteBookmarks() {
  document.querySelectorAll('.site-bookmark-card').forEach(card => {
    card.addEventListener('click', () => {
      const targetUrl = card.dataset.url;
      if (targetUrl) {
        chrome.tabs.create({ url: targetUrl });
      }
    });
  });
}

// Dynamic Manifest Version Synchronization
function syncVersionTag() {
  try {
    const manifestVer = chrome.runtime?.getManifest?.()?.version;
    const verTag = document.getElementById('extension-version') || document.querySelector('.brand-tag');
    if (verTag && manifestVer) {
      verTag.textContent = `V${manifestVer}`;
    }
  } catch (e) {}
}

if (document.readyState === 'complete' || document.readyState === 'interactive') {
  syncVersionTag();
}

// Initialize Position, Settings & Temp Vault with Tab Memory
window.addEventListener('DOMContentLoaded', () => {
  syncVersionTag();
  loadSettings();
  initTempChatVault();
  setupCardToggleHandlers();
  setupSiteBookmarks();
  setupDevExclusiveThemes();
  setupBackgroundOptions();
  setupZoomAndFontControls();
  setupSettingsBackupSync();

  // Restore previously opened tab
  chrome.storage.local.get('active_popup_tab', (data) => {
    const tab = data.active_popup_tab || 'engine';
    switchTab(tab);
  });
});