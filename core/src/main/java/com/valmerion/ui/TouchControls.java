package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TouchControls {

    // Джойстик: центр фиксирован в левом нижнем углу
    private static final float JS_X = 160f;  // в пикселях экрана
    private static final float JS_Y_FROM_BOTTOM = 140f;
    private static final float JS_R = 80f;   // радиус базы

    // Кнопки (пиксели экрана от правого/нижнего края)
    private static final float BTN_R = 60f;
    private static final float JUMP_RIGHT   = 160f;
    private static final float JUMP_BOTTOM  = 80f;
    private static final float ATK_RIGHT    = 60f;
    private static final float ATK_BOTTOM   = 220f;

    private float knobX, knobY;   // текущее положение ручки джойстика (пиксели экрана)
    private float jsBaseY;        // вычисляется в update по высоте экрана

    private boolean jumpDown, attackDown;
    private boolean jumpJust, attackJust;
    private boolean prevJump, prevAttack;

    // горизонтальное смещение ручки (-1..1)
    private float horizontal = 0f;

    private final Texture circle;
    private final BitmapFont font;

    public TouchControls(BitmapFont font) {
        this.font = font;
        Pixmap pm = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(Color.WHITE);
        pm.fillCircle(128, 128, 126);
        circle = new Texture(pm);
        pm.dispose();
    }

    public void update() {
        prevJump   = jumpDown;
        prevAttack = attackDown;
        jumpDown   = false;
        attackDown = false;
        horizontal = 0f;

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();

        jsBaseY = sh - JS_Y_FROM_BOTTOM;  // y от верха (LibGDX: 0=top)

        float jumpX = sw - JUMP_RIGHT;
        float jumpY = sh - JUMP_BOTTOM;
        float atkX  = sw - ATK_RIGHT;
        float atkY  = sh - ATK_BOTTOM;

        knobX = JS_X;
        knobY = jsBaseY;

        for (int i = 0; i < 5; i++) {
            if (!Gdx.input.isTouched(i)) continue;

            float tx = Gdx.input.getX(i);
            float ty = Gdx.input.getY(i);  // 0 = top

            // Джойстик: тач в левой половине экрана
            if (tx < sw * 0.5f) {
                float dx = tx - JS_X;
                float dy = ty - jsBaseY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist > JS_R) {
                    dx = dx / dist * JS_R;
                    dy = dy / dist * JS_R;
                }
                knobX = JS_X + dx;
                knobY = jsBaseY + dy;
                horizontal = dx / JS_R;  // -1..1
            }

            // Прыжок
            float djx = tx - jumpX, djy = ty - jumpY;
            if (Math.sqrt(djx*djx + djy*djy) < BTN_R + 20) jumpDown = true;

            // Атака
            float dax = tx - atkX, day = ty - atkY;
            if (Math.sqrt(dax*dax + day*day) < BTN_R + 20) attackDown = true;
        }

        jumpJust   = jumpDown   && !prevJump;
        attackJust = attackDown && !prevAttack;
    }

    public float  getHorizontal()        { return Math.abs(horizontal) < 0.1f ? 0f : horizontal; }
    public boolean isMoveLeft()          { return getHorizontal() < 0; }
    public boolean isMoveRight()         { return getHorizontal() > 0; }
    public boolean isJumpJustPressed()   { return jumpJust; }
    public boolean isAttackJustPressed() { return attackJust; }

    public void render(SpriteBatch batch) {
        // Переводим пиксели экрана в игровые координаты
        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();
        float gw = 1280f, gh = 720f;
        float sx = gw / sw, sy = gh / sh;

        float gJsX    = JS_X * sx;
        float gJsY    = (sh - jsBaseY) * sy;   // flip y: экранный top → игровой bottom
        float gKnobX  = knobX * sx;
        float gKnobY  = (sh - knobY) * sy;
        float gJsR    = JS_R * sx;
        float gKnR    = gJsR * 0.5f;
        float gBtnR   = BTN_R * sx;

        float gJumpX  = (sw - JUMP_RIGHT) * sx;
        float gJumpY  = (sh - (sh - JUMP_BOTTOM)) * sy;
        float gAtkX   = (sw - ATK_RIGHT)  * sx;
        float gAtkY   = (sh - (sh - ATK_BOTTOM))  * sy;

        // База джойстика
        batch.setColor(1, 1, 1, 0.20f);
        batch.draw(circle, gJsX - gJsR, gJsY - gJsR, gJsR*2, gJsR*2);
        // Ручка
        batch.setColor(1, 1, 1, 0.60f);
        batch.draw(circle, gKnobX - gKnR, gKnobY - gKnR, gKnR*2, gKnR*2);

        // Прыжок
        batch.setColor(jumpDown ? 0.3f : 0.15f, jumpDown ? 1f : 0.8f, 0.2f, 0.75f);
        batch.draw(circle, gJumpX - gBtnR, gJumpY - gBtnR, gBtnR*2, gBtnR*2);

        // Атака
        batch.setColor(jumpDown ? 1f : 0.8f, 0.2f, 0.2f, 0.75f);
        batch.draw(circle, gAtkX - gBtnR, gAtkY - gBtnR, gBtnR*2, gBtnR*2);

        batch.setColor(Color.WHITE);

        if (font != null) {
            font.setColor(1, 1, 1, 0.9f);
            font.draw(batch, "↑ ПРЫЖОК", gJumpX - 38f, gJumpY - gBtnR - 8f);
            font.draw(batch, "F УДАР",   gAtkX  - 28f, gAtkY  - gBtnR - 8f);
            font.setColor(Color.WHITE);
        }
    }

    public void dispose() { circle.dispose(); }
}
