package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.valmerion.utils.Constants;

public class TouchControls {

    // World-space layout constants
    private static final float JOY_X  = 20f,  JOY_Y  = 20f,  JOY_SZ = 200f;
    private static final float JOY_CX = JOY_X + JOY_SZ / 2f;
    private static final float JOY_CY = JOY_Y + JOY_SZ / 2f;

    // Buttons: Jump (right side, lower) and Attack (right side, upper)
    private static final float BTN_SZ  = 130f;
    private static final float JUMP_CX = Constants.WORLD_WIDTH - 95f;
    private static final float JUMP_CY = 85f;
    private static final float ATK_CX  = Constants.WORLD_WIDTH - 230f;
    private static final float ATK_CY  = 145f;

    private final Stage    stage;
    private final Touchpad touchpad;

    private final Texture texJoyBase;
    private final Texture texJoyKnob;
    private final Texture texJumpBtn;
    private final Texture texAtkBtn;
    private final Texture texShadow;

    private boolean jumpJust, attackJust;

    public TouchControls() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Textures
        texJoyBase = makeJoyBase(200);
        texJoyKnob = makeJoyKnob(80);
        texJumpBtn = makeActionBtn(130, new Color(0.20f, 0.85f, 0.25f, 0.88f), new Color(0.10f, 0.55f, 0.12f, 1f));
        texAtkBtn  = makeActionBtn(130, new Color(0.90f, 0.22f, 0.18f, 0.88f), new Color(0.55f, 0.08f, 0.06f, 1f));
        texShadow  = makeShadowCircle(140);

        // Invisible touchpad — Scene2D handles input math
        TouchpadStyle style = new TouchpadStyle();
        style.background = new TextureRegionDrawable(new TextureRegion(makeCircleTransparent(200)));
        style.knob       = new TextureRegionDrawable(new TextureRegion(makeCircleTransparent(80)));
        touchpad = new Touchpad(15, style);

        float sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        float sx = JOY_X / Constants.WORLD_WIDTH * sw;
        float sy = JOY_Y / Constants.WORLD_HEIGHT * sh;
        float ss = JOY_SZ / Constants.WORLD_WIDTH * sw;
        touchpad.setBounds(sx, sy, ss, ss);
        stage.addActor(touchpad);

