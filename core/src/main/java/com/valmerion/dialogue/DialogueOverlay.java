package com.valmerion.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.valmerion.game.GameState;
import com.valmerion.utils.Constants;

public class DialogueOverlay {

    private static final float BOX_H = 200f;
    private static final float BOX_Y = 10f;
    private static final float BOX_X = 40f;
    private static final float BOX_W = Constants.WORLD_WIDTH - 80f;
    private static final float PAD   = 18f;

    private final BitmapFont font;
    private final Texture    bgTex;
    private final GlyphLayout layout = new GlyphLayout();

    private DialogueLine[] lines;
    private int     lineIndex   = 0;
    private int     choiceIndex = -1;
    private boolean active      = false;
    private boolean finished    = false;

    public DialogueOverlay(BitmapFont font) {
        this.font = font;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0.82f);
        pm.fill();
        bgTex = new Texture(pm);
        pm.dispose();
    }

    public void show(DialogueLine[] lines) {
        this.lines       = lines;
        this.lineIndex   = 0;
        this.choiceIndex = -1;
        this.active      = true;
        this.finished    = false;
    }

    public boolean isActive()          { return active; }
    public boolean isFinished()        { return finished; }
    public int     getLastChoiceIndex(){ return choiceIndex; }

    public void update(float delta) {
        if (!active || finished) return;
        DialogueLine line = lines[lineIndex];

        if (line.choiceTexts == null) {
            if (Gdx.input.isKeyJustPressed(Keys.SPACE)
             || Gdx.input.isKeyJustPressed(Keys.ENTER)
             || Gdx.input.justTouched()) {
                advance();
            }
        } else {
            for (int i = 0; i < line.choiceTexts.length; i++) {
                if (Gdx.input.isKeyJustPressed(Keys.NUM_1 + i)
                 || Gdx.input.isKeyJustPressed(Keys.NUMPAD_1 + i)) {
                    if (line.reputationChange != null)
                        GameState.INSTANCE.addReputation(line.reputationChange[i]);
                    if (line.counterChange != null)
                        GameState.INSTANCE.addHiddenCounter(line.counterChange[i]);
                    choiceIndex = i;
                    advance();
                    break;
                }
            }
        }
    }

    private void advance() {
        lineIndex++;
        if (lineIndex >= lines.length) {
            active   = false;
            finished = true;
        }
    }

    public void render(SpriteBatch batch) {
        if (!active) return;
        DialogueLine line = lines[lineIndex];

        batch.setColor(1, 1, 1, 1);
        batch.draw(bgTex, BOX_X, BOX_Y, BOX_W, BOX_H);

        if (font == null) return;

        float textY = BOX_Y + BOX_H - PAD;
        font.setColor(Color.YELLOW);
        font.draw(batch, line.speaker + ":", BOX_X + PAD, textY);
        textY -= 34f;

        font.setColor(Color.WHITE);
        font.draw(batch, line.text, BOX_X + PAD, textY, BOX_W - PAD * 2,
                  com.badlogic.gdx.utils.Align.left, true);

        if (line.choiceTexts != null) {
            font.setColor(new Color(0.8f, 1f, 0.8f, 1f));
            for (int i = 0; i < line.choiceTexts.length; i++) {
                font.draw(batch, (i + 1) + ". " + line.choiceTexts[i],
                          BOX_X + PAD + i * 380f, BOX_Y + PAD + 24f);
            }
        } else {
            font.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
            font.draw(batch, "[ Пробел / Enter — продолжить ]", BOX_X + PAD, BOX_Y + PAD + 24f);
        }
        font.setColor(Color.WHITE);
    }

    public void dispose() {
        bgTex.dispose();
    }
}
