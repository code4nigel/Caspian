import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = "Images/AMO_Showcase"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Font configurations
FONT_PATH_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_PATH_REG = "C:/Windows/Fonts/segoeui.ttf"
FONT_PATH_SEMI = "C:/Windows/Fonts/seguisb.ttf" if os.path.exists("C:/Windows/Fonts/seguisb.ttf") else FONT_PATH_BOLD

font_badge = ImageFont.truetype(FONT_PATH_BOLD, 15)
font_title = ImageFont.truetype(FONT_PATH_BOLD, 42)
font_desc = ImageFont.truetype(FONT_PATH_REG, 18)
font_bullet = ImageFont.truetype(FONT_PATH_SEMI, 16)
font_footer = ImageFont.truetype(FONT_PATH_REG, 14)

def create_gradient_canvas(width=1280, height=800, top_color=(11, 15, 25), bottom_color=(15, 23, 42), glow_color=(14, 165, 233, 45)):
    base = Image.new("RGBA", (width, height), top_color)
    draw = ImageDraw.Draw(base)
    for y in range(height):
        factor = y / height
        r = int(top_color[0] + (bottom_color[0] - top_color[0]) * factor)
        g = int(top_color[1] + (bottom_color[1] - top_color[1]) * factor)
        b = int(top_color[2] + (bottom_color[2] - top_color[2]) * factor)
        draw.line([(0, y), (width, y)], fill=(r, g, b, 255))
    
    # Add ambient subtle glow circle in top right
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_cx, glow_cy = int(width * 0.75), int(height * 0.35)
    radius = 350
    glow_draw.ellipse([glow_cx - radius, glow_cy - radius, glow_cx + radius, glow_cy + radius], fill=glow_color)
    glow = glow.filter(ImageFilter.GaussianBlur(120))
    
    base = Image.alpha_composite(base, glow)
    return base

def draw_pill(draw, x, y, text, font, bg_color=(14, 165, 233, 40), border_color=(56, 189, 248, 180), text_color=(56, 189, 248, 255)):
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    pad_x, pad_y = 16, 7
    pill_box = [x, y, x + tw + pad_x * 2, y + th + pad_y * 2]
    draw.rounded_rectangle(pill_box, radius=999, fill=bg_color, outline=border_color, width=1)
    draw.text((x + pad_x, y + pad_y - bbox[1]), text, font=font, fill=text_color)
    return pill_box[3]

