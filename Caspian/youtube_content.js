// ==========================================================
// CASPIAN - YOUTUBE HOME FEED OPTIMIZER & NOT-INTERESTED ENGINE
// ==========================================================

(function () {
  'use strict';

  console.log('[Caspian] YouTube Engine Active');

  // Insert styles for floating Not-Interested button
  function injectStyles() {
    if (document.getElementById('caspian-yt-styles')) return;
    const style = document.createElement('style');
    style.id = 'caspian-yt-styles';
    style.textContent = `
      .caspian-yt-not-interested-btn {
        position: absolute !important;
        top: 8px !important;
        left: 8px !important;
        z-index: 2147483647 !important;
        width: 32px !important;
        height: 32px !important;
        border-radius: 50% !important;
        background: rgba(15, 23, 42, 0.94) !important;
        backdrop-filter: blur(10px) !important;
        -webkit-backdrop-filter: blur(10px) !important;
        border: 1.5px solid rgba(255, 255, 255, 0.6) !important;
        color: #ffffff !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        cursor: pointer !important;
        opacity: 0 !important;
        pointer-events: none !important;
        transform: scale(0.85) !important;
        transition: opacity 0.18s ease, transform 0.18s ease, background 0.18s ease !important;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.75) !important;
        padding: 0 !important;
      }
      .caspian-yt-btn-visible {
        opacity: 1 !important;
        pointer-events: auto !important;
        transform: scale(1) !important;
      }
      ytd-rich-item-renderer:hover .caspian-yt-not-interested-btn,
      ytd-video-renderer:hover .caspian-yt-not-interested-btn,
      ytd-grid-video-renderer:hover .caspian-yt-not-interested-btn,
      ytd-compact-video-renderer:hover .caspian-yt-not-interested-btn,
      ytd-rich-grid-media:hover .caspian-yt-not-interested-btn,
      ytd-thumbnail:hover .caspian-yt-not-interested-btn,
      #thumbnail:hover .caspian-yt-not-interested-btn,
      #dismissible:hover .caspian-yt-not-interested-btn,
      .caspian-yt-not-interested-btn:hover {
        opacity: 1 !important;
        pointer-events: auto !important;
        transform: scale(1) !important;
      }
      .caspian-yt-not-interested-btn:hover {
        background: #ef4444 !important;
        border-color: #fca5a5 !important;
        color: #ffffff !important;
        transform: scale(1.18) !important;
        box-shadow: 0 6px 22px rgba(239, 68, 68, 0.85) !important;
      }
      .caspian-yt-not-interested-btn:active {
        transform: scale(0.92) !important;
      }
      .caspian-yt-toast {
        position: fixed;
        bottom: 30px;
        left: 50%;
        transform: translateX(-50%);
        background: #0f172a;
        color: #ffffff;
        padding: 10px 22px;
        border-radius: 24px;
        font-family: system-ui, -apple-system, BlinkMacSystemFont, sans-serif;
        font-size: 13.5px;
        font-weight: 600;
        z-index: 2147483647;
        box-shadow: 0 10px 32px rgba(0,0,0,0.55);
        border: 1px solid #334155;
        animation: caspianFadeInOut 2.5s forwards;
      }
      @keyframes caspianFadeInOut {
        0% { opacity: 0; transform: translate(-50%, 12px); }
        15% { opacity: 1; transform: translate(-50%, 0); }
        85% { opacity: 1; transform: translate(-50%, 0); }
        100% { opacity: 0; transform: translate(-50%, 12px); }
      }
    `;
    (document.head || document.documentElement).appendChild(style);
  }

  function applyYouTubeOptimizations() {
    injectStyles();

    chrome.storage.local.get(['yt_feed_limit_enabled', 'yt_feed_limit', 'yt_not_interested_enabled'], (data) => {
      const feedEnabled = data.yt_feed_limit_enabled ?? true;
      const feedLimit = data.yt_feed_limit ?? 12;
      const notInterestedEnabled = data.yt_not_interested_enabled ?? true;

      const path = (window.location.pathname || '').toLowerCase();
      const isHomePage = path === '/' || path === '' || path === '/feed/you' || path === '/feed/explore';

      // 1. YouTube Feed Video Limiter
      const richItems = Array.from(document.querySelectorAll('ytd-rich-item-renderer, ytd-rich-section-renderer'));
      if (isHomePage && feedEnabled && feedLimit < 9999) {
        let visibleCount = 0;
        richItems.forEach(item => {
          if (visibleCount < feedLimit) {
            item.style.removeProperty('display');
            visibleCount++;
          } else {
            item.style.setProperty('display', 'none', 'important');
          }
        });
      } else {
        richItems.forEach(item => item.style.removeProperty('display'));
      }

      // 2. Floating Quick "Not Interested" Button
      if (notInterestedEnabled) {
        const videoCards = Array.from(document.querySelectorAll('ytd-rich-item-renderer, ytd-video-renderer, ytd-grid-video-renderer, ytd-compact-video-renderer, ytd-rich-grid-media'));
        videoCards.forEach(card => {
          if (card.querySelector('.caspian-yt-not-interested-btn')) return;

          const hostEl = card.querySelector('#dismissible') || card.querySelector('ytd-thumbnail') || card.querySelector('#thumbnail') || card;
          if (!hostEl) return;

          const computedPos = window.getComputedStyle(hostEl).position;
          if (computedPos === 'static') {
            hostEl.style.position = 'relative';
          }

          const btn = document.createElement('button');
          btn.className = 'caspian-yt-not-interested-btn';
          btn.title = 'Not Interested';
          btn.innerHTML = `
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"></line>
            </svg>
          `;

          // Seamless debounced hover listeners (750ms grace period)
          let hideTimer = null;
          const showBtn = () => {
            if (hideTimer) {
              clearTimeout(hideTimer);
              hideTimer = null;
            }
            btn.classList.add('caspian-yt-btn-visible');
          };
          const hideBtn = () => {
            if (hideTimer) clearTimeout(hideTimer);
            hideTimer = setTimeout(() => {
              try {
                if (!card.matches(':hover') && !hostEl.matches(':hover') && !btn.matches(':hover')) {
                  btn.classList.remove('caspian-yt-btn-visible');
                }
              } catch(e) {
                btn.classList.remove('caspian-yt-btn-visible');
              }
            }, 750);
          };

          card.addEventListener('mouseenter', showBtn);
          card.addEventListener('mouseleave', hideBtn);
          card.addEventListener('pointerenter', showBtn);
          card.addEventListener('pointerleave', hideBtn);
          hostEl.addEventListener('mouseenter', showBtn);
          hostEl.addEventListener('mouseleave', hideBtn);
          btn.addEventListener('mouseenter', showBtn);
          btn.addEventListener('mouseleave', hideBtn);

          // Helper to locate 3-dots menu button across all YouTube versions (including modern lockup="true")
          function findActionMenuButton(parentCard) {
            // 1. Explicit aria-label match
            const allButtons = Array.from(parentCard.querySelectorAll('button'));
            for (const b of allButtons) {
              if (b.classList.contains('caspian-yt-not-interested-btn')) continue;
              const label = (b.getAttribute('aria-label') || '').toLowerCase();
              if (label.includes('action') || label.includes('more') || label.includes('menu')) {
                return b;
              }
            }

            // 2. Modern YouTube Lockup view model button
            const lockupBtn = parentCard.querySelector('yt-lockup-metadata-view-model button, yt-button-shape button, button.yt-spec-button-shape-next, yt-icon-button button');
            if (lockupBtn && !lockupBtn.classList.contains('caspian-yt-not-interested-btn')) return lockupBtn;

            // 3. Legacy ytd-menu-renderer button
            const legacyBtn = parentCard.querySelector('#menu button, ytd-menu-renderer yt-icon-button, ytd-menu-renderer button, yt-icon-button.dropdown-trigger');
            if (legacyBtn) return legacyBtn.querySelector('button') || legacyBtn;

            // 4. Any icon button in metadata row (excluding playback controls)
            for (const b of allButtons) {
              if (b.classList.contains('caspian-yt-not-interested-btn')) continue;
              const label = (b.getAttribute('aria-label') || '').toLowerCase();
              if (label.includes('mute') || label.includes('play') || label.includes('volume') || label.includes('caption') || label.includes('time')) continue;
              if (b.querySelector('svg, yt-icon, yt-touch-feedback-shape')) {
                return b;
              }
            }

            return null;
          }

          // Click handler to execute native YouTube Not-Interested action
          btn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();

            console.log('%c[Caspian YT] 🚫 Not Interested Button Clicked', 'color: #38bdf8; font-weight: bold;');

            // Locate the 3-dots menu button in this video card
            const targetBtn = findActionMenuButton(card);

            if (!targetBtn) {
              console.warn('[Caspian YT] ⚠️ Could not find 3-dots button on video card', card);
              // Graceful direct dismiss fallback if 3-dots button is not found
              card.style.transition = 'all 0.3s ease';
              card.style.opacity = '0';
              setTimeout(() => { card.style.display = 'none'; }, 300);
              return;
            }

            console.log('%c[Caspian YT] Found 3-dots menu element:', 'color: #38bdf8;', targetBtn);
            
            // Dispatch full interaction events
            const eventOpts = { bubbles: true, cancelable: true, view: window, composed: true };
            targetBtn.dispatchEvent(new MouseEvent('mouseover', eventOpts));
            targetBtn.dispatchEvent(new PointerEvent('pointerdown', eventOpts));
            targetBtn.dispatchEvent(new MouseEvent('mousedown', eventOpts));
            targetBtn.dispatchEvent(new PointerEvent('pointerup', eventOpts));
            targetBtn.dispatchEvent(new MouseEvent('mouseup', eventOpts));
            targetBtn.click();

            let attempts = 0;
            const checkDropdown = setInterval(() => {
              attempts++;

              const menuItems = Array.from(document.querySelectorAll('ytd-menu-service-item-renderer, tp-yt-paper-item, yt-list-item-view-model, ytd-menu-navigation-item-renderer, yt-formatted-string, [role="menuitem"]'));
              
              if (attempts === 1 || attempts % 10 === 0) {
                const itemTexts = menuItems.map(i => (i.textContent || '').trim()).filter(Boolean);
                console.log(`[Caspian YT] (Attempt ${attempts}) Found popup menu items:`, itemTexts);
              }

              for (const item of menuItems) {
                const text = (item.textContent || '').trim().toLowerCase();
                if (text.includes('not interested') || text.includes("don't recommend")) {
                  clearInterval(checkDropdown);
                  console.log('%c[Caspian YT] ✅ Found and clicking "Not interested" option:', 'color: #4ade80; font-weight: bold;', item);
                  
                  const clickable = item.closest('ytd-menu-service-item-renderer, tp-yt-paper-item, yt-list-item-view-model, [role="menuitem"]') || item;
                  clickable.dispatchEvent(new PointerEvent('pointerdown', eventOpts));
                  clickable.dispatchEvent(new MouseEvent('mousedown', eventOpts));
                  clickable.dispatchEvent(new PointerEvent('pointerup', eventOpts));
                  clickable.dispatchEvent(new MouseEvent('mouseup', eventOpts));
                  clickable.click();
                  return;
                }
              }

              if (attempts >= 40) {
                clearInterval(checkDropdown);
                console.warn('[Caspian YT] ❌ Timed out waiting for "Not interested" option in dropdown.');
                const backdrop = document.querySelector('tp-yt-iron-overlay-backdrop');
                if (backdrop) backdrop.click();
                else document.body.click();
              }
            }, 20);
          });

          hostEl.appendChild(btn);
        });
      } else {
        document.querySelectorAll('.caspian-yt-not-interested-btn').forEach(btn => btn.remove());
      }
    });
  }

  // Run on load and observe DOM changes
  if (document.readyState === 'complete' || document.readyState === 'interactive') {
    applyYouTubeOptimizations();
  } else {
    window.addEventListener('DOMContentLoaded', applyYouTubeOptimizations);
  }

  const observer = new MutationObserver(() => {
    applyYouTubeOptimizations();
  });

  if (document.body) {
    observer.observe(document.body, { childList: true, subtree: true });
  } else {
    window.addEventListener('DOMContentLoaded', () => {
      observer.observe(document.body, { childList: true, subtree: true });
    });
  }

  setInterval(applyYouTubeOptimizations, 800);

  chrome.storage.onChanged.addListener((changes, area) => {
    if (area === 'local' && (changes.yt_feed_limit_enabled || changes.yt_feed_limit || changes.yt_not_interested_enabled)) {
      applyYouTubeOptimizations();
    }
  });
})();
