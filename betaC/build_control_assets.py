import os
import re

def update_html(version_name):
    html_path = os.path.join(os.path.dirname(__file__), 'app', 'src', 'main', 'assets', 'browser_control.html')
    with open(html_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Update sheet brand tag
    content = re.sub(r'<span class="sheet-brand-tag">V[^<]+</span>', f'<span class="sheet-brand-tag">V{version_name}</span>', content)
    # Update updater card sub
    content = re.sub(r'<div id="updater-status-sub" class="m3-card-sub">Current: V[^•]+•', f'<div id="updater-status-sub" class="m3-card-sub">Current: V{version_name} •', content)

    with open(html_path, 'w', encoding='utf-8') as f:
        f.write(content)

gradle_path = os.path.join(os.path.dirname(__file__), 'app', 'build.gradle.kts')
vname = '1.1.87-BetaC'
if os.path.exists(gradle_path):
    with open(gradle_path, 'r', encoding='utf-8') as gf:
        m = re.search(r'versionName\s*=\s*"([^"]+)"', gf.read())
        if m:
            vname = m.group(1)

update_html(vname)
print(f"Successfully updated browser_control.html for Caspian Flow (version: {vname})!")
