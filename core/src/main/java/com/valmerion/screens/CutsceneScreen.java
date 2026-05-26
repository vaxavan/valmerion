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
import com.valmerion.utils.PlaceholderTextures;

/**
 * Cutscene — 4 slides with cross-fade + Russian subtitle.
 *
 * <pre>
 *  Slide 1 — "Тьма поглощает земли Валмериона..."
 *  Slide 2 — "Некогда великое королевство гибнет..."
 *  Slide 3 — "Древнее зло пробудилось в глубинах..."
 *  Slide 4 — "Академия — твоя последняя надежда."
 * </pre>
 *
 * Tap / SPACE / ENTER to skip.
 * A 0.6 s skip-delay prevents accidental skip from the menu tap on Android.
 */
public class CutsceneScreen extends BaseScreen {

    private static final String[] SUBTITLES = {
        "Тьма поглощает земли Валмериона...",
        "Некогда великое королевство гибнет...",
        "Древнее зло пробудилось в глубинах...",
        "Академия — твоя последняя надежда.",
    };

    private static final float SLIDE_HOLD = Constants.CUTSCENE_SLIDE_DURATION;
    private static final float FADE_DUR   = 0.9f;
    private static final float SUBTITLE_Y = 72f;
    private static final float SKIP_DELAY = 0.6f;   // prevent instant skip on Android

    // ── State ─────────────────────────────────────────────────────────────────
    private final Texture[]  slides;
    private int     index     = 0;
    private float   timer     = 0f;
    private float   alpha     = 0f;
    private float   skipTimer = 0f;
    private boolean fadingOut = false;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();

    public CutsceneScreen(ValmerionGame game) {
        super(game);
        BitmapFont loaded = assets.font(AssetLoader.FONT_MAIN);
        font = loaded != null ? loaded : new BitmapFont();

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
        skipTimer = 0f;
        fadingOut = false;
    }

    @Override
    public void render(float delta) {
        timer     += delta;
        skipTimer += delta;

        // ── Fade logic ────────────────────────────────────────────────────────
        if (!fadingOut) {
            alpha = Math.min(1f, alpha + delta / FADE_DUR);
            if (timer >= FADE_DUR + SLIDE_HOLD) fadingOut = true;
        } else {
            alpha = Math.max(0f, alpha - delta / FADE_DUR);
            if (alpha <= 0f) advanceSlide();
        }

        // ── Draw ──────────────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (index < slides.length) {
            Texture slide = slides[index];
            if (slide != null) {
                batch.setColor(alpha, alpha, alpha, 1f);
                batch.draw(slide, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                // Placeholder dark background
                Texture wp = PlaceholderTextures.whitePixel();
                batch.setColor(0.05f * alpha, 0.03f * alpha, 0.12f * alpha, 1f);
                batch.draw(wp, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
                batch.setColor(1f, 1f, 1f, 1f);
            }
        }

        // Subtitle
        if (index < SUBTITLES.length) {
            layout.setText(font, SUBTITLES[index]);
            float tx = (Constants.WORLD_WIDTH - layout.width) / 2f;

            font.setColor(0f, 0f, 0f, alpha * 0.8f);
            font.draw(batch, SUBTITLES[index], tx + 2f, SUBTITLE_Y + layout.height - 2f);

            font.setColor(1f, 0.92f, 0.65f, alpha);
            font.draw(batch, SUBTITLES[index], tx, SUBTITLE_Y + layout.height);
            font.setColor(1f, 1f, 1f, 1f);
        }

        // Skip hint
        if (skipTimer > SKIP_DELAY && alpha > 0.4f) {
            font.setColor(0.55f, 0.55f, 0.55f, alpha * 0.7f);
            font.draw(batch, "[ НАЖМИ чтобы пропустить ]",
                      Constants.WORLD_WIDTH - 310f, 36f);
            font.setColor(1f, 1f, 1f, 1f);
        }

        batch.end();

        // ── Skip ──────────────────────────────────────────────────────────────
        if (skipTimer > SKIP_DELAY) {
            if (Gdx.input.isKeyJustPressed(Keys.SPACE)
                    || Gdx.input.isKeyJustPressed(Keys.ENTER)
                    || Gdx.input.justTouched()) {
                game.setScreen(new AcademyScreen(game));
            }
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
