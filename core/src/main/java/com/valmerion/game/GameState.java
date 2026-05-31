package com.valmerion.game;

import com.badlogic.gdx.Gdx;

public class GameState {
    public static final GameState INSTANCE = new GameState();

    public float   reputation    = 0f;
    public float   hiddenCounter = 50f;
    public float   hunger        = 75f;
    public int     chapter       = 1;
    public int     storyStage    = 0;
    public float   markXp        = 0f;
    public String  playerRank    = "E";
    public String  selectedClass = "archer";
    public boolean gameStarted   = false;

    private static final String PREFS = "valmerion_save";

    private GameState() {}

    public void save() {
        var p = Gdx.app.getPreferences(PREFS);
        p.putFloat  ("reputation",    reputation);
        p.putFloat  ("hiddenCounter", hiddenCounter);
        p.putFloat  ("hunger",        hunger);
        p.putInteger("chapter",       chapter);
        p.putInteger("storyStage",    storyStage);
        p.putFloat  ("markXp",        markXp);
        p.putString ("playerRank",    playerRank);
        p.putString ("selectedClass", selectedClass);
        p.putBoolean("gameStarted",   gameStarted);
        p.flush();
    }

    public void load() {
        var p = Gdx.app.getPreferences(PREFS);
        reputation    = p.getFloat  ("reputation",    0f);
        hiddenCounter = p.getFloat  ("hiddenCounter", 50f);
        hunger        = p.getFloat  ("hunger",        75f);
        chapter       = p.getInteger("chapter",       1);
        storyStage    = p.getInteger("storyStage",    0);
        markXp        = p.getFloat  ("markXp",        0f);
        playerRank    = p.getString ("playerRank",    "E");
        selectedClass = p.getString ("selectedClass", "archer");
        gameStarted   = p.getBoolean("gameStarted",   false);
    }

    public void resetAndSave() {
        reputation    = 0f;
        hiddenCounter = 50f;
        hunger        = 75f;
        chapter       = 1;
        storyStage    = 0;
        markXp        = 0f;
        playerRank    = "E";
        selectedClass = "archer";
        gameStarted   = false;
        save();
    }

    public void addReputation(float amount) {
        reputation = Math.max(-100, Math.min(100, reputation + amount));
    }

    public void addHiddenCounter(float amount) {
        hiddenCounter = Math.max(0, Math.min(100, hiddenCounter + amount));
    }
}
