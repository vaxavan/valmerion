package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Clickable button that shows a texture (with optional hover texture)
 * or falls back to a coloured label when textures are absent.
 *
 * <p>Coordinate system: world-space at 1280×720 (FitViewport).
 */
public class MenuButton {

    private static final float HOVER_SCALE = 1.05f;
    private static final float SCALE_SPEED = 10f;

    private final Texture   texNormal;
    private final Texture   texHover;   // may be null → tint normal instead
    private final String    label;
    private final Rectangle bounds;
    private final GlyphLayout layout = new GlyphLayout();

    private float   scale        = 1f;
    private boolean hovered      = false;
    private boolean justClicked  = false;

    // Lazily created fallback font (shared across instances)
    private static BitmapFont fallbackFont;

    /**
     * @param texNormal normal-state texture (may be null)
     * @param texHover  hover-state texture  (may be null)
     * @param label     fallback / accessibility label
     */
    public MenuButton(Texture texNormal, Texture texHover, String label,
                      float x, float y, float w, float h) {
        this.texNormal = texNormal;
        this.texHover  = texHover;
        this.label     = label;
        this.bounds    = new Rectangle(x, y, w, h);
    }

    /** Convenience constructor without a hover texture. */
    public MenuButton(Texture texNormal, String label,
                      float x, float y, float w, float h) {
        this(texNormal, null, label, x, y, w, h);
    }

    /**
     * Update + draw. Must be called while the SpriteBatch is active and
     * the viewport has already been applied.
     */
    public void render(SpriteBatch batch, float delta) {
        justClicked = false;
        updateHover();

        // Smooth scale lerp
        float target = hovered ? HOVER_SCALE : 1f;
        scale += (target - scale) * SCALE_SPEED * delta;

        float sw = bounds.width  * scale;
        float sh = bounds.height * scale;
        float dx = bounds.x + (bounds.width  - sw) / 2f;
        float dy = bounds.y + (bounds.height - sh) / 2f;

        if (texNormal != null) {
            Texture draw = (hovered && texHover != null) ? texHover : texNormal;
            // Brighten on hover when there is no separate hover texture
            if (hovered && texHover == null) {
                batch.setColor(1f, 1f, 0.85f, 1f);
            }
            batch.draw(draw, dx, dy, sw, sh);
            batch.setColor(Color.WHITE);
        } else {
            drawFallback(batch, dx, dy, sw, sh);
        }
    }

    /** @return true only on the exact frame the button was clicked. */
    public boolean isJustClicked() { return justClicked; }

    // ── Private ───────────────────────────────────────────────────────────────

    private void updateHover() {
        // Remap raw mouse coords to world coords (FitViewport 1280×720)
        float wx = Gdx.input.getX() / (float) Gdx.graphics.getWidth()  * 1280f;
        float wy = (1f - Gdx.input.getY() / (float) Gdx.graphics.getHeight()) * 720f;

        hovered = bounds.contains(wx, wy);
        if (hovered && Gdx.input.justTouched()) {
            justClicked = true;
        }
    }

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
