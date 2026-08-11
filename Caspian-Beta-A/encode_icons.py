import base64

def get_b64(path):
    with open(path, 'rb') as f:
        return 'data:image/png;base64,' + base64.b64encode(f.read()).decode('utf-8')

gpt_b64 = get_b64('d:/Projects/Chatgpt Pruner/Caspian-Android/icon images/chatgpt-icon.png')
gemini_b64 = get_b64('d:/Projects/Chatgpt Pruner/Caspian-Android/icon images/google-gemini-icon.png')
google_b64 = get_b64('d:/Projects/Chatgpt Pruner/Caspian-Android/icon images/google.png')
youtube_b64 = get_b64('d:/Projects/Chatgpt Pruner/Caspian-Android/icon images/youtube2.png')

# Write to both Caspian-Android and Caspian-Beta-A
paths = [
    'd:/Projects/Chatgpt Pruner/Caspian-Android/assets/site_icons.py',
    'd:/Projects/Chatgpt Pruner/Caspian-Beta-A/assets/site_icons.py'
]

for p in paths:
    with open(p, 'w') as f:
        f.write(f"GPT_ICON_B64 = '{gpt_b64}'\n")
        f.write(f"GEMINI_ICON_B64 = '{gemini_b64}'\n")
        f.write(f"GOOGLE_ICON_B64 = '{google_b64}'\n")
        f.write(f"YOUTUBE_ICON_B64 = '{youtube_b64}'\n")

print("Successfully encoded site icons for both tracks!")
