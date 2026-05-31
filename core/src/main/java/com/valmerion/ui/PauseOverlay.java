package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
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

    private static final float PANEL_W = 480f;
    private static final float PANEL_H = 380f;   // taller so title + buttons don't overlap
    private static final float BTN_W   = 380f;
    private static final float BTN_H   = 80f;

    private final Rectangle iconR;
    private final Rectangle btnResume;
    private final Rectangle btnMenu;

    private boolean paused    = false;
    private boolean wantsMenu = false;

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

        // Buttons sit in lower portion; title takes upper ~35%
        float btnAreaTop = cy + PANEL_H * 0.44f;
        btnResume = new Rectangle(bx, btnAreaTop,                 BTN_W, BTN_H);
        btnMenu   = new Rectangle(bx, btnAreaTop - BTN_H - 18f,  BTN_W, BTN_H);
    }

    /** Call every frame before player update. Returns true while paused. */
    public boolean update() {
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

        // Purple border
        batch.setColor(0.50f, 0.40f, 0.80f, 1f);
        batch.draw(px, cx,            cy,            PANEL_W, 3);
        batch.draw(px, cx,            cy+PANEL_H-3,  PANEL_W, 3);
        batch.draw(px, cx,            cy,            3,       PANEL_H);
        batch.draw(px, cx+PANEL_W-3,  cy,            3,       PANEL_H);

        // ── Beautiful title ───────────────────────────────────────────────────
        float titleY = cy + PANEL_H - 55f;
        drawCentred("ПАУЗА", titleY, 1f, 0.84f, 0.20f);   // gold

        // Decorative lines flanking the title
        if (font != null) {
            layout.setText(font, "ПАУЗА");
            float lineW  = (PANEL_W - layout.width) / 2f - 30f;
            float lineY2 = titleY - layout.height / 2f;
            float lineX1 = cx + 18f;
            float lineX2 = cx + PANEL_W - 18f - lineW;
            batch.setColor(1f, 0.84f, 0.20f, 0.55f);
            batch.draw(px, lineX1, lineY2, lineW, 2f);
            batch.draw(px, lineX2, lineY2, lineW, 2f);
        }

        // Thin gold separator below title
        float sepY = cy + PANEL_H - 80f;
        batch.setColor(1f, 0.84f, 0.20f, 0.35f);
        batch.draw(px, cx + 20f, sepY, PANEL_W - 40f, 1.5f);
        batch.setColor(Color.WHITE);

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
