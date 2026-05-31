package com.valmerion.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.valmerion.ValmerionGame;
import com.valmerion.assets.AssetLoader;
import com.valmerion.dialogue.DialogueLine;
import com.valmerion.dialogue.DialogueOverlay;
import com.valmerion.entities.Player;
import com.valmerion.game.GameState;
import com.valmerion.ui.HealthBar;
import com.valmerion.ui.MenuButton;
import com.valmerion.ui.PauseOverlay;
import com.valmerion.ui.TouchControls;
import com.valmerion.utils.Constants;
import com.valmerion.utils.PlaceholderTextures;

public class WorldScreen extends BaseScreen {

    private static final float GROUND_Y          = 160f;
    private static final float NPC_INTERACT_DIST = 90f;
    private static final float ZAK_X    = 500f;
    private static final float TENKAI_X = 900f;

    private final Player          player;
    private final BitmapFont      font;
    private final Texture         background;
    private final Texture         zakTex;
    private final Texture         tenkaiTex;
    private final Texture         npcPlaceholder;
    private final DialogueOverlay dialogueOverlay;
    private final HealthBar       hpBar;
    private final HealthBar       hungerBar;
    private final PauseOverlay    pause;
    private final MenuButton      btnTalk;
    private final Texture         whitePixel;
    private final TouchControls   touchControls;

    private String  nearNpc         = null;
    private boolean dialogueStarted = false;

    public WorldScreen(ValmerionGame game) {
        super(game);
        font          = assets.font(AssetLoader.FONT_MAIN);
        touchControls = new TouchControls();

        // bg_plaza — ночной средневековый город Артартель
        Texture plaza = assets.texture(AssetLoader.SLIDE_02);
        background = plaza != null ? plaza : assets.texture(AssetLoader.TEX_WORLD_ARTARTEL);

        zakTex     = assets.texture(AssetLoader.TEX_NPC_ZAK);
        tenkaiTex  = assets.texture(AssetLoader.TEX_NPC_TENKAI);
        npcPlaceholder = PlaceholderTextures.solid(80, 96, com.badlogic.gdx.graphics.Color.CYAN);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        whitePixel = new Texture(pm);
        pm.dispose();
        HealthBar.setWhitePixel(whitePixel);

        player = new Player(assets, 150f, GROUND_Y);
        player.setTouchControls(touchControls);
        player.unlockAll();

        dialogueOverlay = new DialogueOverlay(font);
        hpBar     = new HealthBar(20, Constants.WORLD_HEIGHT - 54, 220, 28, "Здоровье", font);
        hungerBar = new HealthBar(20, Constants.WORLD_HEIGHT - 90, 220, 22, "Голод",    font);
        pause     = new PauseOverlay(font);
        MenuButton.setFont(font);
        // "Говорить" button — bottom center, shown when near NPC
        float bw = 260f, bh = 72f;
        btnTalk = new MenuButton(null, null, "Говорить",
            Constants.WORLD_WIDTH / 2f - bw / 2f, 20f, bw, bh);
    }

    @Override public void show() { dialogueStarted = false; }

    @Override
    public void render(float delta) {
        update(delta);
        draw();
    }

    private void update(float delta) {
        touchControls.update();

        boolean paused = pause.update();
        if (pause.wantsMainMenu()) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        if (paused) return;

        if (!dialogueOverlay.isActive()) player.update(delta);
        dialogueOverlay.update(delta);

        GameState.INSTANCE.hunger = Math.max(0f, GameState.INSTANCE.hunger - delta * 0.3f);
        hpBar.update(delta, player.getHp(), player.getMaxHp());
        hungerBar.update(delta, GameState.INSTANCE.hunger, 100f);

        if (dialogueOverlay.isFinished() && dialogueStarted) {
            dialogueStarted = false;
            onDialogueFinished();
            return;
        }

        int stage = GameState.INSTANCE.storyStage;
        nearNpc = null;
        if (stage == 0) {
            if (Math.abs(player.getPosition().x - ZAK_X) < NPC_INTERACT_DIST) nearNpc = "zak";
        } else if (stage == 1) {
            if (Math.abs(player.getPosition().x - TENKAI_X) < NPC_INTERACT_DIST) nearNpc = "tenkai";
        } else if (stage >= 2) {
            game.setScreen(new CongratulatoryScreen(game));
            return;
        }

        if (nearNpc != null && btnTalk.isJustClicked() && !dialogueOverlay.isActive())
            startDialogue(nearNpc);
    }

