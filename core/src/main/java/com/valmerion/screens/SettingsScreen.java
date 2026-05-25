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
 * Settings screen — placeholder ("coming soon" UI).
 * Press ESC or BACKSPACE to return to the main menu.
 */
public class SettingsScreen extends BaseScreen {

    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();
    private float fadeAlpha = 0f;

    public SettingsScreen(ValmerionGame game) {
        super(game);
        font = assets.font(AssetLoader.FONT_MAIN);
    }

    @Override
    public void show() {
        fadeAlpha = 0f;
    }

    @Override
    public void render(float delta) {
        fadeAlpha = Math.min(1f, fadeAlpha + delta * 2f);

        Gdx.gl.glClearColor(0.05f, 0.04f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(1, 1, 1, fadeAlpha);

        if (font != null) {
            String title = "Настройки";
            layout.setText(font, title);
            font.setColor(1f, 0.85f, 0.4f, fadeAlpha);
            font.draw(batch, title,
                    (Constants.WORLD_WIDTH - layout.width) / 2f,
                    Constants.WORLD_HEIGHT / 2f + 60f);

            String sub = "В разработке...";
            layout.setText(font, sub);
            font.setColor(0.8f, 0.8f, 0.8f, fadeAlpha * 0.8f);
            font.draw(batch, sub,
                    (Constants.WORLD_WIDTH - layout.width) / 2f,
                    Constants.WORLD_HEIGHT / 2f);

            String back = "[ ESC ] — вернуться";
            layout.setText(font, back);
            font.setColor(0.6f, 0.6f, 0.6f, fadeAlpha * 0.6f);
            font.draw(batch, back,
                    (Constants.WORLD_WIDTH - layout.width) / 2f,
                    100f);
            font.setColor(1f, 1f, 1f, 1f);
        }

        batch.setColor(1, 1, 1, 1);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Keys.BACKSPACE)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void dispose() {}
}
