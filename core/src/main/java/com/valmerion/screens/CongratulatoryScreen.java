package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;
import com.valmerion.utils.PlaceholderTextures;

/**
 * End-of-tutorial victory screen.
 *
 * <p>Three text lines fade in sequentially; pressing ENTER / tapping
 * returns to the main menu.
 */
public class CongratulatoryScreen extends BaseScreen {

    private static final String LINE_CONGRATS = "Поздравляем с прохождением обучения!";
    private static final String LINE_SOON     = "В скором времени будет продолжение...";
    private static final String LINE_PRESS    = "[ НАЖМИ ] чтобы вернуться в меню";

    private final BitmapFont  font;
    private final BitmapFont  titleFont;
    private final GlyphLayout layout = new GlyphLayout();

    private float totalTime = 0f;
    private float alpha1, alpha2, alpha3;

    // ── Stars ─────────────────────────────────────────────────────────────────
    private static final int STAR_COUNT = 80;
    private final float[] starX     = new float[STAR_COUNT];
    private final float[] starY     = new float[STAR_COUNT];
    private final float[] starSpeed = new float[STAR_COUNT];
    private final float[] starSize  = new float[STAR_COUNT];

    public CongratulatoryScreen(ValmerionGame game) {
        super(game);
        BitmapFont loaded = assets.font(AssetLoader.FONT_MAIN);
        font      = loaded != null ? loaded : new BitmapFont();
        BitmapFont loadedTitle = assets.font(AssetLoader.FONT_TITLE);
        titleFont = loadedTitle != null ? loadedTitle : font;

        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i]     = rnd.nextFloat() * Constants.WORLD_WIDTH;
            starY[i]     = rnd.nextFloat() * Constants.WORLD_HEIGHT;
            starSpeed[i] = 20f + rnd.nextFloat() * 40f;
            starSize[i]  = 2f + rnd.nextFloat() * 4f;
        }
    }

    @Override
    public void show() {
        totalTime = 0f;
        alpha1 = alpha2 = alpha3 = 0f;
    }

    @Override
    public void render(float delta) {
        totalTime += delta;

        alpha1 = clamp((totalTime - 0.5f) / 2f, 0f, 1f);
        alpha2 = clamp((totalTime - 2.5f) / 2f, 0f, 1f);
        alpha3 = clamp((totalTime - 5.0f) / 2f, 0f, 1f);

        // Update stars
        for (int i = 0; i < STAR_COUNT; i++) {
            starY[i] += starSpeed[i] * delta;
            if (starY[i] > Constants.WORLD_HEIGHT) starY[i] = 0;
        }

        Gdx.gl.glClearColor(0.03f, 0.02f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        drawStars();
        drawLines();

        batch.end();

        if (alpha3 > 0.5f
                && (Gdx.input.isKeyJustPressed(Keys.ENTER)
                    || Gdx.input.isKeyJustPressed(Keys.ESCAPE)
                    || Gdx.input.justTouched())) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void dispose() {}

    // ── Private ───────────────────────────────────────────────────────────────

    private void drawStars() {
        com.badlogic.gdx.graphics.Texture wp = PlaceholderTextures.whitePixel();
        if (wp == null) return;
        for (int i = 0; i < STAR_COUNT; i++) {
            float a = (float)(0.3f + 0.5f * Math.sin(totalTime * 1.2f + i * 0.4f));
            batch.setColor(0.85f, 0.85f, 1f, a);
            batch.draw(wp, starX[i], starY[i], starSize[i], starSize[i]);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawLines() {
        float cy = Constants.WORLD_HEIGHT / 2f;
        drawCentred(titleFont, LINE_CONGRATS, cy + 90f, 1f, 0.90f, 0.40f, alpha1);
        drawCentred(font,      LINE_SOON,     cy + 10f, 0.75f, 0.85f, 1f,  alpha2);
        drawCentred(font,      LINE_PRESS,    cy - 80f, 0.55f, 0.55f, 0.55f, alpha3);
    }

    private void drawCentred(BitmapFont f, String text, float y,
                              float r, float g, float b, float a) {
        layout.setText(f, text);
        f.setColor(r, g, b, a);
        f.draw(batch, text, (Constants.WORLD_WIDTH - layout.width) / 2f, y);
        f.setColor(1f, 1f, 1f, 1f);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
