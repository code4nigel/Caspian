// ==========================================
// CASPIAN - CONTENT SCRIPT & TEMP CHAT VAULT
// ==========================================

// ------------------------------------------
// 1. SITE & DOM TURBO PRUNER (Lag Fixer)
// ------------------------------------------
function isSiteDisabled(disabledSites = []) {
  const host = window.location.hostname;
  return disabledSites.some(d => host.includes(d));
}

function applyTurbo() {
  chrome.storage.local.get(['pruningEnabled', 'enabled', 'limit', 'disabledSites'], (data) => {
    if (isSiteDisabled(data.disabledSites || [])) {
      const messages = document.querySelectorAll('[data-testid^="conversation-turn"]');
      messages.forEach(msg => msg.style.setProperty('display', 'block', 'important'));
      return;
    }

    const isEnabled = data.pruningEnabled ?? (data.enabled ?? true);
    const limit = data.limit ?? 5;
    const messages = document.querySelectorAll('[data-testid^="conversation-turn"]');

    messages.forEach((msg, index) => {
      if (isEnabled && limit < 9999 && index < messages.length - limit) {
        msg.style.setProperty('display', 'none', 'important');
      } else {
        msg.style.setProperty('display', 'block', 'important');
      }
    });
  });
}

const observer = new MutationObserver(applyTurbo);
observer.observe(document.body, { childList: true, subtree: true });
chrome.storage.onChanged.addListener(applyTurbo);
applyTurbo();

// ------------------------------------------
// 2. KEYBOARD SHORTCUT LISTENER (Ctrl+Shift+X / Ctrl+Shift+C)
// ------------------------------------------
window.addEventListener('keydown', (e) => {
  const isTargetKey = (e.key === 'x' || e.key === 'X' || e.code === 'KeyX' || e.key === 'c' || e.key === 'C' || e.code === 'KeyC');
  const isCtrlAlt = e.ctrlKey && e.altKey;
  const isCtrlShift = e.ctrlKey && e.shiftKey;

  if (isTargetKey && (isCtrlAlt || isCtrlShift)) {
    e.preventDefault();
    chrome.storage.local.get(['pruningEnabled', 'enabled'], (data) => {
      const current = data.pruningEnabled ?? (data.enabled ?? true);
      const nextState = !current;
      chrome.storage.local.set({ pruningEnabled: nextState, enabled: nextState }, () => {
        showCaspianToast(`⚡ Chat Pruning ${nextState ? 'Activated' : 'Paused'}`);
      });
    });
  }
});

// ------------------------------------------
// 3. TEMPORARY CHAT DETECTION & EXTRACTION
// ------------------------------------------
function isTemporaryChat() {
  const isUrlTemp = window.location.href.includes('temporary-chat=true');
  const isDomTemp = !!document.querySelector('[data-testid="temporary-chat-indicator"]') ||
                    !!document.querySelector('button[aria-label*="Temporary"]') ||
                    (document.body && document.body.innerText && document.body.innerText.toLowerCase().includes('temporary chat'));
  return isUrlTemp || isDomTemp;
}

function getChatTitle() {
  const titleEl = document.querySelector('title') || document.querySelector('h1');
  let title = titleEl ? titleEl.textContent.trim() : 'ChatGPT Conversation';
  title = title.replace(/ - (ChatGPT|Gemini)$/i, '').replace(/^(ChatGPT|Gemini) - /i, '').trim();
  return title || 'Saved Conversation';
}

