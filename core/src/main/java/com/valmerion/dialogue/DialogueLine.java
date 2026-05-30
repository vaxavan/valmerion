package com.valmerion.dialogue;

public class DialogueLine {
    public final String   speaker;
    public final String   text;
    public final String[] choiceTexts;
    public final float[]  reputationChange;
    public final float[]  counterChange;

    public DialogueLine(String speaker, String text) {
        this(speaker, text, null, null, null);
    }

    public DialogueLine(String speaker, String text, String[] choiceTexts,
                        float[] reputationChange, float[] counterChange) {
        this.speaker          = speaker;
        this.text             = text;
        this.choiceTexts      = choiceTexts;
        this.reputationChange = reputationChange;
        this.counterChange    = counterChange;
    }
}
