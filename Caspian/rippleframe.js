// ==========================================================================
// CASPIAN - RIPPLEFRAME STUDIO CONTROLLER
// ==========================================================================

(function () {
  'use strict';

  // State
  let currentTool = 'pan'; // pan, crop, blur, blackout, pen, highlighter, arrow, rect, circle, text
  let currentColor = '#ef4444';
  let currentStrokeSize = 4;
  let currentFontSize = 18;

  // Zoom & Pan
  let scale = 1.0;
  let panX = 0;
  let panY = 0;
  let isPanning = false;
  let startPanX = 0;
  let startPanY = 0;

  // Canvas & Layers
  const viewport = document.getElementById('canvas-viewport');
  const transformLayer = document.getElementById('canvas-transform-layer');
  const canvas = document.getElementById('main-editor-canvas');
  const ctx = canvas.getContext('2d');

  let baseImage = null;
  let canvasWidth = 0;
  let canvasHeight = 0;

  // Action History Stack
  let historyStack = [];
  let redoStack = [];
  const MAX_HISTORY = 30;

  // Active Drawing Variables
  let isDrawing = false;
  let drawStartX = 0;
  let drawStartY = 0;
  let currentPath = [];

  // Interactive Crop State
  let cropState = {
    active: false,
    x: 0,
    y: 0,
    w: 0,
    h: 0,
    dragHandle: null // 'tl', 'tr', 'bl', 'br', 'move', or null
  };

  // --------------------------------------------------------------------------
  // 1. IndexedDB Image Loader
  // --------------------------------------------------------------------------

  function openDatabase() {
    return new Promise((resolve, reject) => {
      const req = indexedDB.open('caspian_rippleframe_db', 1);
      req.onupgradeneeded = (e) => {
        const db = e.target.result;
        if (!db.objectStoreNames.contains('captures')) {
          db.createObjectStore('captures', { keyPath: 'id' });
        }
      };
      req.onsuccess = () => resolve(req.result);
      req.onerror = () => reject(req.error);
    });
  }

  async function loadCaptureFromDB() {
    try {
      const db = await openDatabase();
      return new Promise((resolve, reject) => {
        const tx = db.transaction('captures', 'readonly');
        const store = tx.objectStore('captures');
        const req = store.get('latest_capture');
        req.onsuccess = () => resolve(req.result);
        req.onerror = () => reject(req.error);
      });
    } catch (e) {
      console.warn('DB load failed, trying demo fallback:', e);
      return null;
    }
  }

  // --------------------------------------------------------------------------
  // 2. Initialization & Viewport Setup
  // --------------------------------------------------------------------------

  async function initStudio() {
    const captureData = await loadCaptureFromDB();

    if (captureData && captureData.dataUrl) {
      const img = new Image();
      img.onload = () => {
        baseImage = img;
        canvasWidth = img.naturalWidth || img.width;
        canvasHeight = img.naturalHeight || img.height;
        canvas.width = canvasWidth;
        canvas.height = canvasHeight;

        // Set initial filename
        const cleanTitle = (captureData.title || 'Caspian_Capture')
          .replace(/[^a-zA-Z0-9_-]/g, '_')
          .substring(0, 40);
        document.getElementById('studio-filename-input').value = `RippleFrame_${cleanTitle}`;
        document.getElementById('export-filename-field').value = `RippleFrame_${cleanTitle}`;

        renderCanvas();
        saveHistoryState();
        fitToScreen();
      };
      img.src = captureData.dataUrl;
    } else {
      // Fallback empty canvas if opened directly
      canvasWidth = 1280;
      canvasHeight = 720;
      canvas.width = canvasWidth;
      canvas.height = canvasHeight;
      ctx.fillStyle = '#1e293b';
      ctx.fillRect(0, 0, canvasWidth, canvasHeight);
      ctx.fillStyle = '#94a3b8';
      ctx.font = '24px Outfit, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('No capture loaded. Trigger RippleFrame from popup to edit screenshots.', canvasWidth / 2, canvasHeight / 2);
      fitToScreen();
    }

    setupEventListeners();
    updateToolCursor();
  }

  // --------------------------------------------------------------------------
  // 3. Render Canvas & History
  // --------------------------------------------------------------------------

  function renderCanvas() {
    if (!baseImage) return;
    ctx.clearRect(0, 0, canvasWidth, canvasHeight);
    ctx.drawImage(baseImage, 0, 0, canvasWidth, canvasHeight);
    updateDimensionsBadge();
  }

  function updateDimensionsBadge() {
    const badge = document.getElementById('image-dimensions-badge');
    if (badge) badge.textContent = `${canvasWidth} × ${canvasHeight} px`;
    const resDisplay = document.getElementById('export-res-display');
    if (resDisplay) resDisplay.textContent = `${canvasWidth} × ${canvasHeight} px`;
  }

  function saveHistoryState() {
    try {
      const stateData = ctx.getImageData(0, 0, canvasWidth, canvasHeight);
      historyStack.push({
        imageData: stateData,
        width: canvasWidth,
        height: canvasHeight
      });
      if (historyStack.length > MAX_HISTORY) historyStack.shift();
      redoStack = []; // Clear redo stack on new action
      updateHistoryButtons();
    } catch (e) {
      console.warn('History save warning:', e);
    }
  }

  function undo() {
    if (historyStack.length <= 1) return;
    const currentState = historyStack.pop();
    redoStack.push(currentState);

    const previousState = historyStack[historyStack.length - 1];
    restoreState(previousState);
    updateHistoryButtons();
  }

  function redo() {
    if (redoStack.length === 0) return;
    const nextState = redoStack.pop();
    historyStack.push(nextState);
    restoreState(nextState);
    updateHistoryButtons();
  }

  function restoreState(state) {
    if (!state) return;
    canvasWidth = state.width;
    canvasHeight = state.height;
    canvas.width = canvasWidth;
    canvas.height = canvasHeight;
    ctx.putImageData(state.imageData, 0, 0);

    // Update baseImage reference for subsequent crops
    const tempCanvas = document.createElement('canvas');
    tempCanvas.width = canvasWidth;
    tempCanvas.height = canvasHeight;
    tempCanvas.getContext('2d').putImageData(state.imageData, 0, 0);

    const updatedImg = new Image();
    updatedImg.src = tempCanvas.toDataURL();
    baseImage = updatedImg;

    updateDimensionsBadge();
  }

  function updateHistoryButtons() {
    const undoBtn = document.getElementById('btn-undo');
    const redoBtn = document.getElementById('btn-redo');
    if (undoBtn) undoBtn.disabled = historyStack.length <= 1;
    if (redoBtn) redoBtn.disabled = redoStack.length === 0;
  }

  // --------------------------------------------------------------------------
  // 4. Zoom & Pan Transform Controls
  // --------------------------------------------------------------------------

  function updateTransform() {
    transformLayer.style.transform = `translate(${panX}px, ${panY}px) scale(${scale})`;
    const zoomText = document.getElementById('zoom-percentage-display');
    if (zoomText) zoomText.textContent = `${Math.round(scale * 100)}%`;
  }

  function zoomAtPoint(factor, clientX, clientY) {
    const rect = viewport.getBoundingClientRect();
    const mouseX = clientX - rect.left;
    const mouseY = clientY - rect.top;

    const prevScale = scale;
    scale = Math.max(0.05, Math.min(5.0, scale * factor));

    // Anchor zoom around mouse position
    panX = mouseX - (mouseX - panX) * (scale / prevScale);
    panY = mouseY - (mouseY - panY) * (scale / prevScale);

    updateTransform();
  }

  function fitToScreen() {
    const rect = viewport.getBoundingClientRect();
    const padding = 40;
    const availWidth = rect.width - padding;
    const availHeight = rect.height - padding;

    if (canvasWidth > 0 && canvasHeight > 0) {
      const scaleX = availWidth / canvasWidth;
      const scaleY = availHeight / canvasHeight;
      scale = Math.min(scaleX, scaleY, 1.0); // Don't zoom above 100% on initial fit
      scale = Math.max(0.05, scale);

      panX = (rect.width - canvasWidth * scale) / 2;
      panY = (rect.height - canvasHeight * scale) / 2;
    }
    updateTransform();
  }

  function zoom100() {
    const rect = viewport.getBoundingClientRect();
    scale = 1.0;
    panX = (rect.width - canvasWidth) / 2;
    panY = (rect.height - canvasHeight) / 2;
    updateTransform();
  }

  // Convert client viewport coordinates to canvas pixel space
  function getCanvasCoords(clientX, clientY) {
    const rect = viewport.getBoundingClientRect();
    const vx = clientX - rect.left;
    const vy = clientY - rect.top;

    const cx = (vx - panX) / scale;
    const cy = (vy - panY) / scale;
    return {
      x: Math.max(0, Math.min(canvasWidth, cx)),
      y: Math.max(0, Math.min(canvasHeight, cy))
    };
  }

  // --------------------------------------------------------------------------
  // 5. Drawing, Redaction & Annotation Tools
  // --------------------------------------------------------------------------

  // Apply Pixelation / Heavy Box Blur to Rectangle
  function applyBlurToRegion(x, y, w, h, blockSize = 10) {
    if (w <= 0 || h <= 0) return;
    const rx = Math.max(0, Math.floor(x));
    const ry = Math.max(0, Math.floor(y));
    const rw = Math.min(canvasWidth - rx, Math.ceil(w));
    const rh = Math.min(canvasHeight - ry, Math.ceil(h));

    if (rw <= 0 || rh <= 0) return;

    const imgData = ctx.getImageData(rx, ry, rw, rh);
    const data = imgData.data;

    for (let row = 0; row < rh; row += blockSize) {
      for (let col = 0; col < rw; col += blockSize) {
        let r = 0, g = 0, b = 0, count = 0;

        // Sample block
        for (let subY = 0; subY < blockSize && (row + subY) < rh; subY++) {
          for (let subX = 0; subX < blockSize && (col + subX) < rw; subX++) {
            const idx = ((row + subY) * rw + (col + subX)) * 4;
            r += data[idx];
            g += data[idx + 1];
            b += data[idx + 2];
            count++;
          }
        }

        r = Math.floor(r / count);
        g = Math.floor(g / count);
        b = Math.floor(b / count);

        // Fill block with average color
        for (let subY = 0; subY < blockSize && (row + subY) < rh; subY++) {
          for (let subX = 0; subX < blockSize && (col + subX) < rw; subX++) {
            const idx = ((row + subY) * rw + (col + subX)) * 4;
            data[idx] = r;
            data[idx + 1] = g;
            data[idx + 2] = b;
          }
        }
      }
    }

    ctx.putImageData(imgData, rx, ry);
  }

  // Draw Arrow Vector with Head
  function drawArrow(fromX, fromY, toX, toY, color, width) {
    const headLen = Math.max(12, width * 3.5);
    const angle = Math.atan2(toY - fromY, toX - fromX);

    ctx.strokeStyle = color;
    ctx.fillStyle = color;
    ctx.lineWidth = width;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    ctx.beginPath();
    ctx.moveTo(fromX, fromY);
    ctx.lineTo(toX, toY);
    ctx.stroke();

    // Arrow head
    ctx.beginPath();
    ctx.moveTo(toX, toY);
    ctx.lineTo(toX - headLen * Math.cos(angle - Math.PI / 6), toY - headLen * Math.sin(angle - Math.PI / 6));
    ctx.lineTo(toX - headLen * Math.cos(angle + Math.PI / 6), toY - headLen * Math.sin(angle + Math.PI / 6));
    ctx.closePath();
    ctx.fill();
  }

  // --------------------------------------------------------------------------
  // 6. Interactive Crop Engine
  // --------------------------------------------------------------------------

  function startCropMode() {
    cropState.active = true;
    cropState.x = Math.round(canvasWidth * 0.1);
    cropState.y = Math.round(canvasHeight * 0.1);
    cropState.w = Math.round(canvasWidth * 0.8);
    cropState.h = Math.round(canvasHeight * 0.8);
    document.getElementById('crop-bar').style.display = 'flex';
    renderCropOverlay();
  }

  function cancelCropMode() {
    cropState.active = false;
    document.getElementById('crop-bar').style.display = 'none';
    renderCanvas();
    // Restore latest history image
    if (historyStack.length > 0) {
      restoreState(historyStack[historyStack.length - 1]);
    }
  }

  function applyCrop() {
    if (!cropState.active || cropState.w <= 10 || cropState.h <= 10) return;

    const croppedCanvas = document.createElement('canvas');
    croppedCanvas.width = cropState.w;
    croppedCanvas.height = cropState.h;
    const cctx = croppedCanvas.getContext('2d');

    // Slice image data
    cctx.drawImage(
      canvas,
      cropState.x, cropState.y, cropState.w, cropState.h,
      0, 0, cropState.w, cropState.h
    );

    canvasWidth = cropState.w;
    canvasHeight = cropState.h;
    canvas.width = canvasWidth;
    canvas.height = canvasHeight;

    ctx.drawImage(croppedCanvas, 0, 0);

    const newImg = new Image();
    newImg.src = croppedCanvas.toDataURL();
    baseImage = newImg;

    cropState.active = false;
    document.getElementById('crop-bar').style.display = 'none';

    saveHistoryState();
    fitToScreen();
    setTool('pan');
  }

  function renderCropOverlay() {
    if (!cropState.active) return;
    // Draw underlying image first
    if (historyStack.length > 0) {
      ctx.putImageData(historyStack[historyStack.length - 1].imageData, 0, 0);
    }

    // Draw darkened scrim outside crop box
    ctx.fillStyle = 'rgba(0, 0, 0, 0.65)';
    // Top
    ctx.fillRect(0, 0, canvasWidth, cropState.y);
    // Bottom
    ctx.fillRect(0, cropState.y + cropState.h, canvasWidth, canvasHeight - (cropState.y + cropState.h));
    // Left
    ctx.fillRect(0, cropState.y, cropState.x, cropState.h);
    // Right
    ctx.fillRect(cropState.x + cropState.w, cropState.y, canvasWidth - (cropState.x + cropState.w), cropState.h);

    // Draw neon dashed boundary
    ctx.strokeStyle = '#38bdf8';
    ctx.lineWidth = 2 / scale;
    ctx.setLineDash([6 / scale, 4 / scale]);
    ctx.strokeRect(cropState.x, cropState.y, cropState.w, cropState.h);
    ctx.setLineDash([]);

    // Draw 4 corner handles
    const handleSize = 10 / scale;
    ctx.fillStyle = '#ffffff';
    ctx.strokeStyle = '#0284c7';
    ctx.lineWidth = 1.5 / scale;

    const corners = [
      { x: cropState.x, y: cropState.y },
      { x: cropState.x + cropState.w, y: cropState.y },
      { x: cropState.x, y: cropState.y + cropState.h },
      { x: cropState.x + cropState.w, y: cropState.y + cropState.h }
    ];

    corners.forEach(c => {
      ctx.fillRect(c.x - handleSize / 2, c.y - handleSize / 2, handleSize, handleSize);
      ctx.strokeRect(c.x - handleSize / 2, c.y - handleSize / 2, handleSize, handleSize);
    });
  }

  // --------------------------------------------------------------------------
  // 7. Mouse & Tool Interaction Handlers
  // --------------------------------------------------------------------------

  function updateToolCursor() {
    viewport.classList.remove('pan-mode', 'panning', 'draw-mode');
    if (currentTool === 'pan') {
      viewport.classList.add('pan-mode');
    } else {
      viewport.classList.add('draw-mode');
    }
  }

  function setTool(toolName) {
    if (cropState.active && toolName !== 'crop') {
      cancelCropMode();
    }

    currentTool = toolName;
    document.querySelectorAll('.tool-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.tool === toolName);
    });

    updateToolCursor();

    if (toolName === 'crop') {
      startCropMode();
    }
  }

  function setupEventListeners() {
    // Tool buttons
    document.querySelectorAll('.tool-btn').forEach(btn => {
      btn.addEventListener('click', () => setTool(btn.dataset.tool));
    });

    // Swatches
    document.querySelectorAll('.color-swatch').forEach(swatch => {
      swatch.addEventListener('click', () => {
        document.querySelectorAll('.color-swatch').forEach(s => s.classList.remove('active'));
        swatch.classList.add('active');
        currentColor = swatch.dataset.color;
      });
    });

    const customColor = document.getElementById('custom-color-picker');
    if (customColor) {
      customColor.addEventListener('input', (e) => {
        currentColor = e.target.value;
        document.querySelectorAll('.color-swatch').forEach(s => s.classList.remove('active'));
      });
    }

    // Stroke size slider
    const strokeSlider = document.getElementById('stroke-size-slider');
    const strokeLabel = document.getElementById('stroke-size-label');
    if (strokeSlider) {
      strokeSlider.addEventListener('input', (e) => {
        currentStrokeSize = parseInt(e.target.value);
        if (strokeLabel) strokeLabel.textContent = `${currentStrokeSize}px`;
      });
    }

    // Zoom buttons
    document.getElementById('btn-zoom-in')?.addEventListener('click', () => zoomAtPoint(1.2, viewport.clientWidth / 2, viewport.clientHeight / 2));
    document.getElementById('btn-zoom-out')?.addEventListener('click', () => zoomAtPoint(0.8, viewport.clientWidth / 2, viewport.clientHeight / 2));
    document.getElementById('btn-zoom-fit')?.addEventListener('click', fitToScreen);
    document.getElementById('btn-zoom-100')?.addEventListener('click', zoom100);

    // History buttons
    document.getElementById('btn-undo')?.addEventListener('click', undo);
    document.getElementById('btn-redo')?.addEventListener('click', redo);
    document.getElementById('btn-reset-canvas')?.addEventListener('click', () => {
      if (confirm('Reset canvas to original capture? All annotations will be removed.')) {
        if (historyStack.length > 0) {
          restoreState(historyStack[0]);
          historyStack = [historyStack[0]];
          redoStack = [];
          updateHistoryButtons();
        }
      }
    });

    // Crop buttons
    document.getElementById('btn-cancel-crop')?.addEventListener('click', cancelCropMode);
    document.getElementById('btn-apply-crop')?.addEventListener('click', applyCrop);

    // Viewport mouse wheel zoom
    viewport.addEventListener('wheel', (e) => {
      e.preventDefault();
      const factor = e.deltaY < 0 ? 1.15 : 0.85;
      zoomAtPoint(factor, e.clientX, e.clientY);
    }, { passive: false });

    // Viewport Mouse Events for Panning & Drawing
    viewport.addEventListener('mousedown', onMouseDown);
    window.addEventListener('mousemove', onMouseMove);
    window.addEventListener('mouseup', onMouseUp);

    // Keyboard Shortcuts
    window.addEventListener('keydown', onKeyDown);

    // Export Modal listeners
    setupExportModal();
  }

  function onMouseDown(e) {
    // Middle click or Space+click initiates pan
    if (e.button === 1 || e.spaceKey || currentTool === 'pan') {
      isPanning = true;
      startPanX = e.clientX - panX;
      startPanY = e.clientY - panY;
      viewport.classList.add('panning');
      return;
    }

    if (e.button !== 0) return; // Only left click for drawing

    const coords = getCanvasCoords(e.clientX, e.clientY);
    drawStartX = coords.x;
    drawStartY = coords.y;
    isDrawing = true;

    // Crop Mode dragging
    if (cropState.active) {
      cropState.x = drawStartX;
      cropState.y = drawStartY;
      cropState.w = 0;
      cropState.h = 0;
      return;
    }

    // Freehand pen / highlighter path start
    if (currentTool === 'pen' || currentTool === 'highlighter') {
      currentPath = [{ x: drawStartX, y: drawStartY }];
    }

    // Text tool click prompt
    if (currentTool === 'text') {
      isDrawing = false;
      const text = prompt('Enter annotation text:');
      if (text) {
        ctx.font = `bold ${currentStrokeSize * 4 + 12}px Outfit, sans-serif`;
        ctx.fillStyle = currentColor;
        ctx.fillText(text, drawStartX, drawStartY);
        saveHistoryState();
      }
    }
  }

  function onMouseMove(e) {
    if (isPanning) {
      panX = e.clientX - startPanX;
      panY = e.clientY - startPanY;
      updateTransform();
      return;
    }

    if (!isDrawing) return;

    const coords = getCanvasCoords(e.clientX, e.clientY);
    const currX = coords.x;
    const currY = coords.y;

    if (cropState.active) {
      cropState.w = currX - cropState.x;
      cropState.h = currY - cropState.y;
      renderCropOverlay();
      return;
    }

    // Real-time canvas preview for tools
    if (currentTool === 'pen') {
      currentPath.push({ x: currX, y: currY });
      ctx.strokeStyle = currentColor;
      ctx.lineWidth = currentStrokeSize;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';

      ctx.beginPath();
      const p1 = currentPath[currentPath.length - 2];
      const p2 = currentPath[currentPath.length - 1];
      if (p1 && p2) {
        ctx.moveTo(p1.x, p1.y);
        ctx.lineTo(p2.x, p2.y);
        ctx.stroke();
      }
      return;
    }

    if (currentTool === 'highlighter') {
      currentPath.push({ x: currX, y: currY });
      ctx.strokeStyle = currentColor + '55'; // ~35% alpha
      ctx.lineWidth = currentStrokeSize * 3;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';

      ctx.beginPath();
      const p1 = currentPath[currentPath.length - 2];
      const p2 = currentPath[currentPath.length - 1];
      if (p1 && p2) {
        ctx.moveTo(p1.x, p1.y);
        ctx.lineTo(p2.x, p2.y);
        ctx.stroke();
      }
      return;
    }

    // For Shapes & Boxes, restore last history snapshot for smooth non-destructive preview
    if (historyStack.length > 0) {
      ctx.putImageData(historyStack[historyStack.length - 1].imageData, 0, 0);
    }

    const w = currX - drawStartX;
    const h = currY - drawStartY;

    if (currentTool === 'rect') {
      ctx.strokeStyle = currentColor;
      ctx.lineWidth = currentStrokeSize;
      ctx.strokeRect(drawStartX, drawStartY, w, h);
    } else if (currentTool === 'circle') {
      ctx.strokeStyle = currentColor;
      ctx.lineWidth = currentStrokeSize;
      ctx.beginPath();
      const rx = Math.abs(w / 2);
      const ry = Math.abs(h / 2);
      const cx = drawStartX + w / 2;
      const cy = drawStartY + h / 2;
      ctx.ellipse(cx, cy, rx, ry, 0, 0, 2 * Math.PI);
      ctx.stroke();
    } else if (currentTool === 'arrow') {
      drawArrow(drawStartX, drawStartY, currX, currY, currentColor, currentStrokeSize);
    } else if (currentTool === 'blackout') {
      ctx.fillStyle = currentColor;
      ctx.fillRect(drawStartX, drawStartY, w, h);
    } else if (currentTool === 'blur') {
      // Draw live outline for blur box
      ctx.strokeStyle = '#38bdf8';
      ctx.lineWidth = 1.5;
      ctx.setLineDash([4, 4]);
      ctx.strokeRect(drawStartX, drawStartY, w, h);
      ctx.setLineDash([]);
    }
  }

  function onMouseUp(e) {
    if (isPanning) {
      isPanning = false;
      viewport.classList.remove('panning');
      return;
    }

    if (!isDrawing) return;
    isDrawing = false;

    const coords = getCanvasCoords(e.clientX, e.clientY);
    const currX = coords.x;
    const currY = coords.y;

    if (cropState.active) {
      // Normalize negative crop dimensions
      if (cropState.w < 0) {
        cropState.x += cropState.w;
        cropState.w = Math.abs(cropState.w);
      }
      if (cropState.h < 0) {
        cropState.y += cropState.h;
        cropState.h = Math.abs(cropState.h);
      }
      renderCropOverlay();
      return;
    }

    const w = currX - drawStartX;
    const h = currY - drawStartY;

    if (currentTool === 'blur') {
      // Restore clean snapshot before applying real blur
      if (historyStack.length > 0) {
        ctx.putImageData(historyStack[historyStack.length - 1].imageData, 0, 0);
      }
      const bx = Math.min(drawStartX, currX);
      const by = Math.min(drawStartY, currY);
      const bw = Math.abs(w);
      const bh = Math.abs(h);
      applyBlurToRegion(bx, by, bw, bh, Math.max(8, currentStrokeSize * 2));
    }

    saveHistoryState();
  }

  function onKeyDown(e) {
    if (e.target.tagName === 'INPUT') return;

    if (e.ctrlKey && e.key.toLowerCase() === 'z') {
      e.preventDefault();
      undo();
      return;
    }
    if (e.ctrlKey && e.key.toLowerCase() === 'y') {
      e.preventDefault();
      redo();
      return;
    }

    const key = e.key.toLowerCase();
    if (key === 'v') setTool('pan');
    else if (key === 'c') setTool('crop');
    else if (key === 'b') setTool('blur');
    else if (key === 'x') setTool('blackout');
    else if (key === 'p') setTool('pen');
    else if (key === 'h') setTool('highlighter');
    else if (key === 'a') setTool('arrow');
    else if (key === 'r') setTool('rect');
    else if (key === 'o') setTool('circle');
    else if (key === 't') setTool('text');
  }

  // --------------------------------------------------------------------------
  // 8. Export Suite (PNG, JPG, PDF)
  // --------------------------------------------------------------------------

  function setupExportModal() {
    const modal = document.getElementById('export-modal');
    const openBtn = document.getElementById('btn-open-export-modal');
    const closeBtn = document.getElementById('btn-close-export-modal');
    const cancelBtn = document.getElementById('btn-cancel-export');
    const downloadBtn = document.getElementById('btn-confirm-download');

    let selectedFormat = 'png';
    let jpegQuality = 0.92;

    const openModal = () => {
      document.getElementById('export-filename-field').value = document.getElementById('studio-filename-input').value;
      modal.style.display = 'flex';
    };

    const closeModal = () => {
      modal.style.display = 'none';
    };

    openBtn?.addEventListener('click', openModal);
    closeBtn?.addEventListener('click', closeModal);
    cancelBtn?.addEventListener('click', closeModal);

    // Format selection pills
    document.querySelectorAll('.format-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('.format-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        selectedFormat = pill.dataset.format;

        const qualityGroup = document.getElementById('jpg-quality-group');
        if (qualityGroup) {
          qualityGroup.style.display = selectedFormat === 'jpg' ? 'flex' : 'none';
        }
      });
    });

    // JPEG quality slider
    const qualitySlider = document.getElementById('jpg-quality-slider');
    const qualityDisplay = document.getElementById('quality-val-display');
    if (qualitySlider) {
      qualitySlider.addEventListener('input', (e) => {
        const val = parseInt(e.target.value);
        jpegQuality = val / 100;
        if (qualityDisplay) qualityDisplay.textContent = `${val}%`;
      });
    }

    // Trigger Download
    downloadBtn?.addEventListener('click', () => {
      const rawName = document.getElementById('export-filename-field').value.trim() || 'Caspian_Capture';
      const filename = rawName.replace(/[^a-zA-Z0-9_-]/g, '_');

      if (selectedFormat === 'png') {
        canvas.toBlob((blob) => {
          triggerFileDownload(blob, `${filename}.png`);
          closeModal();
        }, 'image/png');
      } else if (selectedFormat === 'jpg') {
        // Create canvas with white background for JPEG
        const jpgCanvas = document.createElement('canvas');
        jpgCanvas.width = canvasWidth;
        jpgCanvas.height = canvasHeight;
        const jctx = jpgCanvas.getContext('2d');
        jctx.fillStyle = '#ffffff';
        jctx.fillRect(0, 0, canvasWidth, canvasHeight);
        jctx.drawImage(canvas, 0, 0);

        jpgCanvas.toBlob((blob) => {
          triggerFileDownload(blob, `${filename}.jpg`);
          closeModal();
        }, 'image/jpeg', jpegQuality);
      } else if (selectedFormat === 'pdf') {
        generatePDF(filename);
        closeModal();
      }
    });
  }

  function triggerFileDownload(blob, fullFilename) {
    const url = URL.createObjectURL(blob);
    if (chrome && chrome.downloads && chrome.downloads.download) {
      chrome.downloads.download({
        url: url,
        filename: fullFilename,
        saveAs: true // Prompt user for custom save location in laptop
      }, () => {
        setTimeout(() => URL.revokeObjectURL(url), 4000);
      });
    } else {
      const a = document.createElement('a');
      a.href = url;
      a.download = fullFilename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      setTimeout(() => URL.revokeObjectURL(url), 4000);
    }
  }

  // Pure JavaScript Client-Side High-Resolution PDF Generator
  function generatePDF(filename) {
    const imgData = canvas.toDataURL('image/jpeg', 0.95);
    
    // HTML-based print/PDF renderer
    const printWin = window.open('', '_blank');
    if (!printWin) {
      alert('Pop-up blocked. Please allow pop-ups to export PDF.');
      return;
    }

    printWin.document.write(`
      <!DOCTYPE html>
      <html>
      <head>
        <title>${filename}</title>
        <style>
          @page { margin: 0; size: auto; }
          body { margin: 0; padding: 0; background: #fff; display: flex; justify-content: center; }
          img { max-width: 100%; height: auto; display: block; }
        </style>
      </head>
      <body>
        <img src="${imgData}" onload="window.print(); setTimeout(() => window.close(), 1000);" />
      </body>
      </html>
    `);
    printWin.document.close();
  }

  // Boot Studio
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initStudio);
  } else {
    initStudio();
  }

})();
