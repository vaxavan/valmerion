package com.valmerion.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.assets.AssetLoader;
import com.valmerion.ui.TouchControls;
import com.valmerion.utils.Constants;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import java.util.ArrayList;
import java.util.List;

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
    private static final float GROUND_Y      = 128f;   // platform top

    // ── Class & Animation ─────────────────────────────────────────────────────
    private PlayerClass  playerClass;
    private AnimationSet animations;
    private final TextureAtlas atlas;
    private com.badlogic.gdx.graphics.Texture placeholder;

    // ── State flags ───────────────────────────────────────────────────────────
    private boolean onGround     = false;
    private boolean attacking    = false;
    private float   attackTimer  = 0f;
    private static final float ATTACK_DURATION = 0.35f;

    private float hitFlashTimer  = 0f;

    // ── Sounds ────────────────────────────────────────────────────────────────
    private final Sound sndJump;
    private final Sound sndAttack;

    // ── Arrows (archer) ──────────────────────────────────────────────────────
    private final List<Arrow> arrows = new ArrayList<>();

    // ── Controls lock (tutorial) ──────────────────────────────────────────────
    private boolean canMove   = true;
    private boolean canJump   = false;
    private boolean canAttack = false;

    // ── Touch controls (optional, set from screen) ────────────────────────────
    private TouchControls touchControls = null;

    public void setTouchControls(TouchControls tc) { this.touchControls = tc; }

    public Player(AssetLoader assets, float x, float y) {
        this(assets, x, y, PlayerClass.ARCHER);
    }

    public Player(AssetLoader assets, float x, float y, PlayerClass startClass) {
        super(x, y, HITBOX_W, HITBOX_H, startClass.maxHp);

        this.atlas       = assets.atlas(AssetLoader.ATLAS_PLAYER);
        this.playerClass = startClass;
        this.animations  = new AnimationSet(atlas, startClass.prefix);

        // Чёрный квадрат — заглушка пока нет реального спрайта
        com.badlogic.gdx.graphics.Pixmap pm =
            new com.badlogic.gdx.graphics.Pixmap((int)DISPLAY_W, (int)DISPLAY_H,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(com.badlogic.gdx.graphics.Color.BLACK);
        pm.fill();
        pm.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pm.drawRectangle(0, 0, (int)DISPLAY_W, (int)DISPLAY_H);
        placeholder = new com.badlogic.gdx.graphics.Texture(pm);
        pm.dispose();

        sndJump   = assets.sound(AssetLoader.SFX_JUMP);
        sndAttack = assets.sound(AssetLoader.SFX_ATTACK);
    }

    // ── Class switching ───────────────────────────────────────────────────────

    public void setPlayerClass(PlayerClass cls) {
        if (cls == playerClass) return;
        playerClass = cls;
        animations  = new AnimationSet(atlas, cls.prefix);
        maxHp       = cls.maxHp;
        hp          = Math.min(hp, maxHp);
    }

    public void cycleClass() {
        setPlayerClass(playerClass.next());
    }

    public PlayerClass getPlayerClass() { return playerClass; }

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

        arrows.removeIf(a -> !a.isActive());
        for (Arrow a : arrows) a.update(delta);

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
        } else {

        // Flash white when hit
            if (hitFlashTimer > 0f) {
                float t = (hitFlashTimer / 0.25f);
                batch.setColor(1f, 1f - t * 0.6f, 1f - t * 0.6f, 1f);
            }

            float dw    = DISPLAY_W * displayScale;
            float dh    = DISPLAY_H * displayScale;
            float drawX = position.x + (HITBOX_W - dw) / 2f;
            float drawY = position.y;

            if (!facingRight) {
                batch.draw(frame, drawX + dw, drawY, -dw, dh);
            } else {
                batch.draw(frame, drawX, drawY, dw, dh);
            }
            batch.setColor(1f, 1f, 1f, 1f);
        }

        for (Arrow a : arrows) a.render(batch);
    }

    public List<Arrow> getArrows() { return arrows; }

    @Override
    public void dispose() {
        if (placeholder != null) { placeholder.dispose(); placeholder = null; }
        Arrow.disposeTexture();
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
        // Class switch: Tab
        if (Gdx.input.isKeyJustPressed(Keys.TAB)) {
            cycleClass();
        }

        // Horizontal movement — keyboard OR touch zones
        float speed = playerClass.moveSpeed;
        velocity.x = 0;
        if (canMove) {
            boolean left  = Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT)
                            || (touchControls != null && touchControls.isMoveLeft());
            boolean right = Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT)
                            || (touchControls != null && touchControls.isMoveRight());
            if (left)  { velocity.x = -speed; facingRight = false; }
            if (right) { velocity.x =  speed; facingRight = true;  }
        }

        // Jump — keyboard OR touch button
        boolean jumpKey   = Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.UP);
        boolean jumpTouch = (touchControls != null) && touchControls.isJumpJustPressed();
        if (canJump && onGround && (jumpKey || jumpTouch)) {
            velocity.y = Constants.PLAYER_JUMP_IMPULSE;
            onGround = false;
            if (sndJump != null) sndJump.play(0.6f);
        }

        // Attack — keyboard OR touch button
        boolean attackKey   = Gdx.input.isKeyJustPressed(Keys.F) || Gdx.input.isKeyJustPressed(Keys.Z);
        boolean attackTouch = (touchControls != null) && touchControls.isAttackJustPressed();
        if (canAttack && !attacking && (attackKey || attackTouch)) {
            attacking   = true;
            attackTimer = 0f;
            if (sndAttack != null) sndAttack.play(0.7f);
            if (playerClass == PlayerClass.ARCHER) {
                float arrowX = facingRight ? position.x + HITBOX_W : position.x;
                float arrowY = position.y + HITBOX_H * 0.60f;
                arrows.add(new Arrow(arrowX, arrowY, facingRight));
            }
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
        if (placeholder == null) return;
        float dw    = DISPLAY_W * displayScale;
        float dh    = DISPLAY_H * displayScale;
        float drawX = position.x + (HITBOX_W - dw) / 2f;
        if (!facingRight) {
            batch.draw(placeholder, drawX + dw, position.y, -dw, dh);
        } else {
            batch.draw(placeholder, drawX, position.y, dw, dh);
        }
    }
}
