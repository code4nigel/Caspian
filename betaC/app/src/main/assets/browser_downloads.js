// ==========================================================================
// CASPIAN DOWNLOAD MANAGER JAVASCRIPT CONTROLLER
// ==========================================================================

(function () {
  let currentDownloadsTab = 'active'; // 'active' | 'completed'
  let cachedDownloads = [];
  let expandedDownloadIds = new Set();

  function formatDownloadBytes(bytes) {
    if (!bytes || bytes <= 0) return '0 B';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }

  function getFileCategoryInfo(fileName, mimeType) {
    const ext = fileName ? fileName.split('.').pop().toLowerCase() : '';
    if (ext === 'apk' || (mimeType && mimeType.includes('vnd.android.package-archive'))) {
      return { icon: '📦', class: 'apk', label: 'APK', title: 'Android App Package (APK)' };
    }
    if (ext === 'pdf' || (mimeType && mimeType.includes('pdf'))) {
      return { icon: '📄', class: 'pdf', label: 'PDF', title: 'PDF Document' };
    }
    if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext) || (mimeType && mimeType.startsWith('image/'))) {
      return { icon: '🖼️', class: 'media', label: 'IMG', title: 'Image Graphic' };
    }
    if (['mp4', 'mkv', 'webm', 'mov', 'avi'].includes(ext) || (mimeType && mimeType.startsWith('video/'))) {
      return { icon: '🎬', class: 'media', label: 'VID', title: 'Video Recording' };
    }
    if (['mp3', 'wav', 'ogg', 'm4a', 'flac'].includes(ext) || (mimeType && mimeType.startsWith('audio/'))) {
      return { icon: '🎵', class: 'media', label: 'AUD', title: 'Audio Music Track' };
    }
    if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext) || (mimeType && (mimeType.includes('zip') || mimeType.includes('compressed')))) {
      return { icon: '🗜️', class: 'archive', label: 'ZIP', title: 'Compressed Zip Archive' };
    }
    if (['html', 'js', 'json', 'css', 'py', 'java', 'xml', 'txt', 'md'].includes(ext)) {
      return { icon: '💻', class: 'code', label: ext.toUpperCase(), title: ext.toUpperCase() + ' Source File' };
    }
    return { icon: '📁', class: 'doc', label: ext ? ext.toUpperCase() : 'FILE', title: (ext ? ext.toUpperCase() + ' ' : '') + 'File' };
  }

  window.refreshBrowserDownloads = refreshDownloadsList;

  function switchDownloadsTab(tab) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    currentDownloadsTab = tab;
    const activeBtn = document.getElementById('downloads-tab-active-btn');
    const completedBtn = document.getElementById('downloads-tab-completed-btn');
    const activeList = document.getElementById('downloads-active-list');
    const completedList = document.getElementById('downloads-completed-list');

    if (tab === 'active') {
      if (activeBtn) activeBtn.className = 'oneui-pill-btn primary';
      if (completedBtn) completedBtn.className = 'oneui-pill-btn secondary';
      if (activeList) activeList.style.display = 'flex';
      if (completedList) completedList.style.display = 'none';
    } else {
      if (activeBtn) activeBtn.className = 'oneui-pill-btn secondary';
      if (completedBtn) completedBtn.className = 'oneui-pill-btn primary';
      if (activeList) activeList.style.display = 'none';
      if (completedList) completedList.style.display = 'flex';
    }
    renderDownloadsUI();
  }

  function refreshDownloadsList() {
    if (window.CaspianBridge && typeof window.CaspianBridge.getDownloadsJson === 'function') {
      try {
        const jsonStr = window.CaspianBridge.getDownloadsJson();
        cachedDownloads = JSON.parse(jsonStr || '[]');
      } catch (e) {
        console.error('Failed to parse downloads JSON', e);
      }
    }
    renderDownloadsUI();
  }

  function updateDownloadsBadge(activeCount) {
    const badge = document.getElementById('downloads-badge');
    if (!badge) return;
    if (activeCount > 0) {
      badge.textContent = activeCount;
      badge.style.display = 'flex';
    } else {
      badge.style.display = 'none';
    }
  }

  window.toggleExpandDownloadCard = function (id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (expandedDownloadIds.has(id)) {
      expandedDownloadIds.delete(id);
    } else {
      expandedDownloadIds.add(id);
    }
    renderDownloadsUI();
  };

  function renderDownloadsUI() {
    const activeListEl = document.getElementById('downloads-active-list');
    const completedListEl = document.getElementById('downloads-completed-list');
    const activeEmptyEl = document.getElementById('downloads-active-empty');
    const completedEmptyEl = document.getElementById('downloads-completed-empty');
    const activeCountEl = document.getElementById('downloads-tab-active-count');
    const completedCountEl = document.getElementById('downloads-tab-completed-count');

    if (!activeListEl || !completedListEl) return;

    const activeItems = cachedDownloads.filter(d => d.status === 'DOWNLOADING' || d.status === 'PENDING' || d.status === 'PAUSED');
    const completedItems = cachedDownloads.filter(d => d.status === 'COMPLETED' || d.status === 'FAILED' || d.status === 'CANCELLED');

    if (activeCountEl) activeCountEl.textContent = '(' + activeItems.length + ')';
    if (completedCountEl) completedCountEl.textContent = '(' + completedItems.length + ')';
    updateDownloadsBadge(activeItems.length);

    // Render Active
    activeListEl.innerHTML = '';
    if (activeItems.length === 0) {
      activeListEl.innerHTML = `
        <div id="downloads-active-empty" style="padding: 30px 10px; text-align: center; color: var(--text-muted); font-size: 12px;">
          <div style="font-size: 28px; margin-bottom: 8px;">⚡</div>
          No active downloads right now
        </div>`;
    } else {
      activeItems.forEach(item => {
        const cat = getFileCategoryInfo(item.fileName, item.mimeType);
        const percent = item.totalBytes > 0 ? Math.min(100, Math.round((item.downloadedBytes * 100) / item.totalBytes)) : 0;
        const isIndeterminate = item.totalBytes <= 0;
        const isPaused = item.status === 'PAUSED';
        const isExpanded = expandedDownloadIds.has(item.id);

        let statsText = formatDownloadBytes(item.downloadedBytes);
        if (item.totalBytes > 0) statsText += ' / ' + formatDownloadBytes(item.totalBytes) + ' (' + percent + '%)';
        if (item.speedBytesPerSec > 0 && !isPaused) statsText += ' • ' + (item.speedBytesPerSec / (1024 * 1024)).toFixed(1) + ' MB/s';
        if (item.etaSeconds > 0 && !isPaused) statsText += ' • ' + Math.round(item.etaSeconds) + 's left';
        if (isPaused) statsText += ' • Paused';

        const card = document.createElement('div');
        card.className = 'download-item-card' + (isExpanded ? ' expanded' : '');
        card.onclick = function () { window.toggleExpandDownloadCard(item.id); };

        if (!isExpanded) {
          card.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 12.5px; font-weight: 700; color: var(--text-main); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">` + (item.fileName || 'Download') + `</div>
                <div style="font-size: 10.5px; color: var(--text-muted); margin-top: 2px;">` + statsText + `</div>
              </div>
              <div style="display: flex; gap: 4px; align-items: center;">
                <button class="download-action-btn ` + (isPaused ? 'primary' : '') + `" onclick="event.stopPropagation(); window.toggleDownloadPause('` + item.id + `', ` + isPaused + `);" title="` + (isPaused ? 'Resume' : 'Pause') + `">
                  ` + (isPaused ? '▶️' : '⏸️') + `
                </button>
                <button class="download-action-btn danger" onclick="event.stopPropagation(); window.cancelDownloadItem('` + item.id + `');" title="Cancel">
                  ✕
                </button>
              </div>
            </div>
            <div class="download-progress-track">
              <div class="download-progress-fill ` + (isIndeterminate ? 'indeterminate' : '') + `" style="width: ` + percent + `%;"></div>
            </div>`;
        } else {
          // Expanded view: Full filename visible without truncation, emoji + text explanation, expanded buttons!
          card.innerHTML = `
            <div style="display: flex; align-items: flex-start; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `" style="margin-top: 2px;">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 13.5px; font-weight: 800; color: var(--text-main); word-break: break-all; line-height: 1.35;">` + (item.fileName || 'Download') + `</div>
                <div style="margin-top: 6px;">
                  <div class="download-type-pill ` + cat.class + `">
                    <span>` + cat.icon + `</span> <span>` + cat.title + `</span>
                  </div>
                </div>
                <div style="font-size: 11px; color: var(--text-muted); margin-top: 6px;">` + statsText + `</div>
              </div>
            </div>
            <div class="download-progress-track" style="margin-top: 6px;">
              <div class="download-progress-fill ` + (isIndeterminate ? 'indeterminate' : '') + `" style="width: ` + percent + `%;"></div>
            </div>
            <div class="download-expanded-actions">
              <button class="download-action-btn ` + (isPaused ? 'primary' : '') + `" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.toggleDownloadPause('` + item.id + `', ` + isPaused + `);">
                ` + (isPaused ? '▶️ Resume Download' : '⏸️ Pause Download') + `
              </button>
              <button class="download-action-btn danger" style="padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.cancelDownloadItem('` + item.id + `');">
                ✕ Cancel
              </button>
            </div>`;
        }
        activeListEl.appendChild(card);
      });
    }

    // Render Completed
    completedListEl.innerHTML = '';
    if (completedItems.length === 0) {
      completedListEl.innerHTML = `
        <div id="downloads-completed-empty" style="padding: 30px 10px; text-align: center; color: var(--text-muted); font-size: 12px;">
          <div style="font-size: 28px; margin-bottom: 8px;">📂</div>
          No downloaded files yet
        </div>`;
    } else {
      completedItems.forEach(item => {
        const cat = getFileCategoryInfo(item.fileName, item.mimeType);
        const isFailed = item.status === 'FAILED';
        const isCancelled = item.status === 'CANCELLED';
        const isApk = cat.class === 'apk';
        const isExpanded = expandedDownloadIds.has(item.id);

        let subText = formatDownloadBytes(item.totalBytes > 0 ? item.totalBytes : item.downloadedBytes);
        let statusBadge = '';
        if (isFailed) {
          statusBadge = '<span style="color: #ef4444; font-weight: 700; margin-left: 4px;">Failed</span>';
        } else if (isCancelled) {
          statusBadge = '<span style="color: #f59e0b; font-weight: 700; margin-left: 4px;">Cancelled</span>';
        }

        const card = document.createElement('div');
        card.className = 'download-item-card' + (isExpanded ? ' expanded' : '');
        card.onclick = function () { window.toggleExpandDownloadCard(item.id); };

        if (!isExpanded) {
          card.innerHTML = `
            <div style="display: flex; align-items: center; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 12.5px; font-weight: 700; color: var(--text-main); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">` + (item.fileName || 'File') + `</div>
                <div style="font-size: 10.5px; color: var(--text-muted); margin-top: 2px;">
                  ` + subText + ` ` + statusBadge + `
                </div>
              </div>
              <div style="display: flex; gap: 4px; align-items: center;">
                ` + (!isFailed && !isCancelled ? `
                  <button class="download-action-btn primary" onclick="event.stopPropagation(); window.openDownloadedItem('` + item.id + `');" title="` + (isApk ? 'Install' : 'Open') + `">
                    ` + (isApk ? '🚀 Install' : '📂 Open') + `
                  </button>
                  <button class="download-action-btn" onclick="event.stopPropagation(); window.shareDownloadedItem('` + item.id + `');" title="Share">
                    📤
                  </button>
                ` : `
                  <button class="download-action-btn primary" onclick="event.stopPropagation(); window.retryDownloadItem('` + item.id + `');" title="Retry">
                    🔄
                  </button>
                `) + `
                <button class="download-action-btn danger" onclick="event.stopPropagation(); window.deleteDownloadItem('` + item.id + `');" title="Delete">
                  🗑️
                </button>
              </div>
            </div>`;
        } else {
          // Expanded view: Full filename visible without truncation, emoji + text explanation, expanded buttons!
          card.innerHTML = `
            <div style="display: flex; align-items: flex-start; gap: 10px;">
              <div class="download-file-icon-box ` + cat.class + `" style="margin-top: 2px;">` + cat.icon + `</div>
              <div style="flex: 1; min-width: 0;">
                <div style="font-size: 13.5px; font-weight: 800; color: var(--text-main); word-break: break-all; line-height: 1.35;">` + (item.fileName || 'File') + `</div>
                <div style="margin-top: 6px;">
                  <div class="download-type-pill ` + cat.class + `">
                    <span>` + cat.icon + `</span> <span>` + cat.title + `</span>
                  </div>
                </div>
                <div style="font-size: 11px; color: var(--text-muted); margin-top: 6px;">
                  Size: ` + subText + ` ` + statusBadge + ` • Saved in Downloads
                </div>
              </div>
            </div>
            <div class="download-expanded-actions">
              ` + (!isFailed && !isCancelled ? `
                <button class="download-action-btn primary" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.openDownloadedItem('` + item.id + `');">
                  ` + (isApk ? '🚀 Install APK' : '📂 Open File') + `
                </button>
                <button class="download-action-btn" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.shareDownloadedItem('` + item.id + `');">
                  📤 Share File
                </button>
              ` : `
                <button class="download-action-btn primary" style="flex: 1; justify-content: center; padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.retryDownloadItem('` + item.id + `');">
                  🔄 Retry Download
                </button>
              `) + `
              <button class="download-action-btn danger" style="padding: 7px 12px; font-size: 11.5px;" onclick="event.stopPropagation(); window.deleteDownloadItem('` + item.id + `');">
                🗑️ Delete
              </button>
            </div>`;
        }
        completedListEl.appendChild(card);
      });
    }
  }

  window.toggleDownloadPause = function (id, isPaused) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (window.CaspianBridge) {
      if (isPaused) {
        window.CaspianBridge.resumeDownload(id);
      } else {
        window.CaspianBridge.pauseDownload(id);
      }
      setTimeout(refreshDownloadsList, 150);
    }
  };

  window.cancelDownloadItem = function (id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (window.CaspianBridge) {
      if (typeof window.CaspianBridge.deleteDownload === 'function') {
        window.CaspianBridge.deleteDownload(id, true);
      } else {
        window.CaspianBridge.cancelDownload(id);
      }
      setTimeout(refreshDownloadsList, 150);
    }
  };

  window.openDownloadedItem = function (id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (window.CaspianBridge) {
      window.CaspianBridge.openDownloadedFile(id);
    }
  };

  window.shareDownloadedItem = function (id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (window.CaspianBridge) {
      window.CaspianBridge.shareDownloadedFile(id);
    }
  };

  window.retryDownloadItem = function (id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (window.CaspianBridge) {
      window.CaspianBridge.resumeDownload(id);
      setTimeout(refreshDownloadsList, 150);
    }
  };

  window.deleteDownloadItem = function (id) {
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    if (window.CaspianBridge) {
      window.CaspianBridge.deleteDownload(id, true);
      setTimeout(refreshDownloadsList, 150);
    }
  };

  // Android callbacks
  window.onDownloadStarted = function (item) {
    console.log('Download started:', item);
    refreshDownloadsList();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('📥 Downloading ' + (item.fileName || 'file') + '...');
    }
  };

  window.onDownloadProgress = function (item) {
    let found = false;
    for (let i = 0; i < cachedDownloads.length; i++) {
      if (cachedDownloads[i].id === item.id) {
        cachedDownloads[i] = item;
        found = true;
        break;
      }
    }
    if (!found) cachedDownloads.unshift(item);

    const modal = document.getElementById('caspian-downloads-modal');
    if (modal && modal.style.display === 'flex') {
      renderDownloadsUI();
    } else {
      const activeItems = cachedDownloads.filter(d => d.status === 'DOWNLOADING' || d.status === 'PENDING' || d.status === 'PAUSED');
      updateDownloadsBadge(activeItems.length);
    }
  };

  window.onDownloadCompleted = function (item) {
    console.log('Download completed:', item);
    try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (e) { }
    refreshDownloadsList();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('✅ Download complete: ' + item.fileName);
    }
  };

  window.onDownloadFailed = function (item, error) {
    console.error('Download failed:', item, error);
    refreshDownloadsList();
    if (window.CaspianBridge && typeof window.CaspianBridge.showToast === 'function') {
      window.CaspianBridge.showToast('❌ Download failed: ' + item.fileName);
    }
  };

  window.onDownloadCancelled = function (item) {
    refreshDownloadsList();
  };

  // Setup UI Listeners when DOM is ready
  function initDownloadManagerUI() {
    const modal = document.getElementById('caspian-downloads-modal');
    if (modal) {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          window.backFromBrowserDestination();
        }
      });
    }

    const closeDownloadsBtn = document.getElementById('downloads-modal-close-btn');
    if (closeDownloadsBtn) {
      closeDownloadsBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        window.closeDownloadsModal();
      });
    }

    const tabActiveBtn = document.getElementById('downloads-tab-active-btn');
    if (tabActiveBtn) {
      tabActiveBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        switchDownloadsTab('active');
      });
    }

    const tabCompletedBtn = document.getElementById('downloads-tab-completed-btn');
    if (tabCompletedBtn) {
      tabCompletedBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        switchDownloadsTab('completed');
      });
    }

    const openFolderBtn = document.getElementById('downloads-open-folder-btn');
    if (openFolderBtn) {
      openFolderBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (err) { }
        if (window.CaspianBridge && typeof window.CaspianBridge.openDownloadsFolder === 'function') {
          window.CaspianBridge.openDownloadsFolder();
        }
      });
    }

    const clearHistoryBtn = document.getElementById('downloads-clear-btn');
    if (clearHistoryBtn) {
      clearHistoryBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        try { if (window.playSFX) window.playSFX('tb_clicks'); } catch (err) { }
        if (window.CaspianBridge && typeof window.CaspianBridge.clearCompletedDownloads === 'function') {
          window.CaspianBridge.clearCompletedDownloads();
          refreshDownloadsList();
        }
      });
    }

    refreshDownloadsList();
    setTimeout(refreshDownloadsList, 250);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDownloadManagerUI);
  } else {
    initDownloadManagerUI();
  }
})();
