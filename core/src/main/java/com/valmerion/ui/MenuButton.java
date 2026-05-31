package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Black/transparent button with gold border and gold text.
 * Works entirely by touch — no keyboard.
 */
public class MenuButton {

    // Gold palette
    private static final Color GOLD        = new Color(1.00f, 0.84f, 0.20f, 1f);
    private static final Color GOLD_DIM    = new Color(0.70f, 0.58f, 0.10f, 1f);
    private static final Color BG_NORMAL   = new Color(0f,    0f,    0f,    0.65f);
    private static final Color BG_HOVER    = new Color(0.10f, 0.08f, 0f,    0.82f);

    private final String    label;
    private final Rectangle bounds;
    private final GlyphLayout layout = new GlyphLayout();

    private boolean justClicked = false;

    private static BitmapFont sharedFont;
    public  static void setFont(BitmapFont f) { sharedFont = f; }

    public MenuButton(Object ignored1, Object ignored2, String label,
                      float x, float y, float w, float h) {
        this.label  = label;
        this.bounds = new Rectangle(x, y, w, h);
    }

    public MenuButton(Object ignored, String label, float x, float y, float w, float h) {
        this(null, null, label, x, y, w, h);
    }

    public void render(SpriteBatch batch, float delta) {
        justClicked = false;
        boolean hovered = isHovered();
        if (hovered && Gdx.input.justTouched()) justClicked = true;

        com.badlogic.gdx.graphics.Texture px = HealthBar.getWhitePixel();
        if (px != null) {
            // Drop shadow
            batch.setColor(0f, 0f, 0f, 0.45f);
            batch.draw(px, bounds.x + 5f, bounds.y - 5f, bounds.width, bounds.height);
            // Background
            batch.setColor(hovered ? BG_HOVER : BG_NORMAL);
            batch.draw(px, bounds.x, bounds.y, bounds.width, bounds.height);

            // Gold border (2 px)
            Color border = hovered ? GOLD : GOLD_DIM;
            batch.setColor(border);
            float x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
            batch.draw(px, x,       y,       w,  2);
            batch.draw(px, x,       y+h-2,   w,  2);
            batch.draw(px, x,       y,       2,  h);
            batch.draw(px, x+w-2,   y,       2,  h);

            batch.setColor(Color.WHITE);
        }

        // Label
        BitmapFont f = getFont();
        if (f != null && label != null) {
            layout.setText(f, label);
            float tx = bounds.x + (bounds.width  - layout.width)  / 2f;
            float ty = bounds.y + (bounds.height + layout.height) / 2f;
            // Shadow
            f.setColor(0, 0, 0, 0.85f);
            f.draw(batch, label, tx + 2f, ty - 2f);
            // Gold text
            f.setColor(hovered ? Color.WHITE : GOLD);
            f.draw(batch, label, tx, ty);
            f.setColor(Color.WHITE);
        }
    }

    public boolean isJustClicked() { return justClicked; }

    private boolean isHovered() {
        float wx = Gdx.input.getX() / (float) Gdx.graphics.getWidth()  * 1280f;
        float wy = (1f - Gdx.input.getY() / (float) Gdx.graphics.getHeight()) * 720f;
        return bounds.contains(wx, wy);
    }

    private static BitmapFont getFont() {
        if (sharedFont != null) return sharedFont;
        sharedFont = new BitmapFont();
        sharedFont.getData().setScale(2.5f);
        return sharedFont;
    }
}
