#!/usr/bin/env python3
"""
Generates a placeholder player spritesheet + LibGDX .atlas file.
Each class gets colored stick-figure sprites for every animation state.
Replace spritesheet.png later with real art — atlas layout stays the same.
"""

import os
import math
from PIL import Image, ImageDraw

FRAME_W, FRAME_H = 96, 96

# (class_name, base_color_rgb)
CLASSES = [
    ("archer",  (70,  130, 200)),
    ("mage",    (160,  70, 200)),
    ("warrior", (200,  70,  70)),
]

# (state_name, num_frames, is_looped)
STATES = [
    ("idle",   4, True),
    ("walk",   4, True),
    ("run",    4, True),   # also used for jump + fall
    ("attack", 4, False),
    ("hit",    2, False),
    ("dead",   4, False),
]

COLS = max(nf for _, nf, _ in STATES)  # = 4
ROWS_PER_CLASS = len(STATES)
TOTAL_ROWS = ROWS_PER_CLASS * len(CLASSES)

SHEET_W = FRAME_W * COLS
SHEET_H = FRAME_H * TOTAL_ROWS


def lerp_color(c1, c2, t):
    return tuple(int(a + (b - a) * t) for a, b in zip(c1, c2))


def draw_stick_figure(draw: ImageDraw.ImageDraw, cx: int, cy: int,
                      color, state: str, frame: int, num_frames: int):
    """Draw a simple stick figure animated by frame index."""
    t = frame / max(num_frames - 1, 1)  # 0..1

    # Body bob / lean based on state
    bob = 0
    lean = 0
    arm_angle = 0

    if state == "idle":
        bob = int(math.sin(t * math.pi * 2) * 2)
    elif state == "walk":
        bob = int(math.sin(t * math.pi * 2) * 4)
        lean = int(math.sin(t * math.pi * 2) * 3)
    elif state == "run":
        bob = int(math.sin(t * math.pi * 2) * 6)
        lean = 8
    elif state == "attack":
        arm_angle = int(t * 90)
        lean = int(t * 10)
    elif state == "hit":
        lean = -10 + int(t * 5)
        bob = int(t * 5)
    elif state == "dead":
        # Fall over
        pass

    dark = lerp_color(color, (0, 0, 0), 0.4)
    bright = lerp_color(color, (255, 255, 255), 0.3)

    if state == "dead":
        # Lying flat
        progress = t  # 0→upright, 1→flat
        body_x = cx + int(progress * 20)
        body_y = cy + 10 + int(progress * 25)
        # Head
        draw.ellipse([body_x - 8, body_y - 35, body_x + 8, body_y - 19], fill=bright, outline=dark)
        # Body
        draw.line([body_x, body_y - 19, body_x + int(progress * 20), body_y + int(10 - progress * 5)],
                  fill=color, width=4)
        return

    head_y = cy - 28 + bob
    body_y1 = cy - 18 + bob
    body_y2 = cy + 5 + bob

    # Head
    draw.ellipse([cx - 9 + lean, head_y - 9, cx + 9 + lean, head_y + 9],
                 fill=bright, outline=dark, width=2)
    # Body
    draw.line([cx + lean, body_y1, cx + lean, body_y2], fill=color, width=4)

    # Arms
    if state == "attack":
        # Swing arm
        ax = cx + lean + int(math.cos(math.radians(arm_angle - 45)) * 18)
        ay = body_y1 + 6 + int(math.sin(math.radians(arm_angle - 45)) * 18)
        draw.line([cx + lean, body_y1 + 6, ax, ay], fill=dark, width=3)
        # Weapon slash
        wx = ax + int(math.cos(math.radians(arm_angle)) * 14)
        wy = ay + int(math.sin(math.radians(arm_angle)) * 14)
        draw.line([ax, ay, wx, wy], fill=(255, 220, 50), width=3)
    else:
        arm_swing = int(math.sin(t * math.pi * 2) * 12) if state in ("walk", "run") else 5
        draw.line([cx + lean, body_y1 + 6, cx + lean - 14, body_y1 + 6 + arm_swing],
                  fill=dark, width=3)
        draw.line([cx + lean, body_y1 + 6, cx + lean + 14, body_y1 + 6 - arm_swing],
                  fill=dark, width=3)

    # Legs
    leg_swing = int(math.sin(t * math.pi * 2) * 14) if state in ("walk", "run") else 0
    draw.line([cx + lean, body_y2, cx + lean - 10 + leg_swing, cy + 22 + bob],
              fill=dark, width=4)
    draw.line([cx + lean, body_y2, cx + lean + 10 - leg_swing, cy + 22 + bob],
              fill=dark, width=4)

    # Class-specific prop
    if "archer" in str(color):  # won't match, use index below
        pass


