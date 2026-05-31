package com.valmerion.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class HealthBar {

    private static final Color BG_COLOR = new Color(0.1f, 0.1f, 0.1f, 0.85f);
    private static final Color HP_HIGH  = new Color(0.15f, 0.75f, 0.15f, 1f);
    private static final Color HP_MED   = new Color(0.9f, 0.75f, 0.1f,  1f);
    private static final Color HP_LOW   = new Color(0.85f, 0.15f, 0.15f, 1f);
    private static final Color HUNGER_C = new Color(0.85f, 0.55f, 0.1f, 1f);

    private final float x, y, width, height;
    private final String  label;
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
        this.isHunger = label.contains("олод");
    }

    /** Legacy constructor without font — label won't render */
    public HealthBar(float x, float y, float w, float h, String label) {
        this(x, y, w, h, label, null);
    }

    public void update(float delta, float val, float max) {
        float target = max > 0 ? val / max : 0f;
        displayRatio = MathUtils.lerp(displayRatio, target, delta * 6f);
    }

    public void render(SpriteBatch batch) {
        if (whitePixel == null) return;

        // Background bar
        batch.setColor(BG_COLOR);
        batch.draw(whitePixel, x, y, width, height);

        // Fill
        Color fill = isHunger ? HUNGER_C
                   : displayRatio > 0.5f ? HP_HIGH
                   : displayRatio > 0.25f ? HP_MED
                   : HP_LOW;
        batch.setColor(fill);
        batch.draw(whitePixel, x + 2, y + 2, (width - 4) * displayRatio, height - 4);

        // Label
        if (font != null && label != null) {
            layout.setText(font, label);
            // Shadow
            font.setColor(0f, 0f, 0f, 0.9f);
            font.draw(batch, label, x + 7f, y + height - 4f);
            // Text
            font.setColor(Color.WHITE);
            font.draw(batch, label, x + 6f, y + height - 3f);
            font.setColor(Color.WHITE);
        }

        batch.setColor(Color.WHITE);
    }
}
