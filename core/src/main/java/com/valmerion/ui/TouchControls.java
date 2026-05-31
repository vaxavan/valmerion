package com.valmerion.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TouchControls {

    private final Stage    stage;
    private final Touchpad touchpad;

    private final Texture baseTex;
    private final Texture knobTex;
    private final Texture jumpTex;
    private final Texture atkTex;

    private boolean jumpJust, attackJust;

    public TouchControls() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        baseTex = makeCircle(200, new Color(1f, 1f, 1f, 0.18f));
        knobTex = makeCircle(100, new Color(1f, 1f, 1f, 0.55f));
        jumpTex = makeCircle(120, new Color(0.2f, 0.85f, 0.2f, 0.65f));
        atkTex  = makeCircle(120, new Color(0.9f, 0.2f, 0.2f, 0.65f));

        // Touchpad
        TouchpadStyle style = new TouchpadStyle();
        style.background = new TextureRegionDrawable(new TextureRegion(baseTex));
        style.knob       = new TextureRegionDrawable(new TextureRegion(knobTex));
        touchpad = new Touchpad(15, style);
        touchpad.setBounds(20, 20, 200, 200);
        stage.addActor(touchpad);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // Jump button — bottom right
        com.badlogic.gdx.scenes.scene2d.ui.Image jumpBtn =
            new com.badlogic.gdx.scenes.scene2d.ui.Image(
                new TextureRegionDrawable(new TextureRegion(jumpTex)));
        jumpBtn.setBounds(sw - 280, 20, 120, 120);
        jumpBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int b) {
                jumpJust = true; return true;
            }
        });
        stage.addActor(jumpBtn);

        // Attack button — above jump
        com.badlogic.gdx.scenes.scene2d.ui.Image atkBtn =
            new com.badlogic.gdx.scenes.scene2d.ui.Image(
                new TextureRegionDrawable(new TextureRegion(atkTex)));
        atkBtn.setBounds(sw - 160, 160, 120, 120);
        atkBtn.addListener(new ClickListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int ptr, int b) {
                attackJust = true; return true;
            }
        });
        stage.addActor(atkBtn);
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
        stage.draw();
        batch.begin();
    }

    // renderLabels no-op kept for call-site compatibility
    public void renderLabels(SpriteBatch batch, com.badlogic.gdx.graphics.g2d.BitmapFont font) {}

    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }

    public void dispose() {
        stage.dispose();
        baseTex.dispose(); knobTex.dispose();
        jumpTex.dispose(); atkTex.dispose();
    }

    private static Texture makeCircle(int size, Color color) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(color);
        pm.fillCircle(size / 2, size / 2, size / 2 - 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
