import os

with open('d:/Projects/Chatgpt Pruner/Caspian-Android/assets/dev_avatar.txt', 'r') as f:
    dev_avatar_b64 = f.read().strip()

html_content = f'''<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Caspian Android Control Center</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@500;700&family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="mobile_control.css">
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
          <span class="sheet-brand-tag">V1.0.13 (NATIVE)</span>
        </div>
      </div>
      <div class="header-icon-actions">
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

    <!-- Tab Navigation Bar (Engine, Sites, Settings) -->
    <div class="mobile-tab-nav">
      <button id="tab-btn-engine" class="tab-nav-btn active" data-tab="engine">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M2 6c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path><path d="M2 12c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path><path d="M2 18c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"></path></svg>
        <span>Engine</span>
      </button>
      <button id="tab-btn-sites" class="tab-nav-btn" data-tab="sites">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1 4-10z"></path></svg>
        <span>Sites</span>
      </button>
      <button id="tab-btn-settings" class="tab-nav-btn" data-tab="settings">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
        <span>Settings</span>
      </button>
    </div>

    <!-- TAB 1: ENGINE TAB -->
    <div id="tab-pane-engine" class="tab-pane active">
      <!-- Chat Pruning Card -->
      <div class="m3-card">
        <div class="m3-card-row">
          <div class="m3-card-left">
            <div id="status-dot" class="status-dot active"></div>
            <div>
              <div id="status-title" class="m3-card-title">Chat Pruning Active</div>
              <div id="status-sub" class="m3-card-sub">Lag Fixer & DOM Limit Active</div>
            </div>
          </div>
          <span id="active-limit-badge" class="m3-badge">5 Turns</span>
        </div>
      </div>

      <!-- Temporary Chat Saver Card -->
      <div class="m3-card">
        <div class="m3-card-row">
          <div class="m3-card-left">
            <div id="temp-dot" class="status-dot active"></div>
            <div>
              <div class="m3-card-title">Temporary Chat Saver</div>
              <div id="temp-sub" class="m3-card-sub">Save or convert temporary mobile sessions anytime.</div>
            </div>
          </div>
        </div>

        <!-- Clean Action Row Buttons -->
        <div class="mobile-action-row">
          <button id="convert-btn" class="oneui-pill-btn primary">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 1l4 4-4 4"></path><path d="M3 11V9a4 4 0 0 1 4-4h14"></path><path d="M7 23l-4-4 4-4"></path><path d="M21 13v2a4 4 0 0 1-4 4H3"></path></svg>
            <span>Convert Chat</span>
          </button>
          
          <div style="flex: 1; position: relative;">
            <button id="export-dropdown-trigger" class="oneui-pill-btn secondary" style="width: 100%;">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
              <span>Export ▾</span>
            </button>
            
            <div id="export-menu" class="export-menu-overlay">
              <button class="export-opt-btn" data-fmt="md">Markdown (.md)</button>
              <button class="export-opt-btn" data-fmt="txt">Plain Text (.txt)</button>
              <button class="export-opt-btn" data-fmt="doc">Google Doc (.doc)</button>
              <button class="export-opt-btn" data-fmt="nativepdf">Document PDF (.pdf)</button>
              <button class="export-opt-btn" data-fmt="styledpdf">Caspian PDF Format</button>
            </div>
          </div>

          <button id="copy-btn" class="oneui-pill-btn icon-only" title="Copy Transcript">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
            </svg>
          </button>
        </div>
      </div>

      <!-- Visible Message Turns Section -->
      <div class="limit-section-title">
        <span>Visible Message Turns</span>
        <span>Auto-Prune Older</span>
      </div>
      <div class="pill-grid">
        <button class="limit-pill" data-val="1">1</button>
        <button class="limit-pill" data-val="3">3</button>
        <button class="limit-pill active" data-val="5">5</button>
        <button class="limit-pill" data-val="8">8</button>
        <button class="limit-pill" data-val="15">15</button>
        <button class="limit-pill" data-val="9999" title="Unlimited (Show All)">∞</button>
      </div>
    </div>

    <!-- TAB 2: SITES TAB -->
    <div id="tab-pane-sites" class="tab-pane" style="display: none;">
      <div class="m3-card">
        <div class="m3-card-title" style="margin-bottom: 10px;">Select Active AI Platform</div>
        
        <div class="service-pill-bar">
          <button id="switch-hub-btn" class="service-pill" style="flex: 0.8;">Hub</button>
          <button id="switch-chatgpt-btn" class="service-pill active">ChatGPT</button>
          <button id="switch-gemini-btn" class="service-pill">Google Gemini</button>
        </div>

        <div class="site-row" style="margin-top: 14px;">
          <span>ChatGPT (chatgpt.com)</span>
          <span class="m3-badge" style="background: rgba(16,185,129,0.15); color: #10b981;">Active</span>
        </div>
        <div class="site-row" style="margin-top: 10px;">
          <span>Google Gemini (gemini.google.com)</span>
          <span class="m3-badge" style="background: rgba(16,185,129,0.15); color: #10b981;">Active</span>
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
            <button id="theme-btn-dark" class="mode-pill active">Dark</button>
            <button id="theme-btn-light" class="mode-pill">Light</button>
          </div>
        </div>

        <!-- Background Color Presets -->
        <div class="setting-section-header" style="margin-top: 14px;">Background Tone</div>
        <div class="preset-pill-grid" style="margin-top: 6px;">
          <button class="bg-preset-btn active" data-bg="#050811"><span class="color-dot" style="background: #050811; border: 1px solid #333;"></span> OLED Black</button>
          <button class="bg-preset-btn" data-bg="#000000"><span class="color-dot" style="background: #000000;"></span> Pitch Black</button>
          <button class="bg-preset-btn" data-bg="#0a1128"><span class="color-dot" style="background: #0a1128;"></span> Bluish Dark</button>
          <button class="bg-preset-btn" data-bg="#ffffff"><span class="color-dot" style="background: #ffffff; border: 1px solid #ccc;"></span> Pure White</button>
        </div>

        <!-- Background Color Picker -->
        <div class="setting-row" style="margin-top: 10px;">
          <span class="setting-label">Custom Background</span>
          <div class="color-input-box">
            <input type="color" id="bg-color-picker" value="#050811">
            <input type="text" id="bg-color-hex" class="hex-text-input" value="#050811">
          </div>
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

        <!-- Gradient Color Pickers -->
        <div class="setting-row" style="margin-top: 14px;">
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
      </div>

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
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/></svg>
              <span>github.com/code4nigel</span>
            </a>
          </div>
        </div>

        <!-- Interactive Nigel Facts Box -->
        <div id="nigel-fact-card" class="nigel-fact-box" title="Tap for another Nigel fact!">
          <div class="nigel-fact-title">💡 Click here for a Nigel Fact!</div>
          <div id="nigel-fact-text" class="nigel-fact-body">
            Legend has it Nigel spent his time building Caspian instead of studying for his End-Sem exams or preparing for company placement interviews tomorrow... Absolute madman! 💀
          </div>
        </div>

      </div>

    </div>

  </div>

  <script src="mobile_control.js"></script>
</body>
</html>
'''

with open('d:/Projects/Chatgpt Pruner/Caspian-Android/assets/mobile_control.html', 'w', encoding='utf-8') as out:
    out.write(html_content)

print("Successfully updated mobile_control.html with Native Overlay configuration!")
