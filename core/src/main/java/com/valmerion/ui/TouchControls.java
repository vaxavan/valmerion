package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * On-screen virtual controls: left joystick + Jump/Attack buttons.
 * Works with both touch (mobile) and mouse click (desktop).
 */
public class TouchControls {

    // Joystick centre position in game-world coords
    private static final float JS_X = 130f;
    private static final float JS_Y = 120f;
    private static final float JS_R = 70f;   // base radius
    private static final float KN_R = 35f;   // knob radius

    // Button centres in game-world coords
    private static final float JUMP_X   = 1120f;
    private static final float JUMP_Y   = 90f;
    private static final float ATTACK_X = 1210f;
    private static final float ATTACK_Y = 160f;
    private static final float BTN_R    = 52f;

    private final Texture baseTex;
    private final Texture knobTex;
    private final Texture btnTex;

    // Knob position (follows finger)
    private float knobX = JS_X;
    private float knobY = JS_Y;

    private boolean jumpPressed    = false;
    private boolean attackPressed  = false;
    private boolean jumpJustPressed   = false;
    private boolean attackJustPressed = false;

    private boolean prevJump   = false;
    private boolean prevAttack = false;

    private final Vector3 tmp = new Vector3();

    public TouchControls() {
        baseTex = circle((int)(JS_R * 2), new Color(1,1,1,0.20f), new Color(1,1,1,0.45f));
        knobTex = circle((int)(KN_R * 2), new Color(1,1,1,0.55f), new Color(1,1,1,0.85f));
        btnTex  = circle((int)(BTN_R * 2), new Color(0,0,0,0.01f), new Color(1,1,1,0.7f));
    }

    /**
     * Call once per frame BEFORE reading input.
     * Viewport is used to convert screen pixels → game world coords.
     */
    public void update(Viewport viewport) {
        prevJump   = jumpPressed;
        prevAttack = attackPressed;
        jumpPressed   = false;
        attackPressed = false;
        knobX = JS_X;
        knobY = JS_Y;

        for (int i = 0; i < 5; i++) {
            if (!Gdx.input.isTouched(i)) continue;

            // Convert screen → world
            tmp.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
            viewport.unproject(tmp);
            float wx = tmp.x;
            float wy = tmp.y;

            // Joystick: touch is within 2× base radius of joystick centre
            float djx = wx - JS_X;
            float djy = wy - JS_Y;
            if (Math.sqrt(djx*djx + djy*djy) < JS_R * 2.5f) {
                float dist = (float) Math.sqrt(djx*djx + djy*djy);
                if (dist > JS_R) { djx = djx/dist*JS_R; djy = djy/dist*JS_R; }
                knobX = JS_X + djx;
                knobY = JS_Y + djy;
            }

            // Jump button
            float jdx = wx - JUMP_X, jdy = wy - JUMP_Y;
            if (Math.sqrt(jdx*jdx + jdy*jdy) < BTN_R + 15) jumpPressed = true;

            // Attack button
            float adx = wx - ATTACK_X, ady = wy - ATTACK_Y;
            if (Math.sqrt(adx*adx + ady*ady) < BTN_R + 15) attackPressed = true;
        }

        jumpJustPressed   = jumpPressed   && !prevJump;
        attackJustPressed = attackPressed && !prevAttack;
    }

    /** -1.0 = full left, 0 = centre, +1.0 = full right */
    public float getHorizontal() {
        float dx = knobX - JS_X;
        return Math.abs(dx) < 8f ? 0f : dx / JS_R;
    }

    public boolean isJumpJustPressed()   { return jumpJustPressed; }
    public boolean isAttackJustPressed() { return attackJustPressed; }

    public void render(SpriteBatch batch) {
        batch.setColor(1, 1, 1, 1);
        // Joystick base
        batch.draw(baseTex, JS_X - JS_R, JS_Y - JS_R, JS_R*2, JS_R*2);
        // Knob
        batch.draw(knobTex, knobX - KN_R, knobY - KN_R, KN_R*2, KN_R*2);

        // Jump (green)
        batch.setColor(0.25f, 0.85f, 0.25f, 0.85f);
        batch.draw(btnTex, JUMP_X - BTN_R, JUMP_Y - BTN_R, BTN_R*2, BTN_R*2);

        // Attack (red)
        batch.setColor(0.9f, 0.25f, 0.25f, 0.85f);
        batch.draw(btnTex, ATTACK_X - BTN_R, ATTACK_Y - BTN_R, BTN_R*2, BTN_R*2);

        batch.setColor(1, 1, 1, 1);
    }

    public void dispose() {
        baseTex.dispose();
        knobTex.dispose();
        btnTex.dispose();
    }

    private static Texture circle(int size, Color fill, Color edge) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        int r = size / 2;
        pm.setColor(fill);
        pm.fillCircle(r, r, r - 2);
        pm.setColor(edge);
        pm.drawCircle(r, r, r - 1);
        pm.drawCircle(r, r, r - 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
