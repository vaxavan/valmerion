#!/usr/bin/env python3
"""Generate world and NPC textures for Valmerion."""
import os
from PIL import Image, ImageDraw

BASE = os.path.join(os.path.dirname(__file__), "..", "assets", "textures")

def ensure(path):
    os.makedirs(os.path.dirname(path), exist_ok=True)

# ── artartel_bg.png ────────────────────────────────────────────────────────────
path = os.path.join(BASE, "world", "artartel_bg.png")
ensure(path)
W, H = 1280, 720
img = Image.new("RGBA", (W, H), (10, 8, 30, 255))
draw = ImageDraw.Draw(img)

# sky gradient
for y in range(H):
    t = y / H
    r = int(10 + t * 20)
    g = int(8 + t * 10)
    b = int(30 + t * 15)
    draw.line([(0, y), (W, y)], fill=(r, g, b, 255))

# stars
import random
random.seed(42)
for _ in range(200):
    sx = random.randint(0, W)
    sy = random.randint(0, H // 2)
    br = random.randint(150, 255)
    draw.ellipse([sx-1, sy-1, sx+1, sy+1], fill=(br, br, br, 255))

# buildings silhouettes
buildings = [
    (0, 200, 120, H),
    (100, 280, 200, H),
    (190, 180, 280, H),
    (270, 260, 380, H),
    (360, 150, 460, H),
    (440, 300, 530, H),
    (510, 200, 600, H),
    (580, 250, 680, H),
    (660, 170, 760, H),
    (740, 290, 830, H),
    (810, 220, 910, H),
    (890, 160, 990, H),
    (960, 300, 1060, H),
    (1040, 240, 1140, H),
    (1120, 200, 1280, H),
]
for (x1, y1, x2, y2) in buildings:
    draw.rectangle([x1, y1, x2, y2], fill=(25, 20, 35, 255))
    # windows
    for wy in range(y1 + 20, y2 - 20, 30):
        for wx in range(x1 + 10, x2 - 10, 20):
            if random.random() > 0.5:
                draw.rectangle([wx, wy, wx+8, wy+12], fill=(255, 200, 80, 180))

# ground
draw.rectangle([0, H - 160, W, H], fill=(40, 30, 20, 255))

# lanterns
for lx in range(100, W, 200):
    draw.ellipse([lx-15, H-200, lx+15, H-170], fill=(255, 220, 100, 200))
    draw.line([(lx, H-170), (lx, H-160)], fill=(100, 80, 60, 255), width=3)

img.save(path)
print(f"Saved: {path}")

# ── Helper: stick figure ────────────────────────────────────────────────────────
def draw_stick_figure(draw, cx, cy, color, head_detail=None):
    # head
    draw.ellipse([cx-15, cy-90, cx+15, cy-60], fill=color, outline=(0,0,0,255))
    # body
    draw.line([(cx, cy-60), (cx, cy-20)], fill=color, width=4)
    # arms
    draw.line([(cx-25, cy-50), (cx+25, cy-50)], fill=color, width=4)
    # legs
    draw.line([(cx, cy-20), (cx-20, cy+10)], fill=color, width=4)
    draw.line([(cx, cy-20), (cx+20, cy+10)], fill=color, width=4)
    if head_detail:
        head_detail(draw, cx, cy)

# ── zak.png ───────────────────────────────────────────────────────────────────
path = os.path.join(BASE, "characters", "npc", "zak.png")
ensure(path)
img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)
draw_stick_figure(draw, 48, 96, (80, 130, 220, 255))
# glasses
draw.rectangle([36, 68, 44, 74], outline=(200,200,200,255))
draw.rectangle([52, 68, 60, 74], outline=(200,200,200,255))
draw.line([(44, 71), (52, 71)], fill=(200,200,200,255), width=1)
img.save(path)
print(f"Saved: {path}")

# ── hazan.png ─────────────────────────────────────────────────────────────────
path = os.path.join(BASE, "characters", "npc", "hazan.png")
ensure(path)
img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)
# broader, more muscular
color = (120, 80, 40, 255)
draw.ellipse([30, 5, 66, 35], fill=color, outline=(0,0,0,255))
draw.rectangle([25, 35, 71, 65], fill=color)  # wide body
draw.line([(25, 45), (5, 55)], fill=color, width=6)
draw.line([(71, 45), (91, 55)], fill=color, width=6)
draw.line([(40, 65), (30, 96)], fill=color, width=5)
draw.line([(56, 65), (66, 96)], fill=color, width=5)
img.save(path)
print(f"Saved: {path}")

# ── tenkai.png ────────────────────────────────────────────────────────────────
path = os.path.join(BASE, "characters", "npc", "tenkai.png")
ensure(path)
img = Image.new("RGBA", (96, 96), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)
draw_stick_figure(draw, 48, 96, (200, 60, 60, 255))
# hair spikes
for dx in [-10, -5, 0, 5, 10]:
    draw.line([(48+dx, 6), (48+dx-2, -4)], fill=(180, 40, 40, 255), width=2)
img.save(path)
print(f"Saved: {path}")

# ── dialogue_bg.png ───────────────────────────────────────────────────────────
path = os.path.join(BASE, "ui", "dialogue_bg.png")
ensure(path)
img = Image.new("RGBA", (1200, 180), (0, 0, 0, 210))
draw = ImageDraw.Draw(img)
draw.rectangle([0, 0, 1199, 179], outline=(100, 80, 40, 255), width=2)
img.save(path)
print(f"Saved: {path}")

print("Done!")
