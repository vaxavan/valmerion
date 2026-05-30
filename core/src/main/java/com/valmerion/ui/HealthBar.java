package com.valmerion.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Draws a simple HP bar using a 1×1 white pixel texture or coloured quads.
 * For now relies on batch colour + a shared white pixel texture.
 */
public class HealthBar {

    private static final Color BG_COLOR  = new Color(0.15f, 0.1f, 0.1f, 0.8f);
    private static final Color HP_COLOR  = new Color(0.8f, 0.15f, 0.15f, 1f);
    private static final Color HP_HIGH   = new Color(0.2f, 0.8f, 0.2f, 1f);
    private static final Color HP_MED    = new Color(0.9f, 0.75f, 0.1f, 1f);

    private final float x, y, width, height;
    private float displayRatio = 1f;  // smoothed towards actual ratio
    private final String label;

    // Shared pixel — set once from AcademyScreen
    private static com.badlogic.gdx.graphics.Texture whitePixel;

    public static void setWhitePixel(com.badlogic.gdx.graphics.Texture t) { whitePixel = t; }
    public static com.badlogic.gdx.graphics.Texture getWhitePixel()       { return whitePixel; }

    public HealthBar(float x, float y, float w, float h, String label) {
        this.x = x; this.y = y; this.width = w; this.height = h;
        this.label = label;
    }

    public void update(float delta, float hp, float maxHp) {
        float target = maxHp > 0 ? hp / maxHp : 0f;
        displayRatio = MathUtils.lerp(displayRatio, target, delta * 6f);
    }

    public void render(SpriteBatch batch) {
        if (whitePixel == null) return;

        // Background
        batch.setColor(BG_COLOR);
        batch.draw(whitePixel, x, y, width, height);

        // HP fill
        Color hpColor = displayRatio > 0.5f ? HP_HIGH
                      : displayRatio > 0.25f ? HP_MED
                      : HP_COLOR;
        batch.setColor(hpColor);
        batch.draw(whitePixel, x + 2, y + 2, (width - 4) * displayRatio, height - 4);

        batch.setColor(Color.WHITE);
    }
}
