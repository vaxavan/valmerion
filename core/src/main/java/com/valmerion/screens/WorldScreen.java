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

    private static final float GROUND_Y          = 128f;
    private static final float NPC_INTERACT_DIST = 90f;
    private static final float ZAK_X    = 500f;
    private static final float HENKAI_X = 900f;

    private final Player          player;
    private final BitmapFont      font;
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout glyphLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    private final Texture         background;
    private final Texture         zakTex;
    private final Texture         henkaiTex;
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

        Texture plaza = assets.texture(AssetLoader.SLIDE_02);
        background = plaza != null ? plaza : assets.texture(AssetLoader.TEX_WORLD_ARTARTEL);

        zakTex    = assets.texture(AssetLoader.TEX_NPC_ZAK);
        henkaiTex = assets.texture(AssetLoader.TEX_NPC_TENKAI);
        npcPlaceholder = PlaceholderTextures.solid(80, 96, com.badlogic.gdx.graphics.Color.CYAN);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        whitePixel = new Texture(pm);
        pm.dispose();
        HealthBar.setWhitePixel(whitePixel);

        com.valmerion.entities.PlayerClass pClass = classFromState();
        player = new Player(assets, 150f, GROUND_Y, pClass);
        player.setTouchControls(touchControls);
        player.setDisplayScale(1.5f);
        player.unlockAll();

        dialogueOverlay = new DialogueOverlay(font);
        hpBar     = new HealthBar(20, Constants.WORLD_HEIGHT - 54, 220, 28, "Здоровье", font);
        hungerBar = new HealthBar(20, Constants.WORLD_HEIGHT - 90, 220, 22, "Голод",    font);
        pause     = new PauseOverlay(font);
        MenuButton.setFont(font);
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
            GameState.INSTANCE.save();
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
        if (stage >= 3) {
            game.setScreen(new CongratulatoryScreen(game));
            return;
        }
        nearNpc = null;
        if (stage == 0) {
            if (Math.abs(player.getPosition().x - ZAK_X) < NPC_INTERACT_DIST) nearNpc = "zak";
        } else if (stage == 2) {
            if (Math.abs(player.getPosition().x - HENKAI_X) < NPC_INTERACT_DIST) nearNpc = "henkai";
        }

        if (nearNpc != null && btnTalk.isJustClicked() && !dialogueOverlay.isActive())
            startDialogue(nearNpc);
    }

    private void startDialogue(String npc) {
        dialogueStarted = true;
        if ("zak".equals(npc))    dialogueOverlay.show(buildZakDialogue());
        else if ("henkai".equals(npc)) dialogueOverlay.show(buildHenkaiDialogue());
    }

    private void onDialogueFinished() {
        int stage = GameState.INSTANCE.storyStage;
        if (stage == 0) {
            GameState.INSTANCE.storyStage = 1;
            GameState.INSTANCE.save();
            game.setScreen(new AcademyScreen(game));
        } else if (stage == 2) {
            GameState.INSTANCE.storyStage = 3;
            GameState.INSTANCE.save();
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
        Texture henkaiDraw = henkaiTex != null ? henkaiTex : npcPlaceholder;

        if (stage == 0) {
            batch.draw(zakDraw, ZAK_X - 40, GROUND_Y, 80, 96);
            if (font != null) {
                font.setColor(1f, 0.84f, 0.2f, 1f);
                font.draw(batch, "Зак", ZAK_X - 14, GROUND_Y + 110f);
                font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            }
        } else if (stage == 2) {
            batch.draw(henkaiDraw, HENKAI_X - 40, GROUND_Y, 80, 96);
            if (font != null) {
                font.setColor(1f, 0.84f, 0.2f, 1f);
                font.draw(batch, "Хенкай", HENKAI_X - 30, GROUND_Y + 110f);
                font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            }
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

        // Movement hint — show at start before player has talked to Zak
        if (stage == 0 && !dialogueOverlay.isActive()) {
            drawHint();
        }

        if (!dialogueOverlay.isActive() && !pause.isPaused()) {
            touchControls.render(batch);
            touchControls.renderLabels(batch, font);
            if (nearNpc != null) btnTalk.render(batch, 0);
        }
        dialogueOverlay.render(batch);
        pause.render(batch);
        batch.end();
    }

    private void drawHint() {
        Texture px = HealthBar.getWhitePixel();
        float hx = Constants.WORLD_WIDTH / 2f - 260f;
        float hy = Constants.WORLD_HEIGHT - 120f;
        float hw = 520f, hh = 52f;
        if (px != null) {
            batch.setColor(0f, 0f, 0f, 0.6f);
            batch.draw(px, hx, hy, hw, hh);
            batch.setColor(1f, 0.84f, 0.2f, 0.5f);
            batch.draw(px, hx, hy, hw, 2f);
            batch.draw(px, hx, hy + hh - 2f, hw, 2f);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if (font != null) {
            String hint = nearNpc == null
                ? "Используй джойстик чтобы идти к Заку »»"
                : "Нажми «Говорить» чтобы поговорить с Заком";
            glyphLayout.setText(font, hint);
            com.badlogic.gdx.graphics.g2d.GlyphLayout gl = glyphLayout;
            font.setColor(1f, 0.95f, 0.75f, 1f);
            font.draw(batch, hint, hx + (hw - gl.width) / 2f, hy + (hh + gl.height) / 2f);
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
    }

    private DialogueLine[] buildZakDialogue() {
        return new DialogueLine[]{
            new DialogueLine("Зак",
                "Арнольд! Ты вернулся! Три года прошло... где же ты пропадал?!",
                new String[]{"Память отшибло. Ты кто?", "Привет... не помню тебя."},
                new float[]{0f, 5f}, new float[]{0f, 1f}),
            new DialogueLine("Зак",
                "Я — Зак, твой лучший друг. Ладно, по новой познакомимся. Главное — ты вернулся."),
            new DialogueLine("Зак",
                "Война Света и Тьмы уже начинается. Тебе нужно пройти обучение в Академии Валмерион."),
            new DialogueLine("Зак",
                "Используй джойстик слева чтобы двигаться. Зелёная кнопка — прыжок. Красная — атака. Вперёд, в Академию!"),
        };
    }

    private DialogueLine[] buildHenkaiDialogue() {
        return new DialogueLine[]{
            new DialogueLine("Хенкай",
                "Зак говорил о тебе. Ты прошёл обучение — это редкий дар в наше время."),
            new DialogueLine("Хенкай",
                "Я — Хенкай, глава Академии Валмерион. Ранг A. Готов взять тебя в поход."),
            new DialogueLine("Хенкай",
                "Война уже у наших ворот. Ты нужен нам, Арнольд. Готов сражаться?",
                new String[]{"Готов. Веди меня.", "Мне нужно время подготовиться."},
                new float[]{10f, 0f}, new float[]{1f, 0f}),
            new DialogueLine("Хенкай",
                "Тогда выступаем. История Валмериона только начинается..."),
        };
    }

    private static com.valmerion.entities.PlayerClass classFromState() {
        String s = GameState.INSTANCE.selectedClass;
        for (com.valmerion.entities.PlayerClass c : com.valmerion.entities.PlayerClass.values()) {
            if (c.prefix.equals(s)) return c;
        }
        return com.valmerion.entities.PlayerClass.ARCHER;
    }

    @Override
    public void dispose() {
        player.dispose();
        dialogueOverlay.dispose();
        if (npcPlaceholder != null) npcPlaceholder.dispose();
        if (whitePixel     != null) whitePixel.dispose();
    }
}
