package com.valmerion.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;

/**
 * Goblin enemy.
 *
 * <p>Two behaviour modes:
 * <ul>
 *   <li>{@code static} — stands still (tutorial dummy)</li>
 *   <li>{@code aggressive} — walks toward player and attacks</li>
 * </ul>
 */
public class Goblin extends Entity {

    private static final float DISPLAY_W  = 80f;
    private static final float DISPLAY_H  = 80f;
    private static final float HITBOX_W   = 48f;
    private static final float HITBOX_H   = 64f;
    private static final float GROUND_Y   = 160f;
    private static final float GRAVITY    = -900f;

    public enum Mode { STATIC, AGGRESSIVE }

    private Mode  mode;
    private float attackCooldown = 0f;
    private float hitFlashTimer  = 0f;

    // Callback so the screen knows when the goblin attacks
    public interface OnAttackListener { void onGoblinAttack(float damage); }
    private OnAttackListener attackListener;

    private final AnimationSet animations;
    private final Sound sndHit;

    public Goblin(AssetLoader assets, float x, float y, Mode mode) {
        super(x, y, HITBOX_W, HITBOX_H, Constants.GOBLIN_MAX_HP);
        this.mode = mode;
        animations = new AnimationSet(assets.atlas(AssetLoader.ATLAS_GOBLIN), "goblin");
        sndHit     = assets.sound(AssetLoader.SFX_HIT);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setAttackListener(OnAttackListener l) {
        this.attackListener = l;
    }

    @Override
    public void takeDamage(float amount) {
        super.takeDamage(amount);
        hitFlashTimer = 0.2f;
        if (sndHit != null) sndHit.play(0.5f);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public void update(float delta) {
        if (!alive) {
            animations.setState(AnimationSet.State.DEAD);
            animations.update(delta);
            return;
        }

        hitFlashTimer = Math.max(0, hitFlashTimer - delta);
        attackCooldown = Math.max(0, attackCooldown - delta);
        applyGravity(delta);
        position.y = Math.max(GROUND_Y, position.y);
        syncHitbox();
        animations.update(delta);
    }

    /**
     * AI step — call each frame with the player's position.
     * Static goblins do nothing; aggressive ones chase and attack.
     */
    public void updateAI(float delta, float playerX, float playerCenterX) {
        if (!alive || mode == Mode.STATIC) {
            animations.setState(AnimationSet.State.IDLE);
            return;
        }

        float dist = Math.abs(playerCenterX - (position.x + HITBOX_W / 2f));

        if (dist > Constants.GOBLIN_AGGRO_RANGE) {
            // Idle — out of range
            velocity.x = 0;
            animations.setState(AnimationSet.State.IDLE);
        } else if (dist > Constants.GOBLIN_ATTACK_RANGE) {
            // Chase
            float dir = playerCenterX > position.x + HITBOX_W / 2f ? 1f : -1f;
            velocity.x = dir * Constants.GOBLIN_MOVE_SPEED;
            facingRight = dir > 0;
            position.x += velocity.x * delta;
            position.x = Math.max(0, Math.min(position.x, Constants.WORLD_WIDTH - HITBOX_W));
            syncHitbox();
            animations.setState(AnimationSet.State.WALK);
        } else {
            // Attack range
            velocity.x = 0;
            animations.setState(AnimationSet.State.ATTACK);
            if (attackCooldown <= 0f) {
                attackCooldown = Constants.GOBLIN_ATTACK_COOLDOWN;
                if (attackListener != null) {
                    attackListener.onGoblinAttack(Constants.GOBLIN_ATTACK_DAMAGE);
                }
            }
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = animations.getCurrentFrame();
        if (frame == null) return;

        if (hitFlashTimer > 0) {
            float t = hitFlashTimer / 0.2f;
            batch.setColor(1f, 1f - t, 1f - t, 1f);
        }

        float drawX = position.x + (HITBOX_W - DISPLAY_W) / 2f;

        if (!facingRight) {
            batch.draw(frame, drawX + DISPLAY_W, position.y, -DISPLAY_W, DISPLAY_H);
        } else {
            batch.draw(frame, drawX, position.y, DISPLAY_W, DISPLAY_H);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void applyGravity(float delta) {
        velocity.y += GRAVITY * delta;
        position.y += velocity.y * delta;
        if (position.y <= GROUND_Y) {
            position.y = GROUND_Y;
            velocity.y = 0;
        }
    }
}
