package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.ui.MenuButton;
import com.valmerion.utils.Constants;

/**
 * Main menu screen — background + 3 buttons (New Game, Settings, Exit).
 *
 * <p>Buttons are centred vertically with equal spacing.
 * Fade-in animation plays on entry.
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

    public MenuScreen(ValmerionGame game) {
        super(game);

        background = assets.texture(AssetLoader.TEX_MENU_BG);

        // Button textures (null-safe — MenuButton handles missing textures)
        Texture tNew     = assets.texture(AssetLoader.TEX_BTN_NEW_GAME);
        Texture tNewHov  = assets.texture(AssetLoader.TEX_BTN_NEW_GAME_HOV);
        Texture tSet     = assets.texture(AssetLoader.TEX_BTN_SETTINGS);
        Texture tExit    = assets.texture(AssetLoader.TEX_BTN_EXIT);

        float cx   = Constants.WORLD_WIDTH  / 2f - BTN_W / 2f;
        float totalH = BTN_H * 3 + BTN_GAP * 2;
        float baseY  = Constants.WORLD_HEIGHT / 2f - totalH / 2f - 40f; // slight downward offset

        btnNewGame  = new MenuButton(tNew,  tNewHov, "New Game",  cx, baseY + (BTN_H + BTN_GAP) * 2, BTN_W, BTN_H);
        btnSettings = new MenuButton(tSet,  null,    "Settings",  cx, baseY + (BTN_H + BTN_GAP),     BTN_W, BTN_H);
        btnExit     = new MenuButton(tExit, null,    "Exit",       cx, baseY,                         BTN_W, BTN_H);

        // Music
        music = assets.music(AssetLoader.MUSIC_MENU);
        if (music != null) {
            music.setLooping(true);
            music.setVolume(0.45f);
            music.play();
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        fadeAlpha = 0f;
    }

    @Override
    public void render(float delta) {
        fadeAlpha = Math.min(1f, fadeAlpha + delta * 1.8f);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Background
        batch.setColor(fadeAlpha, fadeAlpha, fadeAlpha, 1f);
        if (background != null) {
            batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        }

        // Buttons
        batch.setColor(1f, 1f, 1f, fadeAlpha);
        if (fadeAlpha >= 0.5f) {
            btnNewGame .render(batch, delta);
            btnSettings.render(batch, delta);
            btnExit    .render(batch, delta);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        // Handle clicks only after fade is mostly done
        if (fadeAlpha >= 0.85f) {
            handleInput();
        }
    }

    @Override
    public void dispose() {
        if (music != null) music.stop();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void handleInput() {
        if (btnNewGame.isJustClicked()) {
            click();
            game.setScreen(new CutsceneScreen(game));
        }
        if (btnSettings.isJustClicked()) {
            click();
            game.setScreen(new SettingsScreen(game));
        }
        if (btnExit.isJustClicked() || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void click() {
        Sound s = assets.sound(AssetLoader.SFX_BTN_CLICK);
        if (s != null) s.play(0.75f);
    }
}
