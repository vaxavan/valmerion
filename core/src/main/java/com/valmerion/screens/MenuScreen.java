package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.ui.MenuButton;
import com.valmerion.utils.Constants;
import com.valmerion.utils.PlaceholderTextures;

/**
 * Main menu screen — background + 3 buttons (New Game, Settings, Exit).
 *
 * <p>Touch/cursor position is unprojected through the FitViewport before
 * being passed to each button, so hit-testing is correct on all Android
 * devices regardless of letterbox/pillarbox padding.
 */
public class MenuScreen extends BaseScreen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final float BTN_W   = 380f;
    private static final float BTN_H   = 95f;
    private static final float BTN_GAP = 28f;

    private final MenuButton btnNewGame;
    private final MenuButton btnSettings;
    private final MenuButton btnExit;

    // ── Visuals ───────────────────────────────────────────────────────────────
    private final Texture background;
    private float fadeAlpha = 0f;

    // ── Audio ─────────────────────────────────────────────────────────────────
    private final Music music;

    // ── Touch ─────────────────────────────────────────────────────────────────
    private final Vector3 touchWorld = new Vector3();

    public MenuScreen(ValmerionGame game) {
        super(game);

        Texture bg = assets.texture(AssetLoader.TEX_MENU_BG);
        background = bg != null ? bg : PlaceholderTextures.menuBackground();

        Texture tNew    = assets.texture(AssetLoader.TEX_BTN_NEW_GAME);
        Texture tNewHov = assets.texture(AssetLoader.TEX_BTN_NEW_GAME_HOV);
        Texture tSet    = assets.texture(AssetLoader.TEX_BTN_SETTINGS);
        Texture tExit   = assets.texture(AssetLoader.TEX_BTN_EXIT);

        if (tNew  == null) tNew  = PlaceholderTextures.button();
        if (tSet  == null) tSet  = PlaceholderTextures.button();
        if (tExit == null) tExit = PlaceholderTextures.button();

        float cx     = Constants.WORLD_WIDTH  / 2f - BTN_W / 2f;
        float totalH = BTN_H * 3 + BTN_GAP * 2;
        float baseY  = Constants.WORLD_HEIGHT / 2f - totalH / 2f - 40f;

        btnNewGame  = new MenuButton(tNew,  tNewHov, "Новая игра", cx, baseY + (BTN_H + BTN_GAP) * 2, BTN_W, BTN_H);
        btnSettings = new MenuButton(tSet,  null,    "Настройки",  cx, baseY + (BTN_H + BTN_GAP),     BTN_W, BTN_H);
        btnExit     = new MenuButton(tExit, null,    "Выход",      cx, baseY,                         BTN_W, BTN_H);

        music = assets.music(AssetLoader.MUSIC_MENU);
        if (music != null) {
            music.setLooping(true);
            music.setVolume(0.45f);
            music.play();
        }
    }

    @Override public void show() { fadeAlpha = 0f; }

    @Override
    public void render(float delta) {
        fadeAlpha = Math.min(1f, fadeAlpha + delta * 1.8f);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        batch.setColor(fadeAlpha, fadeAlpha, fadeAlpha, 1f);
        batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        if (fadeAlpha >= 0.5f) {
            // Unproject cursor/touch through viewport to get world-space position
            touchWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchWorld);

            batch.setColor(1f, 1f, 1f, fadeAlpha);
            btnNewGame .render(batch, delta, touchWorld.x, touchWorld.y);
            btnSettings.render(batch, delta, touchWorld.x, touchWorld.y);
            btnExit    .render(batch, delta, touchWorld.x, touchWorld.y);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        if (fadeAlpha >= 0.85f) handleInput();
    }

    @Override
    public void dispose() {
        if (music != null) music.stop();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void handleInput() {
        if (btnNewGame.isJustClicked()) {
            playClick();
            game.setScreen(new CutsceneScreen(game));
        }
        if (btnSettings.isJustClicked()) {
            playClick();
            game.setScreen(new SettingsScreen(game));
        }
        if (btnExit.isJustClicked() || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void playClick() {
        Sound s = assets.sound(AssetLoader.SFX_BTN_CLICK);
        if (s != null) s.play(0.75f);
    }
}
