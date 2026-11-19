"""Compose the five 1280 × 800 listing images from Firefox screenshots."""

from argparse import ArgumentParser
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageOps


SIZE = (1280, 800)
OUTPUT = Path(__file__).resolve().parents[1] / "Images" / "AMO_Showcase"


def backdrop(color, glow_color):
    base = Image.new("RGB", SIZE, color)
    glow = Image.new("RGB", SIZE, glow_color)
    mask = Image.new("L", SIZE, 0)
    ImageDraw.Draw(mask).ellipse((160, -90, 1120, 720), fill=200)
    return Image.composite(glow, base, mask.filter(ImageFilter.GaussianBlur(170)))


def place(canvas, path, center, size, radius):
    with Image.open(path) as source:
        shot = ImageOps.contain(source.convert("RGB"), size, Image.Resampling.LANCZOS)
    x = center[0] - shot.width // 2
    y = center[1] - shot.height // 2
    bounds = (x, y + 14, x + shot.width, y + shot.height + 14)
    shadow = Image.new("L", SIZE, 0)
    ImageDraw.Draw(shadow).rounded_rectangle(bounds, radius=radius, fill=72)
    canvas.paste("#000000", (0, 0, *SIZE), shadow.filter(ImageFilter.GaussianBlur(24)))

    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        (0, 0, shot.width - 1, shot.height - 1), radius=radius, fill=255
    )
    canvas.paste(shot, (x, y), mask)


def main():
    parser = ArgumentParser(description=__doc__)
    parser.add_argument("screenshots", type=Path, help="Directory of Firefox PNG screenshots")
    screenshots = parser.parse_args().screenshots
    scenes = (
        ("1_ai_chat_pruner", "chat", "sites", "#b7c6d3", "#eff9fd"),
        ("2_universal_flow_speed", "speed", "speed-options", "#39434d", "#657783"),
        ("3_youtube_feed_cleaner", "youtube-off", "youtube", "#cab5b8", "#f8e7e8"),
        ("5_modern_adaptive_ui", "light", "dark", "#9d97ad", "#eae2f2"),
    )
    for filename, left, right, color, glow in scenes:
        canvas = backdrop(color, glow)
        for name, x in ((left, 379), (right, 901)):
            place(canvas, screenshots / f"{name}.png", (x, 400), (458, 680), 32)
        canvas.save(OUTPUT / f"{filename}.png", optimize=True)

    canvas = backdrop("#40394a", "#79718b")
    place(canvas, screenshots / "studio.png", (549, 367), (1010, 645), 19)
    place(canvas, screenshots / "export-dialog.png", (1007, 554), (456, 408), 19)
    canvas.save(OUTPUT / "4_rippleframe_and_vault.png", optimize=True)


if __name__ == "__main__":
    main()
