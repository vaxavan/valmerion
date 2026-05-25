package com.valmerion.utils;

/**
 * Global game constants — resolution, physics scale, layer z-orders, timing.
 */
public final class Constants {

    private Constants() {}

    // ── Viewport ──────────────────────────────────────────────────────────────
    public static final float WORLD_WIDTH  = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // ── Physics (pixels per metre) ────────────────────────────────────────────
    public static final float PPM = 64f;

    // ── Player ────────────────────────────────────────────────────────────────
    public static final float PLAYER_MOVE_SPEED  = 180f;
    public static final float PLAYER_JUMP_IMPULSE = 420f;
    public static final float PLAYER_MAX_HP      = 100f;
    public static final float PLAYER_ATTACK_DAMAGE = 25f;
    public static final float PLAYER_ATTACK_RANGE  = 70f;

    // ── Goblin ────────────────────────────────────────────────────────────────
    public static final float GOBLIN_MOVE_SPEED   = 60f;
    public static final float GOBLIN_MAX_HP        = 50f;
    public static final float GOBLIN_ATTACK_DAMAGE = 8f;
    public static final float GOBLIN_ATTACK_RANGE  = 55f;
    public static final float GOBLIN_ATTACK_COOLDOWN = 1.8f;
    public static final float GOBLIN_AGGRO_RANGE   = 320f;

    // ── Cutscene ──────────────────────────────────────────────────────────────
    public static final float CUTSCENE_SLIDE_DURATION = 4f;   // seconds per slide

    // ── Tutorial ──────────────────────────────────────────────────────────────
    public static final float HINT_FADE_DURATION = 0.5f;
    public static final float HINT_DISPLAY_TIME  = 3f;

    // ── Z-layers (for manual draw-order) ─────────────────────────────────────
    public static final int LAYER_BG        = 0;
    public static final int LAYER_PLATFORM  = 1;
    public static final int LAYER_ENTITY    = 2;
    public static final int LAYER_FX        = 3;
    public static final int LAYER_UI        = 4;
}
