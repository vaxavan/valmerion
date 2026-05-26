package com.valmerion.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * On-screen virtual controls for touchscreen devices.
 *
 * <p>Layout in world coordinates (1280×720):
 * <pre>
 *   [←] [→]                        [↑]
 *                                 [ATK]
 * </pre>
 */
public class TouchController implements InputController {

    // ── Button geometry (world coords) ────────────────────────────────────────
    private static final float BTN_R   = 58f;   // radius

    private static final float LEFT_X  =  85f;
    private static final float LEFT_Y  =  90f;
    private static final float RIGHT_X = 210f;
    private static final float RIGHT_Y =  90f;

    private static final float JUMP_X  = 1145f;
    private static final float JUMP_Y  = 205f;
    private static final float ATK_X   = 1220f;
    private static final float ATK_Y   =  90f;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean moveLeft, moveRight;
    private boolean jumpHeld,  jumpWas;
    private boolean atkHeld,   atkWas;
    private boolean jumpJustPressed, atkJustPressed;

    // ── Rendering ─────────────────────────────────────────────────────────────
    private final Viewport    viewport;
    private final Texture     circleTex;
    private final BitmapFont  font;
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector3     tmp    = new Vector3();

    public TouchController(Viewport viewport) {
        this.viewport  = viewport;
        this.circleTex = buildCircle(128);
        this.font      = new BitmapFont();
        this.font.getData().setScale(2.2f);
    }

    // ── InputController ───────────────────────────────────────────────────────

    @Override
    public void update() {
        moveLeft  = false;
        moveRight = false;
        boolean newJump = false;
        boolean newAtk  = false;

        for (int p = 0; p < 10; p++) {
            if (!Gdx.input.isTouched(p)) continue;
            tmp.set(Gdx.input.getX(p), Gdx.input.getY(p), 0);
            viewport.unproject(tmp);
            float wx = tmp.x, wy = tmp.y;

            if (inCircle(wx, wy, LEFT_X,  LEFT_Y))  moveLeft  = true;
            if (inCircle(wx, wy, RIGHT_X, RIGHT_Y)) moveRight = true;
            if (inCircle(wx, wy, JUMP_X,  JUMP_Y))  newJump   = true;
            if (inCircle(wx, wy, ATK_X,   ATK_Y))   newAtk    = true;
        }

        jumpJustPressed = newJump && !jumpWas;
        atkJustPressed  = newAtk  && !atkWas;
        jumpWas = jumpHeld = newJump;
        atkWas  = atkHeld  = newAtk;
    }

    @Override public boolean isMoveLeft()          { return moveLeft;          }
    @Override public boolean isMoveRight()         { return moveRight;         }
    @Override public boolean isJumpJustPressed()   { return jumpJustPressed;   }
    @Override public boolean isAttackJustPressed() { return atkJustPressed;    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void renderOverlay(SpriteBatch batch) {
        drawBtn(batch, LEFT_X,  LEFT_Y,  moveLeft,  0.25f, 0.45f, 0.95f, "<");
        drawBtn(batch, RIGHT_X, RIGHT_Y, moveRight, 0.25f, 0.45f, 0.95f, ">");
        drawBtn(batch, JUMP_X,  JUMP_Y,  jumpHeld,  0.20f, 0.80f, 0.35f, "^");
        drawBtn(batch, ATK_X,   ATK_Y,   atkHeld,   0.90f, 0.30f, 0.20f, "F");
    }

    @Override
    public void dispose() {
        circleTex.dispose();
        font.dispose();
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void drawBtn(SpriteBatch batch, float cx, float cy, boolean pressed,
                         float r, float g, float b, String label) {
        float alpha = pressed ? 0.80f : 0.40f;
        float side  = BTN_R * 2f;

        // Circle background
        batch.setColor(r, g, b, alpha);
        batch.draw(circleTex, cx - BTN_R, cy - BTN_R, side, side);

        // Icon label
        layout.setText(font, label);
        font.setColor(1f, 1f, 1f, alpha + 0.15f);
        font.draw(batch, label,
                  cx - layout.width  / 2f,
                  cy + layout.height / 2f);

        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    private static boolean inCircle(float wx, float wy, float cx, float cy) {
        float dx = wx - cx, dy = wy - cy;
        return dx * dx + dy * dy <= BTN_R * BTN_R;
    }

    private static Texture buildCircle(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setColor(1f, 1f, 1f, 0f);
        pm.fill();
        pm.setColor(1f, 1f, 1f, 1f);
        pm.fillCircle(size / 2, size / 2, size / 2 - 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
