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
 * Cutscene — 4 slides with cross-fade + subtitle text.
 *
 * <pre>
 *  Slide 1 (dark forest)   — "Тьма поглощает земли Валмериона..."
 *  Slide 2 (medieval city) — "Некогда великое королевство гибнет..."
 *  Slide 3 (cave/dungeon)  — "Древнее зло пробудилось в глубинах..."
 *  Slide 4 (wizard study)  — "Академия — твоя последняя надежда."
 * </pre>
 *
 * SPACE / ENTER / tap to skip.
 */
public class CutsceneScreen extends BaseScreen {

    private static final String[] SUBTITLES = {
        "Тьма поглощает земли Валмериона...",
        "Некогда великое королевство гибнет...",
        "Древнее зло пробудилось в глубинах...",
        "Академия — твоя последняя надежда.",
    };

    private static final float SLIDE_HOLD    = Constants.CUTSCENE_SLIDE_DURATION;
    private static final float FADE_DUR      = 0.9f;
    private static final float SUBTITLE_Y    = 72f;

    // ── State ─────────────────────────────────────────────────────────────────
    private final Texture[] slides;
    private int   index      = 0;
    private float timer      = 0f;
    private float alpha      = 0f;
    private boolean fadingOut = false;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();

    public CutsceneScreen(ValmerionGame game) {
        super(game);
        font = assets.font(AssetLoader.FONT_MAIN);

        slides = new Texture[AssetLoader.CUTSCENE_SLIDES.length];
        for (int i = 0; i < slides.length; i++) {
            slides[i] = assets.texture(AssetLoader.CUTSCENE_SLIDES[i]);
        }
    }

    @Override
    public void show() {
        index     = 0;
        timer     = 0f;
        alpha     = 0f;
        fadingOut = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        timer += delta;

        // ── Fade logic ────────────────────────────────────────────────────────
        if (!fadingOut) {
            alpha = Math.min(1f, alpha + delta / FADE_DUR);
            if (timer >= FADE_DUR + SLIDE_HOLD) fadingOut = true;
        } else {
            alpha = Math.max(0f, alpha - delta / FADE_DUR);
            if (alpha <= 0f) advanceSlide();
        }

        // ── Draw ──────────────────────────────────────────────────────────────
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (index < slides.length && slides[index] != null) {
            batch.setColor(alpha, alpha, alpha, 1f);
            batch.draw(slides[index], 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        }

        // Subtitle
        if (font != null && index < SUBTITLES.length) {
            layout.setText(font, SUBTITLES[index]);
            float tx = (Constants.WORLD_WIDTH - layout.width) / 2f;

            // Shadow
            font.setColor(0, 0, 0, alpha * 0.8f);
            font.draw(batch, SUBTITLES[index], tx + 2, SUBTITLE_Y + layout.height - 2);

            font.setColor(1f, 0.92f, 0.65f, alpha);
            font.draw(batch, SUBTITLES[index], tx, SUBTITLE_Y + layout.height);
            font.setColor(1, 1, 1, 1);
        }

        batch.setColor(1, 1, 1, 1);
        batch.end();

        // ── Skip ──────────────────────────────────────────────────────────────
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Keys.ENTER)
                || Gdx.input.justTouched()) {
            game.setScreen(new AcademyScreen(game));
        }
    }

    @Override
    public void dispose() {}

    // ── Private ───────────────────────────────────────────────────────────────

    private void advanceSlide() {
        index++;
        timer     = 0f;
        alpha     = 0f;
        fadingOut = false;
        if (index >= slides.length) {
            game.setScreen(new AcademyScreen(game));
        }
    }
}
