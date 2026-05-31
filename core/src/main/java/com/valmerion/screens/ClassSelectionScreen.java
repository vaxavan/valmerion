package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.entities.PlayerClass;
import com.valmerion.game.GameState;
import com.valmerion.ui.HealthBar;
import com.valmerion.utils.Constants;

public class ClassSelectionScreen extends BaseScreen {

    private static final float CARD_W   = 300f;
    private static final float CARD_H   = 400f;
    private static final float CARD_GAP = 40f;
    private static final float CARDS_Y  = 140f;

    // Central "Выбрать" button
    private static final float BTN_W = 340f;
    private static final float BTN_H = 70f;
    private static final float BTN_X = (Constants.WORLD_WIDTH - BTN_W) / 2f;
    private static final float BTN_Y = 32f;

    private final BitmapFont  font;
    private final BitmapFont  titleFont;
    private final GlyphLayout layout = new GlyphLayout();
    private final TextureAtlas atlas;

    private final Rectangle[] cards   = new Rectangle[3];
    private final Rectangle    btnRect = new Rectangle(BTN_X, BTN_Y, BTN_W, BTN_H);

    private static final PlayerClass[] CLASSES  = { PlayerClass.ARCHER, PlayerClass.MAGE, PlayerClass.WARRIOR };
    private static final boolean[]     UNLOCKED = { true, false, false };

    private int     selected     = -1;  // -1 = nothing tapped yet
    private boolean confirming   = false;
    private float   confirmTimer = 0f;

    public ClassSelectionScreen(ValmerionGame game) {
        super(game);
        font      = assets.font(AssetLoader.FONT_MAIN);
        titleFont = assets.font(AssetLoader.FONT_TITLE);
        atlas     = assets.atlas(AssetLoader.ATLAS_PLAYER);

        float totalW = CARD_W * 3 + CARD_GAP * 2;
        float startX = (Constants.WORLD_WIDTH - totalW) / 2f;
        for (int i = 0; i < 3; i++)
            cards[i] = new Rectangle(startX + i * (CARD_W + CARD_GAP), CARDS_Y, CARD_W, CARD_H);
    }

    @Override public void show() { confirming = false; confirmTimer = 0f; selected = -1; }

