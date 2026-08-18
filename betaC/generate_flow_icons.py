import os
import math
from PIL import Image, ImageDraw

def create_caspian_flow_icon(size):
    # Create RGBA image with high resolution supersampling
    scale = 4
    canvas_size = size * scale
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Base Squircle / Rounded Rectangle
    corner_radius = int(canvas_size * 0.22)
    
    # Draw Background Gradient (Electric Cyan #00B4D8 to Deep Sapphire Ocean #03045E)
    # Start: (0, 180, 216) -> End: (3, 4, 94)
    start_color = (0, 180, 216)
    end_color = (3, 4, 94)

    # Render gradient mask
    mask = Image.new("L", (canvas_size, canvas_size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([0, 0, canvas_size, canvas_size], radius=corner_radius, fill=255)

    grad_img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    for y in range(canvas_size):
        r = int(start_color[0] + (end_color[0] - start_color[0]) * (y / canvas_size))
        g = int(start_color[1] + (end_color[1] - start_color[1]) * (y / canvas_size))
        b = int(start_color[2] + (end_color[2] - start_color[2]) * (y / canvas_size))
        draw_line = ImageDraw.Draw(grad_img)
        draw_line.line([(0, y), (canvas_size, y)], fill=(r, g, b, 255))

    # Apply rounded squircle mask to gradient
    img.paste(grad_img, (0, 0), mask)

    # 2. Draw Subtle Inner Ambient Glow / Border
    draw_glow = ImageDraw.Draw(img)
    draw_glow.rounded_rectangle([0, 0, canvas_size - 1, canvas_size - 1], radius=corner_radius, outline=(255, 255, 255, 45), width=int(scale * 1.5))

    # 3. Draw Caspian Fluid Ribbon Wave
    wave_points_upper = []
    wave_points_lower = []
    
    # S-curve ribbon parameters
    wave_thickness = canvas_size * 0.16
    mid_y = canvas_size * 0.50

    steps = 200
    for i in range(steps + 1):
        x = (canvas_size * i) / steps
        # Sine wave progression
        angle = (i / steps) * math.pi * 2.0
        y_offset = math.sin(angle) * (canvas_size * 0.14)
        
        wave_points_upper.append((x, mid_y + y_offset - wave_thickness / 2))
        wave_points_lower.append((x, mid_y + y_offset + wave_thickness / 2))

    poly_points = wave_points_upper + wave_points_lower[::-1]
    
    # Draw smooth wave with pure white/silver shimmer
    wave_img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    wave_draw = ImageDraw.Draw(wave_img)
    wave_draw.polygon(poly_points, fill=(245, 248, 255, 245))

    # Clip wave to background mask
    img.paste(wave_img, (0, 0), mask)

    # Supersampling downscale for ultra crisp smooth anti-aliasing
    final_icon = img.resize((size, size), Image.Resampling.LANCZOS)
    return final_icon

def create_round_icon(size):
    scale = 4
    canvas_size = size * scale
    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    
    start_color = (0, 180, 216)
    end_color = (3, 4, 94)

    mask = Image.new("L", (canvas_size, canvas_size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.ellipse([0, 0, canvas_size, canvas_size], fill=255)

    grad_img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    for y in range(canvas_size):
        r = int(start_color[0] + (end_color[0] - start_color[0]) * (y / canvas_size))
        g = int(start_color[1] + (end_color[1] - start_color[1]) * (y / canvas_size))
        b = int(start_color[2] + (end_color[2] - start_color[2]) * (y / canvas_size))
        draw_line = ImageDraw.Draw(grad_img)
        draw_line.line([(0, y), (canvas_size, y)], fill=(r, g, b, 255))

    img.paste(grad_img, (0, 0), mask)

    wave_thickness = canvas_size * 0.16
    mid_y = canvas_size * 0.50
    wave_points_upper = []
    wave_points_lower = []
    steps = 200
    for i in range(steps + 1):
        x = (canvas_size * i) / steps
        angle = (i / steps) * math.pi * 2.0
        y_offset = math.sin(angle) * (canvas_size * 0.14)
        wave_points_upper.append((x, mid_y + y_offset - wave_thickness / 2))
        wave_points_lower.append((x, mid_y + y_offset + wave_thickness / 2))

    poly_points = wave_points_upper + wave_points_lower[::-1]
    wave_img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    wave_draw = ImageDraw.Draw(wave_img)
    wave_draw.polygon(poly_points, fill=(245, 248, 255, 245))

    img.paste(wave_img, (0, 0), mask)
    final_icon = img.resize((size, size), Image.Resampling.LANCZOS)
    return final_icon

# Icon sizes across Android mipmaps
densities = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

base_res = r'D:\Projects\Chatgpt Pruner\betaC\app\src\main\res'

for folder, size in densities.items():
    target_dir = os.path.join(base_res, folder)
    os.makedirs(target_dir, exist_ok=True)
    
    icon = create_caspian_flow_icon(size)
    icon.save(os.path.join(target_dir, 'ic_launcher.png'), 'PNG')
    
    round_icon = create_round_icon(size)
    round_icon.save(os.path.join(target_dir, 'ic_launcher_round.png'), 'PNG')
    print(f"Generated {folder} ({size}x{size})")

print("All Caspian Flow icons generated successfully with electric cyan / sapphire ocean hue!")
