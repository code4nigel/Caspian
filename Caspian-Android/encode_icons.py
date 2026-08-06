import base64

def get_b64(path):
    with open(path, 'rb') as f:
        return 'data:image/png;base64,' + base64.b64encode(f.read()).decode('utf-8')

gpt_b64 = get_b64('d:/Projects/Chatgpt Pruner/Caspian-Android/icon images/chatgpt-icon.png')
gemini_b64 = get_b64('d:/Projects/Chatgpt Pruner/Caspian-Android/icon images/google-gemini-icon.png')

with open('d:/Projects/Chatgpt Pruner/Caspian-Android/assets/site_icons.py', 'w') as f:
    f.write(f"GPT_ICON_B64 = '{gpt_b64}'\n")
    f.write(f"GEMINI_ICON_B64 = '{gemini_b64}'\n")

print("Successfully encoded site icons!")
