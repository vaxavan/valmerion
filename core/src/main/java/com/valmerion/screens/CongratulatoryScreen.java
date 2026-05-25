package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;

/**
 * End-of-tutorial screen.
 *
 * <p>Shows congratulations text and "В скором времени будет продолжение..."
 * with a slow animated fade and star particle effect.
 */
public class CongratulatoryScreen extends BaseScreen {

    private static final String LINE_CONGRATS  = "Поздравляем с прохождением обучения!";
    private static final String LINE_SOON      = "В скором времени будет продолжение...";
    private static final String LINE_PRESS     = "[ ENTER ] — вернуться в главное меню";

    private final BitmapFont  font;
    private final BitmapFont  titleFont;
    private final GlyphLayout layout = new GlyphLayout();

    private float totalTime = 0f;
    // Each line fades in one after another
    private float alpha1 = 0f;
    private float alpha2 = 0f;
    private float alpha3 = 0f;

    // Simple star particles
    private final float[] starX;
    private final float[] starY;
    private final float[] starSpeed;
    private final float[] starAlpha;
    private static final int STAR_COUNT = 80;

    public CongratulatoryScreen(ValmerionGame game) {
        super(game);
        font      = assets.font(AssetLoader.FONT_MAIN);
        titleFont = assets.font(AssetLoader.FONT_TITLE);

        // Init stars
        starX     = new float[STAR_COUNT];
        starY     = new float[STAR_COUNT];
        starSpeed = new float[STAR_COUNT];
        starAlpha = new float[STAR_COUNT];
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = rnd.nextFloat() * Constants.WORLD_WIDTH;
            starY[i]     = rnd.nextFloat() * Constants.WORLD_HEIGHT;
            starSpeed[i] = 15f + rnd.nextFloat() * 30f;
            starAlpha[i] = rnd.nextFloat();
        }
    }

    @Override
    public void show() {
        totalTime = 0;
        alpha1 = alpha2 = alpha3 = 0f;
    }

    @Override
    public void render(float delta) {
        totalTime += delta;

        // Staggered fade-ins
        alpha1 = clamp(totalTime - 0.5f, 0f, 2f) / 2f;
        alpha2 = clamp(totalTime - 2.5f, 0f, 2f) / 2f;
        alpha3 = clamp(totalTime - 5.0f, 0f, 2f) / 2f;

        // Update stars
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpeed[i] * delta;
            starAlpha[i] = (float)(0.4f + 0.6f * Math.sin(totalTime * 1.5f + i));
            if (starY[i] > Constants.WORLD_HEIGHT) starY[i] = 0;
        }

        Gdx.gl.glClearColor(0.03f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // (Stars would need a pixel texture — drawn via font dots as fallback)

        if (titleFont != null) {
            drawCentred(titleFont, LINE_CONGRATS, Constants.WORLD_HEIGHT / 2f + 80f,
                    1f, 0.9f, 0.4f, alpha1);
        } else if (font != null) {
            drawCentred(font, LINE_CONGRATS, Constants.WORLD_HEIGHT / 2f + 80f,
                    1f, 0.9f, 0.4f, alpha1);
        }

        if (font != null) {
            drawCentred(font, LINE_SOON,  Constants.WORLD_HEIGHT / 2f,
                    0.8f, 0.85f, 1f, alpha2);
            drawCentred(font, LINE_PRESS, 80f,
                    0.6f, 0.6f, 0.6f, alpha3);
        }

        batch.end();

        if (alpha3 > 0.5f &&
                (Gdx.input.isKeyJustPressed(Keys.ENTER) || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
                 || Gdx.input.justTouched())) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void dispose() {}

    // ── Helpers ───────────────────────────────────────────────────────────────

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
