package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Clickable menu button. Caller must supply the current touch position
 * in world-space (unprojected through the viewport) to {@link #render}.
 *
 * <p>Coordinate system: world-space at 1280×720.
 */
public class MenuButton {

    private static final float HOVER_SCALE = 1.05f;
    private static final float SCALE_SPEED = 10f;

    private final Texture   texNormal;
    private final Texture   texHover;
    private final String    label;
    private final Rectangle bounds;
    private final GlyphLayout layout = new GlyphLayout();

    private float   scale       = 1f;
    private boolean hovered     = false;
    private boolean justClicked = false;

    private static BitmapFont fallbackFont;

    public MenuButton(Texture texNormal, Texture texHover, String label,
                      float x, float y, float w, float h) {
        this.texNormal = texNormal;
        this.texHover  = texHover;
        this.label     = label;
        this.bounds    = new Rectangle(x, y, w, h);
    }

    public MenuButton(Texture texNormal, String label,
                      float x, float y, float w, float h) {
        this(texNormal, null, label, x, y, w, h);
    }

    /**
     * Update and draw.
     *
     * @param batch   active SpriteBatch (viewport already applied)
     * @param delta   frame time
     * @param touchWx touch X in world coordinates (viewport-unprojected)
     * @param touchWy touch Y in world coordinates (viewport-unprojected)
     */
    public void render(SpriteBatch batch, float delta, float touchWx, float touchWy) {
        justClicked = false;
        hovered     = bounds.contains(touchWx, touchWy);

        if (hovered && Gdx.input.justTouched()) {
            justClicked = true;
        }

        float target = hovered ? HOVER_SCALE : 1f;
        scale += (target - scale) * SCALE_SPEED * delta;

        float sw = bounds.width  * scale;
        float sh = bounds.height * scale;
        float dx = bounds.x + (bounds.width  - sw) / 2f;
        float dy = bounds.y + (bounds.height - sh) / 2f;

        if (texNormal != null) {
            Texture draw = (hovered && texHover != null) ? texHover : texNormal;
            if (hovered && texHover == null) batch.setColor(1f, 1f, 0.85f, 1f);
            batch.draw(draw, dx, dy, sw, sh);
            batch.setColor(Color.WHITE);
        } else {
            drawFallback(batch, dx, dy, sw, sh);
        }
    }

    /** @return true only on the exact frame the button was clicked/tapped. */
    public boolean isJustClicked() { return justClicked; }

    // ── Private ───────────────────────────────────────────────────────────────

    private void drawFallback(SpriteBatch batch, float x, float y, float w, float h) {
        BitmapFont f = getFallbackFont();
        layout.setText(f, label);
        f.setColor(hovered ? Color.YELLOW : Color.LIGHT_GRAY);
        f.draw(batch, label,
               x + (w - layout.width)  / 2f,
               y + (h + layout.height) / 2f);
        f.setColor(Color.WHITE);
    }

    private static BitmapFont getFallbackFont() {
        if (fallbackFont == null) {
            fallbackFont = new BitmapFont();
            fallbackFont.getData().setScale(2.2f);
        }
        return fallbackFont;
    }
}
