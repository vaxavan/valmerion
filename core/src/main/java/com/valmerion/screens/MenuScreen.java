package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.game.GameState;
import com.valmerion.ui.HealthBar;
import com.valmerion.ui.MenuButton;
import com.valmerion.utils.Constants;

public class MenuScreen extends BaseScreen {

    private static final float BTN_W   = 400f;
    private static final float BTN_H   = 90f;
    private static final float BTN_GAP = 18f;

    private final MenuButton btnContinue;
    private final MenuButton btnNewGame;
    private final MenuButton btnExit;

    private final Texture     background;
    private final BitmapFont  font;
    private final BitmapFont  titleFont;
    private final GlyphLayout layout = new GlyphLayout();
    private float fadeAlpha = 0f;

    private final Music music;

    public MenuScreen(ValmerionGame game) {
        super(game);

        background = assets.texture(AssetLoader.TEX_MENU_BG);
        font       = assets.font(AssetLoader.FONT_MAIN);
        titleFont  = assets.font(AssetLoader.FONT_TITLE);

        // White pixel for button drawing
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1); pm.fill();
        HealthBar.setWhitePixel(new Texture(pm));
        pm.dispose();

        MenuButton.setFont(font);

        float cx    = Constants.WORLD_WIDTH / 2f - BTN_W / 2f;
        float totalH = BTN_H * 3 + BTN_GAP * 2;
        // Keep buttons in lower half so title has room at top
        float baseY  = Constants.WORLD_HEIGHT / 2f - totalH / 2f - 100f;

        btnContinue = new MenuButton(null, null, "Продолжить", cx, baseY + (BTN_H + BTN_GAP) * 2, BTN_W, BTN_H);
        btnNewGame  = new MenuButton(null, null, "Новая игра", cx, baseY + (BTN_H + BTN_GAP),     BTN_W, BTN_H);
        btnExit     = new MenuButton(null, null, "Выход",      cx, baseY,                          BTN_W, BTN_H);

        music = assets.music(AssetLoader.MUSIC_MENU);
        if (music != null) { music.setLooping(true); music.setVolume(0.45f); music.play(); }
    }

    @Override public void show() { fadeAlpha = 0f; }

    @Override
    public void render(float delta) {
        fadeAlpha = Math.min(1f, fadeAlpha + delta * 1.5f);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        // Background (slightly dimmed)
        batch.setColor(fadeAlpha, fadeAlpha, fadeAlpha, 1f);
        if (background != null)
            batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        // Dark overlay for readability
        Texture px = HealthBar.getWhitePixel();
        if (px != null) {
            batch.setColor(0f, 0f, 0f, fadeAlpha * 0.25f);
            batch.draw(px, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        }

        batch.setColor(1f, 1f, 1f, fadeAlpha);

        // Title "VALMERION"
        drawTitle(batch, fadeAlpha);

        // Buttons (show when faded in)
        if (fadeAlpha >= 0.5f) {
            if (GameState.INSTANCE.gameStarted) btnContinue.render(batch, delta);
            btnNewGame.render(batch, delta);
            btnExit   .render(batch, delta);
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();

        if (fadeAlpha >= 0.85f) handleInput();
    }

    @Override
    public void dispose() {
        if (music != null) music.stop();
    }

    private void drawTitle(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, float alpha) {
        BitmapFont f = titleFont != null ? titleFont : font;
        if (f == null) return;

        // Title sits high — leave plenty of room below for buttons
        String title = "VALMERION";
        layout.setText(f, title);
        float titleH = layout.height;
        float tx = (Constants.WORLD_WIDTH - layout.width) / 2f;
        float ty = Constants.WORLD_HEIGHT - 55f;   // near top

        // Gold glow shadow
        f.setColor(0.55f, 0.35f, 0f, alpha * 0.55f);
        f.draw(batch, title, tx + 5f, ty - 5f);
        // Gold text
        f.setColor(1f, 0.84f, 0.20f, alpha);
        f.draw(batch, title, tx, ty);

        // Decorative line under title
        Texture px = HealthBar.getWhitePixel();
        if (px != null) {
            float lineY = ty - titleH - 8f;
            float lineW = layout.width * 1.1f;
            float lineX = (Constants.WORLD_WIDTH - lineW) / 2f;
            batch.setColor(1f, 0.84f, 0.20f, alpha * 0.6f);
            batch.draw(px, lineX, lineY, lineW, 2f);
            batch.setColor(Color.WHITE);
        }

        // Subtitle — well below the line
        if (font != null) {
            String sub = "Академия Валмерион";
            layout.setText(font, sub);
            float sx2 = (Constants.WORLD_WIDTH - layout.width) / 2f;
            float sy2  = ty - titleH - 28f;        // 28 px below the title line
            font.setColor(0.70f, 0.60f, 0.28f, alpha * 0.85f);
            font.draw(batch, sub, sx2, sy2);
            font.setColor(Color.WHITE);
        }

        f.setColor(Color.WHITE);
    }

    private void handleInput() {
        // hasSave: player has started a game (class chosen = visited ClassSelectionScreen)
        // selectedClass starts as "archer" (default) — use a sentinel to detect "never started"
        boolean hasSave = GameState.INSTANCE.gameStarted;
        if (hasSave && btnContinue.isJustClicked()) {
            click();
            if (music != null) music.stop();
            int s = GameState.INSTANCE.storyStage;
            if (s == 1) game.setScreen(new AcademyScreen(game));
            else        game.setScreen(new WorldScreen(game));
        }
        if (btnNewGame.isJustClicked()) {
            click();
            GameState.INSTANCE.storyStage    = 0;
            GameState.INSTANCE.reputation    = 0f;
            GameState.INSTANCE.hunger        = 75f;
            GameState.INSTANCE.selectedClass = "archer";
            GameState.INSTANCE.gameStarted   = false;
            if (music != null) music.stop();
            game.setScreen(new CutsceneScreen(game));
        }
        if (btnExit.isJustClicked()) {
            Gdx.app.exit();
        }
    }

    private void click() {
        Sound s = assets.sound(AssetLoader.SFX_BTN_CLICK);
        if (s != null) s.play(0.75f);
    }
}