    @Override
    public void render(float delta) {
        if (confirming) {
            confirmTimer += delta;
            if (confirmTimer >= 0.55f) {
                GameState.INSTANCE.selectedClass = CLASSES[selected].prefix;
                GameState.INSTANCE.gameStarted   = true;
                game.setScreen(new WorldScreen(game));
                return;
            }
        }

        if (Gdx.input.justTouched()) handleTouch();

        Gdx.gl.glClearColor(0.04f, 0.03f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        com.badlogic.gdx.graphics.Texture px = HealthBar.getWhitePixel();

        // Title
        drawCentred(titleFont != null ? titleFont : font,
            "ВЫБЕРИ КЛАСС", Constants.WORLD_HEIGHT - 55f, 1f, 0.84f, 0.20f, 1f);
        drawCentred(font,
            "Другие классы появятся в следующих главах",
            Constants.WORLD_HEIGHT - 100f, 0.55f, 0.50f, 0.40f, 1f);

        // Cards
        for (int i = 0; i < 3; i++) drawCard(i, px);

        // Central "Выбрать" button
        drawConfirmButton(px);

        batch.end();
    }

    // ── Card ─────────────────────────────────────────────────────────────────

    private void drawCard(int i, com.badlogic.gdx.graphics.Texture px) {
        Rectangle r   = cards[i];
        boolean locked = !UNLOCKED[i];
        boolean sel    = (i == selected);
        boolean conf   = confirming && sel;

        // Background
        if      (conf)   batch.setColor(0.06f, 0.28f, 0.08f, 1f);
        else if (sel)    batch.setColor(0.10f, 0.08f, 0.26f, 1f);
        else if (locked) batch.setColor(0.07f, 0.06f, 0.11f, 0.75f);
        else             batch.setColor(0.09f, 0.07f, 0.18f, 0.95f);
        if (px != null) batch.draw(px, r.x, r.y, r.width, r.height);

        // Border
        float br, bg2, bb;
        if      (conf)   { br = 0.4f;  bg2 = 1f;    bb = 0.45f; }
        else if (sel)    { br = 1f;    bg2 = 0.84f;  bb = 0.20f; }
        else if (locked) { br = 0.25f; bg2 = 0.22f;  bb = 0.30f; }
        else             { br = 0.42f; bg2 = 0.35f;  bb = 0.58f; }
        batch.setColor(br, bg2, bb, 1f);
        if (px != null) {
            float t = sel ? 3.5f : 2f;
            batch.draw(px, r.x,           r.y,            r.width,  t);
            batch.draw(px, r.x,           r.y+r.height-t, r.width,  t);
            batch.draw(px, r.x,           r.y,            t,        r.height);
            batch.draw(px, r.x+r.width-t, r.y,            t,        r.height);
        }

        // Gold top accent line when selected
        if (sel && !conf && px != null) {
            batch.setColor(1f, 0.84f, 0.20f, 0.35f);
            batch.draw(px, r.x + 4, r.y + r.height - 4, r.width - 8, 4f);
        }

        // Sprite
        if (atlas != null) {
            TextureRegion frame = atlas.findRegion(CLASSES[i].prefix + "_idle_001");
            if (frame != null) {
                float sw = 140f, sh = 140f;
                if (locked) batch.setColor(0.3f, 0.3f, 0.3f, 0.55f);
                else if (conf) batch.setColor(0.7f, 1f, 0.75f, 1f);
                else       batch.setColor(Color.WHITE);
                batch.draw(frame, r.x + (r.width - sw) / 2f, r.y + r.height - sh - 22f, sw, sh);
                batch.setColor(Color.WHITE);
            }
        }

        if (font == null) return;

        // Class name
        float nameY = r.y + r.height - 178f;
        if (locked) drawCentredIn(CLASSES[i].displayName, r, nameY, 0.38f, 0.35f, 0.42f, 1f);
        else if (conf) drawCentredIn(CLASSES[i].displayName, r, nameY, 0.6f, 1f, 0.65f, 1f);
        else           drawCentredIn(CLASSES[i].displayName, r, nameY, 1f, 0.88f, 0.35f, 1f);

        // Stats (only unlocked)
        if (!locked) {
            float sy = nameY - 40f;
            drawCentredIn("HP: "        + (int) CLASSES[i].maxHp,    r, sy,       0.65f, 1f,    0.65f, 1f);
            drawCentredIn("Скорость: "  + (int) CLASSES[i].moveSpeed, r, sy - 34f, 0.65f, 0.82f, 1f,   1f);
            String wpn = CLASSES[i] == PlayerClass.ARCHER ? "Лук"
                       : CLASSES[i] == PlayerClass.MAGE   ? "Посох" : "Меч";
            drawCentredIn("Оружие: " + wpn, r, sy - 68f, 0.82f, 0.73f, 0.52f, 1f);
        }

        // "— скоро —" on locked cards
        if (locked) {
            drawCentredIn("— скоро —", r, r.y + 28f, 0.35f, 0.32f, 0.40f, 1f);
        }
    }

    // ── Central confirm button ────────────────────────────────────────────────

    private void drawConfirmButton(com.badlogic.gdx.graphics.Texture px) {
        if (px == null || font == null) return;

        boolean active = selected >= 0 && !confirming;
        boolean conf   = confirming;

        if (conf) {
            // Green flash
            batch.setColor(0.04f, 0.40f, 0.08f, 1f);
            batch.draw(px, BTN_X - 4, BTN_Y - 4, BTN_W + 8, BTN_H + 8);
            batch.setColor(0.10f, 0.60f, 0.15f, 1f);
            batch.draw(px, BTN_X, BTN_Y, BTN_W, BTN_H);
            batch.setColor(0.55f, 1f, 0.60f, 1f);
            batch.draw(px, BTN_X,        BTN_Y,          BTN_W, 3f);
            batch.draw(px, BTN_X,        BTN_Y+BTN_H-3f, BTN_W, 3f);
            batch.draw(px, BTN_X,        BTN_Y,          3f,    BTN_H);
            batch.draw(px, BTN_X+BTN_W-3f, BTN_Y,        3f,    BTN_H);
            layout.setText(font, "✓ Выбрано!");
            float tx = BTN_X + (BTN_W - layout.width) / 2f;
            float ty = BTN_Y + (BTN_H + layout.height) / 2f;
            font.setColor(0f, 0f, 0f, 0.55f);
            font.draw(batch, "✓ Выбрано!", tx + 1.5f, ty - 1.5f);
            font.setColor(0.85f, 1f, 0.85f, 1f);
            font.draw(batch, "✓ Выбрано!", tx, ty);

        } else if (active) {
            // Gold glow button
            batch.setColor(0.55f, 0.35f, 0f, 0.45f);
            batch.draw(px, BTN_X - 5, BTN_Y - 5, BTN_W + 10, BTN_H + 10);
            batch.setColor(0.20f, 0.13f, 0.02f, 1f);
            batch.draw(px, BTN_X, BTN_Y, BTN_W, BTN_H);
            // Highlight stripe
            batch.setColor(1f, 0.90f, 0.40f, 0.18f);
            batch.draw(px, BTN_X + 4, BTN_Y + BTN_H - 12f, BTN_W - 8, 9f);
            // Gold border
            batch.setColor(1f, 0.84f, 0.20f, 1f);
            batch.draw(px, BTN_X,          BTN_Y,          BTN_W, 3f);
            batch.draw(px, BTN_X,          BTN_Y+BTN_H-3f, BTN_W, 3f);
            batch.draw(px, BTN_X,          BTN_Y,          3f,    BTN_H);
            batch.draw(px, BTN_X+BTN_W-3f, BTN_Y,          3f,    BTN_H);
            layout.setText(font, "» Выбрать «");
            float tx = BTN_X + (BTN_W - layout.width) / 2f;
            float ty = BTN_Y + (BTN_H + layout.height) / 2f;
            font.setColor(0.30f, 0.15f, 0f, 0.75f);
            font.draw(batch, "» Выбрать «", tx + 1.5f, ty - 1.5f);
            font.setColor(1f, 0.92f, 0.28f, 1f);
            font.draw(batch, "» Выбрать «", tx, ty);

        } else {
            // Dimmed — no card chosen yet
            batch.setColor(0.10f, 0.08f, 0.16f, 0.6f);
            batch.draw(px, BTN_X, BTN_Y, BTN_W, BTN_H);
            batch.setColor(0.30f, 0.27f, 0.38f, 0.8f);
            batch.draw(px, BTN_X,          BTN_Y,          BTN_W, 2f);
            batch.draw(px, BTN_X,          BTN_Y+BTN_H-2f, BTN_W, 2f);
            batch.draw(px, BTN_X,          BTN_Y,          2f,    BTN_H);
            batch.draw(px, BTN_X+BTN_W-2f, BTN_Y,          2f,    BTN_H);
            layout.setText(font, "Выбрать");
            font.setColor(0.38f, 0.34f, 0.48f, 1f);
            font.draw(batch, "Выбрать",
                BTN_X + (BTN_W - layout.width) / 2f,
                BTN_Y + (BTN_H + layout.height) / 2f);
        }

        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    private void handleTouch() {
        if (confirming) return;
        float wx = Gdx.input.getX() * Constants.WORLD_WIDTH  / Gdx.graphics.getWidth();
        float wy = (Gdx.graphics.getHeight() - Gdx.input.getY())
                 * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();

        // Tap confirm button if a class is selected
        if (selected >= 0 && btnRect.contains(wx, wy)) {
            confirming   = true;
            confirmTimer = 0f;
            return;
        }

        // Tap a card to select it
        for (int i = 0; i < 3; i++) {
            if (cards[i].contains(wx, wy)) {
                if (UNLOCKED[i]) selected = i;
                return;
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void drawCentred(BitmapFont f, String text, float y,
                              float r, float g, float b, float a) {
        if (f == null) return;
        layout.setText(f, text);
        f.setColor(r, g, b, a);
        f.draw(batch, text, (Constants.WORLD_WIDTH - layout.width) / 2f, y);
        f.setColor(Color.WHITE);
    }

    private void drawCentredIn(String text, Rectangle card, float y,
                                float r, float g, float b, float a) {
        if (font == null) return;
        layout.setText(font, text);
        font.setColor(r, g, b, a);
        font.draw(batch, text, card.x + (card.width - layout.width) / 2f, y);
        font.setColor(Color.WHITE);
    }

    @Override public void dispose() {}
}
