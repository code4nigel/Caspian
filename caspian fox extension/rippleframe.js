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
    activeHandle: null,
    initialX: 0,
    initialY: 0,
    initialW: 0,
    initialH: 0
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

  function loadImage(src) {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => resolve(img);
      img.onerror = (e) => reject(e);
      img.src = src;
    });
  }

  async function getLatestStorageCapture() {
    return new Promise((resolve) => {
      try {
        if (chrome?.storage?.local) {
          chrome.storage.local.get('latest_rippleframe_capture', (data) => {
            resolve(data?.latest_rippleframe_capture || null);
          });
        } else {
          resolve(null);
        }
      } catch (e) {
        resolve(null);
      }
    });
  }

  async function initStudio() {
    let captureData = await loadCaptureFromDB();

    if (!captureData) {
      captureData = await getLatestStorageCapture();
    }

    if (captureData && captureData.slices && captureData.slices.length > 0) {
      try {
        const dpr = captureData.dpr || 1;
        const totalW = Math.round(captureData.viewportWidth * dpr);
        const totalH = Math.round(captureData.totalHeight * dpr);

        canvasWidth = totalW;
        canvasHeight = totalH;
        canvas.width = canvasWidth;
        canvas.height = canvasHeight;

        for (let i = 0; i < captureData.slices.length; i++) {
          const slice = captureData.slices[i];
          const img = await loadImage(slice.dataUrl);
          const destY = Math.round(slice.scrollY * dpr);
          ctx.drawImage(img, 0, destY);
        }

        const stitchedDataUrl = canvas.toDataURL('image/png');
        baseImage = await loadImage(stitchedDataUrl);

        const cleanTitle = (captureData.title || 'Caspian_Capture')
          .replace(/[^a-zA-Z0-9_-]/g, '_')
          .substring(0, 40);
        document.getElementById('studio-filename-input').value = `RippleFrame_${cleanTitle}`;
        document.getElementById('export-filename-field').value = `RippleFrame_${cleanTitle}`;

        renderCanvas();
        saveHistoryState();
        fitToScreen();
      } catch (err) {
        console.error('Failed to stitch slices:', err);
      }
    } else if (captureData && captureData.dataUrl) {
      try {
        const img = await loadImage(captureData.dataUrl);
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
      } catch (err) {
        console.error('Failed to load capture dataUrl:', err);
      }
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

  // Convert client viewport coordinates to canvas pixel space with 100% precision
  function getCanvasCoords(clientX, clientY) {
    const rect = canvas.getBoundingClientRect();
    if (!rect || rect.width === 0 || rect.height === 0) {
      return { x: 0, y: 0 };
    }

    const cx = (clientX - rect.left) * (canvas.width / rect.width);
    const cy = (clientY - rect.top) * (canvas.height / rect.height);
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
    cropState.activeHandle = null;
    document.getElementById('crop-bar').style.display = 'flex';
    renderCropOverlay();
  }

  function cancelCropMode() {
    cropState.active = false;
    cropState.activeHandle = null;
    viewport.style.cursor = '';
    document.getElementById('crop-bar').style.display = 'none';
    renderCanvas();
    // Restore latest history image
    if (historyStack.length > 0) {
      restoreState(historyStack[historyStack.length - 1]);
    }
  }

  function applyCrop() {
    if (!cropState.active || Math.abs(cropState.w) <= 10 || Math.abs(cropState.h) <= 10) return;

    const normX = Math.max(0, Math.min(cropState.x, cropState.x + cropState.w));
    const normY = Math.max(0, Math.min(cropState.y, cropState.y + cropState.h));
    const normW = Math.min(canvasWidth - normX, Math.abs(cropState.w));
    const normH = Math.min(canvasHeight - normY, Math.abs(cropState.h));

    if (normW <= 10 || normH <= 10) return;

    const croppedCanvas = document.createElement('canvas');
    croppedCanvas.width = normW;
    croppedCanvas.height = normH;
    const cctx = croppedCanvas.getContext('2d');

    // Slice image data from original un-scrimmed snapshot
    if (historyStack.length > 0) {
      ctx.putImageData(historyStack[historyStack.length - 1].imageData, 0, 0);
    }

    cctx.drawImage(
      canvas,
      normX, normY, normW, normH,
      0, 0, normW, normH
    );

    canvasWidth = normW;
    canvasHeight = normH;
    canvas.width = canvasWidth;
    canvas.height = canvasHeight;

    ctx.drawImage(croppedCanvas, 0, 0);

    const newImg = new Image();
    newImg.src = croppedCanvas.toDataURL();
    baseImage = newImg;

    cropState.active = false;
    cropState.activeHandle = null;
    viewport.style.cursor = '';
    document.getElementById('crop-bar').style.display = 'none';

    saveHistoryState();
    fitToScreen();
    setTool('pan');
  }

  function getCropHandleAtPoint(cx, cy) {
    if (!cropState.active) return null;

    const handleRadius = Math.max(14, 16 / scale);
    const edgeTolerance = Math.max(8, 12 / scale);

    const x = Math.min(cropState.x, cropState.x + cropState.w);
    const y = Math.min(cropState.y, cropState.y + cropState.h);
    const w = Math.abs(cropState.w);
    const h = Math.abs(cropState.h);

    // 1. Check 4 corners (highest priority)
    if (Math.hypot(cx - x, cy - y) <= handleRadius) return 'nw';
    if (Math.hypot(cx - (x + w), cy - y) <= handleRadius) return 'ne';
    if (Math.hypot(cx - x, cy - (y + h)) <= handleRadius) return 'sw';
    if (Math.hypot(cx - (x + w), cy - (y + h)) <= handleRadius) return 'se';

    // 2. Check 4 edge midpoints
    if (Math.hypot(cx - (x + w / 2), cy - y) <= handleRadius) return 'n';
    if (Math.hypot(cx - (x + w / 2), cy - (y + h)) <= handleRadius) return 's';
    if (Math.hypot(cx - x, cy - (y + h / 2)) <= handleRadius) return 'w';
    if (Math.hypot(cx - (x + w), cy - (y + h / 2)) <= handleRadius) return 'e';

    // 3. Check continuous borders
    if (cx >= x - edgeTolerance && cx <= x + w + edgeTolerance) {
      if (Math.abs(cy - y) <= edgeTolerance) return 'n';
      if (Math.abs(cy - (y + h)) <= edgeTolerance) return 's';
    }
    if (cy >= y - edgeTolerance && cy <= y + h + edgeTolerance) {
      if (Math.abs(cx - x) <= edgeTolerance) return 'w';
      if (Math.abs(cx - (x + w)) <= edgeTolerance) return 'e';
    }

    // 4. Inside crop box (allow moving entire selection)
    if (cx > x && cx < x + w && cy > y && cy < y + h) {
      return 'move';
    }

    return 'new';
  }

  function updateCropCursor(cx, cy) {
    if (!cropState.active) {
      viewport.style.cursor = '';
      return;
    }
    const handle = getCropHandleAtPoint(cx, cy);
    if (handle === 'nw' || handle === 'se') {
      viewport.style.cursor = 'nwse-resize';
    } else if (handle === 'ne' || handle === 'sw') {
      viewport.style.cursor = 'nesw-resize';
    } else if (handle === 'n' || handle === 's') {
      viewport.style.cursor = 'ns-resize';
    } else if (handle === 'w' || handle === 'e') {
      viewport.style.cursor = 'ew-resize';
    } else if (handle === 'move') {
      viewport.style.cursor = 'move';
    } else {
      viewport.style.cursor = 'crosshair';
    }
  }

  function renderCropOverlay() {
    if (!cropState.active) return;
    // Draw underlying image first
    if (historyStack.length > 0) {
      ctx.putImageData(historyStack[historyStack.length - 1].imageData, 0, 0);
    }

    const x = Math.min(cropState.x, cropState.x + cropState.w);
    const y = Math.min(cropState.y, cropState.y + cropState.h);
    const w = Math.abs(cropState.w);
    const h = Math.abs(cropState.h);

    // Draw darkened scrim outside crop box
    ctx.fillStyle = 'rgba(0, 0, 0, 0.65)';
    // Top
    ctx.fillRect(0, 0, canvasWidth, y);
    // Bottom
    ctx.fillRect(0, y + h, canvasWidth, canvasHeight - (y + h));
    // Left
    ctx.fillRect(0, y, x, h);
    // Right
    ctx.fillRect(x + w, y, canvasWidth - (x + w), h);

    if (w > 2 && h > 2) {
      // Draw Rule-of-Thirds Grid
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.22)';
      ctx.lineWidth = 1 / scale;
      ctx.setLineDash([3 / scale, 3 / scale]);
      
      ctx.beginPath();
      // Vertical grid lines
      ctx.moveTo(x + w / 3, y);
      ctx.lineTo(x + w / 3, y + h);
      ctx.moveTo(x + (2 * w) / 3, y);
      ctx.lineTo(x + (2 * w) / 3, y + h);
      // Horizontal grid lines
      ctx.moveTo(x, y + h / 3);
      ctx.lineTo(x + w, y + h / 3);
      ctx.moveTo(x, y + (2 * h) / 3);
      ctx.lineTo(x + w, y + (2 * h) / 3);
      ctx.stroke();

      // Neon crop outline
      ctx.strokeStyle = '#38bdf8';
      ctx.lineWidth = 2 / scale;
      ctx.setLineDash([]);
      ctx.strokeRect(x, y, w, h);

      // Draw 8 interactive handles
      const handleSize = Math.max(8, 10 / scale);
      ctx.fillStyle = '#ffffff';
      ctx.strokeStyle = '#0284c7';
      ctx.lineWidth = 1.5 / scale;

      const handles = [
        { x: x, y: y },                     // NW
        { x: x + w / 2, y: y },             // N
        { x: x + w, y: y },                 // NE
        { x: x + w, y: y + h / 2 },         // E
        { x: x + w, y: y + h },             // SE
        { x: x + w / 2, y: y + h },         // S
        { x: x, y: y + h },                 // SW
        { x: x, y: y + h / 2 }              // W
      ];

      handles.forEach(pt => {
        ctx.fillRect(pt.x - handleSize / 2, pt.y - handleSize / 2, handleSize, handleSize);
        ctx.strokeRect(pt.x - handleSize / 2, pt.y - handleSize / 2, handleSize, handleSize);
      });

      // Live dimensions pill
      const dimText = `${Math.round(w)} × ${Math.round(h)} px`;
      ctx.font = `600 ${Math.max(10, 12 / scale)}px 'JetBrains Mono', monospace`;
      const textMetrics = ctx.measureText(dimText);
      const badgeW = textMetrics.width + 12 / scale;
      const badgeH = 20 / scale;
      const badgeX = x + (w - badgeW) / 2;
      const badgeY = y > badgeH + 6 / scale ? y - badgeH - 4 / scale : y + h + 6 / scale;

      ctx.fillStyle = 'rgba(15, 23, 42, 0.88)';
      ctx.strokeStyle = 'rgba(56, 189, 248, 0.5)';
      ctx.lineWidth = 1 / scale;
      ctx.beginPath();
      ctx.roundRect(badgeX, badgeY, badgeW, badgeH, 4 / scale);
      ctx.fill();
      ctx.stroke();

      ctx.fillStyle = '#38bdf8';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(dimText, badgeX + badgeW / 2, badgeY + badgeH / 2);
    }
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

  function updateToolSizePanel() {
    const panel = document.getElementById('tool-size-panel');
    const label = document.getElementById('tool-size-type-label');
    const badge = document.getElementById('tool-size-val-badge');
    const slider = document.getElementById('stroke-size-slider');

    if (!panel || !label || !badge || !slider) return;

    if (currentTool === 'pan' || currentTool === 'crop') {
      panel.style.display = 'none';
      return;
    }

    panel.style.display = 'flex';

    if (currentTool === 'text') {
      label.textContent = 'Font Size';
      slider.min = '12';
      slider.max = '72';
      slider.step = '2';
      slider.value = currentFontSize;
      badge.textContent = `${currentFontSize}px`;
      
      const textPresets = [16, 20, 24, 32, 48];
      renderSizePresets(textPresets, currentFontSize, (val) => {
        currentFontSize = val;
        slider.value = val;
        badge.textContent = `${val}px`;
      });
    } else if (currentTool === 'blur') {
      label.textContent = 'Blur Radius';
      slider.min = '4';
      slider.max = '32';
      slider.step = '2';
      slider.value = currentStrokeSize;
      badge.textContent = `${currentStrokeSize}px`;

      const blurPresets = [4, 8, 12, 16, 24];
      renderSizePresets(blurPresets, currentStrokeSize, (val) => {
        currentStrokeSize = val;
        slider.value = val;
        badge.textContent = `${val}px`;
      });
    } else {
      label.textContent = 'Stroke Width';
      slider.min = '1';
      slider.max = '40';
      slider.step = '1';
      slider.value = currentStrokeSize;
      badge.textContent = `${currentStrokeSize}px`;

      const strokePresets = [2, 4, 8, 16, 24];
      renderSizePresets(strokePresets, currentStrokeSize, (val) => {
        currentStrokeSize = val;
        slider.value = val;
        badge.textContent = `${val}px`;
      });
    }
  }

  function renderSizePresets(presetArray, activeVal, onSelect) {
    const container = document.getElementById('tool-size-presets');
    if (!container) return;
    container.innerHTML = '';
    presetArray.forEach(val => {
      const btn = document.createElement('button');
      btn.className = `size-preset-pill ${val === activeVal ? 'active' : ''}`;
      btn.textContent = `${val}px`;
      btn.addEventListener('click', () => {
        container.querySelectorAll('.size-preset-pill').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');
        onSelect(val);
      });
      container.appendChild(btn);
    });
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
    updateToolSizePanel();

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

    // Dynamic Tool Size Slider
    const strokeSlider = document.getElementById('stroke-size-slider');
    const sizeBadge = document.getElementById('tool-size-val-badge');
    if (strokeSlider) {
      strokeSlider.addEventListener('input', (e) => {
        const val = parseInt(e.target.value);
        if (currentTool === 'text') {
          currentFontSize = val;
        } else {
          currentStrokeSize = val;
        }
        if (sizeBadge) sizeBadge.textContent = `${val}px`;
        document.querySelectorAll('.size-preset-pill').forEach(pill => {
          pill.classList.toggle('active', pill.textContent === `${val}px`);
        });
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
      cropState.activeHandle = getCropHandleAtPoint(drawStartX, drawStartY);
      cropState.initialX = Math.min(cropState.x, cropState.x + cropState.w);
      cropState.initialY = Math.min(cropState.y, cropState.y + cropState.h);
      cropState.initialW = Math.abs(cropState.w);
      cropState.initialH = Math.abs(cropState.h);

      if (cropState.activeHandle === 'new') {
        cropState.x = drawStartX;
        cropState.y = drawStartY;
        cropState.w = 0;
        cropState.h = 0;
        cropState.initialX = drawStartX;
        cropState.initialY = drawStartY;
        cropState.initialW = 0;
        cropState.initialH = 0;
        renderCropOverlay();
      }
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
        ctx.font = `bold ${currentFontSize}px Outfit, sans-serif`;
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

    const coords = getCanvasCoords(e.clientX, e.clientY);
    const currX = coords.x;
    const currY = coords.y;

    if (!isDrawing) {
      if (cropState.active) {
        updateCropCursor(currX, currY);
      }
      return;
    }

    if (cropState.active) {
      const dx = currX - drawStartX;
      const dy = currY - drawStartY;
      const initX = cropState.initialX;
      const initY = cropState.initialY;
      const initW = cropState.initialW;
      const initH = cropState.initialH;

      if (cropState.activeHandle === 'nw') {
        const nx = Math.min(initX + initW - 10, Math.max(0, initX + dx));
        const ny = Math.min(initY + initH - 10, Math.max(0, initY + dy));
        cropState.x = nx;
        cropState.y = ny;
        cropState.w = (initX + initW) - nx;
        cropState.h = (initY + initH) - ny;
      } else if (cropState.activeHandle === 'ne') {
        const ny = Math.min(initY + initH - 10, Math.max(0, initY + dy));
        cropState.y = ny;
        cropState.h = (initY + initH) - ny;
        cropState.w = Math.min(canvasWidth - initX, Math.max(10, initW + dx));
        cropState.x = initX;
      } else if (cropState.activeHandle === 'sw') {
        const nx = Math.min(initX + initW - 10, Math.max(0, initX + dx));
        cropState.x = nx;
        cropState.w = (initX + initW) - nx;
        cropState.h = Math.min(canvasHeight - initY, Math.max(10, initH + dy));
        cropState.y = initY;
      } else if (cropState.activeHandle === 'se') {
        cropState.x = initX;
        cropState.y = initY;
        cropState.w = Math.min(canvasWidth - initX, Math.max(10, initW + dx));
        cropState.h = Math.min(canvasHeight - initY, Math.max(10, initH + dy));
      } else if (cropState.activeHandle === 'n') {
        const ny = Math.min(initY + initH - 10, Math.max(0, initY + dy));
        cropState.y = ny;
        cropState.h = (initY + initH) - ny;
        cropState.x = initX;
        cropState.w = initW;
      } else if (cropState.activeHandle === 's') {
        cropState.x = initX;
        cropState.y = initY;
        cropState.w = initW;
        cropState.h = Math.min(canvasHeight - initY, Math.max(10, initH + dy));
      } else if (cropState.activeHandle === 'w') {
        const nx = Math.min(initX + initW - 10, Math.max(0, initX + dx));
        cropState.x = nx;
        cropState.w = (initX + initW) - nx;
        cropState.y = initY;
        cropState.h = initH;
      } else if (cropState.activeHandle === 'e') {
        cropState.x = initX;
        cropState.y = initY;
        cropState.h = initH;
        cropState.w = Math.min(canvasWidth - initX, Math.max(10, initW + dx));
      } else if (cropState.activeHandle === 'move') {
        const maxX = Math.max(0, canvasWidth - initW);
        const maxY = Math.max(0, canvasHeight - initH);
        cropState.x = Math.max(0, Math.min(maxX, initX + dx));
        cropState.y = Math.max(0, Math.min(maxY, initY + dy));
        cropState.w = initW;
        cropState.h = initH;
      } else if (cropState.activeHandle === 'new') {
        cropState.x = Math.min(drawStartX, currX);
        cropState.y = Math.min(drawStartY, currY);
        cropState.w = Math.abs(currX - drawStartX);
        cropState.h = Math.abs(currY - drawStartY);
      }

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
      cropState.activeHandle = null;
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
  // 8. Export Suite (PNG, JPG, PDF) & Canvas Clear
  // --------------------------------------------------------------------------

  async function clearStoredCaptures() {
    try {
      const db = await openDatabase();
      const tx = db.transaction('captures', 'readwrite');
      tx.objectStore('captures').clear();
    } catch (e) {}
    try {
      if (chrome?.storage?.local) {
        chrome.storage.local.remove('latest_rippleframe_capture');
      }
    } catch (e) {}
    try {
      if (chrome?.runtime?.sendMessage) {
        chrome.runtime.sendMessage({ action: 'clear_rippleframe_storage' });
      }
    } catch (e) {}
  }

  function updateEstimatedOutputSize(format, quality) {
    const estDisplay = document.getElementById('export-est-display');
    if (!estDisplay) return;

    const totalPixels = canvasWidth * canvasHeight;
    let estBytes = 0;

    if (format === 'png') {
      estBytes = totalPixels * 0.85; // Approx compressed PNG bytes
    } else if (format === 'jpg') {
      estBytes = totalPixels * 0.22 * (quality || 0.92);
    } else if (format === 'pdf') {
      estBytes = totalPixels * 0.26 * (quality || 0.92) + 2048;
    }

    const mb = estBytes / (1024 * 1024);
    if (mb >= 1) {
      estDisplay.textContent = `~${mb.toFixed(1)} MB`;
    } else {
      estDisplay.textContent = `~${Math.round(estBytes / 1024)} KB`;
    }
  }

  function setupExportModal() {
    const modal = document.getElementById('export-modal');
    const openBtn = document.getElementById('btn-open-export-modal');
    const closeBtn = document.getElementById('btn-close-export-modal');
    const cancelBtn = document.getElementById('btn-cancel-export');
    const downloadBtn = document.getElementById('btn-confirm-download');
    const extBadge = document.getElementById('export-ext-badge');

    let selectedFormat = 'png';
    let jpegQuality = 0.92;

    const openModal = () => {
      document.getElementById('export-filename-field').value = document.getElementById('studio-filename-input').value;
      updateEstimatedOutputSize(selectedFormat, jpegQuality);
      modal.style.display = 'flex';
    };

    const closeModal = () => {
      modal.style.display = 'none';
    };

    // Quick 1-Click Export Buttons
    const quickPngBtn = document.getElementById('btn-quick-export-png');
    const quickPdfBtn = document.getElementById('btn-quick-export-pdf');

    if (quickPngBtn) {
      quickPngBtn.addEventListener('click', () => {
        const rawName = document.getElementById('studio-filename-input')?.value.trim() || 'Caspian_Capture';
        const filename = rawName.replace(/[^a-zA-Z0-9_-]/g, '_');
        canvas.toBlob((blob) => {
          if (blob) triggerFileDownload(blob, `${filename}.png`);
        }, 'image/png');
      });
    }

    if (quickPdfBtn) {
      quickPdfBtn.addEventListener('click', () => {
        const rawName = document.getElementById('studio-filename-input')?.value.trim() || 'Caspian_Capture';
        const filename = rawName.replace(/[^a-zA-Z0-9_-]/g, '_');
        generatePDF(filename);
      });
    }

    // Delete / Clear Canvas Button
    const deleteBtn = document.getElementById('btn-delete-capture');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', () => {
        if (confirm('Clear current screenshot from canvas and wipe temporary storage?')) {
          ctx.clearRect(0, 0, canvasWidth, canvasHeight);
          baseImage = null;
          canvasWidth = 1280;
          canvasHeight = 720;
          canvas.width = canvasWidth;
          canvas.height = canvasHeight;
          ctx.fillStyle = '#1e293b';
          ctx.fillRect(0, 0, canvasWidth, canvasHeight);
          historyStack = [];
          redoStack = [];
          updateHistoryButtons();
          updateDimensionsBadge();
          fitToScreen();
          clearStoredCaptures();
        }
      });
    }

    openBtn?.addEventListener('click', openModal);
    closeBtn?.addEventListener('click', closeModal);
    cancelBtn?.addEventListener('click', closeModal);

    // Format selection cards
    document.querySelectorAll('.format-card').forEach(card => {
      card.addEventListener('click', () => {
        document.querySelectorAll('.format-card').forEach(c => c.classList.remove('active'));
        card.classList.add('active');
        selectedFormat = card.dataset.format;

        if (extBadge) {
          extBadge.textContent = `.${selectedFormat}`;
        }

        const qualityGroup = document.getElementById('jpg-quality-group');
        if (qualityGroup) {
          qualityGroup.style.display = selectedFormat === 'jpg' ? 'flex' : 'none';
        }

        updateEstimatedOutputSize(selectedFormat, jpegQuality);
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
        document.querySelectorAll('.quality-preset-btn').forEach(p => {
          p.classList.toggle('active', parseInt(p.dataset.quality) === val);
        });
        updateEstimatedOutputSize(selectedFormat, jpegQuality);
      });
    }

    // JPEG quality preset pills
    document.querySelectorAll('.quality-preset-btn').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('.quality-preset-btn').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        const qVal = parseInt(pill.dataset.quality);
        jpegQuality = qVal / 100;
        if (qualitySlider) qualitySlider.value = qVal;
        if (qualityDisplay) qualityDisplay.textContent = `${qVal}%`;
        updateEstimatedOutputSize(selectedFormat, jpegQuality);
      });
    });

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

    // Auto-wipe stored captures when tab is closed to free up system memory
    window.addEventListener('beforeunload', () => {
      clearStoredCaptures();
    });
    window.addEventListener('pagehide', () => {
      clearStoredCaptures();
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

  // Pure JavaScript Client-Side High-Resolution Binary PDF 1.4 Generator
  function createPdfBlobFromImage(jpegDataUrl, imgWidth, imgHeight) {
    const base64Data = jpegDataUrl.split(',')[1];
    const binaryString = atob(base64Data);
    const jpegBytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      jpegBytes[i] = binaryString.charCodeAt(i);
    }

    // Standard 72 DPI PDF point conversion
    const ptWidth = Math.round(imgWidth * 0.75);
    const ptHeight = Math.round(imgHeight * 0.75);

    const encoder = new TextEncoder();
    const chunks = [];
    let byteOffset = 0;
    const xrefOffsets = [];

    function addChunk(str) {
      const encoded = encoder.encode(str);
      chunks.push(encoded);
      byteOffset += encoded.length;
    }

    function addBytes(bytes) {
      chunks.push(bytes);
      byteOffset += bytes.length;
    }

    // Header
    addChunk("%PDF-1.4\n%\xE2\xE3\xCF\xD3\n");

    // Obj 1: Catalog
    xrefOffsets[1] = byteOffset;
    addChunk("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

    // Obj 2: Pages
    xrefOffsets[2] = byteOffset;
    addChunk("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

    // Obj 3: Page (MediaBox matches capture dimensions)
    xrefOffsets[3] = byteOffset;
    addChunk(`3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${ptWidth} ${ptHeight}] /Contents 4 0 R /Resources << /XObject << /Im0 5 0 R >> >> >>\nendobj\n`);

    // Obj 4: Content Stream
    const contentStream = `q\n${ptWidth} 0 0 ${ptHeight} 0 0 cm\n/Im0 Do\nQ\n`;
    xrefOffsets[4] = byteOffset;
    addChunk(`4 0 obj\n<< /Length ${contentStream.length} >>\nstream\n${contentStream}endstream\nendobj\n`);

    // Obj 5: Image XObject with direct DCTDecode stream
    xrefOffsets[5] = byteOffset;
    addChunk(`5 0 obj\n<< /Type /XObject /Subtype /Image /Width ${imgWidth} /Height ${imgHeight} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpegBytes.length} >>\nstream\n`);
    addBytes(jpegBytes);
    addChunk("\nendstream\nendobj\n");

    // Cross-reference table
    const xrefOffset = byteOffset;
    addChunk("xref\n0 6\n0000000000 65535 f \n");
    for (let i = 1; i <= 5; i++) {
      const offStr = String(xrefOffsets[i]).padStart(10, '0');
      addChunk(`${offStr} 00000 n \n`);
    }

    // Trailer
    addChunk(`trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n${xrefOffset}\n%%EOF\n`);

    return new Blob(chunks, { type: 'application/pdf' });
  }

  function generatePDF(filename) {
    try {
      // Paint onto white backdrop canvas for crystal clear document rendering
      const pdfCanvas = document.createElement('canvas');
      pdfCanvas.width = canvasWidth;
      pdfCanvas.height = canvasHeight;
      const pctx = pdfCanvas.getContext('2d');
      pctx.fillStyle = '#ffffff';
      pctx.fillRect(0, 0, canvasWidth, canvasHeight);
      pctx.drawImage(canvas, 0, 0);

      const jpegDataUrl = pdfCanvas.toDataURL('image/jpeg', 0.94);
      const pdfBlob = createPdfBlobFromImage(jpegDataUrl, canvasWidth, canvasHeight);
      triggerFileDownload(pdfBlob, `${filename}.pdf`);
    } catch (err) {
      console.error('[PDF Export Error]', err);
      alert('❌ Failed to generate PDF document: ' + err.message);
    }
  }

  // Boot Studio
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initStudio);
  } else {
    initStudio();
  }

})();
