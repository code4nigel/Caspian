import os
import struct
import zlib

def make_png(width, height, color_rgb):
    r, g, b = color_rgb
    raw_data = bytearray()
    for y in range(height):
        raw_data.append(0) # filter type none
        for x in range(width):
            # draw circular boundary
            cx, cy = width / 2.0, height / 2.0
            dist = ((x - cx)**2 + (y - cy)**2)**0.5
            if dist <= width * 0.45:
                raw_data.extend([r, g, b, 255])
            else:
                raw_data.extend([0, 0, 0, 0])

    def chunk(tag, data):
        return struct.pack('>I', len(data)) + tag + data + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)

    header = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', header) + chunk(b'IDAT', zlib.compress(raw_data)) + chunk(b'IEND', b'')
    return png

dirs = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

base_path = os.path.join('d:\\Projects\\Chatgpt Pruner\\Caspian-Android', 'app', 'src', 'main', 'res')

for folder, size in dirs.items():
    dir_path = os.path.join(base_path, folder)
    os.makedirs(dir_path, exist_ok=True)
    png_bytes = make_png(size, size, (27, 66, 100)) # Caspian #1B4264
    with open(os.path.join(dir_path, 'ic_launcher.png'), 'wb') as f:
        f.write(png_bytes)
    with open(os.path.join(dir_path, 'ic_launcher_round.png'), 'wb') as f:
        f.write(png_bytes)

print("Generated PNG mipmap icons successfully!")
