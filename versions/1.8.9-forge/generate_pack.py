#!/usr/bin/env python3
"""Generates the built-in "Noob_mod_antor FPS Boost" resource pack textures.

The textures are deliberately simple, programmatically drawn 8x8 (blocks)
and 16x16 (items/fire) images in the style of classic PvP FPS packs: flat
colors with light noise, short swords, low fire, a clear pumpkin overlay
and tiny particle dots. Output goes to
src/main/resources/assets/noob_mod_antor/fpspack/ together with a
packfiles.txt index that FpsPack.java reads at runtime to extract the pack
into the game's resourcepacks folder.

Requires Pillow (PIL). Rerun any time; output is deterministic (seeded).
"""
import json
import os
import random

from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                    "src", "main", "resources", "assets", "noob_mod_antor", "fpspack")
FILES = []


def save(img, rel):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    FILES.append(rel.replace(os.sep, "/"))


def noisy(size, base, spread, seed):
    rng = random.Random(seed)
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    px = img.load()
    for y in range(size[1]):
        for x in range(size[0]):
            d = rng.randint(-spread, spread)
            px[x, y] = tuple(max(0, min(255, base[i] + d)) for i in range(3)) + (base[3] if len(base) > 3 else 255,)
    return img


def block(name, base, spread=10):
    save(noisy((8, 8), base, spread, name), "assets/minecraft/textures/blocks/%s.png" % name)


# ---------------------------------------------------------------- blocks
block("grass_top", (106, 170, 61, 255), 9)
block("stone", (126, 126, 126, 255), 6)
block("dirt", (134, 96, 67, 255), 12)

# grass side: dirt with a green lip on top
img = noisy((8, 8), (134, 96, 67, 255), 12, "grass_side")
rng = random.Random("grass_side_lip")
px = img.load()
for x in range(8):
    for y in range(2):
        d = rng.randint(-9, 9)
        px[x, y] = (106 + d, 170 + d, 61 + d, 255)
    if rng.random() < 0.4:
        d = rng.randint(-9, 9)
        px[x, 2] = (100 + d, 160 + d, 58 + d, 255)
save(img, "assets/minecraft/textures/blocks/grass_side.png")

# planks: two horizontal bands with a dark seam and one vertical joint each
PLANKS = {
    "planks_oak": (168, 134, 80), "planks_spruce": (104, 78, 47),
    "planks_birch": (196, 178, 124), "planks_jungle": (164, 118, 78),
    "planks_acacia": (172, 98, 54), "planks_big_oak": (70, 50, 30),
}
for name, base in PLANKS.items():
    img = noisy((8, 8), base + (255,), 7, name)
    px = img.load()
    dark = tuple(max(0, c - 55) for c in base) + (255,)
    for x in range(8):
        px[x, 3] = dark
        px[x, 7] = dark
    for y in range(3):
        px[5, y] = dark
    for y in range(4, 7):
        px[2, y] = dark
    save(img, "assets/minecraft/textures/blocks/%s.png" % name)

# logs: vertical bark stripes on the side, concentric rings on top
LOGS = {
    "log_oak": ((106, 84, 50), (172, 140, 92)), "log_spruce": ((58, 42, 24), (120, 96, 58)),
    "log_birch": ((196, 190, 178), (210, 200, 150)), "log_jungle": ((88, 70, 40), (150, 122, 78)),
    "log_acacia": ((104, 92, 84), (160, 130, 88)), "log_big_oak": ((50, 38, 22), (104, 82, 50)),
}
for name, (bark, ring) in LOGS.items():
    img = noisy((8, 8), bark + (255,), 6, name)
    px = img.load()
    dark = tuple(max(0, c - 30) for c in bark) + (255,)
    for y in range(8):
        for x in (1, 2, 5, 6):
            px[x, y] = dark
    save(img, "assets/minecraft/textures/blocks/%s.png" % name)
    top = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    tpx = top.load()
    for y in range(8):
        for x in range(8):
            d = min(x, y, 7 - x, 7 - y)
            tpx[x, y] = (bark if d == 0 or d % 2 == 1 else ring) + (255,)
    save(top, "assets/minecraft/textures/blocks/%s_top.png" % name)

