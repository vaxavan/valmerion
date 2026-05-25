package com.valmerion;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.valmerion.assets.AssetLoader;
import com.valmerion.screens.MenuScreen;
import com.valmerion.utils.Constants;

/**
 * Main game class. Entry point for all screens.
 * Holds shared resources: SpriteBatch, AssetLoader.
 */
public class ValmerionGame extends Game {

    private SpriteBatch batch;
    private AssetLoader assetLoader;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetLoader = new AssetLoader();
        assetLoader.loadAll();

        Gdx.app.log("Valmerion", "Game started. Resolution: "
                + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight());

        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        assetLoader.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public AssetLoader getAssets() {
        return assetLoader;
    }
}
