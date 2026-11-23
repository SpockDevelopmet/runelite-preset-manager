package com.presetmanager;

import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class PresetIconTest {
    @Test public void hasExpectedDimensionsAndTransparentCorners() {
        BufferedImage icon = PresetIcon.create();
        assertEquals(16, icon.getWidth());
        assertEquals(16, icon.getHeight());
        assertEquals(0, icon.getRGB(0, 0) >>> 24);
        assertEquals(0, icon.getRGB(15, 15) >>> 24);
    }

    @Test public void selectedSlotIsVisible() {
        BufferedImage icon = PresetIcon.create();
        assertEquals(0xFF66B9A1, icon.getRGB(7, 7));
    }
}
