package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.game.GameState;
import com.valmerion.ui.HealthBar;
import com.valmerion.ui.MenuButton;
import com.valmerion.utils.Constants;

/**
 * Main menu. Shows "Продолжить" button only if a game is already in progress.
 */
public class MenuScreen extends BaseScreen {

    private static final float BTN_W   = 380f;
    private static final float BTN_H   = 95f;
    private static final float BTN_GAP = 22f;

    private final MenuButton btnContinue;  // visible only when storyStage > 0
    private final MenuButton btnNewGame;
    private final MenuButton btnSettings;
    private final MenuButton btnExit;

    private final Texture background;
    private float fadeAlpha = 0f;

    private final Music music;

    public MenuScreen(ValmerionGame game) {
        super(game);

        background = assets.texture(AssetLoader.TEX_MENU_BG);

        // White pixel needed for button backgrounds (solid color buttons)
        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1); pm.fill();
        com.badlogic.gdx.graphics.Texture wp = new com.badlogic.gdx.graphics.Texture(pm);
        pm.dispose();
        HealthBar.setWhitePixel(wp);

        // Pass font to MenuButton so labels render over any texture
        com.badlogic.gdx.graphics.g2d.BitmapFont menuFont = assets.font(AssetLoader.FONT_MAIN);
        MenuButton.setFont(menuFont);

        float cx = Constants.WORLD_WIDTH / 2f - BTN_W / 2f;

        // 4 buttons max (Continue + New Game + Settings + Exit)
        float totalH = BTN_H * 4 + BTN_GAP * 3;
        float baseY  = Constants.WORLD_HEIGHT / 2f - totalH / 2f - 20f;

        // Use null textures — solid colored buttons look better and work everywhere
        btnContinue = new MenuButton(null, null, "Продолжить", cx, baseY + (BTN_H + BTN_GAP) * 3, BTN_W, BTN_H);
        btnNewGame  = new MenuButton(null, null, "Новая игра", cx, baseY + (BTN_H + BTN_GAP) * 2, BTN_W, BTN_H);
        btnSettings = new MenuButton(null, null, "Настройки",  cx, baseY + (BTN_H + BTN_GAP),     BTN_W, BTN_H);
        btnExit     = new MenuButton(null, null, "Выход",      cx, baseY,                          BTN_W, BTN_H);

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
        if (background != null)
            batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        batch.setColor(1f, 1f, 1f, fadeAlpha);
        if (fadeAlpha >= 0.5f) {
            boolean hasSave = GameState.INSTANCE.storyStage > 0;
            if (hasSave) btnContinue.render(batch, delta);
            btnNewGame .render(batch, delta);
            btnSettings.render(batch, delta);
            btnExit    .render(batch, delta);
        }
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        if (fadeAlpha >= 0.85f) handleInput();
    }

    @Override
    public void dispose() {
        if (music != null) music.stop();
    }

    private void handleInput() {
        boolean hasSave = GameState.INSTANCE.storyStage > 0;
        if (hasSave && btnContinue.isJustClicked()) {
            click();
            if (music != null) music.stop();
            // Resume from where player left off
            int stage = GameState.INSTANCE.storyStage;
            if (stage == 0) {
                game.setScreen(new AcademyScreen(game));
            } else {
                game.setScreen(new WorldScreen(game));
            }
        }
        if (btnNewGame.isJustClicked()) {
            click();
            GameState.INSTANCE.storyStage  = 0;
            GameState.INSTANCE.reputation  = 0f;
            GameState.INSTANCE.hunger      = 75f;
            if (music != null) music.stop();
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
