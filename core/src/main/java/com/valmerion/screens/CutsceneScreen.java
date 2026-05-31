package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.ui.HealthBar;
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
        "Когда вселенная Valmerion только родилась, появились три брата-бога...",
        "Элиофан — свет. Эребарх — тьма. Они развязали войну, уничтожая всё живое.",
        "Эон — бог времени — решил запечатать братьев. Но ему нужен был избранный...",
        "...Тот, кто стоит между крайностями. Способный остановить войну. Ты."
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
    private static final float SKIP_X = Constants.WORLD_WIDTH  - 80f;
    private static final float SKIP_Y = Constants.WORLD_HEIGHT - 52f;
    private static final float SKIP_W = 68f;
    private static final float SKIP_H = 40f;

    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();

    public CutsceneScreen(ValmerionGame game) {
        super(game);
        font = assets.font(AssetLoader.FONT_MAIN);

        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1,1,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(1,1,1,1); pm.fill();
        HealthBar.setWhitePixel(new com.badlogic.gdx.graphics.Texture(pm));
        pm.dispose();

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

        // Small ">>" skip button — top-right, subtle
        com.badlogic.gdx.graphics.Texture px = HealthBar.getWhitePixel();
        if (px != null) {
            batch.setColor(0f, 0f, 0f, 0.45f);
            batch.draw(px, SKIP_X, SKIP_Y, SKIP_W, SKIP_H);
            batch.setColor(0.6f, 0.55f, 0.3f, 0.8f);
            batch.draw(px, SKIP_X, SKIP_Y,          SKIP_W, 1.5f);
            batch.draw(px, SKIP_X, SKIP_Y+SKIP_H-1, SKIP_W, 1.5f);
            batch.setColor(Color.WHITE);
        }
        if (font != null) {
            layout.setText(font, "»");
            font.setColor(0.8f, 0.72f, 0.35f, 0.85f);
            font.draw(batch, "»", SKIP_X + (SKIP_W - layout.width) / 2f,
                      SKIP_Y + (SKIP_H + layout.height) / 2f);
            font.setColor(Color.WHITE);
        }

        batch.setColor(1, 1, 1, 1);
        batch.end();

        // Check skip tap
        if (Gdx.input.justTouched()) {
            float wx = Gdx.input.getX() * Constants.WORLD_WIDTH  / Gdx.graphics.getWidth();
            float wy = (Gdx.graphics.getHeight() - Gdx.input.getY())
                     * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();
            if (wx >= SKIP_X && wx <= SKIP_X + SKIP_W && wy >= SKIP_Y && wy <= SKIP_Y + SKIP_H) {
                game.setScreen(new ClassSelectionScreen(game));
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
            game.setScreen(new ClassSelectionScreen(game));
        }
    }
}
