package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Standard menu button — colored rectangle + text label centered on it.
 * Texture is drawn as background if provided, otherwise a solid color is used.
 * Always shows the label text on top regardless of texture.
 */
public class MenuButton {

    private static final Color COLOR_NORMAL  = new Color(0.12f, 0.10f, 0.22f, 0.92f);
    private static final Color COLOR_HOVER   = new Color(0.22f, 0.18f, 0.42f, 0.97f);
    private static final Color COLOR_BORDER  = new Color(0.55f, 0.45f, 0.85f, 1f);
    private static final Color COLOR_TEXT    = new Color(0.95f, 0.90f, 1.00f, 1f);
    private static final Color COLOR_SHADOW  = new Color(0f, 0f, 0f, 0.7f);

    private final Texture   texNormal;
    private final String    label;
    private final Rectangle bounds;
    private final GlyphLayout layout = new GlyphLayout();

    private boolean hovered     = false;
    private boolean justClicked = false;

    private static BitmapFont sharedFont;

    public MenuButton(Texture texNormal, Texture texHover, String label,
                      float x, float y, float w, float h) {
        this.texNormal = texNormal;
        this.label     = label;
        this.bounds    = new Rectangle(x, y, w, h);
    }

    public MenuButton(Texture texNormal, String label,
                      float x, float y, float w, float h) {
        this(texNormal, null, label, x, y, w, h);
    }

    public void render(SpriteBatch batch, float delta) {
        justClicked = false;
        updateInput();

        Texture px = HealthBar.getWhitePixel();

        if (texNormal != null) {
            // Draw texture, slightly brightened on hover
            batch.setColor(hovered ? 1.1f : 1f, hovered ? 1.1f : 1f, hovered ? 1.1f : 1f, 1f);
            batch.draw(texNormal, bounds.x, bounds.y, bounds.width, bounds.height);
            batch.setColor(Color.WHITE);
        } else if (px != null) {
            // Solid color button
            batch.setColor(hovered ? COLOR_HOVER : COLOR_NORMAL);
            batch.draw(px, bounds.x, bounds.y, bounds.width, bounds.height);
            // Border
            batch.setColor(COLOR_BORDER);
            batch.draw(px, bounds.x,                       bounds.y,                       bounds.width,  3);
            batch.draw(px, bounds.x,                       bounds.y + bounds.height - 3,   bounds.width,  3);
            batch.draw(px, bounds.x,                       bounds.y,                       3,  bounds.height);
            batch.draw(px, bounds.x + bounds.width - 3,    bounds.y,                       3,  bounds.height);
            batch.setColor(Color.WHITE);
        }

        // Always draw text on top
        BitmapFont f = getFont();
        if (f != null) {
            layout.setText(f, label);
            float tx = bounds.x + (bounds.width  - layout.width)  / 2f;
            float ty = bounds.y + (bounds.height + layout.height) / 2f;
            // Shadow
            f.setColor(COLOR_SHADOW);
            f.draw(batch, label, tx + 2f, ty - 2f);
            // Text
            f.setColor(hovered ? Color.YELLOW : COLOR_TEXT);
            f.draw(batch, label, tx, ty);
            f.setColor(Color.WHITE);
        }
    }

    public boolean isJustClicked() { return justClicked; }

    public static void setFont(BitmapFont font) { sharedFont = font; }

    private void updateInput() {
        float wx = Gdx.input.getX() / (float) Gdx.graphics.getWidth()  * 1280f;
        float wy = (1f - Gdx.input.getY() / (float) Gdx.graphics.getHeight()) * 720f;
        hovered = bounds.contains(wx, wy);
        if (hovered && Gdx.input.justTouched()) justClicked = true;
    }

    private static BitmapFont getFont() {
        if (sharedFont != null) return sharedFont;
        // Fallback built-in font
        if (sharedFont == null) {
            sharedFont = new BitmapFont();
            sharedFont.getData().setScale(2.5f);
        }
        return sharedFont;
    }
}
