package com.presetmanager;

import java.awt.image.BufferedImage;

/**
 * Original 16px raster insignia: a folded parchment with a three-slot
 * preset selector. Each pixel is authored explicitly, not rescaled artwork.
 *
 * . transparent, K outline, D shadow, P parchment, L highlight,
 * S slot, G selected slot.
 */
final class PresetIcon {
    private static final String[] PIXELS = {
        "................",
        "...KKKKKKKK.....",
        "..KLLLLLLLLK....",
        "..KPPPPPPPPKK...",
        "..KPKKKKKKPPK...",
        "..KPKSSSSKPPK...",
        "..KPKKKKKKPPK...",
        "..KPKGGGGKPPK...",
        "..KPKKKKKKPPK...",
        "..KPKSSSSKPPK...",
        "..KPKKKKKKPPK...",
        "..KPPPPPPPPPK...",
        "..KDDDDDDDDDK...",
        "...KKKKKKKKKK...",
        "................",
        "................"
    };

    static BufferedImage create() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < PIXELS.length; y++) {
            if (PIXELS[y].length() != 16)
                throw new IllegalStateException("Icon row must be 16 pixels");
            for (int x = 0; x < 16; x++) {
                image.setRGB(x, y, color(PIXELS[y].charAt(x)));
            }
        }
        return image;
    }

    private static int color(char pixel) {
        switch (pixel) {
            case '.': return 0x00000000;
            case 'K': return 0xFF29221B;
            case 'D': return 0xFF735639;
            case 'P': return 0xFFC5A476;
            case 'L': return 0xFFE9D5AC;
            case 'S': return 0xFF6B5843;
            case 'G': return 0xFF66B9A1;
            default: throw new IllegalArgumentException("Unknown pixel: " + pixel);
        }
    }

    private PresetIcon() {}
}
