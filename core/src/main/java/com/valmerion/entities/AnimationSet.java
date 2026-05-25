package com.valmerion.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Holds all animation states for a single character.
 *
 * <p>Atlas region naming convention: &lt;prefix&gt;_idle_NNN, &lt;prefix&gt;_walk_NNN, etc.
 * If the atlas is unavailable every getter returns {@code null} gracefully.
 */
public class AnimationSet {

    public enum State { IDLE, WALK, JUMP, FALL, ATTACK, HIT, DEAD }

    private Animation<TextureRegion> idle;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Animation<TextureRegion> fall;
    private Animation<TextureRegion> attack;
    private Animation<TextureRegion> hit;
    private Animation<TextureRegion> dead;

    private float stateTime = 0f;
    private State state     = State.IDLE;

    public AnimationSet(TextureAtlas atlas, String prefix) {
        if (atlas == null) return;
        idle   = buildAnim(atlas, prefix + "_idle",   0.12f, Animation.PlayMode.LOOP);
        walk   = buildAnim(atlas, prefix + "_walk",   0.10f, Animation.PlayMode.LOOP);
        jump   = buildAnim(atlas, prefix + "_jump",   0.10f, Animation.PlayMode.NORMAL);
        fall   = buildAnim(atlas, prefix + "_fall",   0.10f, Animation.PlayMode.LOOP);
        attack = buildAnim(atlas, prefix + "_attack", 0.07f, Animation.PlayMode.NORMAL);
        hit    = buildAnim(atlas, prefix + "_hit",    0.08f, Animation.PlayMode.NORMAL);
        dead   = buildAnim(atlas, prefix + "_dead",   0.10f, Animation.PlayMode.NORMAL);
    }

    public void setState(State newState) {
        if (this.state != newState) {
            this.state    = newState;
            this.stateTime = 0f;
        }
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public TextureRegion getCurrentFrame() {
        Animation<TextureRegion> anim = getAnim(state);
        if (anim == null) return null;
        return anim.getKeyFrame(stateTime);
    }

    public boolean isCurrentAnimFinished() {
        Animation<TextureRegion> anim = getAnim(state);
        return anim != null && anim.isAnimationFinished(stateTime);
    }

    public State getState() { return state; }

    // ── Private ───────────────────────────────────────────────────────────────

    private Animation<TextureRegion> buildAnim(TextureAtlas atlas, String regionName,
                                               float frameDuration,
                                               Animation.PlayMode mode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.isEmpty()) return null;
        Animation<TextureRegion> anim = new Animation<>(frameDuration, regions, mode);
        return anim;
    }

    private Animation<TextureRegion> getAnim(State s) {
        return switch (s) {
            case IDLE   -> idle;
            case WALK   -> walk;
            case JUMP   -> jump;
            case FALL   -> fall;
            case ATTACK -> attack;
            case HIT    -> hit;
            case DEAD   -> dead;
        };
    }
}
