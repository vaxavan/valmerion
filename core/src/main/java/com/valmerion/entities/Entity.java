package com.valmerion.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Base class for every game entity (player, enemy, NPC).
 * Holds position, velocity, AABB hitbox and HP.
 */
public abstract class Entity {

    // Shared 1×1 white texture for placeholder / tinted rendering.
    // Set once from a screen (AcademyScreen) after OpenGL is available.
    private static Texture sharedWhitePixel;

    public  static void    setSharedWhitePixel(Texture t) { sharedWhitePixel = t; }
    protected static Texture getSharedWhitePixel()        { return sharedWhitePixel; }

    protected final Vector2   position  = new Vector2();
    protected final Vector2   velocity  = new Vector2();
    protected final Rectangle hitbox    = new Rectangle();

    protected float maxHp;
    protected float hp;
    protected boolean alive = true;
    protected boolean facingRight = true;

    protected Entity(float x, float y, float w, float h, float maxHp) {
        position.set(x, y);
        hitbox.set(x, y, w, h);
        this.maxHp = maxHp;
        this.hp    = maxHp;
    }

    /** Update logic (movement, AI, animation timers). */
    public abstract void update(float delta);

    /** Draw using the given batch (which must already be begun). */
    public abstract void render(SpriteBatch batch);

    public void dispose() {}

    // ── HP helpers ────────────────────────────────────────────────────────────

    public void takeDamage(float amount) {
        hp = Math.max(0, hp - amount);
        if (hp <= 0) alive = false;
    }

    public float getHp()    { return hp; }
    public float getMaxHp() { return maxHp; }
    public boolean isAlive() { return alive; }

    // ── Position / hitbox ─────────────────────────────────────────────────────

    protected void syncHitbox() {
        hitbox.x = position.x;
        hitbox.y = position.y;
    }

    public Rectangle getHitbox()  { return hitbox; }
    public Vector2   getPosition() { return position; }
    public Vector2   getVelocity() { return velocity; }
    public boolean   isFacingRight() { return facingRight; }
}
