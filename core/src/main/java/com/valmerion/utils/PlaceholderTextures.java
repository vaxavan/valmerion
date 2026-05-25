package com.valmerion.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

/**
 * Generates simple colored placeholder textures so the game can run
 * before real art assets are placed in the assets/ folder.
 *
 * <p>All textures created here must be disposed when no longer needed.
 * They are NOT managed by AssetLoader.
 */
public final class PlaceholderTextures implements Disposable {

    /** 1×1 fully opaque white pixel — useful for tinted quads. */
    public final Texture white;

    /** Generic dark button placeholder. */
    public final Texture button;

    /** Dark atmospheric menu background. */
    public final Texture menuBg;

    public PlaceholderTextures() {
        white   = solid(1, 1, Color.WHITE);
        button  = roundedRect(320, 80, new Color(0.18f, 0.14f, 0.28f, 1f),
                                       new Color(0.7f, 0.55f, 0.2f, 1f));
        menuBg  = gradient(1280, 720,
                           new Color(0.04f, 0.03f, 0.08f, 1f),
                           new Color(0.10f, 0.07f, 0.18f, 1f));
    }

    @Override
    public void dispose() {
        white .dispose();
        button.dispose();
        menuBg.dispose();
    }

    // ── Factories ─────────────────────────────────────────────────────────────

    /** Single-color solid texture. */
    public static Texture solid(int w, int h, Color color) {
        Pixmap pm = new Pixmap(w, h, Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Rectangle with a coloured border (2 px).
     */
    public static Texture roundedRect(int w, int h, Color fill, Color border) {
        Pixmap pm = new Pixmap(w, h, Format.RGBA8888);
        pm.setColor(fill);
        pm.fill();
        pm.setColor(border);
        pm.drawRectangle(0, 0, w, h);
        pm.drawRectangle(1, 1, w - 2, h - 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Vertical linear gradient from {@code top} to {@code bottom}.
     */
    public static Texture gradient(int w, int h, Color top, Color bottom) {
        Pixmap pm = new Pixmap(w, h, Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float t = y / (float) h;
            float r = top.r + (bottom.r - top.r) * t;
            float g = top.g + (bottom.g - top.g) * t;
            float b = top.b + (bottom.b - top.b) * t;
            pm.setColor(r, g, b, 1f);
            pm.drawLine(0, y, w - 1, y);
        }
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }
}
