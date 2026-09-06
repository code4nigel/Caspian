/**
 * Caspian Flow - AI Reader Mode Content Extractor
 */
(function() {
  if (window.caspianReaderModeActive) return;
  window.caspianReaderModeActive = true;

  try {
    const title = document.querySelector('h1')?.innerText || document.title || 'Article';
    const article = document.querySelector('article') || document.querySelector('main') || document.querySelector('.post-content') || document.body;
    
    // Clone node to avoid mutating original during extraction
    const clone = article.cloneNode(true);
    
    // Remove unwanted clutter
    const unwanted = clone.querySelectorAll('script, style, nav, footer, header, .ad, .ads, [class*="sidebar"], [class*="comment"], [class*="share"]');
    unwanted.forEach(el => el.remove());

    const contentText = clone.innerText || clone.textContent;

    // Estimate reading time (approx 200 words/min)
    const wordCount = contentText.trim().split(/\s+/).length;
    const readMinutes = Math.max(1, Math.round(wordCount / 200));

    const html = `
      <div id="caspian-reader-container" style="
        background: #050811;
        color: #E2E8F0;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
        line-height: 1.8;
        padding: 24px 20px 80px 20px;
        min-height: 100vh;
      ">
        <div style="max-width: 680px; margin: 0 auto;">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;">
            <span style="font-size: 11px; font-weight: 700; color: #00E5FF; text-transform: uppercase; letter-spacing: 1px;">⚡ CASPIAN READER</span>
            <span style="font-size: 12px; color: #64748B;">⏱️ ${readMinutes} min read (${wordCount} words)</span>
          </div>
          <h1 style="font-size: 26px; font-weight: 800; color: #FFFFFF; line-height: 1.3; margin-bottom: 20px;">${title}</h1>
          <hr style="border: none; border-top: 1px solid rgba(0, 229, 255, 0.2); margin-bottom: 24px;" />
          <div style="font-size: 16px; color: #CBD5E1; white-space: pre-wrap; word-break: break-word;">${contentText.replace(/</g, '&lt;')}</div>
        </div>
      </div>
    `;

    document.body.innerHTML = html;
    document.body.style.background = '#050811';
    document.body.style.margin = '0';
  } catch (e) {
    console.error('Caspian Reader Extraction error: ' + e);
  }
})();
