package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.entities.Goblin;
import com.valmerion.entities.Goblin.Mode;
import com.valmerion.entities.Player;
import com.valmerion.ui.HealthBar;
import com.valmerion.ui.HintOverlay;
import com.valmerion.ui.TouchControls;
import com.valmerion.utils.Constants;

/**
 * Academy tutorial screen.
 *
 * <pre>
 * Tutorial stages:
 *  0  INTRO        — 2.5 s welcome pause
 *  1  WALK         — move A/D > 80 px
 *  2  JUMP         — press SPACE (unlock jump first)
 *  3  ATTACK       — press F   (unlock attack first)
 *  4  DUMMY_GOBLIN — static goblin spawns; player kills it
 *  5  LIVE_GOBLIN  — aggressive goblin spawns
 *  6  CONGRATS     — 2 s pause → CongratulatoryScreen
 * </pre>
 */
public class AcademyScreen extends BaseScreen {

    // ── Stage IDs ─────────────────────────────────────────────────────────────
    private static final int STAGE_INTRO        = 0;
    private static final int STAGE_WALK         = 1;
    private static final int STAGE_JUMP         = 2;
    private static final int STAGE_ATTACK       = 3;
    private static final int STAGE_DUMMY_GOBLIN = 4;
    private static final int STAGE_LIVE_GOBLIN  = 5;
    private static final int STAGE_CONGRATS     = 6;

    private static final String[] HINTS = {
        "Добро пожаловать в Академию Валмерион!",
        "Нажимай  A / ←  или  D / →  для передвижения",
        "Нажми  ПРОБЕЛ  чтобы прыгнуть",
        "Нажми  F  чтобы атаковать",
        "Победи неподвижного гоблина!",
        "Осторожно — теперь гоблин атакует!",
        "Отличная работа!"
    };

    // ── Game objects ──────────────────────────────────────────────────────────
    private final Player player;
    private       Goblin goblin;

    // ── UI ────────────────────────────────────────────────────────────────────
    private final HintOverlay hint;
    private final HealthBar   playerHpBar;
    private final HealthBar   goblinHpBar;
    private final BitmapFont  font;

    // ── State ─────────────────────────────────────────────────────────────────
    private int   stage      = STAGE_INTRO;
    private float stageTimer = 0f;
    private float initPosX;

    // ── Visuals ───────────────────────────────────────────────────────────────
    private final Texture  background;
    private       Texture  whitePixel;

    // ── Touch controls ────────────────────────────────────────────────────────
    private final TouchControls touchControls;

    // ── Audio ─────────────────────────────────────────────────────────────────
    private final Music music;

    public AcademyScreen(ValmerionGame game) {
        super(game);
        touchControls = new TouchControls();

        // White pixel for HealthBar
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        whitePixel = new Texture(pm);
        pm.dispose();
        HealthBar.setWhitePixel(whitePixel);

        background  = assets.texture(AssetLoader.TEX_ACADEMY_BG);
        font        = assets.font(AssetLoader.FONT_MAIN);
        hint        = new HintOverlay(font);
        playerHpBar = new HealthBar(20, Constants.WORLD_HEIGHT - 54, 220, 30, "HP");
        goblinHpBar = new HealthBar(Constants.WORLD_WIDTH - 240, Constants.WORLD_HEIGHT - 54, 220, 30, "Goblin");

        player   = new Player(assets, 150f, 400f);
        player.setTouchControls(touchControls);
        initPosX = player.getPosition().x;

        music = assets.music(AssetLoader.MUSIC_ACADEMY);
        if (music != null) {
            music.setLooping(true);
            music.setVolume(0.35f);
            music.play();
        }
    }

    // ── Screen lifecycle ──────────────────────────────────────────────────────

    @Override
    public void show() {
        stage      = STAGE_INTRO;
        stageTimer = 0f;
        hint.show(HINTS[STAGE_INTRO]);
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    @Override
    public void dispose() {
        if (music    != null) music.stop();
        if (whitePixel != null) whitePixel.dispose();
        player.dispose();
        if (goblin != null) goblin.dispose();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void update(float delta) {
        touchControls.update();
        stageTimer += delta;
        player.update(delta);

        if (goblin != null) {
            goblin.update(delta);
            goblin.updateAI(delta, player.getPosition().x,
                            player.getPosition().x + 24f);
            checkAttack();
        }

        hint.update(delta);
        playerHpBar.update(delta, player.getHp(), player.getMaxHp());
        if (goblin != null) goblinHpBar.update(delta, goblin.getHp(), goblin.getMaxHp());

        advanceTutorial();
    }

    private void advanceTutorial() {
        switch (stage) {
            case STAGE_INTRO:
                if (stageTimer > 2.5f) enter(STAGE_WALK);
                break;
            case STAGE_WALK:
                if (Math.abs(player.getPosition().x - initPosX) > 80f) enter(STAGE_JUMP);
                break;
            case STAGE_JUMP:
                if (player.getVelocity().y > 50f) enter(STAGE_ATTACK);
                break;
            case STAGE_ATTACK:
                if (player.isAttacking()) enter(STAGE_DUMMY_GOBLIN);
                break;
            case STAGE_DUMMY_GOBLIN:
                if (goblin == null) spawnGoblin(900f, Mode.STATIC);
                else if (!goblin.isAlive()) { goblin = null; enter(STAGE_LIVE_GOBLIN); }
                break;
            case STAGE_LIVE_GOBLIN:
                if (goblin == null) spawnGoblin(980f, Mode.AGGRESSIVE);
                else if (!goblin.isAlive()) { goblin = null; enter(STAGE_CONGRATS); }
                break;
            case STAGE_CONGRATS:
                if (stageTimer > 2.0f) {
                    if (com.valmerion.game.GameState.INSTANCE.storyStage == 0) {
                        game.setScreen(new WorldScreen(game));
                    } else {
                        game.setScreen(new CongratulatoryScreen(game));
                    }
                }
                break;
        }
    }

    private void enter(int s) {
        stage      = s;
        stageTimer = 0f;
        hint.show(HINTS[s]);
        if (s == STAGE_JUMP)   player.unlockJump();
        if (s == STAGE_ATTACK) player.unlockAttack();
    }

    private void spawnGoblin(float x, Mode mode) {
        goblin = new Goblin(assets, x, 400f, mode);
        goblin.setAttackListener(dmg -> {
            if (goblin != null && goblin.isAlive()) player.takeDamage(dmg);
        });
    }

    private void checkAttack() {
        if (goblin == null || !goblin.isAlive()) return;
        com.badlogic.gdx.math.Rectangle atk = player.getAttackHitbox();
        if (atk != null && atk.overlaps(goblin.getHitbox())) {
            goblin.takeDamage(Constants.PLAYER_ATTACK_DAMAGE);
        }
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    private void draw() {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (background != null) {
            batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        }

        player.render(batch);
        if (goblin != null) goblin.render(batch);

        playerHpBar.render(batch);
        if (goblin != null && goblin.isAlive()) goblinHpBar.render(batch);
        hint.render(batch);
        touchControls.render(batch);

        if (font != null) {
            font.draw(batch,
                "Класс: " + player.getPlayerClass().displayName + "  [Tab — сменить]",
                20, 30);
        }

        batch.end();
    }
}
