import base64

with open('d:/Projects/Chatgpt Pruner/Caspian/developer.png', 'rb') as f:
    b64 = base64.b64encode(f.read()).decode('utf-8')

data_uri = f"data:image/png;base64,{b64}"

with open('d:/Projects/Chatgpt Pruner/Caspian-Android/assets/dev_avatar.txt', 'w') as out:
    out.write(data_uri)

print(f"Successfully generated dev avatar Data URI! Length: {len(data_uri)}")