def draw_rounded_shadow_card(canvas, img, target_center_x, target_center_y, max_w, max_h, border_color=(56, 189, 248, 80)):
    # Aspect ratio preserving resize
    img = img.convert("RGBA")
    w, h = img.size
    ratio = min(max_w / w, max_h / h)
    new_w, new_h = int(w * ratio), int(h * ratio)
    img_resized = img.resize((new_w, new_h), Image.Resampling.LANCZOS)
    
    # Rounded corners mask
    corner_radius = 18
    mask = Image.new("L", (new_w, new_h), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([0, 0, new_w, new_h], radius=corner_radius, fill=255)
    
    # Shadow
    shadow_pad = 40
    shadow = Image.new("RGBA", (new_w + shadow_pad * 2, new_h + shadow_pad * 2), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    s_draw.rounded_rectangle([shadow_pad - 4, shadow_pad + 12, shadow_pad + new_w + 4, shadow_pad + new_h + 12], radius=corner_radius + 4, fill=(0, 0, 0, 150))
    shadow = shadow.filter(ImageFilter.GaussianBlur(24))
    
    # Card with border
    card = Image.new("RGBA", (new_w, new_h), (0, 0, 0, 0))
    card.paste(img_resized, (0, 0), mask=mask)
    card_draw = ImageDraw.Draw(card)
    card_draw.rounded_rectangle([0, 0, new_w - 1, new_h - 1], radius=corner_radius, outline=border_color, width=2)
    
    pos_x = target_center_x - new_w // 2
    pos_y = target_center_y - new_h // 2
    
    canvas.alpha_composite(shadow, (pos_x - shadow_pad, pos_y - shadow_pad))
    canvas.alpha_composite(card, (pos_x, pos_y))

def render_slide(filename, badge_text, title, desc, bullets, primary_img_path, secondary_img_path=None, glow_col=(14, 165, 233, 50)):
    canvas = create_gradient_canvas(1280, 800, (11, 15, 25), (15, 23, 42), glow_color=glow_col)
    draw = ImageDraw.Draw(canvas)
    
    # Left Content Column (x: 80 to 540)
    left_x = 80
    curr_y = 90
    
    # Badge
    curr_y = draw_pill(draw, left_x, curr_y, badge_text, font_badge) + 26
    
    # Title
    for line in title.split("\n"):
        draw.text((left_x, curr_y), line, font=font_title, fill=(255, 255, 255, 255))
        curr_y += 50
    curr_y += 12
    
    # Description
    for line in desc.split("\n"):
        draw.text((left_x, curr_y), line, font=font_desc, fill=(148, 163, 184, 255))
        curr_y += 28
    curr_y += 24
    
    # Divider line
    draw.line([(left_x, curr_y), (left_x + 420, curr_y)], fill=(51, 65, 85, 180), width=1)
    curr_y += 24
    
    # Bullet points
    for b in bullets:
        # Checkmark icon pill
        draw.ellipse([left_x, curr_y + 2, left_x + 18, curr_y + 20], fill=(14, 165, 233, 40), outline=(56, 189, 248, 200))
        draw.text((left_x + 5, curr_y + 1), "✓", font=font_footer, fill=(56, 189, 248, 255))
        draw.text((left_x + 28, curr_y + 1), b, font=font_bullet, fill=(226, 232, 240, 255))
        curr_y += 36
        
    # Brand Footer
    draw.text((left_x, 720), "Caspian Productivity Suite  •  Firefox Browser Extension", font=font_footer, fill=(100, 116, 139, 255))
    
    # Right Side Visuals (x: 580 to 1200)
    p_img = Image.open(primary_img_path)
    if secondary_img_path and os.path.exists(secondary_img_path):
        s_img = Image.open(secondary_img_path)
        # Dual card layout
        draw_rounded_shadow_card(canvas, s_img, 980, 420, 520, 560, border_color=(71, 85, 105, 100))
        draw_rounded_shadow_card(canvas, p_img, 840, 390, 420, 580, border_color=(56, 189, 248, 140))
    else:
        # Single prominent hero card
        draw_rounded_shadow_card(canvas, p_img, 920, 400, 540, 640, border_color=(56, 189, 248, 140))
        
    out_path = os.path.join(OUTPUT_DIR, filename)
    canvas.convert("RGB").save(out_path, "PNG", quality=95)
    print(f"Generated {out_path}")

# Slide 1: AI Chat Pruner
render_slide(
    "1_ai_chat_pruner.png",
    "⚡  AI CHAT PERFORMANCE ENGINE",
    "Zero Typing Lag on\nLong Conversations",
    "Intelligently prunes heavy off-screen DOM nodes in\nChatGPT & Google Gemini. Keeps long sessions fast,\nfluid, and completely freeze-free.",
    [
        "Real-Time DOM Pruning Engine",
        "Zero Input Lag on 100+ Turn Dialogues",
        "Safe, Reversible & Preserves All History"
    ],
    "caspian fox extension/images/caspian_mainUI.png",
    "Images/Capsian Flow/Flow_chat_AI_example.jpg",
    glow_col=(14, 165, 233, 50)
)

# Slide 2: Flow Speed Engine
render_slide(
    "2_universal_flow_speed.png",
    "🌊  FLOW SPEED MEDIA ENGINE",
    "Universal Video Speed\non Every Website",
    "Take full control of video playback on YouTube,\nReddit, Netflix, Twitch & modern Web Components.\nFeatures Shadow DOM piercing & instant HUD feedback.",
    [
        "Works on Reddit, YouTube & Shadow DOM Players",
        "Instant Keyboard Shortcuts (Alt+S, Alt+D, [, ])",
        "Modern Floating Neon Wave HUD"
    ],
    "caspian fox extension/images/caspian_siteUI.png",
    "Images/Capsian Flow/Flow_engine_example.jpg",
    glow_col=(6, 182, 212, 50)
)

# Slide 3: YouTube Focus Cleaner
render_slide(
    "3_youtube_feed_cleaner.png",
    "🛡️  DISTRACTION-FREE YOUTUBE",
    "Clean Minimalist\nYouTube Workspace",
    "Eliminate recommendation loops and cognitive fatigue.\nLimit homepage rows, hide addictive Shorts, and\nkeep only what matters.",
    [
        "Custom Home Grid Limits (0 to 10+ Videos)",
        "One-Click Shorts & Distraction Removal",
        "Pure Native Speed with Zero Tracking"
    ],
    "caspian fox extension/images/caspian_settingsUI.png",
    "Images/Capsian Flow/Flow_Youtube_example.jpg",
    glow_col=(239, 68, 68, 35)
)

# Slide 4: RippleFrame Annotation & Vault
render_slide(
    "4_rippleframe_and_vault.png",
    "📸  CAPTURE & ARCHIVE STUDIO",
    "Full-Page Screenshots\n& Chat Vault Exports",
    "Capture crystal-clear scrolling web pages and annotate\ninstantly. Export complete AI conversation history\ninto clean Markdown, JSON, or TXT archives.",
    [
        "Progressive Full-Page Scrolling Capture",
        "Built-in Annotation Studio with Shapes & Text",
        "Instant Chat Vault Export to Markdown / JSON"
    ],
    "caspian fox extension/images/caspian_exportOptions.png",
    "Images/Capsian Flow/Flow_tab_example.jpg",
    glow_col=(168, 85, 247, 45)
)

# Slide 5: Sleek Dark / Light Theme
render_slide(
    "5_modern_adaptive_ui.png",
    "✨  PREMIUM ADAPTIVE DESIGN",
    "Engineered for Speed,\nStyled for Elegance",
    "Tailor Caspian to match your aesthetic with deep OLED\ndark mode, customizable neon accent palettes, and\na lightweight privacy-first footprint.",
    [
        "Sleek Glassmorphic Dark & Light Modes",
        "Full Privacy: 100% Client-Side Local Storage",
        "Cross-Browser Support for Firefox & Chromium"
    ],
    "caspian fox extension/images/Caspian_darkUI.png",
    "caspian fox extension/images/caspian_mainUI.png",
    glow_col=(59, 130, 246, 50)
)
