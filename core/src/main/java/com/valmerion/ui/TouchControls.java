package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * On-screen virtual controls: left joystick + Jump/Attack buttons.
 * Works with both touch (mobile) and mouse (desktop testing).
 */
public class TouchControls {

    // Joystick
    private static final float JS_BASE_X  = 120f;
    private static final float JS_BASE_Y  = 110f;
    private static final float JS_RADIUS  = 70f;
    private static final float KNOB_R     = 35f;

    // Buttons (bottom-right)
    private static final float BTN_JUMP_X   = 1100f;
    private static final float BTN_JUMP_Y   = 60f;
    private static final float BTN_ATTACK_X = 1200f;
    private static final float BTN_ATTACK_Y = 130f;
    private static final float BTN_R        = 50f;

    // Textures
    private final Texture baseCircle;
    private final Texture knobCircle;
    private final Texture btnCircle;

    // State
    private float knobX = JS_BASE_X;
    private float knobY = JS_BASE_Y;
    private boolean jumpPressed    = false;
    private boolean attackPressed  = false;
    private boolean jumpJustPressed   = false;
    private boolean attackJustPressed = false;

    // Touch pointer IDs
    private int jsPointer     = -1;
    private int jumpPointer   = -1;
    private int attackPointer = -1;

    // Previous frame state
    private boolean prevJump   = false;
    private boolean prevAttack = false;

    public TouchControls() {
        baseCircle = makeCircle((int)(JS_RADIUS * 2), new Color(1,1,1,0.25f), new Color(1,1,1,0.5f));
        knobCircle = makeCircle((int)(KNOB_R * 2),    new Color(1,1,1,0.6f), new Color(1,1,1,0.9f));
        btnCircle  = makeCircle((int)(BTN_R * 2),     new Color(0.2f,0.6f,1f,0.5f), new Color(0.4f,0.8f,1f,0.9f));
    }

    public void update() {
        prevJump   = jumpPressed;
        prevAttack = attackPressed;

        jumpPressed   = false;
        attackPressed = false;
        knobX = JS_BASE_X;
        knobY = JS_BASE_Y;

        int pointers = Gdx.input.isTouched() ? 10 : 0;

        for (int i = 0; i < 10; i++) {
            if (!Gdx.input.isTouched(i)) continue;

            // LibGDX y is flipped (0 = top)
            float tx = Gdx.input.getX(i) * (1280f / Gdx.graphics.getWidth());
            float ty = (Gdx.graphics.getHeight() - Gdx.input.getY(i))
                       * (720f / Gdx.graphics.getHeight());

            // Joystick zone: left half, bottom 250px
            if (tx < 640f && ty < 250f) {
                jsPointer = i;
                float dx = tx - JS_BASE_X;
                float dy = ty - JS_BASE_Y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > JS_RADIUS) {
                    dx = dx / dist * JS_RADIUS;
                    dy = dy / dist * JS_RADIUS;
                }
                knobX = JS_BASE_X + dx;
                knobY = JS_BASE_Y + dy;
            }

            // Jump button
            float djx = tx - (BTN_JUMP_X + BTN_R);
            float djy = ty - (BTN_JUMP_Y + BTN_R);
            if (Math.sqrt(djx*djx + djy*djy) < BTN_R + 20) {
                jumpPressed = true;
            }

            // Attack button
            float dax = tx - (BTN_ATTACK_X + BTN_R);
            float day = ty - (BTN_ATTACK_Y + BTN_R);
            if (Math.sqrt(dax*dax + day*day) < BTN_R + 20) {
                attackPressed = true;
            }
        }

        jumpJustPressed   = jumpPressed   && !prevJump;
        attackJustPressed = attackPressed && !prevAttack;
    }

    /** -1 = left, 0 = still, 1 = right */
    public float getHorizontal() {
        float dx = knobX - JS_BASE_X;
        if (Math.abs(dx) < 10f) return 0f;
        return dx / JS_RADIUS;
    }

    public boolean isJumpJustPressed()   { return jumpJustPressed; }
    public boolean isAttackJustPressed() { return attackJustPressed; }
    public boolean isJumpHeld()          { return jumpPressed; }

    public void render(SpriteBatch batch) {
        // Joystick base
        batch.setColor(1,1,1,1);
        batch.draw(baseCircle, JS_BASE_X - JS_RADIUS, JS_BASE_Y - JS_RADIUS,
                   JS_RADIUS*2, JS_RADIUS*2);
        // Knob
        batch.draw(knobCircle, knobX - KNOB_R, knobY - KNOB_R,
                   KNOB_R*2, KNOB_R*2);

        // Jump button
        batch.setColor(0.3f, 0.8f, 0.3f, 0.8f);
        batch.draw(btnCircle, BTN_JUMP_X, BTN_JUMP_Y, BTN_R*2, BTN_R*2);

        // Attack button
        batch.setColor(0.9f, 0.3f, 0.3f, 0.8f);
        batch.draw(btnCircle, BTN_ATTACK_X, BTN_ATTACK_Y, BTN_R*2, BTN_R*2);

        batch.setColor(1,1,1,1);
    }

    public void dispose() {
        baseCircle.dispose();
        knobCircle.dispose();
        btnCircle.dispose();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Texture makeCircle(int size, Color fill, Color border) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0);
        pm.fill();
        int r = size / 2;
        pm.setColor(fill);
        pm.fillCircle(r, r, r - 2);
        pm.setColor(border);
        for (int t = 0; t < 3; t++) {
            pm.drawCircle(r, r, r - t);
        }
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }
}
