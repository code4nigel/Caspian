import os
import shutil

src_icon = os.path.join('d:\\Projects\\Chatgpt Pruner\\Caspian', 'icon128.png')
res_dir = os.path.join('d:\\Projects\\Chatgpt Pruner\\Caspian-Android', 'app', 'src', 'main', 'res')

dirs = ['mipmap-mdpi', 'mipmap-hdpi', 'mipmap-xhdpi', 'mipmap-xxhdpi', 'mipmap-xxxhdpi']

for folder in dirs:
    target_folder = os.path.join(res_dir, folder)
    os.makedirs(target_folder, exist_ok=True)
    
    launcher_path = os.path.join(target_folder, 'ic_launcher.png')
    round_path = os.path.join(target_folder, 'ic_launcher_round.png')
    
    shutil.copyfile(src_icon, launcher_path)
    shutil.copyfile(src_icon, round_path)

print("Successfully copied official Caspian extension icon (Image 4) to all Android mipmap directories!")
