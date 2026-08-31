/**
 * Caspian Flow Native PDF Study Viewer Engine
 * Powered by Mozilla PDF.js with Liquid Glass Study UI
 */

(function () {
  'use strict';

  // Configure PDF.js Worker
  if (window.pdfjsLib) {
    pdfjsLib.GlobalWorkerOptions.workerSrc = 'pdfjs/pdf.worker.min.js';
  }

  // State
  let pdfDoc = null;
  let totalPages = 0;
  let currentPage = 1;
  let currentScale = 1.0;
  let currentRotation = 0;
  let pdfPath = '';
  let pdfTitle = 'Document.pdf';
  let selectedTextCache = '';
  let renderedPages = new Set();
  let searchMatches = [];
  let currentSearchIndex = -1;

  // DOM Elements
  const viewport = document.getElementById('pdf-viewport');
  const loadingEl = document.getElementById('pdf-loading');
  const titleEl = document.getElementById('pdf-doc-title');
  const pageIndicator = document.getElementById('pdf-page-indicator');
  const pageChip = document.getElementById('pdf-page-chip');
  const zoomLabel = document.getElementById('pdf-zoom-label');
  const nightBtn = document.getElementById('btn-pdf-night');
  const searchBtn = document.getElementById('btn-pdf-search');
  const btnRotateLeft = document.getElementById('btn-pdf-rotate-left');
  const btnRotateRight = document.getElementById('btn-pdf-rotate-right');
  const searchBar = document.getElementById('pdf-search-bar');
  const searchInput = document.getElementById('pdf-search-input');
  const searchCount = document.getElementById('pdf-search-count');
  const searchPrev = document.getElementById('btn-search-prev');
  const searchNext = document.getElementById('btn-search-next');
  const searchClose = document.getElementById('btn-search-close');
  const outlineBtn = document.getElementById('btn-pdf-outline');
  const outlineDrawer = document.getElementById('pdf-outline-drawer');
  const outlineList = document.getElementById('pdf-outline-list');
  const outlineClose = document.getElementById('btn-outline-close');
  const jumpModal = document.getElementById('pdf-jump-modal');
  const jumpInput = document.getElementById('pdf-jump-input');
  const jumpCancel = document.getElementById('btn-jump-cancel');
  const jumpConfirm = document.getElementById('btn-jump-confirm');
  const btnWhirlpool = document.getElementById('btn-pdf-whirlpool');
  const whirlpoolOverlay = document.getElementById('pdf-whirlpool-overlay');
  const whirlpoolCanvas = document.getElementById('pdf-whirlpool-canvas');
  const whirlpoolClose = document.getElementById('btn-whirlpool-close');
  const aiFloatingMenu = document.getElementById('pdf-ai-floating-menu');

  // AI Menu Buttons
  const btnAiGoogle = document.getElementById('btn-ai-google');
  const btnAiChatgpt = document.getElementById('btn-ai-chatgpt');
  const btnAiGemini = document.getElementById('btn-ai-gemini');
  const btnAiSplit = document.getElementById('btn-ai-split');
  const btnAiCopy = document.getElementById('btn-ai-copy');

  // Whirlpool State
  let isWhirlpoolActive = false;
  let isDrawingWhirlpool = false;
  let whirlpoolCropBase64 = '';
  let whirlpoolPoints = [];

  // Zoom Buttons
  const btnZoomIn = document.getElementById('btn-zoom-in');
  const btnZoomOut = document.getElementById('btn-zoom-out');
  const btnFitWidth = document.getElementById('btn-fit-width');

  // =========================================================
  // INITIALIZATION & PARAMETER PARSING
  // =========================================================
  function init() {
    const params = new URLSearchParams(window.location.search);
    pdfPath = params.get('file') || params.get('path') || '';
    pdfTitle = params.get('title') || 'Study Document.pdf';

    titleEl.textContent = pdfTitle;
    document.title = pdfTitle;

    // Restore Night Study Mode
    const savedNight = localStorage.getItem('caspian_pdf_night_mode') === 'true';
    if (savedNight) {
      document.body.classList.add('night-mode');
      nightBtn.classList.add('active');
    }

    if (!pdfPath) {
      loadingEl.innerHTML = '<p style="color: #ef4444; font-weight: 700;">No PDF file specified.</p>';
      return;
    }

    loadPdfDocument(pdfPath);
    setupEventListeners();
    setupSelectionListener();
  }

  // =========================================================
  // DOCUMENT LOADING VIA CASPIAN STREAM
  // =========================================================
  async function loadPdfDocument(path) {
    try {
      // Build Caspian Native Stream URL
      const streamUrl = 'https://caspian.pdf/stream?path=' + encodeURIComponent(path);
      
      const loadingTask = pdfjsLib.getDocument({
        url: streamUrl,
        cMapUrl: 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/cmaps/',
        cMapPacked: true,
        enableXfa: true
      });

      loadingTask.onProgress = function (progress) {
        if (progress.total > 0) {
          const percent = Math.round((progress.loaded / progress.total) * 100);
          loadingEl.querySelector('p').textContent = `Loading PDF (${percent}%)...`;
        }
      };

      pdfDoc = await loadingTask.promise;
      totalPages = pdfDoc.numPages;
      pageIndicator.textContent = `1 / ${totalPages}`;
      jumpInput.max = totalPages;

      // Remove loading indicator
      if (loadingEl && loadingEl.parentNode) {
        loadingEl.parentNode.removeChild(loadingEl);
      }

      // Calculate Initial Scale: Fit to device screen width
      calculateFitWidthScale();

      // Create page placeholder wrappers
      createPagePlaceholders();

      // Render visible pages
      renderVisiblePages();

      // Load Outline/Bookmarks
      loadDocumentOutline();
    } catch (err) {
      console.error('PDF Loading Error:', err);
      // Fallback: try base64 via CaspianBridge if stream fails
      if (window.CaspianBridge && typeof window.CaspianBridge.getPdfBase64 === 'function') {
        try {
          const b64 = window.CaspianBridge.getPdfBase64(path);
          if (b64 && b64.length > 0) {
            const raw = atob(b64);
            const uint8 = new Uint8Array(raw.length);
            for (let i = 0; i < raw.length; i++) uint8[i] = raw.charCodeAt(i);

            pdfDoc = await pdfjsLib.getDocument({ data: uint8 }).promise;
            totalPages = pdfDoc.numPages;
            pageIndicator.textContent = `1 / ${totalPages}`;
            if (loadingEl && loadingEl.parentNode) loadingEl.parentNode.removeChild(loadingEl);
            calculateFitWidthScale();
            createPagePlaceholders();
            renderVisiblePages();
            loadDocumentOutline();
            return;
          }
        } catch (fallbackErr) {
          console.error('Base64 Fallback Error:', fallbackErr);
        }
      }
      loadingEl.innerHTML = `<p style="color: #ef4444; font-weight: 700;">Failed to load PDF.<br><span style="font-size: 11px; font-weight: 400; color: #9ca3af;">${err.message}</span></p>`;
    }
  }

  function calculateFitWidthScale() {
    const availableWidth = window.innerWidth - 20; // 10px margins
    currentScale = Math.min(Math.max((availableWidth / 595), 0.7), 2.2); // 595 is standard A4 points
    updateZoomDisplay();
  }

  function updateZoomDisplay() {
    zoomLabel.textContent = `${Math.round(currentScale * 100)}%`;
  }

  // =========================================================
  // PAGE PLACEHOLDERS & VIRTUAL CONTINUOUS SCROLL
  // =========================================================
  function createPagePlaceholders() {
    viewport.innerHTML = '';
    for (let pageNum = 1; pageNum <= totalPages; pageNum++) {
      const container = document.createElement('div');
      container.className = 'pdf-page-container';
      container.id = `page-container-${pageNum}`;
      container.dataset.pageNumber = pageNum;
      container.style.width = '100%';
      container.style.maxWidth = `${Math.round(595 * currentScale)}px`;
      container.style.minHeight = `${Math.round(842 * currentScale)}px`;

      viewport.appendChild(container);
    }
  }

  async function renderPage(pageNum) {
    if (renderedPages.has(pageNum) || !pdfDoc) return;
    renderedPages.add(pageNum);

    const container = document.getElementById(`page-container-${pageNum}`);
    if (!container) return;

    try {
      const page = await pdfDoc.getPage(pageNum);
      const totalRotation = ((page.rotate || 0) + currentRotation) % 360;
      const viewportData = page.getViewport({ scale: currentScale, rotation: totalRotation });

      // Update container dimensions
      container.style.width = `${viewportData.width}px`;
      container.style.height = `${viewportData.height}px`;
      container.style.maxWidth = `${viewportData.width}px`;
      container.style.minHeight = `${viewportData.height}px`;

      // Canvas Layer
      let canvas = container.querySelector('canvas');
      if (!canvas) {
        canvas = document.createElement('canvas');
        canvas.className = 'pdf-page-canvas';
        container.appendChild(canvas);
      }

      const outputScale = window.devicePixelRatio || 1;
      canvas.width = Math.floor(viewportData.width * outputScale);
      canvas.height = Math.floor(viewportData.height * outputScale);
      canvas.style.width = `${Math.floor(viewportData.width)}px`;
      canvas.style.height = `${Math.floor(viewportData.height)}px`;

      const transform = outputScale !== 1 ? [outputScale, 0, 0, outputScale, 0, 0] : null;
      const renderContext = {
        canvasContext: canvas.getContext('2d'),
        transform: transform,
        viewport: viewportData
      };

      await page.render(renderContext).promise;

      // TextLayer for Native Text Selection & Copy
      let textLayerDiv = container.querySelector('.textLayer');
      if (!textLayerDiv) {
        textLayerDiv = document.createElement('div');
        textLayerDiv.className = 'textLayer';
        container.appendChild(textLayerDiv);
      } else {
        textLayerDiv.innerHTML = '';
      }

      textLayerDiv.style.width = `${viewportData.width}px`;
      textLayerDiv.style.height = `${viewportData.height}px`;

      const textContent = await page.getTextContent();
      pdfjsLib.renderTextLayer({
        textContentSource: textContent,
        container: textLayerDiv,
        viewport: viewportData,
        textDivs: []
      });

    } catch (err) {
      console.error(`Error rendering page ${pageNum}:`, err);
      renderedPages.delete(pageNum);
    }
  }

  function renderVisiblePages() {
    const viewTop = viewport.scrollTop;
    const viewBottom = viewTop + viewport.clientHeight;
    const buffer = viewport.clientHeight * 1.5; // pre-render buffer

    for (let pageNum = 1; pageNum <= totalPages; pageNum++) {
      const container = document.getElementById(`page-container-${pageNum}`);
      if (!container) continue;

      const top = container.offsetTop;
      const bottom = top + container.offsetHeight;

      // Check visibility within viewport + buffer
      if (bottom >= (viewTop - buffer) && top <= (viewBottom + buffer)) {
        renderPage(pageNum);
      }

      // Track active current page
      if (top <= viewTop + (viewport.clientHeight / 3) && bottom >= viewTop) {
        if (currentPage !== pageNum) {
          currentPage = pageNum;
          pageIndicator.textContent = `${currentPage} / ${totalPages}`;
        }
      }
    }
  }

  function reRenderAllPages() {
    renderedPages.clear();
    for (let pageNum = 1; pageNum <= totalPages; pageNum++) {
      const container = document.getElementById(`page-container-${pageNum}`);
      if (container) {
        container.innerHTML = '';
        container.style.width = '';
        container.style.height = '';
        container.style.maxWidth = '';
        container.style.minHeight = '';
      }
    }
    renderVisiblePages();
  }

  function rotatePages(deltaDegrees) {
    currentRotation = (currentRotation + deltaDegrees + 360) % 360;
    reRenderAllPages();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast(`Rotated to ${currentRotation}°`);
    }
  }

  // =========================================================
  // CONTEXTUAL "ASK CASPIAN AI" FLOATING LIQUID GLASS MENU
  // =========================================================
  function setupSelectionListener() {
    let selectionDebounce = null;

    const processSelection = () => {
      const selection = window.getSelection();
      if (!selection || selection.isCollapsed) {
        hideAiMenu();
        return;
      }

      const text = selection.toString().trim();
      if (text.length > 0 && selection.rangeCount > 0) {
        selectedTextCache = text;
        try {
          const range = selection.getRangeAt(0);
          const rect = range.getBoundingClientRect();
          if (rect && (rect.width > 0 || rect.height > 0)) {
            positionAiMenu(rect);
            return;
          }
        } catch (e) {}
      }

      hideAiMenu();
    };

    const handleSelectionChange = () => {
      clearTimeout(selectionDebounce);
      selectionDebounce = setTimeout(processSelection, 100);
    };

    document.addEventListener('selectionchange', handleSelectionChange);

    // Explicit touch / mouse release triggers prompt immediately
    viewport.addEventListener('touchend', () => {
      setTimeout(processSelection, 50);
    }, { passive: true });
    viewport.addEventListener('mouseup', () => {
      setTimeout(processSelection, 50);
    });

    // Update position on scroll
    viewport.addEventListener('scroll', () => {
      if (aiFloatingMenu.style.display === 'flex') {
        const selection = window.getSelection();
        if (selection && !selection.isCollapsed && selection.rangeCount > 0) {
          try {
            const rect = selection.getRangeAt(0).getBoundingClientRect();
            positionAiMenu(rect);
          } catch (e) {}
        } else {
          hideAiMenu();
        }
      }
    }, { passive: true });

    // Prevent floating menu clicks from stealing selection focus
    aiFloatingMenu.addEventListener('mousedown', (e) => {
      e.preventDefault();
      e.stopPropagation();
    });
  }

  function positionAiMenu(rect) {
    aiFloatingMenu.style.display = 'flex';

    const menuWidth = aiFloatingMenu.offsetWidth || 310;
    const menuHeight = aiFloatingMenu.offsetHeight || 38;

    // Horizontal positioning: centered over selection, strictly bounded within viewport
    let left = rect.left + (rect.width / 2);
    const halfWidth = menuWidth / 2;
    left = Math.max(halfWidth + 10, Math.min(window.innerWidth - halfWidth - 10, left));

    // Vertical positioning:
    // Prefer placing 14px above the selection
    let top = rect.top - menuHeight - 14;

    // If placing above would hit the top appbar (52px) or go offscreen, place BELOW the selection
    if (top < 72) {
      top = rect.bottom + 14;
    }

    // Strict boundary clamping so the menu CANNOT go offscreen top or bottom!
    const minTop = 64; // Safe buffer below appbar
    const maxTop = window.innerHeight - menuHeight - 65; // Safe buffer above bottom zoom bar
    top = Math.max(minTop, Math.min(maxTop, top));

    aiFloatingMenu.style.left = `${Math.round(left)}px`;
    aiFloatingMenu.style.top = `${Math.round(top)}px`;
  }

  function hideAiMenu() {
    aiFloatingMenu.style.display = 'none';
  }

  // =========================================================
  // EVENT LISTENERS & TOOLS
  // =========================================================
  function setupEventListeners() {
    // Scroll listener for virtual rendering
    viewport.addEventListener('scroll', () => {
      renderVisiblePages();
    }, { passive: true });

    // Night Study Mode
    nightBtn.addEventListener('click', () => {
      const isNight = document.body.classList.toggle('night-mode');
      nightBtn.classList.toggle('active', isNight);
      localStorage.setItem('caspian_pdf_night_mode', isNight ? 'true' : 'false');
      if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
        window.CaspianBridge.showToast(isNight ? '🌙 Night Study Mode Active' : '☀️ Day Reading Mode Active');
      }
    });

    // Zoom Controls
    btnZoomIn.addEventListener('click', () => {
      currentScale = Math.min(currentScale + 0.15, 3.0);
      updateZoomDisplay();
      reRenderAllPages();
    });

    btnZoomOut.addEventListener('click', () => {
      currentScale = Math.max(currentScale - 0.15, 0.5);
      updateZoomDisplay();
      reRenderAllPages();
    });

    btnFitWidth.addEventListener('click', () => {
      calculateFitWidthScale();
      reRenderAllPages();
    });

    // Jump to Page Modal
    pageChip.addEventListener('click', () => {
      jumpInput.value = currentPage;
      jumpModal.style.display = 'flex';
      setTimeout(() => jumpInput.focus(), 50);
    });

    jumpCancel.addEventListener('click', () => {
      jumpModal.style.display = 'none';
    });

    jumpConfirm.addEventListener('click', () => {
      const targetPage = parseInt(jumpInput.value, 10);
      if (targetPage >= 1 && targetPage <= totalPages) {
        jumpToPage(targetPage);
      }
      jumpModal.style.display = 'none';
    });

    // Search Toggle
    searchBtn.addEventListener('click', () => {
      const isOpen = searchBar.style.display === 'flex';
      searchBar.style.display = isOpen ? 'none' : 'flex';
      searchBtn.classList.toggle('active', !isOpen);
      if (!isOpen) {
        setTimeout(() => searchInput.focus(), 60);
      }
    });

    searchClose.addEventListener('click', () => {
      searchBar.style.display = 'none';
      searchBtn.classList.remove('active');
      clearSearchHighlights();
    });

    searchInput.addEventListener('input', () => {
      executeSearch(searchInput.value.trim());
    });

    searchNext.addEventListener('click', () => {
      navigateSearchMatch(1);
    });

    searchPrev.addEventListener('click', () => {
      navigateSearchMatch(-1);
    });

    // Table of Contents Drawer
    outlineBtn.addEventListener('click', () => {
      outlineDrawer.classList.toggle('open');
      outlineBtn.classList.toggle('active', outlineDrawer.classList.contains('open'));
    });

    outlineClose.addEventListener('click', () => {
      outlineDrawer.classList.remove('open');
      outlineBtn.classList.remove('active');
    });

    // Rotate PDF Pages 90° Left and Right
    if (btnRotateLeft) {
      btnRotateLeft.addEventListener('click', () => rotatePages(-90));
    }
    if (btnRotateRight) {
      btnRotateRight.addEventListener('click', () => rotatePages(90));
    }

    // Caspian Whirlpool Toggle
    if (btnWhirlpool) {
      btnWhirlpool.addEventListener('click', () => {
        toggleWhirlpoolMode();
      });
    }

    // Contextual AI Actions (Ask Google / ChatGPT / Gemini / Split / Copy)
    if (btnAiGoogle) {
      btnAiGoogle.addEventListener('click', () => {
        if (whirlpoolCropBase64 && window.CaspianBridge && typeof window.CaspianBridge.launchGoogleLensWithBase64 === 'function') {
          window.CaspianBridge.launchGoogleLensWithBase64(whirlpoolCropBase64);
        } else if (selectedTextCache && window.CaspianBridge && typeof window.CaspianBridge.searchGoogleWithText === 'function') {
          window.CaspianBridge.searchGoogleWithText(selectedTextCache);
        } else if (selectedTextCache && window.CaspianBridge) {
          window.CaspianBridge.askAiFromPdf(selectedTextCache, 'gemini');
        }
        closeWhirlpoolMode();
        hideAiMenu();
      });
    }

    btnAiChatgpt.addEventListener('click', () => {
      if (whirlpoolCropBase64 && window.CaspianBridge && typeof window.CaspianBridge.askAiFromPdfWithImage === 'function') {
        window.CaspianBridge.askAiFromPdfWithImage(selectedTextCache || 'Explain this selected image/diagram', whirlpoolCropBase64, 'chatgpt');
      } else if (selectedTextCache && window.CaspianBridge) {
        window.CaspianBridge.askAiFromPdf(selectedTextCache, 'chatgpt');
      }
      closeWhirlpoolMode();
      hideAiMenu();
    });

    btnAiGemini.addEventListener('click', () => {
      if (whirlpoolCropBase64 && window.CaspianBridge && typeof window.CaspianBridge.askAiFromPdfWithImage === 'function') {
        window.CaspianBridge.askAiFromPdfWithImage(selectedTextCache || 'Explain this selected image/diagram', whirlpoolCropBase64, 'gemini');
      } else if (selectedTextCache && window.CaspianBridge) {
        window.CaspianBridge.askAiFromPdf(selectedTextCache, 'gemini');
      }
      closeWhirlpoolMode();
      hideAiMenu();
    });

    btnAiSplit.addEventListener('click', () => {
      if (whirlpoolCropBase64 && window.CaspianBridge && typeof window.CaspianBridge.askAiFromPdfWithImage === 'function') {
        window.CaspianBridge.askAiFromPdfWithImage(selectedTextCache || 'Explain this selected image/diagram', whirlpoolCropBase64, 'split');
      } else if (selectedTextCache && window.CaspianBridge) {
        window.CaspianBridge.askAiFromPdf(selectedTextCache, 'split');
      }
      closeWhirlpoolMode();
      hideAiMenu();
    });

    btnAiCopy.addEventListener('click', () => {
      if (selectedTextCache) {
        if (window.CaspianBridge && typeof window.CaspianBridge.copyToClipboard === 'function') {
          window.CaspianBridge.copyToClipboard(selectedTextCache);
        } else if (navigator.clipboard) {
          navigator.clipboard.writeText(selectedTextCache);
        }
      }
      if (whirlpoolCropBase64 && window.CaspianBridge && typeof window.CaspianBridge.copyImageToClipboard === 'function') {
        window.CaspianBridge.copyImageToClipboard(whirlpoolCropBase64);
      }
      closeWhirlpoolMode();
      hideAiMenu();
    });

    setupWhirlpoolEngine();
  }

  function jumpToPage(pageNum) {
    const container = document.getElementById(`page-container-${pageNum}`);
    if (container) {
      container.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  // =========================================================
  // DOCUMENT OUTLINE / BOOKMARKS
  // =========================================================
  async function loadDocumentOutline() {
    if (!pdfDoc) return;
    try {
      const outline = await pdfDoc.getOutline();
      outlineList.innerHTML = '';

      if (!outline || outline.length === 0) {
        outlineList.innerHTML = '<p style="padding: 12px; font-size: 11.5px; color: var(--text-muted);">No Table of Contents found in this document.</p>';
        return;
      }

      outline.forEach(item => {
        const row = document.createElement('div');
        row.className = 'pdf-outline-item';
        row.textContent = item.title;
        row.addEventListener('click', async () => {
          if (item.dest) {
            let dest = item.dest;
            if (typeof dest === 'string') {
              dest = await pdfDoc.getDestination(dest);
            }
            if (dest && dest[0]) {
              const pageIndex = await pdfDoc.getPageIndex(dest[0]);
              jumpToPage(pageIndex + 1);
              outlineDrawer.classList.remove('open');
              outlineBtn.classList.remove('active');
            }
          }
        });
        outlineList.appendChild(row);
      });
    } catch (e) {
      console.warn('Outline loading not supported for this document:', e);
    }
  }

  // =========================================================
  // IN-DOCUMENT SEARCH / FIND
  // =========================================================
  async function executeSearch(query) {
    clearSearchHighlights();
    searchMatches = [];
    currentSearchIndex = -1;

    if (!query || query.length < 2 || !pdfDoc) {
      searchCount.textContent = '0 / 0';
      return;
    }

    const queryLower = query.toLowerCase();
    let totalMatchesFound = 0;

    for (let pageNum = 1; pageNum <= totalPages; pageNum++) {
      const page = await pdfDoc.getPage(pageNum);
      const content = await page.getTextContent();
      const strings = content.items.map(item => item.str);
      const fullText = strings.join(' ').toLowerCase();

      if (fullText.includes(queryLower)) {
        searchMatches.push({ pageNum, query: queryLower });
        totalMatchesFound++;
      }
    }

    searchCount.textContent = totalMatchesFound > 0 ? `1 / ${totalMatchesFound}` : '0 / 0';
    if (totalMatchesFound > 0) {
      currentSearchIndex = 0;
      jumpToPage(searchMatches[0].pageNum);
    }
  }

  function navigateSearchMatch(direction) {
    if (searchMatches.length === 0) return;
    currentSearchIndex += direction;
    if (currentSearchIndex >= searchMatches.length) currentSearchIndex = 0;
    if (currentSearchIndex < 0) currentSearchIndex = searchMatches.length - 1;

    searchCount.textContent = `${currentSearchIndex + 1} / ${searchMatches.length}`;
    jumpToPage(searchMatches[currentSearchIndex].pageNum);
  }

  function clearSearchHighlights() {
    searchMatches = [];
    currentSearchIndex = -1;
    searchCount.textContent = '0 / 0';
  }

  // =========================================================
  // CASPIAN WHIRLPOOL ENGINE (CIRCLE TO SEARCH / VISUAL CROP)
  // =========================================================
  function setupWhirlpoolEngine() {
    if (!whirlpoolOverlay || !whirlpoolCanvas) return;

    function resizeCanvas() {
      whirlpoolCanvas.width = window.innerWidth;
      whirlpoolCanvas.height = window.innerHeight;
    }
    window.addEventListener('resize', resizeCanvas);
    resizeCanvas();

    const ctx = whirlpoolCanvas.getContext('2d');

    function startDraw(e) {
      if (!isWhirlpoolActive) return;
      isDrawingWhirlpool = true;
      hideAiMenu();
      const pt = getEventCoord(e);
      whirlpoolPoints = [pt];
      ctx.clearRect(0, 0, whirlpoolCanvas.width, whirlpoolCanvas.height);
    }

    function moveDraw(e) {
      if (!isWhirlpoolActive || !isDrawingWhirlpool) return;
      e.preventDefault();
      const pt = getEventCoord(e);
      whirlpoolPoints.push(pt);
      renderWhirlpoolPath(ctx, whirlpoolPoints);
    }

    function endDraw(e) {
      if (!isWhirlpoolActive || !isDrawingWhirlpool) return;
      isDrawingWhirlpool = false;
      if (whirlpoolPoints.length < 3) {
        ctx.clearRect(0, 0, whirlpoolCanvas.width, whirlpoolCanvas.height);
        return;
      }
      processWhirlpoolSelection(ctx, whirlpoolPoints);
    }

    whirlpoolOverlay.addEventListener('mousedown', startDraw);
    whirlpoolOverlay.addEventListener('mousemove', moveDraw);
    window.addEventListener('mouseup', endDraw);

    whirlpoolOverlay.addEventListener('touchstart', startDraw, { passive: false });
    whirlpoolOverlay.addEventListener('touchmove', moveDraw, { passive: false });
    window.addEventListener('touchend', endDraw);

    if (whirlpoolClose) {
      whirlpoolClose.addEventListener('click', (e) => {
        e.stopPropagation();
        closeWhirlpoolMode();
      });
    }
  }

  function getEventCoord(e) {
    if (e.touches && e.touches.length > 0) {
      return { x: e.touches[0].clientX, y: e.touches[0].clientY };
    }
    return { x: e.clientX, y: e.clientY };
  }

  function renderWhirlpoolPath(ctx, pts) {
    ctx.clearRect(0, 0, whirlpoolCanvas.width, whirlpoolCanvas.height);
    if (pts.length < 2) return;

    ctx.save();
    ctx.beginPath();
    ctx.moveTo(pts[0].x, pts[0].y);
    for (let i = 1; i < pts.length; i++) {
      ctx.lineTo(pts[i].x, pts[i].y);
    }
    ctx.closePath();

    // Glowing cyan stroke
    ctx.strokeStyle = '#38bdf8';
    ctx.lineWidth = 2.5;
    ctx.shadowColor = '#0284c7';
    ctx.shadowBlur = 10;
    ctx.stroke();

    // Subtle inner fill
    ctx.fillStyle = 'rgba(56, 189, 248, 0.10)';
    ctx.fill();
    ctx.restore();
  }

  function processWhirlpoolSelection(ctx, pts) {
    let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
    pts.forEach(p => {
      if (p.x < minX) minX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.x > maxX) maxX = p.x;
      if (p.y > maxY) maxY = p.y;
    });

    const width = maxX - minX;
    const height = maxY - minY;

    if (width < 18 || height < 18) {
      ctx.clearRect(0, 0, whirlpoolCanvas.width, whirlpoolCanvas.height);
      return;
    }

    // Keep visual frame with glowing accent
    ctx.save();
    ctx.strokeStyle = '#10b981';
    ctx.lineWidth = 2;
    ctx.setLineDash([6, 4]);
    ctx.strokeRect(minX, minY, width, height);
    ctx.restore();

    // Crop image from active rendered page canvas
    cropImageFromSelection(minX, minY, width, height);

    // Extract any text within the selection bounding box
    extractTextInBounds(minX, minY, maxX, maxY);

    // Position floating AI menu near the selection
    positionAiMenu({
      left: minX,
      top: minY,
      width: width,
      height: height,
      bottom: maxY
    });
  }

  function cropImageFromSelection(screenX, screenY, width, height) {
    whirlpoolCropBase64 = '';
    const pageContainers = document.querySelectorAll('.pdf-page-container');
    for (const container of pageContainers) {
      const rect = container.getBoundingClientRect();
      const interLeft = Math.max(screenX, rect.left);
      const interTop = Math.max(screenY, rect.top);
      const interRight = Math.min(screenX + width, rect.right);
      const interBottom = Math.min(screenY + height, rect.bottom);

      if (interRight > interLeft && interBottom > interTop) {
        const pageCanvas = container.querySelector('canvas');
        if (pageCanvas) {
          try {
            const scaleX = pageCanvas.width / rect.width;
            const scaleY = pageCanvas.height / rect.height;

            const cropX = (interLeft - rect.left) * scaleX;
            const cropY = (interTop - rect.top) * scaleY;
            const cropW = (interRight - interLeft) * scaleX;
            const cropH = (interBottom - interTop) * scaleY;

            const offscreen = document.createElement('canvas');
            offscreen.width = cropW;
            offscreen.height = cropH;
            const offCtx = offscreen.getContext('2d');
            offCtx.drawImage(pageCanvas, cropX, cropY, cropW, cropH, 0, 0, cropW, cropH);

            whirlpoolCropBase64 = offscreen.toDataURL('image/png');
          } catch (e) {
            console.error('Whirlpool crop error:', e);
          }
        }
        break;
      }
    }
  }

  function extractTextInBounds(minX, minY, maxX, maxY) {
    let collected = [];
    const textSpans = document.querySelectorAll('.textLayer span');
    for (const span of textSpans) {
      const r = span.getBoundingClientRect();
      if (r.right >= minX && r.left <= maxX && r.bottom >= minY && r.top <= maxY) {
        const t = span.textContent.trim();
        if (t) collected.push(t);
      }
    }
    if (collected.length > 0) {
      selectedTextCache = collected.join(' ');
    }
  }

  function toggleWhirlpoolMode() {
    if (isWhirlpoolActive) {
      closeWhirlpoolMode();
    } else {
      openWhirlpoolMode();
    }
  }

  function openWhirlpoolMode() {
    isWhirlpoolActive = true;
    if (whirlpoolOverlay) {
      whirlpoolOverlay.style.display = 'block';
      whirlpoolCanvas.width = window.innerWidth;
      whirlpoolCanvas.height = window.innerHeight;
      const ctx = whirlpoolCanvas.getContext('2d');
      ctx.clearRect(0, 0, whirlpoolCanvas.width, whirlpoolCanvas.height);
    }
    if (btnWhirlpool) btnWhirlpool.classList.add('whirlpool-active');
    hideAiMenu();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('🌀 Caspian Whirlpool: Circle or drag to search');
    }
  }

  function closeWhirlpoolMode() {
    isWhirlpoolActive = false;
    isDrawingWhirlpool = false;
    if (whirlpoolOverlay) {
      whirlpoolOverlay.style.display = 'none';
      if (whirlpoolCanvas) {
        const ctx = whirlpoolCanvas.getContext('2d');
        ctx.clearRect(0, 0, whirlpoolCanvas.width, whirlpoolCanvas.height);
      }
    }
    if (btnWhirlpool) btnWhirlpool.classList.remove('whirlpool-active');
    hideAiMenu();
  }

  // Start on DOMContentLoaded
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
