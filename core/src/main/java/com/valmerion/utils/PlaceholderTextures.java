package com.valmerion.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Procedurally generated placeholder textures.
 * Used when real art files are absent.
 *
 * <p>All textures are lazily created and cached as singletons.
 * Call {@link #disposeAll()} on application shutdown.
 */
public final class PlaceholderTextures {

    private PlaceholderTextures() {}

    // ── Cached instances ─────────────────────────────────────────────────────

    private static Texture white;
    private static Texture button;
    private static Texture menuBg;

    // ── Public API ────────────────────────────────────────────────────────────

    /** 1×1 white pixel — used for solid-colour drawing. */
    public static Texture whitePixel() {
        if (white == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            white = new Texture(pm);
            pm.dispose();
        }
        return white;
    }

    /** Dark-bordered button placeholder (320×80). */
    public static Texture button() {
        if (button == null) {
            int w = 320, h = 80;
            Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

            pm.setColor(0.12f, 0.12f, 0.20f, 1f);
            pm.fill();

            pm.setColor(0.75f, 0.65f, 0.20f, 1f);  // gold border
            pm.drawRectangle(0, 0, w, h);
            pm.drawRectangle(2, 2, w - 4, h - 4);

            button = new Texture(pm);
            pm.dispose();
        }
        return button;
    }

    /** 1280×720 purple gradient menu background. */
    public static Texture menuBackground() {
        if (menuBg == null) {
            int w = 256, h = 144; // scaled down for memory
            Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            for (int y = 0; y < h; y++) {
                float t = (float) y / h;
                pm.setColor(0.07f + t * 0.12f, 0.04f, 0.18f + t * 0.10f, 1f);
                pm.drawLine(0, y, w, y);
            }
            menuBg = new Texture(pm);
            pm.dispose();
        }
        return menuBg;
    }

    public static void disposeAll() {
        if (white  != null) { white.dispose();  white  = null; }
        if (button != null) { button.dispose(); button = null; }
        if (menuBg != null) { menuBg.dispose(); menuBg = null; }
    }
}
