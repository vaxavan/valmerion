package com.valmerion.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.valmerion.utils.PlaceholderTextures;

/**
 * Animated health bar — green → yellow → red as HP falls.
 *
 * <p>Uses a white-pixel texture (set via {@link #setWhitePixel(Texture)})
 * to draw filled rectangles.
 */
public class HealthBar {

    private static final float LERP_SPEED = 5f;

    private static Texture whitePixel;

    /** Provide a 1×1 white pixel texture for solid-colour rendering. */
    public static void setWhitePixel(Texture t) { whitePixel = t; }

    // ── Layout ────────────────────────────────────────────────────────────────
    private final float x, y, w, h;
    private final String label;

    // ── State ─────────────────────────────────────────────────────────────────
    private float displayRatio = 1f;   // smoothly interpolated

    // ── Font (optional) ───────────────────────────────────────────────────────
    private static BitmapFont labelFont;
    private final GlyphLayout layout = new GlyphLayout();

    public HealthBar(float x, float y, float w, float h, String label) {
        this.x     = x;
        this.y     = y;
        this.w     = w;
        this.h     = h;
        this.label = label;
    }

    /** Update the displayed HP value (lerps toward target). */
    public void update(float delta, float currentHp, float maxHp) {
        float target  = maxHp > 0 ? currentHp / maxHp : 0f;
        displayRatio += (target - displayRatio) * LERP_SPEED * delta;
        displayRatio  = Math.max(0f, Math.min(1f, displayRatio));
    }

    public void render(SpriteBatch batch) {
        Texture px = whitePixel != null ? whitePixel : PlaceholderTextures.whitePixel();
        if (px == null) return;

        // Background (dark)
        batch.setColor(0.15f, 0.10f, 0.10f, 0.85f);
        batch.draw(px, x, y, w, h);

        // Filled portion — colour depends on ratio
        float r, g;
        if (displayRatio > 0.5f) {
            // green → yellow
            float t = (displayRatio - 0.5f) * 2f;
            r = 1f - t; g = 1f;
        } else {
            // yellow → red
            float t = displayRatio * 2f;
            r = 1f; g = t;
        }
        batch.setColor(r, g, 0.1f, 0.90f);
        batch.draw(px, x + 1, y + 1, (w - 2) * displayRatio, h - 2);

        // Border
        batch.setColor(0.55f, 0.50f, 0.45f, 1f);
        batch.draw(px, x,         y,         w, 1f);
        batch.draw(px, x,         y + h - 1, w, 1f);
        batch.draw(px, x,         y,         1f, h);
        batch.draw(px, x + w - 1, y,         1f, h);

        // Label
        BitmapFont f = getFont();
        layout.setText(f, label);
        f.setColor(0.9f, 0.85f, 0.75f, 1f);
        f.draw(batch, label, x + (w - layout.width) / 2f, y + h + layout.height + 2f);
        f.setColor(1f, 1f, 1f, 1f);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private static BitmapFont getFont() {
        if (labelFont == null) {
            labelFont = new BitmapFont();
            labelFont.getData().setScale(0.85f);
        }
        return labelFont;
    }
}