function extractConversationData() {
  let turnEls = document.querySelectorAll('[data-testid^="conversation-turn"]');
  if (!turnEls || turnEls.length === 0) turnEls = document.querySelectorAll('article');
  if (!turnEls || turnEls.length === 0) turnEls = document.querySelectorAll('main [data-message-author-role], main div.group');

  const turns = [];
  turnEls.forEach((el, idx) => {
    const isUser = !!el.querySelector('[data-message-author-role="user"]') ||
                   el.getAttribute('data-message-author-role') === 'user' ||
                   (el.innerText && el.innerText.includes('You said:'));
    const role = isUser ? 'User' : 'ChatGPT';

    const bodyEl = el.querySelector('.markdown') || el.querySelector('[data-message-author-role]') || el;
    const text = bodyEl ? bodyEl.innerText.trim() : '';
    const html = bodyEl ? bodyEl.innerHTML : '';

    if (text) {
      turns.push({ role, content: text, htmlContent: html, index: idx + 1 });
    }
  });

  const title = getChatTitle();
  const dateStr = new Date().toLocaleString();
  const isTemp = isTemporaryChat();

  let markdown = `# ${title}\n\n`;
  markdown += `*Exported via Caspian on ${dateStr}*\n`;
  markdown += `*Session Mode: ${isTemp ? 'Temporary Chat Session' : 'Standard Session'}*\n\n`;
  markdown += `---\n\n`;

  turns.forEach((t) => {
    const icon = t.role === 'User' ? '👤 **User**' : '🤖 **AI**';
    markdown += `### ${icon}\n\n${t.content}\n\n---\n\n`;
  });

  return {
    title,
    isTemporary: isTemp,
    turnCount: turns.length,
    turns,
    markdown
  };
}

// ------------------------------------------
// 4. AUTO-RESTORATION ON NEW NORMAL CHAT
// ------------------------------------------
function checkAndRestoreTransferContext() {
  const isGemini = window.location.hostname.includes('gemini');
  const isNormalChat = isGemini ? true : !window.location.href.includes('temporary-chat=true');
  if (!isNormalChat) return;

  chrome.storage.local.get(['pendingTransferContext', 'disabledSites'], (data) => {
    if (!data.pendingTransferContext || isSiteDisabled(data.disabledSites || [])) return;

    const transferData = data.pendingTransferContext;
    console.log('[Caspian] Restoring temporary chat into new normal chat session on', window.location.hostname);

    let attempts = 0;
    const interval = setInterval(() => {
      attempts++;
      
      const foundEl = document.querySelector('rich-textarea div[contenteditable="true"]') ||
                      document.querySelector('div[contenteditable="true"]') ||
                      document.querySelector('[aria-label*="Ask Gemini"]') ||
                      document.querySelector('rich-textarea p') ||
                      document.querySelector('.ql-editor p') ||
                      document.querySelector('.ql-editor') ||
                      document.querySelector('#prompt-textarea') || 
                      document.querySelector('textarea') ||
                      document.querySelector('p[data-placeholder]');
      
      if (foundEl) {
        clearInterval(interval);

        const promptPrefix = `Below is a saved conversation history from a temporary chat session ("${transferData.title}"). Please review and remember this context so we can seamlessly continue our session here:\n\n---\n\n`;
        const fullPrompt = promptPrefix + transferData.markdown;

        // 1. Copy to clipboard as guaranteed 100% backup fallback
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(fullPrompt).catch(() => {});
        }

        const editable = foundEl.closest('[contenteditable="true"]') || foundEl;

        editable.click();
        editable.focus();

        if (editable.tagName === 'TEXTAREA' || editable.id === 'prompt-textarea') {
          if (document.queryCommandSupported && document.queryCommandSupported('insertText')) {
            document.execCommand('insertText', false, fullPrompt);
          } else {
            editable.value = fullPrompt;
            editable.dispatchEvent(new Event('input', { bubbles: true }));
          }
        } else {
          // 2. Gemini rich-textarea / Lit / Quill contenteditable handling
          try {
            editable.innerText = fullPrompt;
            editable.textContent = fullPrompt;

            const pChild = editable.querySelector('p');
            if (pChild) pChild.innerText = fullPrompt;

            const sel = window.getSelection();
            if (sel) {
              const range = document.createRange();
              range.selectNodeContents(editable);
              range.collapse(false);
              sel.removeAllRanges();
              sel.addRange(range);
            }

            const events = ['focus', 'keydown', 'keypress', 'beforeinput', 'input', 'change', 'keyup'];
            events.forEach(type => {
              editable.dispatchEvent(new Event(type, { bubbles: true, composed: true }));
            });

            if (document.queryCommandSupported && document.queryCommandSupported('insertText')) {
              document.execCommand('insertText', false, ' ');
            }
          } catch (err) {
            console.error('[Caspian] Gemini injection error:', err);
          }
        }

        showCaspianToast(`✨ Temporary Chat Context Ready! Injected & Copied to Clipboard (Press Ctrl+V if needed).`);
        chrome.storage.local.remove('pendingTransferContext');
      }

      if (attempts > 60) {
        clearInterval(interval);
      }
    }, 500);
  });
}

