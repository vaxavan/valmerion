package com.valmerion.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.game.GameState;
import com.valmerion.utils.Constants;

public class DialogueOverlay {

    private static final float BOX_H  = 220f;
    private static final float BOX_Y  = 10f;
    private static final float BOX_X  = 40f;
    private static final float BOX_W  = Constants.WORLD_WIDTH - 80f;
    private static final float PAD    = 18f;

    // Choice button layout
    private static final float BTN_H  = 52f;
    private static final float BTN_GAP = 10f;

    private final BitmapFont font;
    private final Texture    bgTex;
    private final Texture    px;
    private final GlyphLayout layout = new GlyphLayout();

    private DialogueLine[] lines;
    private int     lineIndex   = 0;
    private int     choiceIndex = -1;
    private boolean active      = false;
    private boolean finished    = false;

    // Continue button (no choices)
    private final Rectangle continueBtn;

    // Choice buttons (up to 3)
    private final Rectangle[] choiceBtns = new Rectangle[3];

    public DialogueOverlay(BitmapFont font) {
        this.font = font;

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0.88f);
        pm.fill();
        bgTex = new Texture(pm);
        pm.dispose();

        Pixmap pm2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm2.setColor(1f, 1f, 1f, 1f);
        pm2.fill();
        px = new Texture(pm2);
        pm2.dispose();

        // "▶" continue button — bottom-right of dialogue box
        continueBtn = new Rectangle(BOX_X + BOX_W - 90f, BOX_Y + 10f, 80f, 44f);

        for (int i = 0; i < choiceBtns.length; i++) {
            choiceBtns[i] = new Rectangle();
        }
    }

    public void show(DialogueLine[] lines) {
        this.lines       = lines;
        this.lineIndex   = 0;
        this.choiceIndex = -1;
        this.active      = true;
        this.finished    = false;
    }

    public boolean isActive()           { return active; }
    public boolean isFinished()         { return finished; }
    public int     getLastChoiceIndex() { return choiceIndex; }

    public void update(float delta) {
        if (!active || finished) return;
        DialogueLine line = lines[lineIndex];

        if (!Gdx.input.justTouched()) return;

        float wx = Gdx.input.getX() * Constants.WORLD_WIDTH  / Gdx.graphics.getWidth();
        float wy = (Gdx.graphics.getHeight() - Gdx.input.getY())
                 * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();

        if (line.choiceTexts == null) {
            // tap anywhere on the box or the ▶ button
            if (new Rectangle(BOX_X, BOX_Y, BOX_W, BOX_H).contains(wx, wy)) {
                advance();
            }
        } else {
            for (int i = 0; i < line.choiceTexts.length; i++) {
                if (choiceBtns[i].contains(wx, wy)) {
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

    public void render(SpriteBatch batch) {
        if (!active) return;
        DialogueLine line = lines[lineIndex];

        // Box background
        batch.setColor(1, 1, 1, 1);
        batch.draw(bgTex, BOX_X, BOX_Y, BOX_W, BOX_H);

        // Gold top border
        batch.setColor(1f, 0.84f, 0.2f, 1f);
        batch.draw(px, BOX_X, BOX_Y + BOX_H - 3f, BOX_W, 3f);
        batch.setColor(Color.WHITE);

        if (font == null) return;

        // Speaker name (gold)
        float textY = BOX_Y + BOX_H - PAD - 4f;
        font.setColor(1f, 0.84f, 0.2f, 1f);
        font.draw(batch, line.speaker, BOX_X + PAD, textY);
        textY -= 36f;

        // Dialogue text
        font.setColor(Color.WHITE);
        font.draw(batch, line.text, BOX_X + PAD, textY,
                  BOX_W - PAD * 2, com.badlogic.gdx.utils.Align.left, true);

        if (line.choiceTexts != null) {
            renderChoices(batch, line);
        } else {
            renderContinue(batch);
        }

        font.setColor(Color.WHITE);
    }

    private void renderChoices(SpriteBatch batch, DialogueLine line) {
        int count = line.choiceTexts.length;
        float totalW = BOX_W - PAD * 2;
        float btnW   = (totalW - BTN_GAP * (count - 1)) / count;
        float btnY   = BOX_Y + 12f;

        for (int i = 0; i < count; i++) {
            float btnX = BOX_X + PAD + i * (btnW + BTN_GAP);
            choiceBtns[i].set(btnX, btnY, btnW, BTN_H);

            // Button background
            batch.setColor(0.08f, 0.06f, 0.15f, 0.95f);
            batch.draw(px, btnX, btnY, btnW, BTN_H);
            // Gold border
            batch.setColor(1f, 0.84f, 0.2f, 1f);
            batch.draw(px, btnX,        btnY,          btnW, 2);
            batch.draw(px, btnX,        btnY+BTN_H-2,  btnW, 2);
            batch.draw(px, btnX,        btnY,          2,    BTN_H);
            batch.draw(px, btnX+btnW-2, btnY,          2,    BTN_H);

            // Text centered on button
            layout.setText(font, line.choiceTexts[i]);
            float tx = btnX + (btnW - layout.width)  / 2f;
            float ty = btnY + (BTN_H + layout.height) / 2f;
            font.setColor(0, 0, 0, 0.7f);
            font.draw(batch, line.choiceTexts[i], tx + 1f, ty - 1f);
            font.setColor(1f, 0.95f, 0.8f, 1f);
            font.draw(batch, line.choiceTexts[i], tx, ty);
            batch.setColor(Color.WHITE);
        }
    }

    private void renderContinue(SpriteBatch batch) {
        // Small "▶" button bottom-right
        float bx = BOX_X + BOX_W - 70f;
        float by = BOX_Y + 12f;
        float bw = 60f, bh = 40f;
        continueBtn.set(bx, by, bw, bh);

        batch.setColor(0.08f, 0.06f, 0.15f, 0.9f);
        batch.draw(px, bx, by, bw, bh);
        batch.setColor(1f, 0.84f, 0.2f, 1f);
        batch.draw(px, bx,      by,      bw, 2);
        batch.draw(px, bx,      by+bh-2, bw, 2);
        batch.draw(px, bx,      by,      2,  bh);
        batch.draw(px, bx+bw-2, by,      2,  bh);

        layout.setText(font, "▶");
        font.setColor(1f, 0.84f, 0.2f, 1f);
        font.draw(batch, "▶", bx + (bw - layout.width) / 2f,
                  by + (bh + layout.height) / 2f);

        // Hint text
        font.setColor(0.5f, 0.5f, 0.5f, 1f);
        font.draw(batch, "нажми чтобы продолжить", BOX_X + PAD, BOX_Y + 36f);

        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    private void advance() {
        lineIndex++;
        if (lineIndex >= lines.length) {
            active   = false;
            finished = true;
        }
    }

    public void dispose() {
        bgTex.dispose();
        px.dispose();
    }
}
