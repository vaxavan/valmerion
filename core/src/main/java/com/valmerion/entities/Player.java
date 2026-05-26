package com.valmerion.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.assets.AssetLoader;
import com.valmerion.input.InputController;
import com.valmerion.utils.Constants;

/**
 * Player-controlled hero.
 *
 * <p>Movement is driven by an {@link InputController}, which abstracts
 * keyboard (desktop) and on-screen buttons (Android) behind a single API.
 *
 * <p>Gravity and ground collision handled internally (no Box2D).
 */
public class Player extends Entity {

    // Sprite display size (may differ from hitbox)
    private static final float DISPLAY_W = 96f;
    private static final float DISPLAY_H = 96f;
    private static final float HITBOX_W  = 48f;
    private static final float HITBOX_H  = 80f;

    private static final float GRAVITY        = -900f;
    private static final float GROUND_Y       = 160f;    // platform surface
    private static final float ATTACK_DURATION = 0.35f;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final AnimationSet    animations;
    private final InputController input;
    private final Sound           sndJump;
    private final Sound           sndAttack;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean onGround      = false;
    private boolean attacking     = false;
    private float   attackTimer   = 0f;
    private float   hitFlashTimer = 0f;

    // ── Tutorial ability locks ─────────────────────────────────────────────────
    private boolean canMove   = true;
    private boolean canJump   = false;
    private boolean canAttack = false;

    // ── Construction ──────────────────────────────────────────────────────────

    public Player(AssetLoader assets, float x, float y, InputController input) {
        super(x, y, HITBOX_W, HITBOX_H, Constants.PLAYER_MAX_HP);
        this.input = input;
        animations = new AnimationSet(assets.atlas(AssetLoader.ATLAS_PLAYER), "player");
        sndJump    = assets.sound(AssetLoader.SFX_JUMP);
        sndAttack  = assets.sound(AssetLoader.SFX_ATTACK);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void update(float delta) {
        if (!alive) {
            animations.setState(AnimationSet.State.DEAD);
            animations.update(delta);
            return;
        }

        handleInput(delta);
        applyGravity(delta);
        move(delta);
        updateAnimation(delta);

        hitFlashTimer = Math.max(0f, hitFlashTimer - delta);
    }

    @Override
    public void takeDamage(float amount) {
        super.takeDamage(amount);
        hitFlashTimer = 0.30f;
    }

    /**
     * Bring the player back to life (used when goblin kills them during tutorial).
     *
     * @param newHp HP to restore (clamped to max).
     */
    public void revive(float newHp) {
        hp           = Math.min(newHp, maxHp);
        alive        = true;
        velocity.set(0, 0);
        position.set(200f, GROUND_Y + 10f);
        syncHitbox();
        hitFlashTimer = 0.5f;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = animations.getCurrentFrame();
        if (frame == null) {
            drawPlaceholder(batch);
            return;
        }

        if (hitFlashTimer > 0f) {
            float t = hitFlashTimer / 0.30f;
            batch.setColor(1f, 1f - t * 0.6f, 1f - t * 0.6f, 1f);
        }

        float drawX = position.x + (HITBOX_W - DISPLAY_W) / 2f;
        float drawY = position.y;

        if (!facingRight) {
            batch.draw(frame, drawX + DISPLAY_W, drawY, -DISPLAY_W, DISPLAY_H);
        } else {
            batch.draw(frame, drawX, drawY, DISPLAY_W, DISPLAY_H);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    // ── Tutorial locks ────────────────────────────────────────────────────────

    public void unlockJump()   { canJump   = true; }
    public void unlockAttack() { canAttack = true; }
    public void lockAll()      { canMove   = false; canJump = false; canAttack = false; }
    public void unlockAll()    { canMove   = true;  canJump = true;  canAttack = true;  }

    public boolean isAttacking() { return attacking; }

    /** Returns the attack hitbox in front of the player. Null when not attacking. */
    public Rectangle getAttackHitbox() {
        if (!attacking) return null;
        float ax = facingRight
                ? position.x + HITBOX_W
                : position.x - Constants.PLAYER_ATTACK_RANGE;
        return new Rectangle(ax, position.y + 10f, Constants.PLAYER_ATTACK_RANGE, 60f);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void handleInput(float delta) {
        velocity.x = 0;
        if (canMove) {
            if (input.isMoveLeft())  { velocity.x = -Constants.PLAYER_MOVE_SPEED; facingRight = false; }
            if (input.isMoveRight()) { velocity.x =  Constants.PLAYER_MOVE_SPEED; facingRight = true;  }
        }

        if (canJump && onGround && input.isJumpJustPressed()) {
            velocity.y = Constants.PLAYER_JUMP_IMPULSE;
            onGround   = false;
            if (sndJump != null) sndJump.play(0.6f);
        }

        if (canAttack && !attacking && input.isAttackJustPressed()) {
            attacking   = true;
            attackTimer = 0f;
            if (sndAttack != null) sndAttack.play(0.7f);
        }

        if (attacking) {
            attackTimer += delta;
            if (attackTimer >= ATTACK_DURATION) attacking = false;
        }
    }

    private void applyGravity(float delta) {
        if (!onGround) velocity.y += GRAVITY * delta;
    }

    private void move(float delta) {
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        if (position.y <= GROUND_Y) {
            position.y = GROUND_Y;
            velocity.y = 0;
            onGround   = true;
        }

        position.x = Math.max(0, Math.min(position.x, Constants.WORLD_WIDTH - HITBOX_W));
        syncHitbox();
    }

    private void updateAnimation(float delta) {
        AnimationSet.State next;
        if (attacking)      next = AnimationSet.State.ATTACK;
        else if (!onGround) next = velocity.y > 0 ? AnimationSet.State.JUMP : AnimationSet.State.FALL;
        else if (velocity.x != 0) next = AnimationSet.State.WALK;
        else                next = AnimationSet.State.IDLE;

        animations.setState(next);
        animations.update(delta);
    }

    private void drawPlaceholder(SpriteBatch batch) {
        // Draw a blue-ish rectangle as placeholder
        batch.setColor(0.3f, 0.5f, 0.9f, 1f);
        // We need a pixel texture — use HealthBar's white pixel if available
        com.badlogic.gdx.graphics.Texture wp = com.valmerion.utils.PlaceholderTextures.whitePixel();
        if (wp != null) {
            float drawX = position.x + (HITBOX_W - DISPLAY_W) / 2f;
            batch.draw(wp, drawX, position.y, DISPLAY_W, DISPLAY_H);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