function showCaspianToast(message) {
  const existing = document.getElementById('caspian-toast-overlay');
  if (existing) existing.remove();

  const toast = document.createElement('div');
  toast.id = 'caspian-toast-overlay';
  toast.style.position = 'fixed';
  toast.style.bottom = '24px';
  toast.style.right = '24px';
  toast.style.zIndex = '999999';
  toast.style.background = 'linear-gradient(135deg, var(--accent, #A2A9A9), var(--secondary, #1B4264))';
  toast.style.color = '#ffffff';
  toast.style.padding = '12px 18px';
  toast.style.borderRadius = '12px';
  toast.style.fontFamily = 'system-ui, -apple-system, sans-serif';
  toast.style.fontSize = '13px';
  toast.style.fontWeight = '600';
  toast.style.boxShadow = '0 8px 24px rgba(0, 0, 0, 0.2)';
  toast.style.transition = 'all 0.3s ease';
  toast.innerText = message;

  document.body.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// ------------------------------------------
// 5. CHROME MESSAGE LISTENER (POPUP COMM)
// ------------------------------------------
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === 'GET_CHAT_INFO') {
    const data = extractConversationData();
    sendResponse({
      isTemporary: data.isTemporary,
      turnCount: data.turnCount,
      title: data.title
    });
    return false;
  }

  if (request.action === 'DO_COPY') {
    const data = extractConversationData();
    if (data.markdown && data.turnCount > 0) {
      navigator.clipboard.writeText(data.markdown).then(() => {
        showCaspianToast('📋 Full transcript copied to clipboard!');
        sendResponse({ success: true, count: data.turnCount });
      }).catch(() => {
        const ta = document.createElement('textarea');
        ta.value = data.markdown;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        showCaspianToast('📋 Full transcript copied to clipboard!');
        sendResponse({ success: true, count: data.turnCount });
      });
    } else {
      showCaspianToast('⚠️ No conversation messages found on this page.');
      sendResponse({ success: false, count: 0 });
    }
    return true;
  }

  if (request.action === 'DO_EXPORT') {
    const data = extractConversationData();
    if (data.markdown && data.turnCount > 0) {
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

      showCaspianToast('💾 Transcript exported as Markdown (.md)!');
      sendResponse({ success: true, count: data.turnCount });
    } else {
      showCaspianToast('⚠️ No conversation messages found to export.');
      sendResponse({ success: false, count: 0 });
    }
    return false;
  }

  if (request.action === 'DO_CONVERT') {
    const data = extractConversationData();
    if (data.markdown && data.turnCount > 0) {
      const isGemini = window.location.hostname.includes('gemini');
      const targetUrl = isGemini ? 'https://gemini.google.com/app' : 'https://chatgpt.com/';

      chrome.storage.local.set({ pendingTransferContext: data }, () => {
        showCaspianToast('🚀 Opening new permanent chat session...');
        window.open(targetUrl, '_blank');
        sendResponse({ success: true, count: data.turnCount });
      });
    } else {
      showCaspianToast('⚠️ No conversation messages found to convert.');
      sendResponse({ success: false, count: 0 });
    }
    return true;
  }

  if (request.action === 'DO_WEBPDF') {
    let printStyle = document.getElementById('caspian-print-styles');
    if (!printStyle) {
      printStyle = document.createElement('style');
      printStyle.id = 'caspian-print-styles';
      printStyle.innerHTML = `
        @media print {
          @page { margin: 1cm; size: auto; }
          body, html, main, div, article {
            overflow: visible !important;
            height: auto !important;
            max-height: none !important;
            min-height: 0 !important;
            position: static !important;
          }
          [data-testid^="conversation-turn"], article, main div.group {
            display: block !important;
            page-break-inside: avoid !important;
            break-inside: avoid !important;
            margin-bottom: 20px !important;
          }
          #caspian-print-loader, nav, header, [data-testid="sidebar"] {
            display: none !important;
          }
        }
      `;
      document.head.appendChild(printStyle);
    }

    const messages = document.querySelectorAll('[data-testid^="conversation-turn"], article, main div.group');
    messages.forEach(msg => msg.style.setProperty('display', 'block', 'important'));

    const scroller = document.querySelector('main div.overflow-y-auto') ||
                     document.querySelector('main') ||
                     document.documentElement ||
                     document.body;

    const oldOverlay = document.getElementById('caspian-print-loader');
    if (oldOverlay) oldOverlay.remove();

    const overlay = document.createElement('div');
    overlay.id = 'caspian-print-loader';
    overlay.style.cssText = 'position:fixed;top:24px;left:50%;transform:translateX(-50%);z-index:9999999;background:linear-gradient(135deg, #0f172a, #1e293b);color:#ffffff;padding:12px 24px;border-radius:30px;font-family:system-ui,-apple-system,sans-serif;font-size:13px;font-weight:600;box-shadow:0 10px 30px rgba(0,0,0,0.4);border:1px solid rgba(255,255,255,0.15);pointer-events:none;';
    overlay.innerHTML = '⚡ Caspian Loading Full Conversation... Please wait';
    document.body.appendChild(overlay);

    let currentY = 0;
    const viewportH = window.innerHeight || 800;
    const step = Math.max(300, Math.floor(viewportH * 0.7));

    const scrollInterval = setInterval(() => {
      const totalH = Math.max(
        scroller.scrollHeight || 0,
        document.body.scrollHeight || 0,
        document.documentElement.scrollHeight || 0
      );
      const maxScroll = Math.max(1, totalH - viewportH);

      currentY += step;

      if (scroller && typeof scroller.scrollTo === 'function') {
        scroller.scrollTo(0, currentY);
      }
      if (scroller) scroller.scrollTop = currentY;
      window.scrollTo(0, currentY);

      if (scroller && typeof scroller.dispatchEvent === 'function') {
        scroller.dispatchEvent(new Event('scroll', { bubbles: true }));
      }
      window.dispatchEvent(new Event('scroll', { bubbles: true }));

      const progress = Math.min(100, Math.round((currentY / maxScroll) * 100));
      overlay.innerHTML = `⚡ Rendering Conversation Messages (${progress}%)...`;

      if (currentY >= totalH + step) {
        clearInterval(scrollInterval);

        const virtualHolders = document.querySelectorAll('main div[style*="height"], main div[style*="min-height"]');
        virtualHolders.forEach(div => {
          div.style.setProperty('height', 'auto', 'important');
          div.style.setProperty('min-height', '0px', 'important');
        });

        if (scroller && typeof scroller.scrollTo === 'function') scroller.scrollTo(0, 0);
        window.scrollTo(0, 0);
        overlay.innerHTML = '✨ All Pages Rendered! Launching Print View...';

        setTimeout(() => {
          if (overlay) overlay.remove();
          window.print();
          applyTurbo();
          sendResponse({ success: true });
        }, 500);
      }
    }, 90);

    return true;
  }
});

if (document.readyState === 'complete' || document.readyState === 'interactive') {
  checkAndRestoreTransferContext();
} else {
  window.addEventListener('DOMContentLoaded', checkAndRestoreTransferContext);
}