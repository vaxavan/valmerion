package com.valmerion.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.valmerion.ValmerionGame;

/**
 * Android entry point for Valmerion.
 *
 * <p>Runs in immersive fullscreen landscape mode.
 */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;
        initialize(new ValmerionGame(), config);
    }
}
