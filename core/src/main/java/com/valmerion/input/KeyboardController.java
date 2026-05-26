package com.valmerion.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Desktop keyboard input controller.
 * Move: A / LEFT, D / RIGHT
 * Jump: SPACE / UP
 * Attack: F / Z
 */
public class KeyboardController implements InputController {

    private boolean jumpJustPressed;
    private boolean attackJustPressed;

    @Override
    public void update() {
        jumpJustPressed   = Gdx.input.isKeyJustPressed(Keys.SPACE)
                         || Gdx.input.isKeyJustPressed(Keys.UP);
        attackJustPressed = Gdx.input.isKeyJustPressed(Keys.F)
                         || Gdx.input.isKeyJustPressed(Keys.Z);
    }

    @Override public boolean isMoveLeft()         { return Gdx.input.isKeyPressed(Keys.A)    || Gdx.input.isKeyPressed(Keys.LEFT);  }
    @Override public boolean isMoveRight()        { return Gdx.input.isKeyPressed(Keys.D)    || Gdx.input.isKeyPressed(Keys.RIGHT); }
    @Override public boolean isJumpJustPressed()  { return jumpJustPressed;   }
    @Override public boolean isAttackJustPressed(){ return attackJustPressed; }

    @Override public void renderOverlay(SpriteBatch batch) { /* no overlay for keyboard */ }
    @Override public void dispose() {}
}
