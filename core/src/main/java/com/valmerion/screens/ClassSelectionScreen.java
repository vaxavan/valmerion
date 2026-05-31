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

/**
 * Class selection screen.
 * Three cards: Archer (unlocked), Mage and Warrior (locked, coming soon).
 * Tap the archer card to continue.
 */
public class ClassSelectionScreen extends BaseScreen {

    private static final float CARD_W  = 300f;
    private static final float CARD_H  = 380f;
    private static final float CARD_GAP = 40f;
    private static final float CARDS_Y = (Constants.WORLD_HEIGHT - CARD_H) / 2f - 20f;

    private final BitmapFont  font;
    private final BitmapFont  titleFont;
    private final GlyphLayout layout = new GlyphLayout();

    private final TextureAtlas atlas;

    // Card positions
    private final Rectangle[] cards = new Rectangle[3];
    private static final PlayerClass[] CLASSES = {
        PlayerClass.ARCHER, PlayerClass.MAGE, PlayerClass.WARRIOR
    };
    private static final boolean[] UNLOCKED = { true, false, false };

    private int   selected = 0;   // index into CLASSES
    private float confirmTimer = 0f;
    private boolean confirming = false;

    public ClassSelectionScreen(ValmerionGame game) {
        super(game);
        font      = assets.font(AssetLoader.FONT_MAIN);
        titleFont = assets.font(AssetLoader.FONT_TITLE);
        atlas     = assets.atlas(AssetLoader.ATLAS_PLAYER);

        float totalW = CARD_W * 3 + CARD_GAP * 2;
        float startX = (Constants.WORLD_WIDTH - totalW) / 2f;
        for (int i = 0; i < 3; i++) {
            cards[i] = new Rectangle(startX + i * (CARD_W + CARD_GAP), CARDS_Y, CARD_W, CARD_H);
        }
    }

    @Override public void show() { confirming = false; confirmTimer = 0f; }

