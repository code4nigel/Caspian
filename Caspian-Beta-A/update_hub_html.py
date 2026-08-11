import os
from assets.site_icons import GPT_ICON_B64, GEMINI_ICON_B64, GOOGLE_ICON_B64, YOUTUBE_ICON_B64

html_content = f'''<!DOCTYPE html>
<html lang="en" data-theme="light">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>Caspian Mobile Hub</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@500;700&family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {{
      --accent: #A2A9A9;
      --secondary: #1B4264;
      --accent-glow: rgba(162, 169, 169, 0.35);
      --accent-gradient: linear-gradient(135deg, var(--accent), var(--secondary));
      --bg-deep: #f8fafc;
      --bg-card: #ffffff;
      --border-glass: rgba(0, 0, 0, 0.08);
      --text-main: #0f172a;
      --text-muted: #64748b;
    }}
    [data-theme="dark"] {{
      --bg-deep: #050811;
      --bg-card: #121824;
      --border-glass: rgba(255, 255, 255, 0.08);
      --text-main: #f8fafc;
      --text-muted: #94a3b8;
    }}
    [data-theme="dark"] .brand-title {{
      color: #ffffff;
    }}
    html[data-theme="dark"] body {{
      background: linear-gradient(135deg, #090d16 0%, #03050a 100%);
    }}
    html[data-theme="dark"] .hub-container {{
      background: rgba(18, 24, 36, 0.9);
      box-shadow: 0 16px 40px rgba(0, 0, 0, 0.4);
    }}
    * {{ box-sizing: border-box; margin: 0; padding: 0; user-select: none; }}
    body {{
      font-family: 'Outfit', sans-serif;
      background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
      color: var(--text-main);
      height: 100vh;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 24px;
    }}
    .hub-container {{
      width: 100%;
      max-width: 400px;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(20px);
      border: 1px solid var(--border-glass);
      border-radius: 28px;
      padding: 32px 24px;
      box-shadow: 0 16px 40px rgba(0, 0, 0, 0.08);
      text-align: center;
    }}
    .brand-logo {{
      width: 60px;
      height: 60px;
      border-radius: 18px;
      background: var(--accent-gradient);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px auto;
      box-shadow: 0 8px 24px var(--accent-glow);
    }}
    .brand-title {{
      font-size: 22px;
      font-weight: 800;
      letter-spacing: 2px;
      color: #1B4264;
    }}
    .brand-sub {{
      font-size: 11px;
      color: var(--text-muted);
      margin-top: 4px;
      margin-bottom: 28px;
      letter-spacing: 1px;
    }}
    .horizontal-grid {{
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 14px;
    }}
    .app-icon-card {{
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 20px 12px;
      background: var(--bg-card);
      border: 1.5px solid var(--border-glass);
      border-radius: 20px;
      cursor: pointer;
      box-shadow: 0 4px 12px rgba(0,0,0,0.04);
      transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
    }}
    .app-icon-card:active {{
      transform: scale(0.94);
    }}
    .app-icon-img {{
      width: 48px;
      height: 48px;
      border-radius: 14px;
      object-fit: cover;
      margin-bottom: 10px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.08);
    }}
    .app-icon-title {{
      font-size: 14px;
      font-weight: 700;
      color: var(--text-main);
      margin-bottom: 2px;
    }}
    .app-icon-domain {{
      font-size: 10px;
      color: var(--text-muted);
    }}
  </style>
</head>
<body>

  <div class="hub-container">
    <div class="brand-logo">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M4 7c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
        <path d="M4 12c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
        <path d="M4 17c1.2.8 2.8 1.3 5 1.3 3.2 0 3.8-2 7-2 1.8 0 2.8.5 4 1.3"></path>
      </svg>
    </div>

    <div class="brand-title">CASPIAN MOBILE</div>
    <div class="brand-sub">SELECT AI PLATFORM</div>

    <div class="horizontal-grid">
      <div id="launch-chatgpt" class="app-icon-card">
        <img src="{GPT_ICON_B64}" class="app-icon-img" alt="ChatGPT" />
        <div class="app-icon-title">ChatGPT</div>
        <div class="app-icon-domain">chatgpt.com</div>
      </div>

      <div id="launch-gemini" class="app-icon-card">
        <img src="{GEMINI_ICON_B64}" class="app-icon-img" alt="Google Gemini" />
        <div class="app-icon-title">Google Gemini</div>
        <div class="app-icon-domain">gemini.google.com</div>
      </div>

      <div id="launch-google" class="app-icon-card">
        <img src="{GOOGLE_ICON_B64}" class="app-icon-img" alt="Google Search" />
        <div class="app-icon-title">Google Search</div>
        <div class="app-icon-domain">google.com</div>
      </div>

      <div id="launch-youtube" class="app-icon-card">
        <img src="{YOUTUBE_ICON_B64}" class="app-icon-img" alt="YouTube" style="transform: scale(1.45);" />
        <div class="app-icon-title">YouTube</div>
        <div class="app-icon-domain">youtube.com</div>
      </div>
    </div>
  </div>

  <script>
    function syncTheme() {{
      try {{
        if (window.CaspianBridge && typeof window.CaspianBridge.getSettings === 'function') {{
          const prefs = JSON.parse(window.CaspianBridge.getSettings());
          const isDark = prefs.themeMode === 'dark';
          document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
        }}
      }} catch(e) {{}}
    }}
    syncTheme();
    setInterval(syncTheme, 1000);

    document.getElementById('launch-chatgpt').addEventListener('click', () => {{
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {{
        window.CaspianBridge.switchService('chatgpt');
      }} else {{
        window.location.href = 'https://chatgpt.com/';
      }}
    }});

    document.getElementById('launch-gemini').addEventListener('click', () => {{
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {{
        window.CaspianBridge.switchService('gemini');
      }} else {{
        window.location.href = 'https://gemini.google.com/';
      }}
    }});

    document.getElementById('launch-google').addEventListener('click', () => {{
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {{
        window.CaspianBridge.switchService('google');
      }} else {{
        window.location.href = 'https://www.google.com/';
      }}
    }});

    document.getElementById('launch-youtube').addEventListener('click', () => {{
      if (window.CaspianBridge && typeof window.CaspianBridge.switchService === 'function') {{
        window.CaspianBridge.switchService('youtube');
      }} else {{
        window.location.href = 'https://www.youtube.com/';
      }}
    }});
  </script>
</body>
</html>
'''

# Write to both Caspian-Android and Caspian-Beta-A
paths = [
    'd:/Projects/Chatgpt Pruner/Caspian-Android/assets/launch_hub.html',
    'd:/Projects/Chatgpt Pruner/Caspian-Beta-A/assets/launch_hub.html'
]

for p in paths:
    with open(p, 'w', encoding='utf-8') as out:
        out.write(html_content)

print("Successfully updated launch_hub.html for both tracks!")