def draw_class_prop(draw, cx, cy, cls_name, color, state, frame, num_frames):
    t = frame / max(num_frames - 1, 1)
    bob = int(math.sin(t * math.pi * 2) * 2)
    cy_bob = cy + bob - 5

    if cls_name == "archer":
        # Bow
        if state != "dead":
            draw.arc([cx - 22, cy_bob - 20, cx - 6, cy_bob + 20],
                     start=-90, end=90, fill=(139, 90, 43), width=3)
            if state == "attack":
                draw.line([cx - 14, cy_bob - 15, cx - 14, cy_bob + 15],
                          fill=(200, 200, 200), width=1)
    elif cls_name == "mage":
        # Staff with glowing orb
        if state != "dead":
            draw.line([cx + 16, cy_bob + 20, cx + 20, cy_bob - 20],
                      fill=(139, 90, 43), width=3)
            glow = (100, 200, 255) if state != "attack" else (255, 100, 50)
            draw.ellipse([cx + 16, cy_bob - 25, cx + 26, cy_bob - 15],
                         fill=glow, outline=(255, 255, 255))
    elif cls_name == "warrior":
        # Sword
        if state != "dead":
            sx = cx + 18
            sy = cy_bob - 5
            if state == "attack":
                angle = t * 60
                ex = sx + int(math.cos(math.radians(-30 + angle)) * 22)
                ey = sy + int(math.sin(math.radians(-30 + angle)) * 22)
                draw.line([sx, sy, ex, ey], fill=(200, 200, 220), width=4)
            else:
                draw.line([sx, sy - 20, sx, sy + 8], fill=(200, 200, 220), width=4)
            # Shield
            draw.ellipse([cx - 24, cy_bob - 5, cx - 10, cy_bob + 15],
                         fill=(150, 100, 50), outline=(80, 50, 20), width=2)


def generate():
    sheet = Image.new("RGBA", (SHEET_W, SHEET_H), (0, 0, 0, 0))
    draw_sheet = ImageDraw.Draw(sheet)

    atlas_regions = []

    for ci, (cls_name, base_color) in enumerate(CLASSES):
        for si, (state_name, num_frames, _) in enumerate(STATES):
            row = ci * ROWS_PER_CLASS + si
            for fi in range(num_frames):
                x = fi * FRAME_W
                y = row * FRAME_H
                cx = x + FRAME_W // 2
                cy = y + FRAME_H // 2 + 10

                # Background tint
                tint = lerp_color(base_color, (20, 20, 30), 0.85)
                draw_sheet.rectangle([x, y, x + FRAME_W - 1, y + FRAME_H - 1],
                                     fill=(*tint, 255))

                # Figure
                draw_stick_figure(draw_sheet, cx, cy, base_color, state_name, fi, num_frames)
                draw_class_prop(draw_sheet, cx, cy, cls_name, base_color, state_name, fi, num_frames)

                # Region name
                region_name = f"{cls_name}_{state_name}_{fi + 1:03d}"
                atlas_regions.append((region_name, x, y, FRAME_W, FRAME_H))

                # Aliases: run → jump, run → fall
                if state_name == "run":
                    atlas_regions.append((f"{cls_name}_jump_{fi + 1:03d}", x, y, FRAME_W, FRAME_H))
                    atlas_regions.append((f"{cls_name}_fall_{fi + 1:03d}", x, y, FRAME_W, FRAME_H))

    return sheet, atlas_regions


def write_atlas(atlas_path, png_name, regions, sheet_w, sheet_h):
    lines = [
        png_name,
        f"size: {sheet_w},{sheet_h}",
        "format: RGBA8888",
        "filter: Linear,Linear",
        "repeat: none",
        "",
    ]
    for (name, x, y, w, h) in regions:
        lines += [
            name,
            "  rotate: false",
            f"  xy: {x}, {y}",
            f"  size: {w}, {h}",
            f"  orig: {w}, {h}",
            "  offset: 0, 0",
            "  index: -1",
        ]
    with open(atlas_path, "w") as f:
        f.write("\n".join(lines) + "\n")
    print(f"Atlas: {atlas_path}  ({len(regions)} regions)")


if __name__ == "__main__":
    out_dir = os.path.join(os.path.dirname(__file__),
                           "../assets/textures/characters/player")
    os.makedirs(out_dir, exist_ok=True)

    print("Generating spritesheet...")
    sheet, regions = generate()

    png_path   = os.path.join(out_dir, "player.png")
    atlas_path = os.path.join(out_dir, "player.atlas")

    sheet.save(png_path)
    print(f"Spritesheet: {png_path}  ({SHEET_W}×{SHEET_H})")

    write_atlas(atlas_path, "player.png", regions, SHEET_W, SHEET_H)
    print("Done.")