    @Override
    public void render(float delta) {
        if (confirming) {
            confirmTimer += delta;
            if (confirmTimer >= 0.6f) {
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
            "ВЫБЕРИ КЛАСС", Constants.WORLD_HEIGHT - 60f, 1f, 0.84f, 0.20f, 1f);

        // Subtitle
        drawCentred(font, "Другие классы появятся в следующих главах",
            Constants.WORLD_HEIGHT - 105f, 0.6f, 0.55f, 0.45f, 1f);

        for (int i = 0; i < 3; i++) {
            drawCard(i, px);
        }

        // Bottom tip
        drawCentred(font, "Нажми на карточку класса чтобы выбрать",
            50f, 0.55f, 0.55f, 0.55f, 1f);

        batch.end();
    }

    private void drawCard(int i, com.badlogic.gdx.graphics.Texture px) {
        Rectangle r = cards[i];
        boolean locked    = !UNLOCKED[i];
        boolean isSelected = (i == selected);
        boolean isConfirm  = confirming && isSelected;

        // Card background
        if (locked) {
            batch.setColor(0.08f, 0.07f, 0.12f, 0.7f);
        } else if (isConfirm) {
            batch.setColor(0.10f, 0.35f, 0.10f, 1f);
        } else if (isSelected) {
            batch.setColor(0.10f, 0.08f, 0.25f, 1f);
        } else {
            batch.setColor(0.08f, 0.06f, 0.18f, 0.95f);
        }
        if (px != null) batch.draw(px, r.x, r.y, r.width, r.height);

        // Border
        float br = locked ? 0.3f : (isSelected ? 1f : 0.5f);
        float bg2 = locked ? 0.25f : (isSelected ? 0.84f : 0.42f);
        float bb = locked ? 0.3f : (isSelected ? 0.20f : 0.10f);
        batch.setColor(br, bg2, bb, 1f);
        if (px != null) {
            batch.draw(px, r.x,         r.y,         r.width, 3f);
            batch.draw(px, r.x,         r.y+r.height-3f, r.width, 3f);
            batch.draw(px, r.x,         r.y,         3f, r.height);
            batch.draw(px, r.x+r.width-3f, r.y,     3f, r.height);
        }

        // Sprite preview — idle frame 1 from atlas
        if (atlas != null) {
            String regionName = CLASSES[i].prefix + "_idle_001";
            TextureRegion frame = atlas.findRegion(regionName);
            if (frame != null) {
                float sw = 128f, sh = 128f;
                float sx = r.x + (r.width - sw) / 2f;
                float sy = r.y + r.height - sh - 28f;
                if (locked) batch.setColor(0.35f, 0.35f, 0.35f, 0.6f);
                else        batch.setColor(Color.WHITE);
                batch.draw(frame, sx, sy, sw, sh);
                batch.setColor(Color.WHITE);
            }
        }

        if (font == null) return;

        // Class name
        float nameY = r.y + r.height - 172f;
        if (locked) {
            drawCentredIn(CLASSES[i].displayName, r, nameY, 0.4f, 0.4f, 0.4f, 1f);
        } else {
            drawCentredIn(CLASSES[i].displayName, r, nameY, 1f, 0.88f, 0.35f, 1f);
        }

        // Stats
        float statsY = nameY - 42f;
        if (!locked) {
            drawCentredIn("HP: " + (int) CLASSES[i].maxHp, r, statsY,      0.7f, 1f, 0.7f, 1f);
            drawCentredIn("Скорость: " + (int) CLASSES[i].moveSpeed, r, statsY - 36f, 0.7f, 0.85f, 1f, 1f);

            String weapon = CLASSES[i] == PlayerClass.ARCHER ? "Оружие: Лук"
                          : CLASSES[i] == PlayerClass.MAGE   ? "Оружие: Посох"
                          :                                     "Оружие: Меч";
            drawCentredIn(weapon, r, statsY - 72f, 0.85f, 0.75f, 0.55f, 1f);
        }

        // Button at bottom of card
        float bw = r.width - 32f, bh = 52f;
        float bx = r.x + 16f, by = r.y + 16f;
        drawSelectBtn(px, bx, by, bw, bh, locked, isSelected, isConfirm);
    }

    private void drawSelectBtn(com.badlogic.gdx.graphics.Texture px,
                               float bx, float by, float bw, float bh,
                               boolean locked, boolean isSelected, boolean isConfirm) {
        if (px == null || font == null) return;

        if (locked) {
            // Flat grey pill
            batch.setColor(0.18f, 0.16f, 0.22f, 0.8f);
            batch.draw(px, bx, by, bw, bh);
            batch.setColor(0.30f, 0.28f, 0.35f, 1f);
            batch.draw(px, bx,      by,      bw, 2f);
            batch.draw(px, bx,      by+bh-2, bw, 2f);
            batch.draw(px, bx,      by,      2f, bh);
            batch.draw(px, bx+bw-2, by,      2f, bh);
            layout.setText(font, "— скоро —");
            font.setColor(0.38f, 0.35f, 0.42f, 1f);
            font.draw(batch, "— скоро —", bx + (bw - layout.width) / 2f, by + (bh + layout.height) / 2f);
            font.setColor(Color.WHITE);
            return;
        }

        if (isConfirm) {
            // Green glow
            batch.setColor(0.05f, 0.45f, 0.10f, 1f);
            batch.draw(px, bx - 2, by - 2, bw + 4, bh + 4);
            batch.setColor(0.12f, 0.65f, 0.18f, 1f);
            batch.draw(px, bx, by, bw, bh);
            batch.setColor(0.5f, 1f, 0.55f, 1f);
            batch.draw(px, bx,      by,      bw, 2f);
            batch.draw(px, bx,      by+bh-2, bw, 2f);
            batch.draw(px, bx,      by,      2f, bh);
            batch.draw(px, bx+bw-2, by,      2f, bh);
            layout.setText(font, "✓ Выбрано!");
            font.setColor(0f, 0f, 0f, 0.6f);
            font.draw(batch, "✓ Выбрано!", bx + (bw - layout.width) / 2f + 1, by + (bh + layout.height) / 2f - 1);
            font.setColor(0.85f, 1f, 0.85f, 1f);
            font.draw(batch, "✓ Выбрано!", bx + (bw - layout.width) / 2f, by + (bh + layout.height) / 2f);
            font.setColor(Color.WHITE);
            return;
        }

        if (isSelected) {
            // Gold glowing button — outer shadow
            batch.setColor(0.55f, 0.38f, 0f, 0.5f);
            batch.draw(px, bx - 3, by - 3, bw + 6, bh + 6);
            // Dark fill
            batch.setColor(0.22f, 0.15f, 0.03f, 1f);
            batch.draw(px, bx, by, bw, bh);
            // Gold border top highlight
            batch.setColor(1f, 0.84f, 0.20f, 1f);
            batch.draw(px, bx,      by,      bw, 2.5f);
            batch.draw(px, bx,      by+bh-2.5f, bw, 2.5f);
            batch.draw(px, bx,      by,      2.5f, bh);
            batch.draw(px, bx+bw-2.5f, by,  2.5f, bh);
            // Inner lighter strip at top for 3-D feel
            batch.setColor(1f, 0.92f, 0.45f, 0.25f);
            batch.draw(px, bx + 3, by + bh - 10f, bw - 6, 7f);
            // Text with shadow
            String label = "» Выбрать «";
            layout.setText(font, label);
            float tx = bx + (bw - layout.width) / 2f;
            float ty = by + (bh + layout.height) / 2f;
            font.setColor(0.3f, 0.15f, 0f, 0.8f);
            font.draw(batch, label, tx + 1.5f, ty - 1.5f);
            font.setColor(1f, 0.92f, 0.30f, 1f);
            font.draw(batch, label, tx, ty);
        } else {
            // Subtle idle button
            batch.setColor(0.10f, 0.08f, 0.18f, 0.9f);
            batch.draw(px, bx, by, bw, bh);
            batch.setColor(0.45f, 0.38f, 0.60f, 1f);
            batch.draw(px, bx,      by,      bw, 2f);
            batch.draw(px, bx,      by+bh-2, bw, 2f);
            batch.draw(px, bx,      by,      2f, bh);
            batch.draw(px, bx+bw-2, by,      2f, bh);
            String label = "Выбрать";
            layout.setText(font, label);
            font.setColor(0.60f, 0.55f, 0.75f, 1f);
            font.draw(batch, label, bx + (bw - layout.width) / 2f, by + (bh + layout.height) / 2f);
        }
        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    private void handleTouch() {
        if (confirming) return;
        float wx = Gdx.input.getX() * Constants.WORLD_WIDTH  / Gdx.graphics.getWidth();
        float wy = (Gdx.graphics.getHeight() - Gdx.input.getY())
                 * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight();
        for (int i = 0; i < 3; i++) {
            if (cards[i].contains(wx, wy)) {
                if (!UNLOCKED[i]) return;
                if (i == selected) {
                    // second tap confirms
                    confirming = true;
                    confirmTimer = 0f;
                } else {
                    selected = i;
                }
                return;
            }
        }
    }

    private void drawCentred(BitmapFont f, String text, float y,
                              float r, float g, float b, float a) {
        if (f == null || text == null) return;
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
