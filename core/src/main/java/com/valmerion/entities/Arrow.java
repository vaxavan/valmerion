package com.valmerion.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.utils.Constants;

public class Arrow {

    private static final float SPEED  = 700f;
    private static final float W      = 24f;
    private static final float H      =  6f;

    private static Texture tex;

    private float x, y;
    private final float velX;
    private boolean active = true;
    private final Rectangle hitbox = new Rectangle();

    public Arrow(float startX, float startY, boolean facingRight) {
        this.x    = startX;
        this.y    = startY + 30f;  // chest height
        this.velX = facingRight ? SPEED : -SPEED;
        hitbox.set(x, y, W, H);

        if (tex == null) {
            Pixmap pm = new Pixmap((int) W, (int) H, Pixmap.Format.RGBA8888);
            pm.setColor(Color.YELLOW);
            pm.fill();
            pm.setColor(Color.ORANGE);
            pm.drawLine(0, (int)(H/2), (int)W - 1, (int)(H/2));
            tex = new Texture(pm);
            pm.dispose();
        }
    }

    public void update(float delta) {
        x += velX * delta;
        hitbox.set(x, y, W, H);
        if (x < 0 || x > Constants.WORLD_WIDTH) active = false;
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        if (velX < 0) {
            batch.draw(tex, x + W, y, -W, H);
        } else {
            batch.draw(tex, x, y, W, H);
        }
    }

    public boolean isActive()       { return active; }
    public void    deactivate()     { active = false; }
    public Rectangle getHitbox()   { return hitbox; }

    public static void disposeTexture() {
        if (tex != null) { tex.dispose(); tex = null; }
    }
}
