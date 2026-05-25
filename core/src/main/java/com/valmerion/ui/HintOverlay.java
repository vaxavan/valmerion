package com.valmerion.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.valmerion.utils.Constants;

/**
 * Displays a temporary tutorial hint that fades in, holds, then fades out.
 */
public class HintOverlay {

    private String text     = "";
    private float  alpha    = 0f;
    private float  timer    = 0f;
    private boolean active  = false;

    private final BitmapFont  font;
    private final GlyphLayout layout;

    public HintOverlay(BitmapFont font) {
        this.font   = font;
        this.layout = new GlyphLayout();
    }

    /** Show a new hint message. */
    public void show(String message) {
        text   = message;
        alpha  = 0f;
        timer  = 0f;
        active = true;
    }

    public boolean isActive() { return active; }

    public void update(float delta) {
        if (!active) return;
        timer += delta;
        float total = Constants.HINT_FADE_DURATION * 2 + Constants.HINT_DISPLAY_TIME;

        if (timer < Constants.HINT_FADE_DURATION) {
            // Fade in
            alpha = timer / Constants.HINT_FADE_DURATION;
        } else if (timer < Constants.HINT_FADE_DURATION + Constants.HINT_DISPLAY_TIME) {
            // Hold
            alpha = 1f;
        } else if (timer < total) {
            // Fade out
            alpha = 1f - (timer - Constants.HINT_FADE_DURATION - Constants.HINT_DISPLAY_TIME)
                    / Constants.HINT_FADE_DURATION;
        } else {
            active = false;
            alpha  = 0f;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active || font == null || alpha <= 0) return;

        layout.setText(font, text);
        float x = (Constants.WORLD_WIDTH  - layout.width)  / 2f;
        float y = Constants.WORLD_HEIGHT  - 60f;

        // Dark shadow
        font.setColor(0, 0, 0, alpha * 0.7f);
        font.draw(batch, text, x + 2, y - 2);

        font.setColor(1f, 0.95f, 0.7f, alpha);
        font.draw(batch, text, x, y);
        font.setColor(Color.WHITE);
    }
}