# wool: the 16 dye colors
WOOL = {
    "white": (221, 221, 221), "orange": (219, 125, 62), "magenta": (179, 80, 188),
    "light_blue": (107, 138, 201), "yellow": (229, 202, 48), "lime": (122, 199, 76),
    "pink": (237, 158, 183), "gray": (70, 70, 70), "silver": (157, 164, 164),
    "cyan": (39, 131, 149), "purple": (126, 61, 181), "blue": (52, 57, 156),
    "brown": (102, 72, 49), "green": (85, 109, 27), "red": (151, 44, 35),
    "black": (26, 22, 22),
}
for color_name, base in WOOL.items():
    block("wool_colored_%s" % color_name, base + (255,), 5)

# glass: thin light border, clear middle, one faint streak
img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
px = img.load()
for i in range(8):
    px[i, 0] = px[i, 7] = px[0, i] = px[7, i] = (228, 246, 246, 210)
px[2, 5] = (228, 246, 246, 90)
save(img, "assets/minecraft/textures/blocks/glass.png")
img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
px = img.load()
for x in range(8):
    px[x, 3] = px[x, 4] = (228, 246, 246, 210)
save(img, "assets/minecraft/textures/blocks/glass_pane_top.png")

# water: flat semi-transparent blue
save(noisy((8, 8), (45, 95, 210, 190), 8, "water_still"), "assets/minecraft/textures/blocks/water_still.png")
save(noisy((8, 8), (45, 95, 210, 190), 8, "water_flow"), "assets/minecraft/textures/blocks/water_flow.png")

# low fire: only the bottom ~30% of the frame is filled
for layer, seed, rows in (("fire_layer_0", "fire0", 5), ("fire_layer_1", "fire1", 4)):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    rng = random.Random(seed)
    gradient = [(255, 210, 62), (255, 165, 31), (247, 122, 0), (230, 80, 0), (217, 54, 0)]
    for i, y in enumerate(range(15, 15 - rows, -1)):
        for x in range(16):
            if i >= 2 and rng.random() < 0.25:
                continue  # flicker gaps near the tips
            px[x, y] = gradient[min(i, len(gradient) - 1)] + (255,)
    save(img, "assets/minecraft/textures/blocks/%s.png" % layer)

# ---------------------------------------------------------------- items: short swords (16x16)
def sword(name, blade, blade_light):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([2, 13, 4, 11], fill=(80, 52, 26, 255), width=2)      # grip
    d.point((1, 14), fill=(170, 140, 60, 255))                    # pommel
    d.line([3, 9, 6, 12], fill=(150, 120, 60, 255), width=1)      # guard
    d.line([6, 9, 11, 4], fill=blade + (255,), width=2)           # short blade
    d.line([6, 9, 11, 4], fill=blade_light + (255,), width=1)     # blade highlight
    d.point((12, 3), fill=blade + (255,))                          # tip
    save(img, "assets/minecraft/textures/items/%s.png" % name)


sword("wooden_sword", (168, 134, 80), (196, 164, 105))
sword("stone_sword", (128, 128, 128), (168, 168, 168))
sword("iron_sword", (216, 216, 216), (242, 242, 242))
sword("golden_sword", (252, 214, 74), (255, 238, 150))
sword("diamond_sword", (84, 222, 204), (170, 255, 244))

# ---------------------------------------------------------------- misc + particles
save(Image.new("RGBA", (16, 16), (0, 0, 0, 0)), "assets/minecraft/textures/misc/pumpkinblur.png")

img = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
px = img.load()
for cell_y in range(8):
    for cell_x in range(8):
        ox, oy = cell_x * 16, cell_y * 16
        for y in range(16):
            for x in range(16):
                dx, dy = abs(x - 7.5), abs(y - 7.5)
                if dx < 1.5 and dy < 1.5:
                    px[ox + x, oy + y] = (255, 255, 255, 230)
                elif dx < 2.5 and dy < 2.5:
                    px[ox + x, oy + y] = (255, 255, 255, 110)
save(img, "assets/minecraft/textures/particle/particles.png")

# ---------------------------------------------------------------- pack.mcmeta + index
mcmeta = {"pack": {"pack_format": 1, "description": "Noob_mod_antor FPS Boost - 8x PvP pack"}}
with open(os.path.join(ROOT, "pack.mcmeta"), "w") as fh:
    json.dump(mcmeta, fh, indent=2)
FILES.insert(0, "pack.mcmeta")

with open(os.path.join(ROOT, "packfiles.txt"), "w") as fh:
    fh.write("\n".join(FILES) + "\n")

print("Generated %d pack files under %s" % (len(FILES), ROOT))
