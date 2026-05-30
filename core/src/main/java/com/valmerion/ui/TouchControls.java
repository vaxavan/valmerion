package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Virtual joystick + 2 buttons. All coordinates in screen percentages (0..1),
 * so it works on any resolution without viewport math.
 */
public class TouchControls {

    // joystick: left 20% of screen, bottom 30%
    private static final float JS_CX = 0.12f;   // centre x  (% of screen width)
    private static final float JS_CY = 0.20f;   // centre y  (% of screen height, 0=bottom)
    private static final float JS_R  = 0.09f;   // radius    (% of screen width)

    // buttons: right side, bottom 30%
    private static final float JUMP_CX   = 0.84f;
    private static final float JUMP_CY   = 0.16f;
    private static final float ATTACK_CX = 0.93f;
    private static final float ATTACK_CY = 0.28f;
    private static final float BTN_R     = 0.065f;

    private final Texture circle;

    // knob offset in % of JS_R (-1..1)
    private float knobDX = 0f;

    private boolean jumpDown   = false;
    private boolean attackDown = false;
    private boolean jumpJust   = false;
    private boolean attackJust = false;
    private boolean prevJump   = false;
    private boolean prevAttack = false;

    public TouchControls() {
        circle = makeCircle(128);
    }

    public void update() {
        prevJump   = jumpDown;
        prevAttack = attackDown;
        jumpDown   = false;
        attackDown = false;
        knobDX     = 0f;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        for (int i = 0; i < 5; i++) {
            if (!Gdx.input.isTouched(i)) continue;

            // screen coords: x left→right, y top→bottom → flip y
            float sx = Gdx.input.getX(i) / (float) sw;          // 0..1
            float sy = 1f - Gdx.input.getY(i) / (float) sh;     // 0=bottom 1=top

            // joystick
            float dx = sx - JS_CX;
            float dy = sy - JS_CY;
            float r  = (float) Math.sqrt(dx*dx + dy*dy);
            if (r < JS_R * 2.5f) {
                knobDX = Math.max(-1f, Math.min(1f, dx / JS_R));
            }

            // jump button
            float jdx = sx - JUMP_CX, jdy = sy - JUMP_CY;
            if (Math.sqrt(jdx*jdx + jdy*jdy) < BTN_R * 1.5f) jumpDown = true;

            // attack button
            float adx = sx - ATTACK_CX, ady = sy - ATTACK_CY;
            if (Math.sqrt(adx*adx + ady*ady) < BTN_R * 1.5f) attackDown = true;
        }

        jumpJust   = jumpDown   && !prevJump;
        attackJust = attackDown && !prevAttack;
    }

    /** -1=left  0=still  +1=right */
    public float getHorizontal() { return Math.abs(knobDX) < 0.15f ? 0f : knobDX; }
    public boolean isJumpJustPressed()   { return jumpJust; }
    public boolean isAttackJustPressed() { return attackJust; }

    /** Draw using game-world coordinates 0..1280 / 0..720. */
    public void render(SpriteBatch batch) {
        int sw = Gdx.graphics.getBackBufferWidth();
        int sh = Gdx.graphics.getBackBufferHeight();

        // We draw in a fixed 1280×720 game space, so convert % → game px
        float gw = 1280f, gh = 720f;

        float jsCX = JS_CX * gw;
        float jsCY = JS_CY * gh;
        float jsR  = JS_R  * gw;
        float knR  = jsR * 0.5f;
        float btnR = BTN_R * gw;

        // joystick base
        batch.setColor(1, 1, 1, 0.30f);
        batch.draw(circle, jsCX - jsR, jsCY - jsR, jsR*2, jsR*2);

        // knob
        batch.setColor(1, 1, 1, 0.70f);
        float kx = jsCX + knobDX * jsR;
        batch.draw(circle, kx - knR, jsCY - knR, knR*2, knR*2);

        // jump (green)
        batch.setColor(jumpDown ? 0.4f : 0.2f, 0.9f, 0.3f, 0.80f);
        float jBx = JUMP_CX * gw, jBy = JUMP_CY * gh;
        batch.draw(circle, jBx - btnR, jBy - btnR, btnR*2, btnR*2);

        // attack (red)
        batch.setColor(0.9f, attackDown ? 0.6f : 0.2f, 0.2f, 0.80f);
        float aBx = ATTACK_CX * gw, aBy = ATTACK_CY * gh;
        batch.draw(circle, aBx - btnR, aBy - btnR, btnR*2, btnR*2);

        batch.setColor(1, 1, 1, 1);
    }

    public void dispose() { circle.dispose(); }

    private static Texture makeCircle(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        int r = size / 2;
        pm.setColor(1, 1, 1, 1);
        pm.fillCircle(r, r, r - 1);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
