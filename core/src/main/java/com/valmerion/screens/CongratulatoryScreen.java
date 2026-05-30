package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;

/**
 * End-of-tutorial / chapter 1 screen.
 * Shows congratulations text on a dark arena background, then "В скором времени..."
 */
public class CongratulatoryScreen extends BaseScreen {

    private static final String LINE_CONGRATS = "Поздравляем с прохождением обучения!";
    private static final String LINE_SOON     = "В скором времени будет продолжение...";
    private static final String LINE_PRESS    = "[ ENTER ] — вернуться в главное меню";

    private final BitmapFont  font;
    private final BitmapFont  titleFont;
    private final Texture     background;
    private final GlyphLayout layout = new GlyphLayout();

    private float totalTime = 0f;
    private float alpha1 = 0f, alpha2 = 0f, alpha3 = 0f;

    // Rising sparkles
    private static final int  SPARK = 60;
    private final float[] sx, sy, ss, sa;

    public CongratulatoryScreen(ValmerionGame game) {
        super(game);
        font       = assets.font(AssetLoader.FONT_MAIN);
        titleFont  = assets.font(AssetLoader.FONT_TITLE);
        background = assets.texture(AssetLoader.TEX_CONGRATS_BG);

        sx = new float[SPARK]; sy = new float[SPARK];
        ss = new float[SPARK]; sa = new float[SPARK];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < SPARK; i++) {
            sx[i] = rnd.nextFloat() * Constants.WORLD_WIDTH;
            sy[i] = rnd.nextFloat() * Constants.WORLD_HEIGHT;
            ss[i] = 20f + rnd.nextFloat() * 50f;
            sa[i] = rnd.nextFloat();
        }
    }

    @Override public void show() { totalTime = 0; alpha1 = alpha2 = alpha3 = 0f; }

    @Override
    public void render(float delta) {
        totalTime += delta;

        alpha1 = clamp((totalTime - 0.5f) / 2f, 0f, 1f);
        alpha2 = clamp((totalTime - 3.0f) / 2f, 0f, 1f);
        alpha3 = clamp((totalTime - 6.0f) / 2f, 0f, 1f);

        for (int i = 0; i < SPARK; i++) {
            sy[i] += ss[i] * delta;
            sa[i] = 0.4f + 0.6f * (float) Math.sin(totalTime * 2f + i);
            if (sy[i] > Constants.WORLD_HEIGHT) sy[i] = 0;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Background with dark overlay
        if (background != null) {
            batch.setColor(0.55f, 0.55f, 0.55f, 1f);
            batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // Dark overlay for readability
        com.badlogic.gdx.graphics.Texture px = com.valmerion.ui.HealthBar.getWhitePixel();
        if (px != null) {
            batch.setColor(0f, 0f, 0f, 0.55f);
            batch.draw(px, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
            // Gold sparkle dots
            batch.setColor(1f, 0.85f, 0.3f, 1f);
            for (int i = 0; i < SPARK; i++) {
                batch.draw(px, sx[i], sy[i], 3f * sa[i], 3f * sa[i]);
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }

        // Text lines
        BitmapFont big = titleFont != null ? titleFont : font;
        if (big  != null) drawCentred(big,  LINE_CONGRATS, Constants.WORLD_HEIGHT / 2f + 100f, 1f, 0.88f, 0.35f, alpha1);
        if (font != null) drawCentred(font, LINE_SOON,     Constants.WORLD_HEIGHT / 2f + 20f,  0.75f, 0.85f, 1f,   alpha2);
        if (font != null) drawCentred(font, LINE_PRESS,    90f,                                  0.55f, 0.55f, 0.55f, alpha3);

        batch.end();

        if (alpha3 > 0.5f &&
                (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
                 || Gdx.input.justTouched())) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void dispose() {}

    private void drawCentred(BitmapFont f, String text, float y,
                              float r, float g, float b, float a) {
        layout.setText(f, text);
        f.setColor(r, g, b, a);
        f.draw(batch, text, (Constants.WORLD_WIDTH - layout.width) / 2f, y);
        f.setColor(1, 1, 1, 1);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
