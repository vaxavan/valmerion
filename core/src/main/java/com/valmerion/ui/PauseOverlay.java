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
 * Pause overlay.
 * - Always shows a small ⏸ icon button in the top-right corner.
 * - Tap it (or press ESC) to toggle pause.
 * - While paused shows "Продолжить" and "Главное меню" buttons.
 */
public class PauseOverlay {

    private static final float ICON  = 64f;
    private static final float IX    = Constants.WORLD_WIDTH  - ICON - 12f;
    private static final float IY    = Constants.WORLD_HEIGHT - ICON - 12f;

    private static final float PANEL_W = 460f;
    private static final float PANEL_H = 300f;
    private static final float BTN_W   = 360f;
    private static final float BTN_H   = 80f;

    private final Rectangle iconR;
    private final Rectangle btnResume;
    private final Rectangle btnMenu;

    private boolean paused    = false;
    private boolean wantsMenu = false;
    private boolean prevEsc   = false;

    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();

    // kept for drawCentred
    private SpriteBatch b;

    public PauseOverlay(BitmapFont font) {
        this.font = font;

        iconR = new Rectangle(IX, IY, ICON, ICON);

        float cx = (Constants.WORLD_WIDTH  - PANEL_W) / 2f;
        float cy = (Constants.WORLD_HEIGHT - PANEL_H) / 2f;
        float bx = (Constants.WORLD_WIDTH  - BTN_W)   / 2f;

        btnResume = new Rectangle(bx, cy + PANEL_H / 2f,                 BTN_W, BTN_H);
        btnMenu   = new Rectangle(bx, cy + PANEL_H / 2f - BTN_H - 20f,  BTN_W, BTN_H);
    }

    /** Call every frame before player update. Returns true while paused. */
    public boolean update() {
        boolean escNow = Gdx.input.isKeyPressed(Keys.ESCAPE);
        if (escNow && !prevEsc) paused = !paused;
        prevEsc = escNow;

        if (Gdx.input.justTouched()) {
            float wx = toWX(Gdx.input.getX());
            float wy = toWY(Gdx.input.getY());

            if (!paused) {
                if (iconR.contains(wx, wy)) paused = true;
            } else {
                if (btnResume.contains(wx, wy)) paused = false;
                if (btnMenu.contains(wx, wy))   wantsMenu = true;
            }
        }
        return paused;
    }

    public boolean isPaused()      { return paused; }
    public boolean wantsMainMenu() { boolean v = wantsMenu; wantsMenu = false; return v; }

    public void render(SpriteBatch batch) {
        this.b = batch;
        com.badlogic.gdx.graphics.Texture px = HealthBar.getWhitePixel();
        if (px == null) return;

        // ── Pause icon (always visible top-right) ────────────────────────────
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(px, IX, IY, ICON, ICON);
        batch.setColor(0.65f, 0.55f, 1f, 1f);
        float bw = ICON * 0.17f, bh = ICON * 0.52f, by2 = IY + (ICON - bh) / 2f;
        batch.draw(px, IX + ICON * 0.27f, by2, bw, bh);
        batch.draw(px, IX + ICON * 0.56f, by2, bw, bh);
        batch.setColor(Color.WHITE);

        if (!paused) return;

        // ── Dim overlay ───────────────────────────────────────────────────────
        batch.setColor(0f, 0f, 0f, 0.65f);
        batch.draw(px, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        // ── Panel ─────────────────────────────────────────────────────────────
        float cx = (Constants.WORLD_WIDTH  - PANEL_W) / 2f;
        float cy = (Constants.WORLD_HEIGHT - PANEL_H) / 2f;

        batch.setColor(0.08f, 0.06f, 0.18f, 0.97f);
        batch.draw(px, cx, cy, PANEL_W, PANEL_H);

        batch.setColor(0.55f, 0.45f, 0.85f, 1f);
        batch.draw(px, cx,            cy,            PANEL_W, 3);
        batch.draw(px, cx,            cy+PANEL_H-3,  PANEL_W, 3);
        batch.draw(px, cx,            cy,            3,       PANEL_H);
        batch.draw(px, cx+PANEL_W-3,  cy,            3,       PANEL_H);

        drawCentred("ПАУЗА", cy + PANEL_H - 62f, 0.85f, 0.75f, 1f);
        drawBtn(px, btnResume, "Продолжить",   0.15f, 0.65f, 0.25f);
        drawBtn(px, btnMenu,   "Главное меню", 0.65f, 0.20f, 0.20f);

        batch.setColor(Color.WHITE);
    }

    private void drawBtn(com.badlogic.gdx.graphics.Texture px,
                         Rectangle r, String text, float r2, float g, float bl) {
        b.setColor(r2*0.35f, g*0.35f, bl*0.35f, 1f);
        b.draw(px, r.x,   r.y,   r.width,   r.height);
        b.setColor(r2, g, bl, 1f);
        b.draw(px, r.x+3, r.y+3, r.width-6, r.height-6);
        drawCentred(text, r.y + r.height/2f + 10f, 1f, 1f, 1f);
        b.setColor(Color.WHITE);
    }

    private void drawCentred(String text, float y, float r, float g, float bl) {
        if (font == null) return;
        layout.setText(font, text);
        float tx = (Constants.WORLD_WIDTH - layout.width) / 2f;
        font.setColor(0, 0, 0, 0.8f);
        font.draw(b, text, tx+2, y-2);
        font.setColor(r, g, bl, 1f);
        font.draw(b, text, tx, y);
        font.setColor(Color.WHITE);
    }

    private static float toWX(float sx) {
        return sx * Constants.WORLD_WIDTH  / Gdx.graphics.getWidth();
    }
    private static float toWY(float sy) {
        return (Gdx.graphics.getHeight() - sy) * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();
    }
}
