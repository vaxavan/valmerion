package com.valmerion.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;

/**
 * Base class for every screen. Provides shared access to game, batch,
 * assets and a FitViewport (letterbox at 1280×720).
 */
public abstract class BaseScreen implements Screen {

    protected final ValmerionGame game;
    protected final SpriteBatch   batch;
    protected final AssetLoader   assets;
    protected final Viewport      viewport;

    protected BaseScreen(ValmerionGame game) {
        this.game     = game;
        this.batch    = game.getBatch();
        this.assets   = game.getAssets();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}
}
