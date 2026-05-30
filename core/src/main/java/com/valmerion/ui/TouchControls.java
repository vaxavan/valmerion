package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Dead-simple touch controls.
 * Screen is split into zones — touch anywhere in a zone to act.
 *
 * Layout (screen fractions):
 *   LEFT  zone  : x 0..0.25              → move left
 *   RIGHT zone  : x 0.25..0.50           → move right
 *   JUMP button : x 0.75..1.0, y 0..0.50 → jump
 *   ATTACK btn  : x 0.75..1.0, y 0.50..1.0 → attack
 */
public class TouchControls {

    private boolean moveLeft   = false;
    private boolean moveRight  = false;
    private boolean jumpDown   = false;
    private boolean attackDown = false;
    private boolean jumpJust   = false;
    private boolean attackJust = false;
    private boolean prevJump   = false;
    private boolean prevAttack = false;

    private final Texture pixel;
    private final BitmapFont font;

    public TouchControls(BitmapFont font) {
        this.font = font;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();
    }

    public void update() {
        prevJump   = jumpDown;
        prevAttack = attackDown;

        moveLeft   = false;
        moveRight  = false;
        jumpDown   = false;
        attackDown = false;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        for (int i = 0; i < 5; i++) {
            if (!Gdx.input.isTouched(i)) continue;

            // sx: 0=left 1=right,  sy: 0=top 1=bottom (LibGDX default)
            float sx = Gdx.input.getX(i) / (float) sw;
            float sy = Gdx.input.getY(i) / (float) sh;  // 0=top, 1=bottom

            if (sx < 0.25f)              moveLeft  = true;
            if (sx >= 0.25f && sx < 0.5f) moveRight = true;
            if (sx >= 0.75f && sy >= 0.5f) jumpDown  = true;
            if (sx >= 0.75f && sy <  0.5f) attackDown = true;
        }

        jumpJust   = jumpDown   && !prevJump;
        attackJust = attackDown && !prevAttack;
    }

    public boolean isMoveLeft()          { return moveLeft; }
    public boolean isMoveRight()         { return moveRight; }
    public boolean isJumpJustPressed()   { return jumpJust; }
    public boolean isAttackJustPressed() { return attackJust; }

    public void render(SpriteBatch batch) {
        float W = 1280f, H = 720f;

        // LEFT arrow zone
        drawRect(batch, 0, 0, W * 0.25f, H * 0.35f,
                 moveLeft ? new Color(1,1,1,0.25f) : new Color(1,1,1,0.10f));

        // RIGHT arrow zone
        drawRect(batch, W * 0.25f, 0, W * 0.25f, H * 0.35f,
                 moveRight ? new Color(1,1,1,0.25f) : new Color(1,1,1,0.10f));

        // JUMP button (bottom-right)
        drawRect(batch, W * 0.75f, 0, W * 0.25f, H * 0.35f,
                 jumpDown ? new Color(0.2f,0.9f,0.2f,0.45f) : new Color(0.2f,0.9f,0.2f,0.20f));

        // ATTACK button (top of bottom-right)
        drawRect(batch, W * 0.75f, H * 0.35f, W * 0.25f, H * 0.30f,
                 attackDown ? new Color(0.9f,0.2f,0.2f,0.45f) : new Color(0.9f,0.2f,0.2f,0.20f));

        // Labels
        if (font != null) {
            font.setColor(1, 1, 1, 0.8f);
            font.draw(batch, "◄",      W * 0.09f, H * 0.18f);
            font.draw(batch, "►",      W * 0.34f, H * 0.18f);
            font.draw(batch, "ПРЫЖОК", W * 0.80f, H * 0.18f);
            font.draw(batch, "УДАР",   W * 0.80f, H * 0.58f);
            font.setColor(Color.WHITE);
        }
    }

    public void dispose() {
        pixel.dispose();
    }

    private void drawRect(SpriteBatch batch, float x, float y, float w, float h, Color c) {
        batch.setColor(c);
        batch.draw(pixel, x, y, w, h);
        batch.setColor(Color.WHITE);
    }
}
