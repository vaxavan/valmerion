package com.valmerion.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class HealthBar {

    private final float  x, y, width, height;
    private final String label;
    private final boolean isHunger;
    private float displayRatio = 1f;

    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();

    private static com.badlogic.gdx.graphics.Texture whitePixel;
    public static void setWhitePixel(com.badlogic.gdx.graphics.Texture t) { whitePixel = t; }
    public static com.badlogic.gdx.graphics.Texture getWhitePixel()       { return whitePixel; }

    public HealthBar(float x, float y, float w, float h, String label, BitmapFont font) {
        this.x = x; this.y = y; this.width = w; this.height = h;
        this.label    = label;
        this.font     = font;
        this.isHunger = label != null && label.contains("олод");
    }

    public HealthBar(float x, float y, float w, float h, String label) {
        this(x, y, w, h, label, null);
    }

    public void update(float delta, float val, float max) {
        float target = max > 0 ? val / max : 0f;
        displayRatio = MathUtils.lerp(displayRatio, target, delta * 6f);
    }

    public void render(SpriteBatch batch) {
        if (whitePixel == null) return;

        // ── Background ────────────────────────────────────────────────────────
        batch.setColor(0.05f, 0.04f, 0.10f, 0.82f);
        batch.draw(whitePixel, x, y, width, height);

        // ── Fill bar ──────────────────────────────────────────────────────────
        Color fill;
        if (isHunger) {
            fill = displayRatio > 0.5f ? new Color(0.90f, 0.58f, 0.08f, 1f)
                 : displayRatio > 0.25f ? new Color(0.95f, 0.35f, 0.05f, 1f)
                 :                        new Color(0.80f, 0.10f, 0.05f, 1f);
        } else {
            fill = displayRatio > 0.5f  ? new Color(0.15f, 0.78f, 0.22f, 1f)
                 : displayRatio > 0.25f ? new Color(0.92f, 0.76f, 0.08f, 1f)
                 :                        new Color(0.88f, 0.12f, 0.12f, 1f);
        }
        batch.setColor(fill);
        float fillW = (width - 4f) * displayRatio;
        batch.draw(whitePixel, x + 2, y + 2, fillW, height - 4f);

        // Shine strip on top of fill
        if (fillW > 4f) {
            batch.setColor(1f, 1f, 1f, 0.12f);
            batch.draw(whitePixel, x + 2, y + height - 5f, fillW, 3f);
        }

        // ── Thin border ───────────────────────────────────────────────────────
        batch.setColor(0.55f, 0.45f, 0.70f, 0.70f);
        batch.draw(whitePixel, x,           y,            width,  1.5f);
        batch.draw(whitePixel, x,           y+height-1.5f, width, 1.5f);
        batch.draw(whitePixel, x,           y,            1.5f,   height);
        batch.draw(whitePixel, x+width-1.5f, y,           1.5f,   height);

        // ── Label (small, gold, left-aligned) ─────────────────────────────────
        if (font != null && label != null) {
            float origSx = font.getScaleX(), origSy = font.getScaleY();
            font.getData().setScale(0.55f);
            layout.setText(font, label);
            float tx = x + 5f;
            float ty = y + height / 2f + layout.height / 2f;
            // Shadow
            font.setColor(0f, 0f, 0f, 0.75f);
            font.draw(batch, label, tx + 1f, ty - 1f);
            // Gold text
            font.setColor(isHunger ? new Color(1f, 0.82f, 0.40f, 1f)
                                   : new Color(0.85f, 0.95f, 0.85f, 1f));
            font.draw(batch, label, tx, ty);
            font.getData().setScale(origSx, origSy);
            font.setColor(Color.WHITE);
        }

        batch.setColor(Color.WHITE);
    }
}
