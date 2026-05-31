package com.valmerion.game;

public class GameState {
    public static final GameState INSTANCE = new GameState();

    public float reputation    = 0f;    // -100 to +100
    public float hiddenCounter = 50f;   // 0=смерть, 100=истинная концовка
    public float hunger        = 75f;   // 0-100
    public int   chapter       = 1;
    public int   storyStage    = 0;     // этап внутри главы
    public float markXp        = 0f;    // XP метки Эона
    public String playerRank   = "E";  // E,D,C,B,A,S

    public String selectedClass = "archer"; // archer | mage | warrior

    private GameState() {}

    public void addReputation(float amount) {
        reputation = Math.max(-100, Math.min(100, reputation + amount));
    }

    public void addHiddenCounter(float amount) {
        hiddenCounter = Math.max(0, Math.min(100, hiddenCounter + amount));
    }
}