        // Jump button actor (invisible, input only)
        addInvisibleBtn(sw, sh, JUMP_CX, JUMP_CY, BTN_SZ, new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int b) {
                jumpJust = true; return true;
            }
        });

        // Attack button actor
        addInvisibleBtn(sw, sh, ATK_CX, ATK_CY, BTN_SZ, new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int b) {
                attackJust = true; return true;
            }
        });
    }

    private void addInvisibleBtn(float sw, float sh, float wcx, float wcy, float wsz,
                                  ClickListener listener) {
        float scx = wcx / Constants.WORLD_WIDTH  * sw;
        float scy = wcy / Constants.WORLD_HEIGHT * sh;
        float ssz = wsz / Constants.WORLD_WIDTH  * sw;
        com.badlogic.gdx.scenes.scene2d.ui.Image img =
            new com.badlogic.gdx.scenes.scene2d.ui.Image(
                new TextureRegionDrawable(new TextureRegion(makeCircleTransparent((int)ssz))));
        img.setBounds(scx - ssz/2f, scy - ssz/2f, ssz, ssz);
        img.addListener(listener);
        stage.addActor(img);
    }

    public void update() { stage.act(Gdx.graphics.getDeltaTime()); }

    public float getHorizontal() {
        float v = touchpad.getKnobPercentX();
        return Math.abs(v) < 0.15f ? 0f : v;
    }

    public boolean isMoveLeft()          { return getHorizontal() < 0; }
    public boolean isMoveRight()         { return getHorizontal() > 0; }
    public boolean isJumpJustPressed()   { boolean v = jumpJust;   jumpJust   = false; return v; }
    public boolean isAttackJustPressed() { boolean v = attackJust; attackJust = false; return v; }

    public void render(SpriteBatch batch) {
        batch.end();
        batch.begin();

        // ── Joystick ──────────────────────────────────────────────────────────
        batch.setColor(Color.WHITE);
        batch.draw(texJoyBase, JOY_X, JOY_Y, JOY_SZ, JOY_SZ);

        // Knob position from touchpad percent
        float kpx = touchpad.getKnobPercentX();
        float kpy = touchpad.getKnobPercentY();
        float maxOffset = JOY_SZ * 0.28f;
        float knobW = JOY_SZ * 0.40f;
        float knobX = JOY_CX + kpx * maxOffset - knobW / 2f;
        float knobY = JOY_CY + kpy * maxOffset - knobW / 2f;
        batch.draw(texJoyKnob, knobX, knobY, knobW, knobW);

        // ── Action buttons ────────────────────────────────────────────────────
        float half = BTN_SZ / 2f;
        float shadow = 6f;

        // Shadows
        batch.setColor(0f, 0f, 0f, 0.35f);
        batch.draw(texShadow, JUMP_CX - half + shadow, JUMP_CY - half - shadow, BTN_SZ, BTN_SZ);
        batch.draw(texShadow, ATK_CX  - half + shadow, ATK_CY  - half - shadow, BTN_SZ, BTN_SZ);

        // Buttons
        batch.setColor(Color.WHITE);
        batch.draw(texJumpBtn, JUMP_CX - half, JUMP_CY - half, BTN_SZ, BTN_SZ);
        batch.draw(texAtkBtn,  ATK_CX  - half, ATK_CY  - half, BTN_SZ, BTN_SZ);
    }

    public void renderLabels(SpriteBatch batch, BitmapFont font) {
        if (font == null) return;
        GlyphLayout gl = new GlyphLayout();
        float origScaleX = font.getScaleX(), origScaleY = font.getScaleY();
        font.getData().setScale(0.75f);

        // Jump label
        gl.setText(font, "↑");
        font.setColor(0f, 0f, 0f, 0.5f);
        font.draw(batch, "↑", JUMP_CX - gl.width/2f + 1f, JUMP_CY + gl.height/2f - 1f);
        font.setColor(1f, 1f, 1f, 0.95f);
        font.draw(batch, "↑", JUMP_CX - gl.width/2f,      JUMP_CY + gl.height/2f);

        // Attack label
        gl.setText(font, "✦");
        font.setColor(0f, 0f, 0f, 0.5f);
        font.draw(batch, "✦", ATK_CX - gl.width/2f + 1f, ATK_CY + gl.height/2f - 1f);
        font.setColor(1f, 1f, 1f, 0.95f);
        font.draw(batch, "✦", ATK_CX - gl.width/2f,      ATK_CY + gl.height/2f);

        font.getData().setScale(origScaleX, origScaleY);
        font.setColor(Color.WHITE);
    }

    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    public void dispose() {
        stage.dispose();
        texJoyBase.dispose(); texJoyKnob.dispose();
        texJumpBtn.dispose(); texAtkBtn.dispose(); texShadow.dispose();
    }

    // ── Texture builders ──────────────────────────────────────────────────────

    /** Joystick outer ring: dark translucent fill + two-tone border ring */
    private static Texture makeJoyBase(int sz) {
        Pixmap pm = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0); pm.fill();
        int r = sz / 2 - 2;
        // Dark fill
        pm.setColor(0.05f, 0.04f, 0.12f, 0.60f);
        pm.fillCircle(sz/2, sz/2, r);
        // Outer ring — purple
        drawRing(pm, sz/2, sz/2, r, r - 6, new Color(0.55f, 0.35f, 0.85f, 0.90f));
        // Inner ring — dimmer
        drawRing(pm, sz/2, sz/2, r - 10, r - 13, new Color(0.35f, 0.20f, 0.55f, 0.55f));
        // Center dot
        pm.setColor(0.60f, 0.45f, 0.85f, 0.40f);
        pm.fillCircle(sz/2, sz/2, 8);
        Texture t = new Texture(pm); pm.dispose();
        return t;
    }

    /** Joystick knob: bright circle with highlight and soft edge */
    private static Texture makeJoyKnob(int sz) {
        Pixmap pm = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0); pm.fill();
        int r = sz / 2 - 2;
        // Main fill
        pm.setColor(0.65f, 0.50f, 0.95f, 0.92f);
        pm.fillCircle(sz/2, sz/2, r);
        // Top highlight
        pm.setColor(0.85f, 0.75f, 1.00f, 0.50f);
        pm.fillCircle(sz/2, sz/2 + r/3, r * 2/3);
        // Border
        drawRing(pm, sz/2, sz/2, r, r - 3, new Color(0.80f, 0.65f, 1.00f, 1f));
        Texture t = new Texture(pm); pm.dispose();
        return t;
    }

    /** Action button: dark shadow rim + colored fill + top highlight shine */
    private static Texture makeActionBtn(int sz, Color main, Color dark) {
        Pixmap pm = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0); pm.fill();
        int r = sz / 2 - 2;
        // Dark outer ring (gives depth)
        pm.setColor(dark.r, dark.g, dark.b, 1f);
        pm.fillCircle(sz/2, sz/2, r);
        // Main color (slightly inset)
        pm.setColor(main.r, main.g, main.b, main.a);
        pm.fillCircle(sz/2, sz/2, r - 4);
        // Top shine highlight
        pm.setColor(1f, 1f, 1f, 0.22f);
        pm.fillCircle(sz/2, sz/2 + r/3, r * 2/3);
        // Border ring
        drawRing(pm, sz/2, sz/2, r, r - 3, new Color(1f, 1f, 1f, 0.30f));
        Texture t = new Texture(pm); pm.dispose();
        return t;
    }

    private static Texture makeShadowCircle(int sz) {
        Pixmap pm = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0); pm.fill();
        pm.setColor(0f, 0f, 0f, 0.5f);
        pm.fillCircle(sz/2, sz/2, sz/2 - 2);
        Texture t = new Texture(pm); pm.dispose();
        return t;
    }

    private static Texture makeCircleTransparent(int sz) {
        Pixmap pm = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0); pm.fill();
        Texture t = new Texture(pm); pm.dispose();
        return t;
    }

    /** Draw a filled annulus (ring) by filling outer circle then over-drawing inner. */
    private static void drawRing(Pixmap pm, int cx, int cy, int outerR, int innerR, Color c) {
        pm.setColor(c);
        pm.fillCircle(cx, cy, outerR);
        pm.setColor(0, 0, 0, 0);
        pm.fillCircle(cx, cy, innerR);
    }
}
