import os
import math
from PIL import Image, ImageDraw

def render_icon(size=512, is_round=False):
    # Render at 4x resolution for pristine anti-aliasing
    scale = 4
    w = size * scale
    h = size * scale

    # Colors sampled from inspo
    bg_yellow = (252, 230, 153, 255)    # Warm buttery pastel cream
    mid_lavender = (145, 122, 244, 255) # Periwinkle / lavender card
    bottom_dark = (16, 20, 26, 255)     # Deep midnight charcoal
    white_check = (255, 255, 255, 255)

    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # 1. Base cream yellow background
    draw.rectangle([0, 0, w, h], fill=bg_yellow)

    # 2. Middle lavender layer
    lav_points = []
    lav_points.append((0, h))
    lav_points.append((0, int(h * 0.38)))

    steps = 100
    for i in range(steps + 1):
        t = i / float(steps)
        p0 = (0.00, 0.38)
        p1 = (0.06, 0.27)
        p2 = (0.42, 0.30)
        p3 = (1.00, 0.40)
        
        x = ((1-t)**3 * p0[0] + 3*(1-t)**2*t * p1[0] + 3*(1-t)*t**2 * p2[0] + t**3 * p3[0]) * w
        y = ((1-t)**3 * p0[1] + 3*(1-t)**2*t * p1[1] + 3*(1-t)*t**2 * p2[1] + t**3 * p3[1]) * h
        lav_points.append((int(x), int(y)))
    
    lav_points.append((w, h))
    draw.polygon(lav_points, fill=mid_lavender)

    # 3. Bottom midnight dark layer
    dark_points = []
    dark_points.append((0, h))
    dark_points.append((0, int(h * 0.58)))

    for i in range(steps + 1):
        t = i / float(steps)
        p0 = (0.00, 0.58)
        p1 = (0.35, 0.54)
        p2 = (0.80, 0.50)
        p3 = (1.00, 0.56)
        
        x = ((1-t)**3 * p0[0] + 3*(1-t)**2*t * p1[0] + 3*(1-t)*t**2 * p2[0] + t**3 * p3[0]) * w
        y = ((1-t)**3 * p0[1] + 3*(1-t)**2*t * p1[1] + 3*(1-t)*t**2 * p2[1] + t**3 * p3[1]) * h
        dark_points.append((int(x), int(y)))

    dark_points.append((w, h))
    draw.polygon(dark_points, fill=bottom_dark)

    # 4. White rounded checkmark
    # Position: in bottom-left quadrant of the dark layer
    chk_start = (int(w * 0.19), int(h * 0.64))
    chk_mid = (int(w * 0.27), int(h * 0.72))
    chk_end = (int(w * 0.38), int(h * 0.60))
    chk_width = int(w * 0.052)

    # Draw rounded checkmark segments
    draw.line([chk_start, chk_mid], fill=white_check, width=chk_width)
    draw.line([chk_mid, chk_end], fill=white_check, width=chk_width)
    r = chk_width // 2
    draw.ellipse([chk_start[0]-r, chk_start[1]-r, chk_start[0]+r, chk_start[1]+r], fill=white_check)
    draw.ellipse([chk_mid[0]-r, chk_mid[1]-r, chk_mid[0]+r, chk_mid[1]+r], fill=white_check)
    draw.ellipse([chk_end[0]-r, chk_end[1]-r, chk_end[0]+r, chk_end[1]+r], fill=white_check)

    # 5. Masking if round or squircle for legacy icons
    mask = Image.new("L", (w, h), 0)
    draw_mask = ImageDraw.Draw(mask)
    if is_round:
        draw_mask.ellipse([0, 0, w, h], fill=255)
    else:
        # Modern smooth squircle (28% radius)
        corner_r = int(w * 0.28)
        draw_mask.rounded_rectangle([0, 0, w, h], radius=corner_r, fill=255)

    output = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    output.paste(img, (0, 0), mask=mask)

    # Downscale smoothly with high-quality Lanczos filter
    final_img = output.resize((size, size), Image.Resampling.LANCZOS)
    return final_img

def generate_all_android_icons(res_dir):
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    for folder, px in densities.items():
        folder_path = os.path.join(res_dir, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        sq = render_icon(px, is_round=False)
        sq.save(os.path.join(folder_path, "ic_launcher.png"))

        rd = render_icon(px, is_round=True)
        rd.save(os.path.join(folder_path, "ic_launcher_round.png"))

    print("All density mipmaps generated successfully!")

if __name__ == "__main__":
    out_dir = r"C:\Users\hp\.gemini\antigravity-ide\brain\e5dc712b-cc53-431d-81e3-a10e13c04c2e"
    os.makedirs(out_dir, exist_ok=True)
    preview = render_icon(512, is_round=False)
    preview.save(os.path.join(out_dir, "icon_preview_squircle.png"))
    
    round_preview = render_icon(512, is_round=True)
    round_preview.save(os.path.join(out_dir, "icon_preview_round.png"))
    print("Previews saved successfully")
