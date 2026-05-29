#!/usr/bin/env python3
"""
Slices player spritesheet into individual frames and generates a LibGDX .atlas file.

Layout assumption (edit LAYOUT below to match your sheet):
  Each class occupies its own set of rows.
  jump and fall share the same run animation frames.

Usage:
  python3 slice_player.py <spritesheet.png>
"""

import sys
import os
from PIL import Image

# ── Spritesheet layout ────────────────────────────────────────────────────────
# Each entry: (class_prefix, state_name, row_index, start_col, num_frames)
# Row and col are 0-based.
#
# Adjust these after inspecting your sheet with the --inspect flag.
FRAME_W = 96   # width of a single sprite in pixels
FRAME_H = 96   # height of a single sprite in pixels

LAYOUT = [
    # ── ARCHER ─────────────────────────────────────────────────────────────
    ("archer", "attack", 0, 0, 4),   # row 0, cols 0-3: archer shooting
    ("archer", "idle",   1, 0, 4),   # row 1, cols 0-3: archer idle with bow
    ("archer", "walk",   2, 0, 4),   # row 2: archer walking
    ("archer", "run",    3, 0, 4),   # row 3: archer running (used for jump/fall too)
    ("archer", "hit",    4, 0, 2),   # row 4: archer hit
    ("archer", "dead",   5, 0, 4),   # row 5: archer dead

    # ── MAGE ───────────────────────────────────────────────────────────────
    ("mage", "attack", 0, 4, 4),
    ("mage", "idle",   1, 4, 4),
    ("mage", "walk",   2, 4, 4),
    ("mage", "run",    3, 4, 4),
    ("mage", "hit",    4, 4, 2),
    ("mage", "dead",   5, 4, 4),

    # ── WARRIOR ────────────────────────────────────────────────────────────
    ("warrior", "attack", 0, 8, 4),
    ("warrior", "idle",   1, 8, 4),
    ("warrior", "walk",   2, 8, 4),
    ("warrior", "run",    3, 8, 4),
    ("warrior", "hit",    4, 8, 2),
    ("warrior", "dead",   5, 8, 4),
]

# ── Helpers ───────────────────────────────────────────────────────────────────

def auto_detect_frame_size(img: Image.Image):
    """Try to detect frame size by finding white→non-white column/row boundaries."""
    w, h = img.size
    pixels = img.load()

    def is_blank_col(x):
        for y in range(h):
            r, g, b, a = pixels[x, y] if img.mode == 'RGBA' else (*pixels[x, y], 255)
            if a > 10 and not (r > 240 and g > 240 and b > 240):
                return False
        return True

    def is_blank_row(y):
        for x in range(w):
            r, g, b, a = pixels[x, y] if img.mode == 'RGBA' else (*pixels[x, y], 255)
            if a > 10 and not (r > 240 and g > 240 and b > 240):
                return False
        return True

    # Find first non-blank col and row
    x0 = next((x for x in range(w) if not is_blank_col(x)), 0)
    y0 = next((y for y in range(h) if not is_blank_row(y)), 0)

    # Find second blank col after x0 to get frame width
    in_sprite = True
    fw = None
    for x in range(x0 + 1, w):
        blank = is_blank_col(x)
        if in_sprite and blank:
            fw = x - x0
            break

    in_sprite = True
    fh = None
    for y in range(y0 + 1, h):
        blank = is_blank_row(y)
        if in_sprite and blank:
            fh = y - y0
            break

    return fw, fh, x0, y0


def inspect(path: str):
    img = Image.open(path).convert("RGBA")
    fw, fh, ox, oy = auto_detect_frame_size(img)
    print(f"Image size: {img.size}")
    print(f"Detected frame size: {fw}×{fh}  (origin: {ox},{oy})")
    cols = (img.width - ox) // (fw or 1)
    rows = (img.height - oy) // (fh or 1)
    print(f"Grid: ~{cols} cols × {rows} rows")


def slice_sheet(sheet_path: str, out_dir: str, atlas_path: str,
                frame_w: int, frame_h: int, ox: int = 0, oy: int = 0):
    img = Image.open(sheet_path).convert("RGBA")
    os.makedirs(out_dir, exist_ok=True)

    atlas_lines = []
    atlas_lines.append(os.path.basename(sheet_path))
    atlas_lines.append("size: {}, {}".format(*img.size))
    atlas_lines.append("format: RGBA8888")
    atlas_lines.append("filter: Linear, Linear")
    atlas_lines.append("repeat: none")
    atlas_lines.append("")

    frame_entries = []

    for (cls, state, row, start_col, num_frames) in LAYOUT:
        for i in range(num_frames):
            col = start_col + i
            x = ox + col * frame_w
            y = oy + row * frame_h
            if x + frame_w > img.width or y + frame_h > img.height:
                print(f"  SKIP {cls}_{state}_{i+1:03d}: out of bounds ({x},{y})")
                continue

            region_name = f"{cls}_{state}_{i+1:03d}"

            # Also generate aliases: run → jump, run → fall
            aliases = [region_name]
            if state == "run":
                aliases.append(f"{cls}_jump_{i+1:03d}")
                aliases.append(f"{cls}_fall_{i+1:03d}")

            for name in aliases:
                frame_entries.append(
                    f"{name}\n"
                    f"  rotate: false\n"
                    f"  xy: {x}, {y}\n"
                    f"  size: {frame_w}, {frame_h}\n"
                    f"  orig: {frame_w}, {frame_h}\n"
                    f"  offset: 0, 0\n"
                    f"  index: -1\n"
                )

    # Write atlas
    with open(atlas_path, "w") as f:
        f.write("\n".join(atlas_lines))
        f.write("\n".join(frame_entries))

    print(f"Atlas written: {atlas_path}  ({len(frame_entries)} regions)")


# ── Main ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 slice_player.py <spritesheet.png> [--inspect]")
        sys.exit(1)

    sheet = sys.argv[1]
    if "--inspect" in sys.argv:
        inspect(sheet)
        sys.exit(0)

    # Try auto-detect or fall back to defaults
    img = Image.open(sheet).convert("RGBA")
    fw, fh, ox, oy = auto_detect_frame_size(img)
    if fw and fh:
        print(f"Auto-detected frame size: {fw}×{fh}  origin: ({ox},{oy})")
    else:
        fw, fh, ox, oy = FRAME_W, FRAME_H, 0, 0
        print(f"Using default frame size: {fw}×{fh}")

    out_dir   = os.path.dirname(sheet)
    atlas_out = os.path.join(out_dir, "player.atlas")
    slice_sheet(sheet, out_dir, atlas_out, fw, fh, ox, oy)
