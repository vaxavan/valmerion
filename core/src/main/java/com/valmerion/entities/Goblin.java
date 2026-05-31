package com.valmerion.entities;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.valmerion.assets.AssetLoader;
import com.valmerion.utils.Constants;

public class Goblin extends Entity {

    private static final float DISPLAY_W  = 96f;
    private static final float DISPLAY_H  = 96f;
    private static final float HITBOX_W   = 48f;
    private static final float HITBOX_H   = 64f;
    private static final float GROUND_Y   = 128f;
    private static final float GRAVITY    = -900f;

    // Attack animation: 4 frames × 0.07 s = 0.28 s.
    // Damage lands on the 3rd frame — the actual swing moment.
    private static final float ATTACK_HIT_TIME = 0.18f;
    private static final float ATTACK_TOTAL    = 0.32f;

    public enum Mode { STATIC, AGGRESSIVE }

    private Mode  mode;
    private float hitFlashTimer  = 0f;

    // Attack state machine
    private boolean attacking      = false;
    private float   attackTimer    = 0f;
    private boolean hitDelivered   = false;
    private float   attackCooldown = 0f;

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

    public void setMode(Mode mode)                    { this.mode = mode; }
    public void setAttackListener(OnAttackListener l) { this.attackListener = l; }

    @Override
    public void takeDamage(float amount) {
        super.takeDamage(amount);
        hitFlashTimer = 0.2f;
        if (sndHit != null) sndHit.play(0.5f);
    }

    @Override
    public void update(float delta) {
        if (!alive) {
            animations.setState(AnimationSet.State.DEAD);
            animations.update(delta);
            return;
        }

        hitFlashTimer  = Math.max(0, hitFlashTimer  - delta);
        attackCooldown = Math.max(0, attackCooldown - delta);

        if (attacking) {
            attackTimer += delta;
            // Deliver damage at the swing frame
            if (!hitDelivered && attackTimer >= ATTACK_HIT_TIME) {
                hitDelivered = true;
                if (attackListener != null)
                    attackListener.onGoblinAttack(Constants.GOBLIN_ATTACK_DAMAGE);
            }
            if (attackTimer >= ATTACK_TOTAL) {
                attacking      = false;
                attackTimer    = 0f;
                attackCooldown = Constants.GOBLIN_ATTACK_COOLDOWN;
                animations.setState(AnimationSet.State.IDLE);
            } else {
                animations.setState(AnimationSet.State.ATTACK);
            }
        }

        applyGravity(delta);
        position.y = Math.max(GROUND_Y, position.y);
        syncHitbox();
        animations.update(delta);
    }

    public void updateAI(float delta, float playerX, float playerCenterX) {
        if (!alive) return;

        if (mode == Mode.STATIC) {
            // Static goblins still attack when player is in range
            if (!attacking && attackCooldown <= 0f) {
                float dist = Math.abs(playerCenterX - (position.x + HITBOX_W / 2f));
                float extra = (displayScale - 1f) * DISPLAY_W * 0.5f;
                if (dist <= Constants.GOBLIN_ATTACK_RANGE + extra) {
                    attacking    = true;
                    attackTimer  = 0f;
                    hitDelivered = false;
                    facingRight  = playerCenterX > position.x + HITBOX_W / 2f;
                    animations.setState(AnimationSet.State.ATTACK);
                    return;
                }
            }
            if (!attacking) animations.setState(AnimationSet.State.IDLE);
            return;
        }

        if (attacking) return;   // don't interrupt an ongoing swing

        float dist = Math.abs(playerCenterX - (position.x + HITBOX_W / 2f));

        // When displayScale > 1 the sprite is visually larger than the hitbox.
        // Extend attack/aggro ranges proportionally so behaviour matches visuals.
        float extra       = (displayScale - 1f) * DISPLAY_W * 0.5f;
        float attackRange = Constants.GOBLIN_ATTACK_RANGE + extra;
        float aggroRange  = Constants.GOBLIN_AGGRO_RANGE  + extra;

        if (dist > aggroRange) {
            velocity.x = 0;
            animations.setState(AnimationSet.State.IDLE);
        } else if (dist > attackRange) {
            float dir   = playerCenterX > position.x + HITBOX_W / 2f ? 1f : -1f;
            velocity.x  = dir * Constants.GOBLIN_MOVE_SPEED;
            facingRight  = dir > 0;
            position.x  += velocity.x * delta;
            position.x   = Math.max(0, Math.min(position.x, Constants.WORLD_WIDTH - HITBOX_W));
            syncHitbox();
            animations.setState(AnimationSet.State.WALK);
        } else {
            velocity.x = 0;
            if (attackCooldown <= 0f) {
                attacking    = true;
                attackTimer  = 0f;
                hitDelivered = false;
                facingRight  = playerCenterX > position.x + HITBOX_W / 2f;
                animations.setState(AnimationSet.State.ATTACK);
            } else {
                animations.setState(AnimationSet.State.IDLE);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion frame = animations.getCurrentFrame();
        if (frame == null) return;

        if (hitFlashTimer > 0) {
            float t = hitFlashTimer / 0.2f;
            batch.setColor(1f, 1f - t * 0.7f, 1f - t * 0.7f, 1f);
        }

        float dw    = DISPLAY_W * displayScale;
        float dh    = DISPLAY_H * displayScale;
        float drawX = position.x + (HITBOX_W - dw) / 2f;
        if (!facingRight) {
            batch.draw(frame, drawX + dw, position.y, -dw, dh);
        } else {
            batch.draw(frame, drawX, position.y, dw, dh);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Returns the visual bounding box (accounts for displayScale). Use for hit detection. */
    public com.badlogic.gdx.math.Rectangle getVisualHitbox() {
        float dw   = DISPLAY_W * displayScale;
        float dh   = DISPLAY_H * displayScale;
        float drawX = position.x + (HITBOX_W - dw) / 2f;
        return new com.badlogic.gdx.math.Rectangle(drawX, position.y, dw, dh);
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