    private void startDialogue(String npc) {
        dialogueStarted = true;
        if ("zak".equals(npc))    dialogueOverlay.show(buildZakDialogue());
        else if ("tenkai".equals(npc)) dialogueOverlay.show(buildTenkaiDialogue());
    }

    private void onDialogueFinished() {
        int stage = GameState.INSTANCE.storyStage;
        if (stage == 0) {
            GameState.INSTANCE.storyStage = 1;
            game.setScreen(new AcademyScreen(game));
        } else if (stage == 1) {
            GameState.INSTANCE.storyStage = 2;
            game.setScreen(new CongratulatoryScreen(game));
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.05f, 0.04f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        if (background != null)
            batch.draw(background, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        int stage = GameState.INSTANCE.storyStage;
        Texture zakDraw    = zakTex    != null ? zakTex    : npcPlaceholder;
        Texture tenkaiDraw = tenkaiTex != null ? tenkaiTex : npcPlaceholder;

        if (stage == 0) {
            batch.draw(zakDraw,    ZAK_X    - 40, GROUND_Y, 80, 96);
            if (font != null) font.draw(batch, "Зак",    ZAK_X    - 14, GROUND_Y + 106f);
        } else if (stage == 1) {
            batch.draw(tenkaiDraw, TENKAI_X - 40, GROUND_Y, 80, 96);
            if (font != null) font.draw(batch, "Тенкай", TENKAI_X - 22, GROUND_Y + 106f);
        }

        player.render(batch);
        hpBar.render(batch);
        hungerBar.render(batch);

        if (font != null) {
            font.setColor(1f, 0.84f, 0.2f, 1f);
            font.draw(batch,
                "Репутация: " + (int) GameState.INSTANCE.reputation
                + "   Класс: " + player.getPlayerClass().displayName,
                20, 30);
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }

        if (!dialogueOverlay.isActive() && !pause.isPaused()) {
            touchControls.render(batch);
            if (nearNpc != null) btnTalk.render(batch, 0);
        }
        dialogueOverlay.render(batch);
        pause.render(batch);
        batch.end();
    }

    private DialogueLine[] buildZakDialogue() {
        return new DialogueLine[]{
            new DialogueLine("Зак",
                "Арнольд! Ты вернулся! Где же ты пропадал все три года?!",
                new String[]{"Память отшибло. Ты кто?", "Привет... не помню тебя."},
                new float[]{0f, 5f}, new float[]{0f, 1f}),
            new DialogueLine("Зак",
                "Неужто старину Зака забыл? Мы же лучшими друзьями были до твоей отлучки. Ладно... начнём по новой."),
            new DialogueLine("Зак",
                "Сейчас непростое время. Война Света и Тьмы не за горами. Тебе нужно выбрать путь бойца. Идём к Хазану!"),
            new DialogueLine("Зак",
                "Хазан — старый оружейник. Он проверит, какое оружие тебе ближе. Потом — тренировочный плац Академии."),
        };
    }

    private DialogueLine[] buildTenkaiDialogue() {
        return new DialogueLine[]{
            new DialogueLine("Тенкай",
                "Зак рассказал о тебе. Говорит, ты прошёл обучение. Это редкий дар."),
            new DialogueLine("Тенкай",
                "Меня зовут Тенкай. Я глава академии Валмерион. Ранг A. Готов взять тебя в обучение."),
            new DialogueLine("Тенкай",
                "Учить буду не здесь — в походах. Но сначала — тренировочный плац. Покажи, что умеешь. Вперёд!",
                new String[]{"Готов!", "Мне нужно подготовиться."},
                new float[]{10f, 0f}, new float[]{1f, 0f}),
        };
    }

    @Override
    public void dispose() {
        player.dispose();
        dialogueOverlay.dispose();
        if (npcPlaceholder != null) npcPlaceholder.dispose();
        if (whitePixel     != null) whitePixel.dispose();
    }
}
