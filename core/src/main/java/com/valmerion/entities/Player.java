package com.valmerion.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;

/**
 * Player-controlled hero.
 *
 * <p>Input: A/D or LEFT/RIGHT — move; SPACE — jump; F — attack.
 * Gravity and ground collision are handled here (no Box2D).
 */
public class Player extends Entity {

    // Sprite display size (may differ from hitbox)
    private static final float DISPLAY_W = 96f;
    private static final float DISPLAY_H = 96f;

    private static final float HITBOX_W  = 48f;
    private static final float HITBOX_H  = 80f;

    private static final float GRAVITY       = -900f;
    private static final float GROUND_Y      = 160f;   // platform top

    // ── Animation ─────────────────────────────────────────────────────────────
    private final AnimationSet animations;

    // ── State flags ───────────────────────────────────────────────────────────
    private boolean onGround     = false;
    private boolean attacking    = false;
    private float   attackTimer  = 0f;
    private static final float ATTACK_DURATION = 0.35f;

    private float hitFlashTimer  = 0f;

    // ── Sounds ────────────────────────────────────────────────────────────────
    private final Sound sndJump;
    private final Sound sndAttack;

    // ── Controls lock (tutorial) ──────────────────────────────────────────────
    private boolean canMove   = true;
    private boolean canJump   = false;
    private boolean canAttack = false;

    public Player(AssetLoader assets, float x, float y) {
        super(x, y, HITBOX_W, HITBOX_H, Constants.PLAYER_MAX_HP);

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
        hitFlashTimer = 0.25f;
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = animations.getCurrentFrame();
        if (frame == null) {
            drawPlaceholder(batch);
            return;
        }

        // Flash white when hit
        if (hitFlashTimer > 0f) {
            float t = (hitFlashTimer / 0.25f);
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

    // ── Tutorial control locks ────────────────────────────────────────────────

    public void unlockJump()   { canJump   = true; }
    public void unlockAttack() { canAttack = true; }
    public void lockAll()      { canMove = false; canJump = false; canAttack = false; }
    public void unlockAll()    { canMove = true;  canJump = true;  canAttack = true; }

    public boolean isAttacking() { return attacking; }

    /** Returns the attack hitbox (in front of player). Null when not attacking. */
    public Rectangle getAttackHitbox() {
        if (!attacking) return null;
        float ax = facingRight
                ? position.x + HITBOX_W
                : position.x - Constants.PLAYER_ATTACK_RANGE;
        return new Rectangle(ax, position.y + 10, Constants.PLAYER_ATTACK_RANGE, 60f);
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void handleInput(float delta) {
        // Horizontal movement
        velocity.x = 0;
        if (canMove) {
            if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)) {
                velocity.x = -Constants.PLAYER_MOVE_SPEED;
                facingRight = false;
            }
            if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)) {
                velocity.x = Constants.PLAYER_MOVE_SPEED;
                facingRight = true;
            }
        }

        // Jump
        if (canJump && onGround
                && (Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.UP))) {
            velocity.y = Constants.PLAYER_JUMP_IMPULSE;
            onGround = false;
            if (sndJump != null) sndJump.play(0.6f);
        }

        // Attack
        if (canAttack && !attacking
                && (Gdx.input.isKeyJustPressed(Keys.F) || Gdx.input.isKeyJustPressed(Keys.Z))) {
            attacking   = true;
            attackTimer = 0f;
            if (sndAttack != null) sndAttack.play(0.7f);
        }

        // Attack duration
        if (attacking) {
            attackTimer += delta;
            if (attackTimer >= ATTACK_DURATION) {
                attacking = false;
            }
        }
    }

    private void applyGravity(float delta) {
        if (!onGround) {
            velocity.y += GRAVITY * delta;
        }
    }

    private void move(float delta) {
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Ground check
        if (position.y <= GROUND_Y) {
            position.y = GROUND_Y;
            velocity.y = 0;
            onGround = true;
        }

        // Clamp to world boundaries
        position.x = Math.max(0, Math.min(position.x,
                Constants.WORLD_WIDTH - HITBOX_W));

        syncHitbox();
    }

    private void updateAnimation(float delta) {
        AnimationSet.State next;
        if (attacking) {
            next = AnimationSet.State.ATTACK;
        } else if (!onGround) {
            next = velocity.y > 0 ? AnimationSet.State.JUMP : AnimationSet.State.FALL;
        } else if (velocity.x != 0) {
            next = AnimationSet.State.WALK;
        } else {
            next = AnimationSet.State.IDLE;
        }
        animations.setState(next);
        animations.update(delta);
    }

    private void drawPlaceholder(SpriteBatch batch) {
        Texture wp = getSharedWhitePixel();
        if (wp == null) return;
        float drawX = position.x + (HITBOX_W - DISPLAY_W) / 2f;
        // Torso (magenta)
        batch.setColor(0.8f, 0.1f, 0.8f, 1f);
        batch.draw(wp, drawX + 16f, position.y, 64f, 72f);
        // Head
        batch.setColor(1.0f, 0.8f, 0.6f, 1f);
        batch.draw(wp, drawX + 28f, position.y + 68f, 40f, 28f);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
