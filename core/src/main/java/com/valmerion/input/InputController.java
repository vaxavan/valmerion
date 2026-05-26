package com.valmerion.input;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Platform-agnostic input abstraction.
 *
 * <p>Implementations:
 * <ul>
 *   <li>{@link KeyboardController} — keyboard / mouse (desktop)</li>
 *   <li>{@link TouchController}   — virtual on-screen buttons (Android)</li>
 * </ul>
 *
 * <p>Call {@link #update()} once per frame before reading any state.
 */
public interface InputController {

    /** Poll the underlying input source. Must be called once per frame. */
    void update();

    boolean isMoveLeft();
    boolean isMoveRight();

    /** True only on the frame the jump button was first pressed. */
    boolean isJumpJustPressed();

    /** True only on the frame the attack button was first pressed. */
    boolean isAttackJustPressed();

    /**
     * Draw touch-control overlay (buttons, etc.).
     * No-op for keyboard controller.
     * Must be called inside an active SpriteBatch.
     */
    void renderOverlay(SpriteBatch batch);

    void dispose();
}
