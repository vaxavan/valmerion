package com.valmerion.entities;

/** Playable class that determines animations and base stats. */
public enum PlayerClass {

    ARCHER  ("archer",  "Лучник",  220f, 180f, 55f),
    MAGE    ("mage",    "Маг",     180f, 200f, 70f),
    WARRIOR ("warrior", "Воин",    200f, 250f, 45f);

    /** Atlas region prefix, e.g. "archer" → "archer_idle_001" */
    public final String prefix;
    public final String displayName;
    /** Base move speed (pixels/sec) */
    public final float  moveSpeed;
    /** Base max HP */
    public final float  maxHp;
    /** Attack range (pixels) */
    public final float  attackRange;

    PlayerClass(String prefix, String displayName, float moveSpeed, float maxHp, float attackRange) {
        this.prefix      = prefix;
        this.displayName = displayName;
        this.moveSpeed   = moveSpeed;
        this.maxHp       = maxHp;
        this.attackRange = attackRange;
    }

    public PlayerClass next() {
        PlayerClass[] vals = values();
        return vals[(ordinal() + 1) % vals.length];
    }
}
