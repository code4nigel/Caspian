import os
import re
from assets.site_icons import GPT_ICON_B64, GEMINI_ICON_B64, GOOGLE_ICON_B64, YOUTUBE_ICON_B64

with open('d:/Projects/Chatgpt Pruner/Caspian-Android/assets/dev_avatar.txt', 'r') as f:
    dev_avatar_b64 = f.read().strip()

def build_html(version_name):
    return f'''<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Caspian Android Control Center</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@500;700&family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="mobile_control.css">
  <script>
    (function() {{
      try {{
        var t = localStorage.getItem('theme') || 'dark';
        document.documentElement.setAttribute('data-theme', t);
        if (t === 'dark') {{
          document.documentElement.classList.add('dark');
          document.documentElement.classList.remove('light');
        }} else {{
          document.documentElement.classList.add('light');
          document.documentElement.classList.remove('dark');
        }}
        var start = localStorage.getItem('theme_start_color') || '#A2A9A9';
        var end = localStorage.getItem('theme_end_color') || '#1B4264';
        var bg = localStorage.getItem('theme_bg_color') || (t === 'dark' ? '#050811' : '#ffffff');
        document.documentElement.style.setProperty('--accent', start, 'important');
        document.documentElement.style.setProperty('--secondary', end, 'important');
        document.documentElement.style.setProperty('--accent-glow', start + '55', 'important');
        document.documentElement.style.setProperty('--accent-gradient', 'linear-gradient(135deg, ' + start + ', ' + end + ')', 'important');
        document.documentElement.style.setProperty('--sheet-bg', bg, 'important');
        if (localStorage.getItem('master_sfx_muted') === 'true') {{
          document.documentElement.classList.add('sfx-muted');
        }}
      }} catch(e) {{}}
    }})();
  </script>
</head>
<body>

  <!-- Slide-Up Bottom Sheet (Samsung One UI + Native Overlay) -->
  <div id="bottom-sheet" class="caspian-bottom-sheet active" style="transform: translateY(0);">
    <div id="sheet-drag-area" class="sheet-drag-area" title="Drag to Resize Sheet Height">
      <div class="sheet-drag-handle"></div>
    </div>

    <!-- Header -->
    <div class="sheet-header">
      <div class="sheet-brand">
        <div class="brand-logo-box">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 7c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
            <path d="M4 12c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
            <path d="M4 17c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
          </svg>
        </div>
        <div style="display: flex; flex-direction: column;">
          <span class="sheet-brand-name">CASPIAN MOBILE</span>
          <span class="sheet-brand-tag">V{version_name}</span>
        </div>
      </div>
      <div class="header-icon-actions">
        <button id="mute-toggle-btn" class="icon-btn" title="Toggle All Sounds">
          <svg id="mute-icon-unmuted" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
            <path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path>
          </svg>
          <svg id="mute-icon-muted" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="display: none;">
            <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
            <line x1="23" y1="9" x2="17" y2="15"></line>
            <line x1="17" y1="9" x2="23" y2="15"></line>
          </svg>
        </button>
        <button id="reload-btn" class="icon-btn" title="Reload Active WebView">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M23 4v6h-6"></path>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
          </svg>
        </button>
        <button id="theme-toggle-btn" class="icon-btn" title="Toggle Theme">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
          </svg>
        </button>
        <button id="power-toggle-btn" class="icon-btn" title="Master Power Switch">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M18.36 6.64a9 9 0 1 1-12.73 0"></path>
            <line x1="12" y1="2" x2="12" y2="12"></line>
          </svg>
        </button>
      </div>
    </div>

    <!-- Tab Navigation Bar (Engine, Tabs, Settings) -->
    <div class="mobile-tab-nav">
      <button id="tab-btn-engine" class="tab-nav-btn active" data-tab="engine">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 6c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path><path d="M2 12c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path><path d="M2 18c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path></svg>
        <span>Engine</span>
      </button>
      <button id="tab-btn-sites" class="tab-nav-btn" data-tab="sites">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="9" y1="3" x2="9" y2="21"></line></svg>
        <span>Tabs</span>
      </button>
      <button id="tab-btn-settings" class="tab-nav-btn" data-tab="settings">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
        <span>Settings</span>
      </button>
    </div>

    <!-- TAB 1: ENGINE TAB -->
    <div id="tab-pane-engine" class="tab-pane active">
      <!-- 1. Temporary Chat Saver Card -->
      <div id="card-temp-saver" class="m3-card engine-card">
        <div class="m3-card-row">
          <div class="m3-card-left">
            <div id="ts-status-dot" class="status-dot active"></div>
            <div>
              <div class="m3-card-title">Temporary Chat Saver</div>
              <div class="m3-card-sub">Save or convert temporary mobile sessions anytime.</div>
            </div>
          </div>
          <button id="toggle-temp-saver-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 4px 10px;">ON</button>
        </div>

        <div id="temp-saver-body" style="margin-top: 10px;">
          <div class="mobile-action-row">
            <button id="convert-btn" class="oneui-pill-btn primary" style="flex: 1; justify-content: center;">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
              </svg>
              <span>Convert Chat</span>
            </button>
            
            <div style="position: relative; flex: 1;">
              <button id="export-dropdown-trigger" class="oneui-pill-btn secondary" style="width: 100%; justify-content: center;">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
                <span>Export ▼</span>
              </button>
              
              <div id="export-menu" class="export-menu-overlay">
                <button class="export-opt-btn" data-fmt="md">Markdown (.md)</button>
                <button class="export-opt-btn" data-fmt="txt">Plain Text (.txt)</button>
                <button class="export-opt-btn" data-fmt="doc">Google Doc (.doc)</button>
                <button class="export-opt-btn" data-fmt="nativepdf">Document PDF (.pdf)</button>
                <button class="export-opt-btn" data-fmt="styledpdf">Caspian PDF Format</button>
              </div>
            </div>

            <button id="copy-btn" class="oneui-pill-btn icon-only" title="Copy Transcript" style="flex-shrink: 0; width: 38px; height: 38px; justify-content: center; padding: 0;">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 2. Expandable Chat Message Limit Card -->
      <div id="card-chat-limit" class="m3-card engine-card expandable" style="margin-top: 14px;">
        <div id="chat-limit-header" class="m3-card-row" style="cursor: pointer;">
          <div class="m3-card-left">
            <div id="status-dot" class="status-dot active"></div>
            <div>
              <div id="status-title" class="m3-card-title">Chat Message Limit</div>
              <div id="status-sub" class="m3-card-sub">Limits message count to improve performance and prevent lagging. Tap to expand.</div>
            </div>
          </div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span id="active-limit-badge" class="m3-badge">5 Messages</span>
            <button id="toggle-chat-limit-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 4px 10px;">ON</button>
          </div>
        </div>

        <!-- Accordion Body: Contains Visible Messages Limit selector -->
        <div id="chat-limit-body" style="display: block; margin-top: 12px; border-top: 1px solid var(--border-glass); padding-top: 10px;">
          <div class="limit-section-title">
            <span>VISIBLE MESSAGES LIMIT</span>
            <span>AUTO-PRUNE OLDER</span>
          </div>
          <div class="pill-grid">
            <button class="limit-pill" data-val="1">1</button>
            <button class="limit-pill" data-val="3">3</button>
            <button class="limit-pill active" data-val="5">5</button>
            <button class="limit-pill" data-val="8">8</button>
            <button class="limit-pill" data-val="15">15</button>
            <button class="limit-pill" data-val="9999" title="Unlimited (Show All)">&#8734;</button>
          </div>
        </div>
      </div>

      <!-- YouTube Control Center Card (Expandable Engine Card) -->
      <div id="youtube-control-card" class="m3-card engine-card expandable" style="margin-top: 14px;">
        <div id="yt-control-header" class="m3-card-row" style="cursor: pointer;">
          <div class="m3-card-left">
            <div id="yt-live-status-dot" class="status-dot active"></div>
            <div>
              <div class="m3-card-title">YouTube Player Controls</div>
              <div class="m3-card-sub">Quick seeking, playback speed, Float Pod, and resolution.</div>
            </div>
          </div>
          <div class="card-controls" style="display: flex; align-items: center; gap: 8px;">
            <button id="yt-toggle-popup-btn" class="oneui-pill-btn secondary" style="font-size: 11px; padding: 4px 10px; gap: 4px;" onclick="event.stopPropagation();">
              <span>🚀 Float Pod</span>
            </button>
            <button id="toggle-yt-engine-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 4px 10px;" onclick="event.stopPropagation();">
              ON
            </button>
          </div>
        </div>

        <!-- Accordion Body -->
        <div id="yt-control-body" style="display: none; margin-top: 12px; border-top: 1px solid var(--border-glass); padding-top: 10px;">
          <!-- Primary Playback & Seek Row -->
          <div style="display: flex; gap: 6px; align-items: center; justify-content: space-between;">
            <button id="yt-seek-back-10-btn" class="oneui-pill-btn secondary" style="flex: 1; justify-content: center; padding: 8px 4px; font-size: 11px;">
              <span>-10s</span>
            </button>
            <button id="yt-seek-back-btn" class="oneui-pill-btn secondary" style="flex: 1; justify-content: center; padding: 8px 4px; font-size: 11px;">
              <span>-5s</span>
            </button>
            <button id="yt-play-pause-btn" class="oneui-pill-btn primary" style="flex: 1.6; justify-content: center; padding: 8px 10px; font-size: 12px; font-weight: 700; gap: 6px;">
              <span id="yt-play-icon">▶️ / ⏸️</span>
              <span id="yt-play-text">Play</span>
            </button>
            <button id="yt-seek-fwd-btn" class="oneui-pill-btn secondary" style="flex: 1; justify-content: center; padding: 8px 4px; font-size: 11px;">
              <span>+5s</span>
            </button>
            <button id="yt-seek-fwd-10-btn" class="oneui-pill-btn secondary" style="flex: 1; justify-content: center; padding: 8px 4px; font-size: 11px;">
              <span>+10s</span>
            </button>
          </div>

          <!-- Playback Speed Section -->
          <div class="limit-section-title" style="margin-top: 14px; margin-bottom: 6px;">PLAYBACK SPEED</div>
          <div class="pill-grid" style="grid-template-columns: repeat(4, 1fr); gap: 6px;">
            <button class="yt-speed-pill" data-speed="0.25">0.25x</button>
            <button class="yt-speed-pill" data-speed="0.5">0.5x</button>
            <button class="yt-speed-pill" data-speed="0.75">0.75x</button>
            <button class="yt-speed-pill active" data-speed="1.0">1x</button>
            <button class="yt-speed-pill" data-speed="1.25">1.25x</button>
            <button class="yt-speed-pill" data-speed="1.5">1.5x</button>
            <button class="yt-speed-pill" data-speed="1.75">1.75x</button>
            <button class="yt-speed-pill" data-speed="2.0">2x</button>
            <button class="yt-speed-pill" data-speed="2.5">2.5x</button>
            <button class="yt-speed-pill" data-speed="3.0">3x</button>
            <button class="yt-speed-pill" data-speed="4.0">4x</button>
          </div>

          <!-- Quality Selector Section -->
          <div class="limit-section-title" style="margin-top: 14px; margin-bottom: 6px;">VIDEO QUALITY</div>
          <div class="pill-grid" style="grid-template-columns: repeat(3, 1fr); gap: 6px;">
            <button class="yt-quality-pill" data-quality="hd1080">1080p</button>
            <button class="yt-quality-pill" data-quality="hd720">720p</button>
            <button class="yt-quality-pill" data-quality="large">480p</button>
            <button class="yt-quality-pill" data-quality="medium">360p</button>
            <button class="yt-quality-pill" data-quality="small">240p</button>
            <button class="yt-quality-pill active" data-quality="auto">Auto</button>
          </div>
        </div>
      </div>

      <!-- 3. Caspian Drift (Speech Dictation & Voice Engine) Card -->
      <div id="card-caspian-current" class="m3-card engine-card expandable" style="margin-top: 14px;">
        <div id="caspian-current-header" class="m3-card-row" style="cursor: pointer;">
          <div class="m3-card-left">
            <div id="cc-status-dot" class="status-dot active"></div>
            <div>
              <div class="m3-card-title">Caspian Drift</div>
              <div class="m3-card-sub">Long-press wave button to dictate speech. Tap to customize STT models & API keys.</div>
            </div>
          </div>
          <button id="toggle-caspian-current-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 4px 10px;">ON</button>
        </div>

        <!-- Accordion Body -->
        <div id="caspian-current-body" style="display: none; margin-top: 12px; border-top: 1px solid var(--border-glass); padding-top: 10px;">
          <div class="limit-section-title" style="margin-bottom: 6px;">SELECT SPEECH-TO-TEXT MODEL</div>
          <div class="pill-grid" style="grid-template-columns: repeat(3, 1fr); gap: 6px; margin-bottom: 10px;">
            <button class="cc-stt-pill active" data-engine="deepgram">⚡ Deepgram</button>
            <button class="cc-stt-pill" data-engine="huggingface">🤗 HuggingFace</button>
            <button class="cc-stt-pill" data-engine="android_native">📱 Native Android</button>
          </div>

          <!-- STT API Key Row -->
          <div class="limit-section-title" id="cc-api-key-label" style="margin-bottom: 6px;">DEEPGRAM API KEY</div>
          <div id="cc-api-key-container" style="display: flex; gap: 6px; align-items: center; margin-bottom: 10px;">
            <input type="password" id="whisper-api-key-input" placeholder="Paste your API key here..." style="flex: 1; background: rgba(0,0,0,0.3); border: 1px solid var(--border-glass); border-radius: 8px; padding: 8px 10px; color: var(--text-main); font-size: 11px; outline: none;" />
            <button id="save-whisper-api-key-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 8px 12px;">Save</button>
          </div>

          <!-- Accent / Language selector -->
          <div class="limit-section-title" style="margin-bottom: 6px;">SPEECH LANGUAGE & ACCENT</div>
          <div class="pill-grid" style="grid-template-columns: repeat(4, 1fr); gap: 6px;">
            <button class="cc-lang-pill active" data-lang="auto">🌐 Auto</button>
            <button class="cc-lang-pill" data-lang="en-US">🇺🇸 EN (US)</button>
            <button class="cc-lang-pill" data-lang="en-GB">🇬🇧 EN (UK)</button>
            <button class="cc-lang-pill" data-lang="en-IN">🇮🇳 EN (IN)</button>
            <button class="cc-lang-pill" data-lang="hi">🇮🇳 Hindi</button>
            <button class="cc-lang-pill" data-lang="es">🇪🇸 Spanish</button>
            <button class="cc-lang-pill" data-lang="fr">🇫🇷 French</button>
            <button class="cc-lang-pill" data-lang="de">🇩🇪 German</button>
          </div>
        </div>
      </div>

      <!-- 4. Expandable AdBlocker Engine Card -->
      <div id="card-adblocker" class="m3-card engine-card expandable" style="margin-top: 14px;">
        <div id="adblock-header" class="m3-card-row" style="cursor: pointer;">
          <div class="m3-card-left">
            <div id="adblock-dot" class="status-dot active"></div>
            <div>
              <div class="m3-card-title">AdBlock Engine</div>
              <div class="m3-card-sub">AdBlock for peaceful internet.</div>
            </div>
          </div>
          <button id="toggle-adblock-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 4px 10px;">ON</button>
        </div>

        <!-- Accordion Body -->
        <div id="adblock-body" style="display: none; margin-top: 12px; border-top: 1px solid var(--border-glass); padding-top: 10px;">
          <!-- Video Defuser Sub-Section -->
          <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 12px; padding: 10px; margin-bottom: 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <div style="display: flex; align-items: center; gap: 6px;">
                <span style="font-size: 12px; font-weight: 700; color: var(--text-main);">🎬 Video Defuser</span>
              </div>
            </div>
            <div style="font-size: 10px; color: var(--text-muted); line-height: 1.4; margin-bottom: 8px;">
              Purges player adPlacements, intercepts SPA fetch streams, and skips server-injected ads in 0ms.
            </div>
            <div style="display: flex; flex-direction: column; gap: 6px;">
              <label style="display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--text-main); cursor: pointer;">
                <span>⚡ Defuse Video Ad Placements</span>
                <input type="checkbox" id="chk-adblock-yt" checked style="accent-color: var(--accent);" />
              </label>
              <label style="display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--text-main); cursor: pointer;">
                <span>⏩ 0ms Auto-Fast-Forward Fallback</span>
                <input type="checkbox" id="chk-adblock-skip" checked style="accent-color: var(--accent);" />
              </label>
            </div>
          </div>

          <!-- General Web AdBlock Sub-Section -->
          <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-glass); border-radius: 12px; padding: 10px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--text-main); margin-bottom: 6px;">🌐 Universal Web Filters</div>
            <div style="display: flex; flex-direction: column; gap: 6px;">
              <label style="display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--text-main); cursor: pointer;">
                <span>🚫 Block Banners & Overlays</span>
                <input type="checkbox" id="chk-adblock-banner" checked style="accent-color: var(--accent);" />
              </label>
              <label style="display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--text-main); cursor: pointer;">
                <span>🔒 Block Trackers & Telemetry</span>
                <input type="checkbox" id="chk-adblock-trackers" checked style="accent-color: var(--accent);" />
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 5. Expandable Google Search Engine Card -->
      <div id="google-search-card" class="m3-card engine-card expandable" style="margin-top: 14px;">
        <div id="google-dock-header" class="m3-card-row" style="cursor: pointer;">
          <div class="m3-card-left">
            <div id="google-dock-status-dot" class="status-dot active"></div>
            <div>
              <div class="m3-card-title">Google Search Dock</div>
              <div class="m3-card-sub">Liquid Glass floating toolbar for search navigation & in-page finder.</div>
            </div>
          </div>
          <div class="card-controls" style="display: flex; align-items: center; gap: 8px;">
            <button id="google-dock-toggle-popup-btn" class="oneui-pill-btn secondary" style="font-size: 11px; padding: 4px 10px; gap: 4px;" onclick="event.stopPropagation();">
              <span>🚀 Toolbar</span>
            </button>
            <button id="toggle-google-dock-btn" class="oneui-pill-btn primary" style="font-size: 11px; padding: 4px 10px;" onclick="event.stopPropagation();">
              ON
            </button>
          </div>
        </div>

        <!-- Accordion Body -->
        <div id="google-dock-body" style="display: none; margin-top: 12px; border-top: 1px solid var(--border-glass); padding-top: 10px;">
          <!-- Quick Options Sub-Section -->
          <div style="background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 12px; padding: 10px;">
            <div style="font-size: 11px; font-weight: 700; color: var(--text-main); margin-bottom: 6px;">🧭 Toolbar Capabilities</div>
            <div style="font-size: 10px; color: var(--text-muted); line-height: 1.4; margin-bottom: 8px;">
              • <b>Tap URL Pill</b>: Focuses Google's search box on the webpage to quickly search new queries in the same tab.<br/>
              • <b>Tap 🔍 Finder</b>: Activates in-page word finder to highlight matches and smoothly jump using ⬆️ / ⬇️ arrows.<br/>
              • <b>Quick Jump Scroll</b>: In normal mode, tap ⬆️ to scroll to top of page and ⬇️ to scroll to bottom of page.<br/>
              • <b>Two-Finger Repositioning</b>: Drag anywhere with 2 fingers to move toolbar across the screen.
            </div>
            <div style="display: flex; flex-direction: column; gap: 6px;">
              <label style="display: flex; align-items: center; justify-content: space-between; font-size: 11px; color: var(--text-main); cursor: pointer;">
                <span>🔽 Auto-Collapse into Floating Ball on Scroll</span>
                <input type="checkbox" id="chk-google-dock-autocollapse" checked style="accent-color: var(--accent);" />
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB 2: TABS & SITES TAB -->
    <div id="tab-pane-sites" class="tab-pane" style="display: none;">
      <div class="m3-card">
        <div class="m3-card-row" style="margin-bottom: 8px;">
          <div class="m3-card-title">OPEN NEW PLATFORM TAB</div>
          <button id="new-tab-btn" class="text-link-btn">+ New Tab</button>
        </div>

        <!-- App Icon Grid Cards -->
        <div class="app-icon-grid">
          <div id="app-card-hub" class="app-icon-card" data-service="hub">
            <div style="width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #1B4264, #A2A9A9); display: flex; align-items: center; justify-content: center; margin-bottom: 8px;">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="2.5"><path d="M4 7c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path><path d="M4 12c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path><path d="M4 17c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path></svg>
            </div>
            <span class="app-icon-label">Caspian Hub</span>
          </div>

          <div id="app-card-chatgpt" class="app-icon-card" data-service="chatgpt">
            <img src="{GPT_ICON_B64}" class="app-icon-img" alt="ChatGPT" />
            <span class="app-icon-label">ChatGPT</span>
          </div>

          <div id="app-card-gemini" class="app-icon-card" data-service="gemini">
            <img src="{GEMINI_ICON_B64}" class="app-icon-img" alt="Gemini" />
            <span class="app-icon-label">Google Gemini</span>
          </div>

          <div id="app-card-google" class="app-icon-card" data-service="google">
            <img src="{GOOGLE_ICON_B64}" class="app-icon-img" alt="Google Search" />
            <span class="app-icon-label">Google Search</span>
          </div>

          <div id="app-card-youtube" class="app-icon-card" data-service="youtube">
            <img src="{YOUTUBE_ICON_B64}" class="app-icon-img" alt="YouTube" style="transform: scale(1.45);" />
            <span class="app-icon-label">YouTube</span>
          </div>
        </div>

        <!-- Active Tabs Container Header with Filter Pills -->
        <div class="m3-card-row" style="margin-top: 16px; margin-bottom: 6px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 6px;">
          <div style="display: flex; align-items: center; gap: 6px;">
            <div class="setting-section-header" style="margin: 0;">Active Tabs</div>
            <span id="tab-count-badge" class="m3-badge" style="background: rgba(0,0,0,0.06); color: var(--text-sub);">1 Tab</span>
          </div>

          <!-- Tab Filter Pills (All / Groups / Single) -->
          <div id="tab-filter-pill-bar" style="display: flex; background: var(--input-bg); border: 1px solid var(--border-glass); border-radius: 14px; padding: 2px; gap: 2px;">
            <button class="tab-filter-pill active" data-filter="all" style="font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 12px; border: none; background: var(--accent); color: #fff; cursor: pointer;">All</button>
            <button class="tab-filter-pill" data-filter="groups" style="font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 12px; border: none; background: transparent; color: var(--text-sub); cursor: pointer;">Groups</button>
            <button class="tab-filter-pill" data-filter="single" style="font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 12px; border: none; background: transparent; color: var(--text-sub); cursor: pointer;">Single</button>
          </div>

          <button id="close-all-tabs-btn" class="oneui-pill-btn secondary" style="padding: 4px 10px; font-size: 11px; color: #ef4444; border-color: rgba(239,68,68,0.3);">Close All</button>
        </div>
        <!-- Inside Group Banner -->
        <div id="inside-group-header" class="inside-group-banner" style="display: none; margin-top: 8px; margin-bottom: 8px; padding: 10px 12px; border-radius: 14px; background: var(--input-bg); border: 1px solid var(--border-glass);">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span id="group-banner-color-dot" style="width: 12px; height: 12px; border-radius: 50%; background: #3b82f6; display: inline-block;"></span>
              <strong id="group-banner-title" style="font-size: 13px; color: var(--text-main);">Group Name</strong>
              <span id="group-banner-count" class="m3-badge">3 Tabs</span>
            </div>
            <button id="btn-close-group-view" class="icon-btn" title="Back to All Tabs" style="font-size: 11px; width: 28px; height: 28px;">✕</button>
          </div>
          <div style="display: flex; gap: 6px; flex-wrap: wrap;">
            <button id="btn-edit-group" class="oneui-pill-btn secondary" style="font-size: 10px; padding: 3px 8px;">✏️ Edit</button>
            <button id="btn-leave-group" class="oneui-pill-btn secondary" style="font-size: 10px; padding: 3px 8px;">📂 Leave Tabs (Ungroup)</button>
            <button id="btn-delete-group" class="oneui-pill-btn danger" style="font-size: 10px; padding: 3px 8px; color: #ef4444; border-color: rgba(239,68,68,0.3);">🗑️ Delete Group & Tabs</button>
          </div>
        </div>

        <div id="tabs-list-container" style="margin-top: 8px; display: flex; flex-direction: column; gap: 8px;">
          <!-- Dynamically populated open tabs -->
        </div>

        <!-- Floating Multi-Select Grouping Toolbar -->
        <div id="floating-grouping-toolbar" class="floating-group-bar" style="display: none;">
          <div style="display: flex; align-items: center; justify-content: space-between; width: 100%; gap: 10px;">
            <span id="grouping-select-count" style="font-weight: 700; font-size: 13px; color: var(--text-main); white-space: nowrap;">0 Selected</span>
            <div style="display: flex; align-items: center; gap: 8px;">
              <button id="toolbar-group-btn" class="oneui-pill-btn primary" style="height: 34px; border-radius: 17px; padding: 0 14px; font-size: 11px; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">📁 Make Group</button>
              <button id="toolbar-deselect-btn" class="oneui-pill-btn secondary" style="height: 34px; border-radius: 17px; padding: 0 14px; font-size: 11px; font-weight: 600; display: inline-flex; align-items: center; justify-content: center;">Deselect</button>
              <button id="toolbar-delete-btn" class="oneui-pill-btn danger" title="Delete Selected Tabs" style="width: 34px; height: 34px; border-radius: 17px; padding: 0; display: inline-flex; align-items: center; justify-content: center; background: rgba(239,68,68,0.18); color: #ef4444; border: 1px solid rgba(239,68,68,0.3); font-size: 15px;">🗑️</button>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- Create / Edit Tab Group Modal Dialog -->
    <div id="modal-create-group" class="modal-overlay" style="display: none;">
      <div class="modal-card">
        <div class="modal-header">
          <span id="modal-group-title-label" class="modal-title">Create Tab Group</span>
          <button id="modal-close-group-btn" class="modal-close-btn">&times;</button>
        </div>
        <div class="modal-body" style="padding: 14px 0;">
          <div class="limit-section-title" style="margin-bottom: 6px;">GROUP NAME</div>
          <input type="text" id="input-group-title" class="caspian-text-input" placeholder="e.g. Research, Work, Videos..." style="width: 100%; padding: 8px 12px; font-size: 12px; border-radius: 8px; background: var(--input-bg); border: 1px solid var(--border-glass); color: var(--text-main); margin-bottom: 14px;" />

          <div class="limit-section-title" style="margin-bottom: 6px;">GROUP EMOJI ICON</div>
          <div id="group-emoji-palette" style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 14px;">
            <div class="group-emoji-dot active" data-emoji="📁" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">📁</div>
            <div class="group-emoji-dot" data-emoji="🚀" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">🚀</div>
            <div class="group-emoji-dot" data-emoji="🔥" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">🔥</div>
            <div class="group-emoji-dot" data-emoji="⭐" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">⭐</div>
            <div class="group-emoji-dot" data-emoji="🎨" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">🎨</div>
            <div class="group-emoji-dot" data-emoji="📚" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">📚</div>
            <div class="group-emoji-dot" data-emoji="🎮" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">🎮</div>
            <div class="group-emoji-dot" data-emoji="💡" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">💡</div>
            <div class="group-emoji-dot" data-emoji="💼" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">💼</div>
            <div class="group-emoji-dot" data-emoji="⚡" style="font-size: 18px; width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--bg-card); border: 1px solid var(--border-glass);">⚡</div>
          </div>

          <div class="limit-section-title" style="margin-bottom: 6px;">GROUP COLOR ACCENT</div>
          <div id="group-color-palette" class="color-palette-grid" style="display: flex; gap: 8px; justify-content: space-between; margin-bottom: 16px;">
            <div class="group-color-dot active" data-color="#ef4444" style="background: #ef4444; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#f97316" style="background: #f97316; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#eab308" style="background: #eab308; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#10b981" style="background: #10b981; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#06b6d4" style="background: #06b6d4; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#3b82f6" style="background: #3b82f6; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#8b5cf6" style="background: #8b5cf6; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
            <div class="group-color-dot" data-color="#ec4899" style="background: #ec4899; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          </div>
        </div>
        <div class="modal-footer" style="display: flex; justify-content: flex-end; gap: 8px;">
          <button id="btn-cancel-create-group" class="oneui-pill-btn secondary">Cancel</button>
          <button id="btn-confirm-create-group" class="oneui-pill-btn primary">Save Group</button>
        </div>
      </div>
    </div>

    <!-- TAB 3: SETTINGS TAB -->
    <div id="tab-pane-settings" class="tab-pane" style="display: none;">
      
      <!-- Aesthetics & Theme Card -->
      <div class="m3-card">
        <div class="m3-card-row" style="margin-bottom: 12px;">
          <div class="m3-card-title">AESTHETICS & THEME</div>
          <button id="reset-theme-btn" class="text-link-btn">Reset Defaults</button>
        </div>

        <!-- Appearance Mode -->
        <div class="setting-row">
          <span class="setting-label">Appearance Mode</span>
          <div class="mode-pill-toggle">
            <button id="theme-btn-dark" class="mode-pill">Dark</button>
            <button id="theme-btn-light" class="mode-pill active">Light</button>
          </div>
        </div>

        <!-- Background Color Presets -->
        <div class="setting-section-header" style="margin-top: 14px;">Background Tone</div>
        <div class="preset-pill-grid" style="margin-top: 6px;">
          <button class="bg-preset-btn active" data-bg="#ffffff"><span class="color-dot" style="background: #ffffff; border: 1px solid #ccc;"></span> Pure White</button>
          <button class="bg-preset-btn" data-bg="#050811"><span class="color-dot" style="background: #050811; border: 1px solid #333;"></span> OLED Black</button>
          <button class="bg-preset-btn" data-bg="#000000"><span class="color-dot" style="background: #000000;"></span> Pitch Black</button>
          <button class="bg-preset-btn" data-bg="#0a1128"><span class="color-dot" style="background: #0a1128;"></span> Bluish Dark</button>
        </div>

        <!-- Quick Presets for Accents -->
        <div class="setting-section-header" style="margin-top: 14px;">Accent Presets</div>
        <div class="preset-pill-grid" style="margin-top: 6px;">
          <button class="preset-btn active" data-preset="caspian"><span class="color-dot" style="background: #A2A9A9;"></span> Caspian</button>
          <button class="preset-btn" data-preset="cyan"><span class="color-dot" style="background: #06b6d4;"></span> Cyan</button>
          <button class="preset-btn" data-preset="violet"><span class="color-dot" style="background: #a855f7;"></span> Violet</button>
          <button class="preset-btn" data-preset="azure"><span class="color-dot" style="background: #3b82f6;"></span> Azure</button>
          <button class="preset-btn" data-preset="emerald"><span class="color-dot" style="background: #10b981;"></span> Emerald</button>
        </div>
      </div>

      <!-- Visuals & Animations Card -->
      <details class="m3-card" id="settings-card-visuals" style="cursor: pointer; padding-bottom: 12px;">
        <summary style="font-size: 11px; font-weight: 700; color: var(--text-sub); letter-spacing: 0.5px; user-select: none; outline: none; list-style: none; display: flex; align-items: center; justify-content: space-between;">
          <span>VISUALS & ANIMATIONS ▼</span>
          <span style="font-size: 10px; opacity: 0.7;">Refresh Rate & Transitions</span>
        </summary>
        
        <div style="margin-top: 12px; border-top: 1px dashed var(--border-glass); padding-top: 10px; cursor: default;" onclick="event.stopPropagation();">
          <!-- Active Refresh Rate (ms) -->
          <div class="setting-row" style="margin-bottom: 12px; position: relative; z-index: 1000;">
            <span class="setting-label" title="Force WebView repaint refresh rate in ms during streaming replies">Active Refresh Rate (ms)</span>
            <div class="caspian-select" id="select-refresh-rate" style="width: 140px;">
              <div class="caspian-select-trigger" style="padding: 4px 10px;">
                <span id="selected-refresh-rate-text" style="font-size: 10px;">100 ms (Fast)</span>
                <svg class="chevron-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
              </div>
              <div class="caspian-select-options">
                <div class="caspian-select-option" data-val="50">50 ms (Realtime)</div>
                <div class="caspian-select-option active" data-val="100">100 ms (Fast)</div>
                <div class="caspian-select-option" data-val="250">250 ms (Normal)</div>
                <div class="caspian-select-option" data-val="500">500 ms (Slow)</div>
                <div class="caspian-select-option" data-val="0">Off (Default)</div>
              </div>
            </div>
          </div>

          <!-- Animation Style -->
          <div class="setting-row" style="margin-bottom: 12px; position: relative; z-index: 950;">
            <span class="setting-label">Menu Animation Style</span>
            <div class="caspian-select" id="select-anim-style" style="width: 140px;">
              <div class="caspian-select-trigger" style="padding: 4px 10px;">
                <span id="selected-anim-style-text" style="font-size: 10px;">Genie</span>
                <svg class="chevron-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
              </div>
              <div class="caspian-select-options">
                <div class="caspian-select-option" data-val="slide">Slide Overlay</div>
                <div class="caspian-select-option active" data-val="genie">Genie</div>
                <div class="caspian-select-option" data-val="none">No Animation</div>
              </div>
            </div>
          </div>

        <!-- Open Duration Settings -->
        <div class="setting-row" style="margin-bottom: 12px;">
          <span class="setting-label">Open Duration</span>
          <div style="display: flex; align-items: center; gap: 8px;">
            <button id="btn-open-dur-minus" class="oneui-pill-btn secondary icon-only" style="width: 28px; height: 28px; border-radius: 8px; font-weight: bold; font-size: 14px;">-</button>
            <span id="txt-open-dur" style="font-size: 11px; font-weight: 700; width: 55px; text-align: center; color: var(--text-main);">150 ms</span>
            <button id="btn-open-dur-plus" class="oneui-pill-btn secondary icon-only" style="width: 28px; height: 28px; border-radius: 8px; font-weight: bold; font-size: 14px;">+</button>
          </div>
        </div>

        <!-- Close Duration Settings -->
        <div class="setting-row" style="margin-bottom: 12px;">
          <span class="setting-label">Close Duration</span>
          <div style="display: flex; align-items: center; gap: 8px;">
            <button id="btn-close-dur-minus" class="oneui-pill-btn secondary icon-only" style="width: 28px; height: 28px; border-radius: 8px; font-weight: bold; font-size: 14px;">-</button>
            <span id="txt-close-dur" style="font-size: 11px; font-weight: 700; width: 55px; text-align: center; color: var(--text-main);">150 ms</span>
            <button id="btn-close-dur-plus" class="oneui-pill-btn secondary icon-only" style="width: 28px; height: 28px; border-radius: 8px; font-weight: bold; font-size: 14px;">+</button>
          </div>
        </div>

        <!-- Action Button Tap Bounce Duration -->
        <div class="setting-row">
          <span class="setting-label">Action Button Tap Feedback</span>
          <div style="display: flex; align-items: center; gap: 8px;">
            <button id="btn-tap-dur-minus" class="oneui-pill-btn secondary icon-only" style="width: 28px; height: 28px; border-radius: 8px; font-weight: bold; font-size: 14px;">-</button>
            <span id="txt-tap-dur" style="font-size: 11px; font-weight: 700; width: 55px; text-align: center; color: var(--text-main);">100 ms</span>
            <button id="btn-tap-dur-plus" class="oneui-pill-btn secondary icon-only" style="width: 28px; height: 28px; border-radius: 8px; font-weight: bold; font-size: 14px;">+</button>
          </div>
        </div>

        <!-- Widget Size & Scale Controls -->
        <div style="margin-top: 14px; border-top: 1px dashed var(--border-glass); padding-top: 10px;">
          <div style="font-size: 10px; color: var(--text-muted); margin-bottom: 8px; font-weight: 700; letter-spacing: 0.5px;">WIDGET SIZE & SCALE</div>
          
          <!-- Caspian Action Button Scale -->
          <div class="setting-row" style="margin-bottom: 10px;">
            <span class="setting-label">Caspian Action Button</span>
            <div class="pill-group" style="display: flex; gap: 4px;">
              <button class="limit-pill btn-action-btn-scale" data-scale="0.85">85%</button>
              <button class="limit-pill btn-action-btn-scale active" data-scale="1.0">100%</button>
              <button class="limit-pill btn-action-btn-scale" data-scale="1.15">115%</button>
              <button class="limit-pill btn-action-btn-scale" data-scale="1.3">130%</button>
            </div>
          </div>

          <!-- YouTube Float Pod Scale -->
          <div class="setting-row" style="margin-bottom: 10px;">
            <span class="setting-label">YouTube Float Pod</span>
            <div class="pill-group" style="display: flex; gap: 4px;">
              <button class="limit-pill btn-yt-pod-scale" data-scale="0.85">85%</button>
              <button class="limit-pill btn-yt-pod-scale active" data-scale="1.0">100%</button>
              <button class="limit-pill btn-yt-pod-scale" data-scale="1.15">115%</button>
              <button class="limit-pill btn-yt-pod-scale" data-scale="1.3">130%</button>
            </div>
          </div>

          <!-- Google Search Toolbar Scale -->
          <div class="setting-row">
            <span class="setting-label">Google Search Toolbar</span>
            <div class="pill-group" style="display: flex; gap: 4px;">
              <button class="limit-pill btn-google-dock-scale" data-scale="0.85">85%</button>
              <button class="limit-pill btn-google-dock-scale active" data-scale="1.0">100%</button>
              <button class="limit-pill btn-google-dock-scale" data-scale="1.15">115%</button>
              <button class="limit-pill btn-google-dock-scale" data-scale="1.3">130%</button>
            </div>
          </div>
        </div>
      </div>
      </details>

      <!-- Audio Settings Card -->
      <details class="m3-card" id="settings-card-audio" style="cursor: pointer; padding-bottom: 12px;">
        <summary style="font-size: 11px; font-weight: 700; color: var(--text-sub); letter-spacing: 0.5px; user-select: none; outline: none; list-style: none; display: flex; align-items: center; justify-content: space-between;">
          <span>AUDIO & SOUND EFFECTS (SFX) ▼</span>
          <span style="font-size: 10px; opacity: 0.7;">Sound Feedback & Volume</span>
        </summary>
        
        <div style="margin-top: 12px; border-top: 1px dashed var(--border-glass); padding-top: 10px; cursor: default;" onclick="event.stopPropagation();">
          <!-- Master Sound Effects (Global Mute) Toggle -->
          <div class="setting-row" style="margin-bottom: 12px; border-bottom: 1px dashed var(--border-glass); padding-bottom: 8px;">
            <div>
              <div class="setting-label" style="font-weight: 700;">Master Sound Effects</div>
              <div style="font-size: 9.5px; color: var(--text-muted);">Global Mute / Unmute All SFX</div>
            </div>
            <label class="switch-toggle">
              <input type="checkbox" id="toggle-sfx-master" checked>
              <span class="slider-round"></span>
            </label>
          </div>

          <!-- Volume Slider -->
          <div style="margin-bottom: 16px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label">SFX Volume</span>
              <span id="sfx-volume-percent" style="font-size: 11px; font-weight: 700; color: var(--text-main);">50%</span>
            </div>
            <input type="range" id="sfx-volume-slider" min="0" max="1" step="0.05" value="0.5" style="width: 100%; accent-color: #10b981; cursor: pointer; height: 6px; border-radius: 3px; background: var(--border-glass); border: none; outline: none;">
          </div>

          <div style="font-size: 10px; color: var(--text-muted); margin-bottom: 8px; font-weight: 700; letter-spacing: 0.5px;">PLAY SOUND FOR ACTIONS</div>

          <!-- Option 1: Switching Main Tabs -->
          <div style="margin-bottom: 10px; background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 10px; padding: 8px 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label" style="font-weight: 700;">Switching Main Tabs</span>
              <label class="switch-toggle">
                <input type="checkbox" id="toggle-sfx-tm-tabs" checked>
                <span class="slider-round"></span>
              </label>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 9.5px; color: var(--text-muted);">Sound Effect:</span>
              <select id="select-sfx-tm-tabs" class="sfx-sound-select" style="background: var(--card-bg); color: var(--text-main); border: 1px solid var(--border-glass); border-radius: 6px; padding: 3px 8px; font-size: 10px; outline: none;">
                <option value="pop_button.mp3">pop_button.mp3 (Default)</option>
                <option value="tap_main.mp3">tap_main.mp3</option>
                <option value="tap_button.mp3">tap_button.mp3</option>
                <option value="tap_alternate.mp3">tap_alternate.mp3</option>
                <option value="pop_button_v2.mp3">pop_button_v2.mp3</option>
                <option value="pop_click.mp3">pop_click.mp3</option>
                <option value="pop_unknown_v1.mp3">pop_unknown_v1.mp3</option>
              </select>
            </div>
          </div>

          <!-- Option 2: Action Button Tap -->
          <div style="margin-bottom: 10px; background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 10px; padding: 8px 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label" style="font-weight: 700;">Action Button Tap</span>
              <label class="switch-toggle">
                <input type="checkbox" id="toggle-sfx-ta" checked>
                <span class="slider-round"></span>
              </label>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 9.5px; color: var(--text-muted);">Sound Effect:</span>
              <select id="select-sfx-ta" class="sfx-sound-select" style="background: var(--card-bg); color: var(--text-main); border: 1px solid var(--border-glass); border-radius: 6px; padding: 3px 8px; font-size: 10px; outline: none;">
                <option value="pop_click.mp3">pop_click.mp3 (Default)</option>
                <option value="tap_alternate.mp3">tap_alternate.mp3</option>
                <option value="tap_main.mp3">tap_main.mp3</option>
                <option value="tap_button.mp3">tap_button.mp3</option>
                <option value="pop_button.mp3">pop_button.mp3</option>
                <option value="pop_button_v2.mp3">pop_button_v2.mp3</option>
                <option value="pop_unknown_v1.mp3">pop_unknown_v1.mp3</option>
              </select>
            </div>
          </div>

          <!-- Option 3: Browser Tab Clicks -->
          <div style="margin-bottom: 10px; background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 10px; padding: 8px 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label" style="font-weight: 700;">Browser Tab Clicks</span>
              <label class="switch-toggle">
                <input type="checkbox" id="toggle-sfx-tb-clicks" checked>
                <span class="slider-round"></span>
              </label>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 9.5px; color: var(--text-muted);">Sound Effect:</span>
              <select id="select-sfx-tb-clicks" class="sfx-sound-select" style="background: var(--card-bg); color: var(--text-main); border: 1px solid var(--border-glass); border-radius: 6px; padding: 3px 8px; font-size: 10px; outline: none;">
                <option value="tap_button.mp3">tap_button.mp3 (Default)</option>
                <option value="tap_main.mp3">tap_main.mp3</option>
                <option value="tap_alternate.mp3">tap_alternate.mp3</option>
                <option value="pop_button.mp3">pop_button.mp3</option>
                <option value="pop_button_v2.mp3">pop_button_v2.mp3</option>
                <option value="pop_click.mp3">pop_click.mp3</option>
                <option value="pop_unknown_v1.mp3">pop_unknown_v1.mp3</option>
              </select>
            </div>
          </div>

          <!-- Option 4: Header Row Controls -->
          <div style="margin-bottom: 10px; background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 10px; padding: 8px 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label" style="font-weight: 700;">Header Controls (Reload/Power)</span>
              <label class="switch-toggle">
                <input type="checkbox" id="toggle-sfx-tm-header" checked>
                <span class="slider-round"></span>
              </label>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 9.5px; color: var(--text-muted);">Sound Effect:</span>
              <select id="select-sfx-tm-header" class="sfx-sound-select" style="background: var(--card-bg); color: var(--text-main); border: 1px solid var(--border-glass); border-radius: 6px; padding: 3px 8px; font-size: 10px; outline: none;">
                <option value="tap_main.mp3">tap_main.mp3 (Default)</option>
                <option value="tap_button.mp3">tap_button.mp3</option>
                <option value="tap_alternate.mp3">tap_alternate.mp3</option>
                <option value="pop_button.mp3">pop_button.mp3</option>
                <option value="pop_button_v2.mp3">pop_button_v2.mp3</option>
                <option value="pop_click.mp3">pop_click.mp3</option>
                <option value="pop_unknown_v1.mp3">pop_unknown_v1.mp3</option>
              </select>
            </div>
          </div>

          <!-- Option 5: Close Browser Tab -->
          <div style="margin-bottom: 10px; background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 10px; padding: 8px 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label" style="font-weight: 700;">Close Browser Tab</span>
              <label class="switch-toggle">
                <input type="checkbox" id="toggle-sfx-tb-close" checked>
                <span class="slider-round"></span>
              </label>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 9.5px; color: var(--text-muted);">Sound Effect:</span>
              <select id="select-sfx-tb-close" class="sfx-sound-select" style="background: var(--card-bg); color: var(--text-main); border: 1px solid var(--border-glass); border-radius: 6px; padding: 3px 8px; font-size: 10px; outline: none;">
                <option value="tap_button.mp3">tap_button.mp3 (Default)</option>
                <option value="tap_main.mp3">tap_main.mp3</option>
                <option value="tap_alternate.mp3">tap_alternate.mp3</option>
                <option value="pop_button.mp3">pop_button.mp3</option>
                <option value="pop_button_v2.mp3">pop_button_v2.mp3</option>
                <option value="pop_click.mp3">pop_click.mp3</option>
                <option value="pop_unknown_v1.mp3">pop_unknown_v1.mp3</option>
              </select>
            </div>
          </div>

          <!-- Option 6: Tab Options Actions -->
          <div style="margin-bottom: 10px; background: rgba(255,255,255,0.03); border: 1px solid var(--border-glass); border-radius: 10px; padding: 8px 10px;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
              <span class="setting-label" style="font-weight: 700;">Tab Options Menu Actions</span>
              <label class="switch-toggle">
                <input type="checkbox" id="toggle-sfx-tb-modal" checked>
                <span class="slider-round"></span>
              </label>
            </div>
            <div style="display: flex; align-items: center; justify-content: space-between;">
              <span style="font-size: 9.5px; color: var(--text-muted);">Sound Effect:</span>
              <select id="select-sfx-tb-modal" class="sfx-sound-select" style="background: var(--card-bg); color: var(--text-main); border: 1px solid var(--border-glass); border-radius: 6px; padding: 3px 8px; font-size: 10px; outline: none;">
                <option value="tap_button.mp3">tap_button.mp3 (Default)</option>
                <option value="tap_main.mp3">tap_main.mp3</option>
                <option value="tap_alternate.mp3">tap_alternate.mp3</option>
                <option value="pop_button.mp3">pop_button.mp3</option>
                <option value="pop_button_v2.mp3">pop_button_v2.mp3</option>
                <option value="pop_click.mp3">pop_click.mp3</option>
                <option value="pop_unknown_v1.mp3">pop_unknown_v1.mp3</option>
              </select>
            </div>
          </div>

          <!-- Save SFX Mapping Button -->
          <div style="margin-top: 14px; border-top: 1px dashed var(--border-glass); padding-top: 10px; display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 9.5px; color: var(--text-muted);">Takes effect on next app launch</div>
            <button id="btn-save-sfx-mapping" class="oneui-pill-btn primary" style="font-size: 11px; padding: 6px 14px; font-weight: 700;">💾 Save SFX Mapping</button>
          </div>
        </div>
      </details>

      <!-- Collapsible Advanced Options Card (Minimized by Default) -->
      <details class="m3-card" id="settings-card-advanced" style="cursor: pointer; padding-bottom: 12px;">
        <summary style="font-size: 11px; font-weight: 700; color: var(--text-sub); letter-spacing: 0.5px; user-select: none; outline: none; list-style: none; display: flex; align-items: center; justify-content: space-between;">
          <span>ADVANCED OPTIONS ▼</span>
          <span style="font-size: 10px; opacity: 0.7;">Custom Colors & Features</span>
        </summary>
        
        <div style="margin-top: 12px; border-top: 1px dashed var(--border-glass); padding-top: 10px; cursor: default;" onclick="event.stopPropagation();">
          <!-- Custom Background Color Picker -->
          <div class="setting-row" style="margin-top: 6px;">
            <span class="setting-label">Custom Background</span>
            <div class="color-input-box">
              <input type="color" id="bg-color-picker" value="#ffffff">
              <input type="text" id="bg-color-hex" class="hex-text-input" value="#FFFFFF">
            </div>
          </div>

          <!-- Gradient Color Pickers -->
          <div class="setting-row" style="margin-top: 10px;">
            <span class="setting-label">Gradient Start</span>
            <div class="color-input-box">
              <input type="color" id="gradient-start-picker" value="#A2A9A9">
              <input type="text" id="gradient-start-hex" class="hex-text-input" value="#A2A9A9">
            </div>
          </div>

          <div class="setting-row" style="margin-top: 10px;">
            <span class="setting-label">Gradient End</span>
            <div class="color-input-box">
              <input type="color" id="gradient-end-picker" value="#1B4264">
              <input type="text" id="gradient-end-hex" class="hex-text-input" value="#1B4264">
            </div>
          </div>

          <!-- App & Floating Icon Customizer -->
          <div style="margin-top: 14px; border-top: 1px dashed rgba(255,255,255,0.15); padding-top: 12px; position: relative; z-index: 900;">
            <div style="font-size: 11px; font-weight: 700; color: #10b981; margin-bottom: 8px;">CASPIAN ACTION BUTTON THEME</div>
            
            <div class="setting-row" style="position: relative; z-index: 950;">
              <span class="setting-label">Action Button Icon Shape</span>
              <div class="caspian-select" id="custom-shape-select">
                <div class="caspian-select-trigger">
                  <span id="selected-shape-text">Circle</span>
                  <svg class="chevron-icon" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
                </div>
                <div class="caspian-select-options">
                  <div class="caspian-select-option" data-val="squircle">Squircle (Default iOS/OneUI)</div>
                  <div class="caspian-select-option" data-val="rounded">Rounded Rectangle</div>
                  <div class="caspian-select-option active" data-val="circle">Circle</div>
                  <div class="caspian-select-option" data-val="square">Full Square</div>
                </div>
              </div>
            </div>

            <!-- Preset Palettes -->
            <div style="font-size: 10px; color: #8e8ea0; margin-top: 10px; margin-bottom: 6px;">PRESET THEMES</div>
            <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 12px;">
              <button class="preset-theme-chip" data-start="#A2A9A9" data-end="#1B4264" style="background: linear-gradient(135deg, #A2A9A9, #1B4264); border: 1px solid #ffffff; color: #fff; padding: 4px 8px; border-radius: 6px; font-size: 10px; font-weight: 600; cursor: pointer;">Caspian Classic</button>
              <button class="preset-theme-chip" data-start="#10a37f" data-end="#047857" style="background: linear-gradient(135deg, #10a37f, #047857); border: 1px solid #ffffff; color: #fff; padding: 4px 8px; border-radius: 6px; font-size: 10px; font-weight: 600; cursor: pointer;">ChatGPT Emerald</button>
              <button class="preset-theme-chip" data-start="#2563eb" data-end="#0f172a" style="background: linear-gradient(135deg, #2563eb, #0f172a); border: 1px solid #ffffff; color: #fff; padding: 4px 8px; border-radius: 6px; font-size: 10px; font-weight: 600; cursor: pointer;">Royal Sapphire</button>
              <button class="preset-theme-chip" data-start="#374151" data-end="#111827" style="background: linear-gradient(135deg, #374151, #111827); border: 1px solid #ffffff; color: #fff; padding: 4px 8px; border-radius: 6px; font-size: 10px; font-weight: 600; cursor: pointer;">Midnight Obsidian</button>
              <button class="preset-theme-chip" data-start="#ec4899" data-end="#3b82f6" style="background: linear-gradient(135deg, #ec4899, #3b82f6); border: 1px solid #ffffff; color: #fff; padding: 4px 8px; border-radius: 6px; font-size: 10px; font-weight: 600; cursor: pointer;">Cyberpunk Neon</button>
            </div>

            <!-- EXPORTS Method Customizer Section -->
            <div style="margin-top: 14px; border-top: 1px dashed rgba(255,255,255,0.15); padding-top: 12px; position: relative; z-index: 800;">
              <div style="font-size: 11px; font-weight: 700; color: #10b981; margin-bottom: 8px;">CASPIAN EXPORT CONFIGURATOR</div>
              
              <!-- ChatGPT Settings -->
              <div style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: 8px; padding: 10px; margin-bottom: 10px;">
                <div style="display: flex; align-items: center; gap: 6px; font-size: 10px; font-weight: 700; color: #10a37f; margin-bottom: 8px;">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                  <span>CHATGPT EXPORT ENGINE</span>
                </div>
                
                <div class="setting-row" style="margin-bottom: 8px; position: relative; z-index: 850;">
                  <span class="setting-label">Normal Chats</span>
                  <div class="caspian-select" id="select-export-chatgpt-normal" style="width: 160px;">
                    <div class="caspian-select-trigger" style="padding: 4px 10px;">
                      <span id="selected-export-chatgpt-normal-text" style="font-size: 10px;">Direct Session API</span>
                      <svg class="chevron-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </div>
                    <div class="caspian-select-options">
                      <div class="caspian-select-option active" data-val="api">Direct Session API</div>
                      <div class="caspian-select-option" data-val="fiber">React Fiber State</div>
                      <div class="caspian-select-option" data-val="sweeper">DOM Layout Sweeper</div>
                    </div>
                  </div>
                </div>

                <div class="setting-row" style="position: relative; z-index: 840;">
                  <span class="setting-label">Private/Temp Chats</span>
                  <div class="caspian-select" id="select-export-chatgpt-temp" style="width: 160px;">
                    <div class="caspian-select-trigger" style="padding: 4px 10px;">
                      <span id="selected-export-chatgpt-temp-text" style="font-size: 10px;">React Fiber State</span>
                      <svg class="chevron-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </div>
                    <div class="caspian-select-options">
                      <div class="caspian-select-option active" data-val="fiber">React Fiber State</div>
                      <div class="caspian-select-option" data-val="sweeper">DOM Layout Sweeper</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Gemini Settings -->
              <div style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); border-radius: 8px; padding: 10px; margin-bottom: 12px;">
                <div style="display: flex; align-items: center; gap: 6px; font-size: 10px; font-weight: 700; color: #7c3aed; margin-bottom: 8px;">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"></path></svg>
                  <span>GEMINI EXPORT ENGINE</span>
                </div>
                
                <div class="setting-row" style="margin-bottom: 8px; position: relative; z-index: 830;">
                  <span class="setting-label">Normal Chats</span>
                  <div class="caspian-select" id="select-export-gemini-normal" style="width: 160px;">
                    <div class="caspian-select-trigger" style="padding: 4px 10px;">
                      <span id="selected-export-gemini-normal-text" style="font-size: 10px;">DOM Layout Sweeper</span>
                      <svg class="chevron-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </div>
                    <div class="caspian-select-options">
                      <div class="caspian-select-option active" data-val="sweeper">DOM Layout Sweeper</div>
                      <div class="caspian-select-option" data-val="fiber">React Fiber State</div>
                    </div>
                  </div>
                </div>

                <div class="setting-row" style="position: relative; z-index: 820;">
                  <span class="setting-label">Private/Temp Chats</span>
                  <div class="caspian-select" id="select-export-gemini-temp" style="width: 160px;">
                    <div class="caspian-select-trigger" style="padding: 4px 10px;">
                      <span id="selected-export-gemini-temp-text" style="font-size: 10px;">DOM Layout Sweeper</span>
                      <svg class="chevron-icon" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2.5"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </div>
                    <div class="caspian-select-options">
                      <div class="caspian-select-option active" data-val="sweeper">DOM Layout Sweeper</div>
                      <div class="caspian-select-option" data-val="fiber">React Fiber State</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Live Icon Preview -->
            <div style="display: flex; align-items: center; justify-content: space-between; background: var(--input-bg); padding: 10px 14px; border-radius: 8px; border: 1px solid var(--border-glass);">
              <span id="icon-preview-label" style="font-size: 11px; color: var(--text-sub); font-weight: 600;">Icon Preview</span>
              <div id="icon-preview-box" style="width: 42px; height: 42px; border-radius: 12px; background: linear-gradient(135deg, #A2A9A9, #1B4264); display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(0,0,0,0.3);">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 6c.6.5 1.2 1 2.5 1C7 7 7 5 9.5 5c2.6 0 2.4 2 5 2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"/><path d="M2 12c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.6 0 2.4 2 5 2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"/><path d="M2 18c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.6 0 2.4 2 5 2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"/></svg>
              </div>
            </div>
          </div>
        </div>
      </details>

      <!-- Developer Profile Card -->
      <div class="m3-card">
        <div class="m3-card-title" style="margin-bottom: 12px;">DEVELOPER</div>
        
        <div class="dev-profile-box">
          <div class="dev-avatar-container">
            <img src="{dev_avatar_b64}" class="dev-avatar-img" alt="NigelWeb" />
          </div>
          <div class="dev-info-col">
            <div class="dev-name">NigelWeb</div>
            <div class="dev-role">Lead Architect of Lsync & Caspian</div>
            <a href="https://github.com/code4nigel" target="_blank" class="dev-github-link">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>
              <span>github.com/code4nigel</span>
            </a>
          </div>
        </div>

        <!-- Interactive Nigel Facts Box (Tap 7 Times Rapidly to Unlock Developer Options) -->
        <div id="nigel-fact-card" class="nigel-fact-box" title="Tap 7 times rapidly to unlock Developer Options!">
          <div class="nigel-fact-title">&#128161; Click here for a Nigel Fact!</div>
          <div id="nigel-fact-text" class="nigel-fact-body">
            Legend has it Nigel spent his time building Caspian instead of studying for his End-Sem exams or preparing for company placement interviews tomorrow... Absolute madman! &#128128;
          </div>
        </div>

        <!-- Hidden Developer Options Card (Placed BELOW Nigel Facts Card & Re-lockable by 7 Taps on UNLOCKED) -->
        <div id="developer-options-card" class="m3-card" style="display: none; border: 1.5px solid #a855f7; background: rgba(168, 85, 247, 0.04); margin-top: 12px;">
          <div class="m3-card-row" style="margin-bottom: 6px;">
            <div style="display: flex; align-items: center; gap: 6px;">
              <span style="font-size: 14px;">&#128736;</span>
              <div class="m3-card-title" style="color: #9333ea;">DEVELOPER OPTIONS</div>
            </div>
            <span id="dev-unlocked-badge" class="m3-badge" style="background: #a855f7; color: #fff; cursor: pointer;" title="Tap 7 times rapidly to re-lock Developer Options">UNLOCKED</span>
          </div>
          <div class="m3-card-row" style="margin-top: 8px;">
            <div class="m3-card-left">
              <div id="debug-rec-dot" class="status-dot"></div>
              <div>
                <div id="debug-rec-title" class="m3-card-title">Console & System Logger</div>
                <div id="debug-rec-sub" class="m3-card-sub">Record console errors, network events & app diagnostics to file.</div>
              </div>
            </div>
            <button id="debug-rec-toggle-btn" class="oneui-pill-btn secondary" style="font-size: 11px; padding: 4px 12px; border-color: #a855f7; color: #9333ea;">Start Rec</button>
          </div>

          <!-- Developer Exclusive Themes Section -->
          <div style="margin-top: 10px; border-top: 1px dashed rgba(168, 85, 247, 0.3); padding-top: 10px;">
            <div style="font-size: 10px; font-weight: 800; color: #a855f7; letter-spacing: 0.5px; margin-bottom: 6px;">
              👑 DEVELOPER EXCLUSIVE THEMES
            </div>
            <div style="display: flex; gap: 6px; flex-wrap: wrap;">
              <button class="preset-theme-chip dev-exclusive-theme" data-start="#7c3aed" data-end="#1e1b4b" style="background: linear-gradient(135deg, #7c3aed, #1e1b4b); border: 1.5px solid #a855f7; color: #fff; padding: 6px 12px; border-radius: 8px; font-size: 10.5px; font-weight: 700; cursor: pointer; box-shadow: 0 0 12px rgba(168,85,247,0.4);">
                ✨ Cosmic Gemini (Developer Exclusive)
              </button>
              <button class="preset-theme-chip dev-exclusive-theme" data-start="#fbbf24" data-end="#78350f" style="background: linear-gradient(135deg, #fbbf24, #78350f); border: 1.5px solid #fbbf24; color: #fff; padding: 6px 12px; border-radius: 8px; font-size: 10.5px; font-weight: 700; cursor: pointer; box-shadow: 0 0 12px rgba(251,191,36,0.4);">
                👑 Ena Shine (Golden Premium)
              </button>
            </div>
          </div>
        </div>

      </div>

    </div>

  </div>

  <!-- Tab Options Menu Modal (Global Overlay - Out of Bottom Sheet) -->
  <div id="tab-options-modal" class="modal-overlay" style="display: none; z-index: 20000000;">
    <div class="modal-card" style="width: 100%; max-width: 350px; background: var(--card-bg); border: 1px solid var(--border-glass); border-radius: 20px; padding: 20px; box-shadow: 0 16px 40px rgba(0,0,0,0.5);">
      <div class="modal-title" style="font-size: 14px; font-weight: 800; color: var(--text-main); margin-bottom: 14px;">Tab Options Menu</div>
      
      <div style="margin-bottom: 14px;">
        <label style="font-size: 11px; font-weight: 700; color: var(--text-muted); display: block; margin-bottom: 6px;">Nickname</label>
        <div style="display: flex; gap: 8px; align-items: center;">
          <input type="text" id="tab-nickname-input" class="m3-text-input" placeholder="Give this tab a nice name..." style="flex: 1; min-width: 0; box-sizing: border-box; background: var(--input-bg); border: 1px solid var(--border-glass); border-radius: 12px; padding: 10px; color: var(--text-main); font-size: 12px; font-weight: 600;">
          <button id="modal-clear-nickname-btn" class="oneui-pill-btn secondary icon-only" style="width: 38px; height: 38px; border-radius: 12px; font-weight: 800; font-size: 16px;" title="Clear Nickname">&times;</button>
        </div>
      </div>
      
      <div style="margin-bottom: 14px;">
        <label style="font-size: 11px; font-weight: 700; color: var(--text-muted); display: block; margin-bottom: 6px;">Full Link</label>
        <div style="display: flex; gap: 8px; align-items: center;">
          <input type="text" id="tab-url-display" class="m3-text-input" style="flex: 1; min-width: 0; background: var(--input-bg); border: 1px solid var(--border-glass); border-radius: 12px; padding: 10px; color: var(--text-main); font-size: 11px; font-weight: 600;">
          <button id="modal-copy-url-btn" class="oneui-pill-btn secondary icon-only" style="width: 38px; height: 38px; border-radius: 12px;" title="Copy Link">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>
          </button>
        </div>
      </div>
      
      <div id="modal-group-actions-row" style="margin-bottom: 14px; display: none;">
        <button id="modal-leave-group-btn" class="oneui-pill-btn secondary" style="width: 100%; justify-content: center; font-size: 11px; padding: 9px 12px; color: var(--text-main); border: 1px solid var(--border-glass); font-weight: 700; border-radius: 12px; display: flex; align-items: center; gap: 6px;">
          📤 Move Out of Group (Ungroup Tab)
        </button>
      </div>

      <div style="margin-top: 20px; display: flex; gap: 10px; align-items: center; justify-content: space-between;">
         <button id="modal-favorite-btn" class="oneui-pill-btn secondary" style="padding: 8px 14px; border-radius: 12px; font-weight: 700; font-size: 11px; display: flex; align-items: center; gap: 4px; border: 1px solid var(--border-glass);" title="Favorite Tab (Protects from Close All)">
           <span id="fav-star-icon">⭐</span>
           <span id="fav-star-text">Favorite</span>
         </button>

         <div style="display: flex; gap: 10px;">
           <button id="modal-cancel-btn" class="oneui-pill-btn secondary" style="padding: 8px 16px; border-radius: 12px;">Cancel</button>
           <button id="modal-save-btn" class="oneui-pill-btn primary" style="padding: 8px 16px; border-radius: 12px;">Save</button>
         </div>
      </div>
    </div>
  </div>

  <!-- Group Options Menu Modal (Separate Top-Level Overlay) -->
  <div id="group-options-modal" class="modal-overlay" style="display: none; z-index: 20000005;">
    <div class="modal-card" style="width: 100%; max-width: 350px; background: var(--card-bg); border: 1px solid var(--border-glass); border-radius: 20px; padding: 20px; box-shadow: 0 16px 40px rgba(0,0,0,0.5);">
      <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-glass); padding-bottom: 10px; margin-bottom: 14px;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <span id="group-modal-color-dot" style="width: 14px; height: 14px; border-radius: 50%; background: #3b82f6; display: inline-block;"></span>
          <span id="group-modal-header-title" class="modal-title" style="font-size: 14px; font-weight: 800; color: var(--text-main);">Group Options</span>
        </div>
        <button id="group-modal-close-x" class="modal-close-btn" style="background: none; border: none; font-size: 20px; color: var(--text-sub); cursor: pointer;">&times;</button>
      </div>

      <div style="margin-bottom: 14px;">
        <label style="font-size: 11px; font-weight: 700; color: var(--text-muted); display: block; margin-bottom: 6px;">GROUP TITLE</label>
        <input type="text" id="group-modal-title-input" class="m3-text-input" placeholder="Group Name..." style="width: 100%; box-sizing: border-box; background: var(--input-bg); border: 1px solid var(--border-glass); border-radius: 12px; padding: 10px 12px; color: var(--text-main); font-size: 12px; font-weight: 600;">
      </div>

      <div style="margin-bottom: 14px;">
        <label style="font-size: 11px; font-weight: 700; color: var(--text-muted); display: block; margin-bottom: 6px;">GROUP EMOJI ICON</label>
        <div id="group-modal-emoji-palette" style="display: flex; gap: 8px; flex-wrap: wrap;">
          <div class="modal-group-emoji-dot active" data-emoji="📁" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">📁</div>
          <div class="modal-group-emoji-dot" data-emoji="🚀" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">🚀</div>
          <div class="modal-group-emoji-dot" data-emoji="🔥" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">🔥</div>
          <div class="modal-group-emoji-dot" data-emoji="⭐" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">⭐</div>
          <div class="modal-group-emoji-dot" data-emoji="🎨" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">🎨</div>
          <div class="modal-group-emoji-dot" data-emoji="📚" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">📚</div>
          <div class="modal-group-emoji-dot" data-emoji="🎮" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">🎮</div>
          <div class="modal-group-emoji-dot" data-emoji="💡" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">💡</div>
          <div class="modal-group-emoji-dot" data-emoji="💼" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">💼</div>
          <div class="modal-group-emoji-dot" data-emoji="⚡" style="font-size: 18px; width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: var(--input-bg); border: 1px solid var(--border-glass);">⚡</div>
        </div>
      </div>

      <div style="margin-bottom: 16px;">
        <label style="font-size: 11px; font-weight: 700; color: var(--text-muted); display: block; margin-bottom: 8px;">GROUP COLOR ACCENT</label>
        <div id="group-modal-color-palette" class="color-palette-grid" style="display: flex; gap: 8px; justify-content: space-between;">
          <div class="modal-group-color-dot active" data-color="#ef4444" style="background: #ef4444; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#f97316" style="background: #f97316; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#eab308" style="background: #eab308; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#10b981" style="background: #10b981; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#06b6d4" style="background: #06b6d4; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#3b82f6" style="background: #3b82f6; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#8b5cf6" style="background: #8b5cf6; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
          <div class="modal-group-color-dot" data-color="#ec4899" style="background: #ec4899; width: 26px; height: 26px; border-radius: 50%; cursor: pointer;"></div>
        </div>
      </div>

      <div style="margin-bottom: 18px; display: flex; flex-direction: column; gap: 8px;">
        <button id="group-modal-ungroup-btn" class="oneui-pill-btn secondary" style="width: 100%; justify-content: center; font-size: 11px; padding: 10px;">📂 Leave Group (Ungroup Tabs)</button>
        <button id="group-modal-delete-btn" class="oneui-pill-btn danger" style="width: 100%; justify-content: center; font-size: 11px; padding: 10px; color: #ef4444; border-color: rgba(239,68,68,0.3);">🗑️ Delete Group & Close All Tabs</button>
      </div>

      <div style="display: flex; gap: 10px; align-items: center; justify-content: space-between;">
         <button id="group-modal-favorite-btn" class="oneui-pill-btn secondary" style="padding: 8px 14px; border-radius: 12px; font-weight: 700; font-size: 11px; display: flex; align-items: center; gap: 4px; border: 1px solid var(--border-glass);" title="Favorite Group (Protects from Close All)">
           <span id="group-fav-star-icon">⭐</span>
           <span id="group-fav-star-text">Favorite</span>
         </button>

         <div style="display: flex; gap: 10px;">
           <button id="group-modal-cancel-btn" class="oneui-pill-btn secondary" style="padding: 8px 16px; border-radius: 12px;">Cancel</button>
           <button id="group-modal-save-btn" class="oneui-pill-btn primary" style="padding: 8px 16px; border-radius: 12px;">Save</button>
         </div>
      </div>
    </div>
  </div>

  <!-- Tab Undo Toast Container (Separate Top-Level Toast) -->
  <div id="undo-toast-container" class="undo-toast" style="display: none;">
    <div class="undo-toast-content">
      <span>Tab closed</span>
      <button id="undo-toast-btn" class="oneui-pill-btn primary" style="padding: 4px 10px; font-size: 11px; margin-left: 12px; border-radius: 8px;">Undo</button>
    </div>
    <button id="close-undo-btn" style="background:none; border:none; color:var(--text-muted); font-size:16px; margin-left:8px; cursor:pointer;">&times;</button>
  </div>

  <script>
    window.GPT_ICON_B64 = "{GPT_ICON_B64}";
    window.GEMINI_ICON_B64 = "{GEMINI_ICON_B64}";
    window.GOOGLE_ICON_B64 = "{GOOGLE_ICON_B64}";
    window.YOUTUBE_ICON_B64 = "{YOUTUBE_ICON_B64}";
  </script>
  <script src="mobile_control.js"></script>
</body>
</html>
'''

# Write to each project track with its own dynamic versionName
projects = [
    'd:/Projects/Chatgpt Pruner/Caspian-Android',
    'd:/Projects/Chatgpt Pruner/Caspian-Beta-A'
]

for pdir in projects:
    vname = "1.0.69"
    gpath = os.path.join(pdir, 'app', 'build.gradle.kts')
    if os.path.exists(gpath):
        try:
            with open(gpath, 'r', encoding='utf-8') as gf:
                m = re.search(r'versionName\s*=\s*"([^"]+)"', gf.read())
                if m:
                    vname = m.group(1)
        except Exception as e:
            pass
    html_out = build_html(vname)
    out_file = os.path.join(pdir, 'assets', 'mobile_control.html')
    with open(out_file, 'w', encoding='utf-8') as out:
        out.write(html_out)

print("Successfully updated assets/mobile_control.html for both tracks with track-specific versionName!")
