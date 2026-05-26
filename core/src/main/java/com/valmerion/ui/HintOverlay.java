package com.valmerion.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.valmerion.utils.Constants;
import com.valmerion.utils.PlaceholderTextures;

/**
 * Temporary tutorial hint: fade-in → hold → fade-out cycle.
 *
 * <p>Falls back to the LibGDX default BitmapFont if no custom font is supplied.
 */
public class HintOverlay {

    private String  text   = "";
    private float   alpha  = 0f;
    private float   timer  = 0f;
    private boolean active = false;

    private final BitmapFont  font;
    private final GlyphLayout layout;

    /** @param font may be null — a fallback font is used instead. */
    public HintOverlay(BitmapFont font) {
        this.font   = font != null ? font : new BitmapFont();
        this.layout = new GlyphLayout();
    }

    /** Display a new hint message from the beginning of its cycle. */
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
        float fadeDur    = Constants.HINT_FADE_DURATION;
        float displayDur = Constants.HINT_DISPLAY_TIME;
        float total      = fadeDur * 2 + displayDur;

        if (timer < fadeDur) {
            alpha = timer / fadeDur;
        } else if (timer < fadeDur + displayDur) {
            alpha = 1f;
        } else if (timer < total) {
            alpha = 1f - (timer - fadeDur - displayDur) / fadeDur;
        } else {
            active = false;
            alpha  = 0f;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active || alpha <= 0f) return;

        layout.setText(font, text);
        float x = (Constants.WORLD_WIDTH  - layout.width)  / 2f;
        float y = Constants.WORLD_HEIGHT - 55f;

        // Dark backdrop
        com.badlogic.gdx.graphics.Texture wp = PlaceholderTextures.whitePixel();
        if (wp != null) {
            float pad = 14f;
            batch.setColor(0f, 0f, 0f, alpha * 0.55f);
            batch.draw(wp, x - pad, y - layout.height - pad / 2f,
                       layout.width + pad * 2, layout.height + pad);
            batch.setColor(Color.WHITE);
        }

        // Shadow
        font.setColor(0f, 0f, 0f, alpha * 0.65f);
        font.draw(batch, text, x + 2f, y - 2f);

        // Text
        font.setColor(1f, 0.95f, 0.70f, alpha);
        font.draw(batch, text, x, y);
        font.setColor(Color.WHITE);
    }
}
