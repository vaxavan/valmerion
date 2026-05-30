package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.utils.Constants;

/**
 * Semi-transparent pause overlay — ESC to toggle.
 * Returns true from {@link #wantsMainMenu()} when player clicks "Главное меню".
 */
public class PauseOverlay {

    private static final float PANEL_W = 420f;
    private static final float PANEL_H = 260f;
    private static final float BTN_W   = 320f;
    private static final float BTN_H   = 70f;

    private boolean paused = false;
    private boolean wantsMenu = false;

    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Shared white pixel from HealthBar
    private final Rectangle btnResume;
    private final Rectangle btnMenu;

    private boolean prevEsc = false;

    public PauseOverlay(BitmapFont font) {
        this.font = font;

        float cx = (Constants.WORLD_WIDTH  - PANEL_W) / 2f;
        float cy = (Constants.WORLD_HEIGHT - PANEL_H) / 2f;

        float bx = (Constants.WORLD_WIDTH - BTN_W) / 2f;
        btnResume = new Rectangle(bx, cy + PANEL_H / 2f - 10f,       BTN_W, BTN_H);
        btnMenu   = new Rectangle(bx, cy + PANEL_H / 2f - 10f - BTN_H - 20f, BTN_W, BTN_H);
    }

    /** Call every frame before player update. Returns true while paused. */
    public boolean update() {
        boolean escNow = Gdx.input.isKeyPressed(Keys.ESCAPE);
        if (escNow && !prevEsc) paused = !paused;
        prevEsc = escNow;

        if (!paused) return false;

        if (Gdx.input.justTouched()) {
            float wx = screenToWorldX(Gdx.input.getX());
            float wy = screenToWorldY(Gdx.input.getY());

            if (btnResume.contains(wx, wy)) paused = false;
            if (btnMenu.contains(wx, wy))   wantsMenu = true;
        }
        return true;
    }

    public boolean isPaused()       { return paused; }
    public boolean wantsMainMenu()  { boolean v = wantsMenu; wantsMenu = false; return v; }

    public void render(SpriteBatch batch) {
        if (!paused) return;
        com.badlogic.gdx.graphics.Texture px = HealthBar.getWhitePixel();
        if (px == null) return;

        // Dim overlay
        batch.setColor(0f, 0f, 0f, 0.6f);
        batch.draw(px, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        // Panel background
        float cx = (Constants.WORLD_WIDTH  - PANEL_W) / 2f;
        float cy = (Constants.WORLD_HEIGHT - PANEL_H) / 2f;
        batch.setColor(0.08f, 0.06f, 0.15f, 0.95f);
        batch.draw(px, cx, cy, PANEL_W, PANEL_H);

        // Panel border
        batch.setColor(0.5f, 0.4f, 0.8f, 1f);
        batch.draw(px, cx,           cy,            PANEL_W, 3);
        batch.draw(px, cx,           cy+PANEL_H-3,  PANEL_W, 3);
        batch.draw(px, cx,           cy,            3,       PANEL_H);
        batch.draw(px, cx+PANEL_W-3, cy,            3,       PANEL_H);

        // "ПАУЗА" title
        if (font != null) {
            drawCentred(batch, "— ПАУЗА —", cy + PANEL_H - 55f, 0.9f, 0.8f, 1f, 1f);
        }

        // Buttons
        drawButton(batch, btnResume, "Продолжить", 0.2f, 0.75f, 0.3f);
        drawButton(batch, btnMenu,   "Главное меню", 0.75f, 0.25f, 0.25f);

        batch.setColor(Color.WHITE);
    }

    private void drawButton(SpriteBatch batch, Rectangle r, String label, float red, float green, float blue) {
        com.badlogic.gdx.graphics.Texture px = HealthBar.getWhitePixel();
        batch.setColor(red * 0.4f, green * 0.4f, blue * 0.4f, 0.9f);
        batch.draw(px, r.x, r.y, r.width, r.height);
        batch.setColor(red, green, blue, 1f);
        batch.draw(px, r.x+2, r.y+2, r.width-4, r.height-4);
        if (font != null) drawCentred(batch, label, r.y + r.height / 2f + 10f, 1f, 1f, 1f, 1f);
    }

    private void drawCentred(SpriteBatch batch, String text, float y,
                              float r, float g, float b, float a) {
        layout.setText(font, text);
        font.setColor(r, g, b, a);
        font.draw(batch, text, (Constants.WORLD_WIDTH - layout.width) / 2f, y);
        font.setColor(1, 1, 1, 1);
    }

    private static float screenToWorldX(float sx) {
        return sx * Constants.WORLD_WIDTH / Gdx.graphics.getWidth();
    }
    private static float screenToWorldY(float sy) {
        return (Gdx.graphics.getHeight() - sy) * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();
    }
}
