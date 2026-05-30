package com.valmerion.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;

/**
 * Centralised asset management. All textures, atlases, audio and fonts
 * are loaded here once and retrieved by key.
 *
 * <p>Call {@link #loadAll()} once at startup, then use the typed getters.
 * If a file is missing it is silently skipped — screens must handle nulls.
 */
public class AssetLoader {

    // ── Menu ──────────────────────────────────────────────────────────────────
    public static final String TEX_MENU_BG           = "textures/menu/menu_bg.png";
    public static final String TEX_BTN_NEW_GAME      = "textures/menu/btn_new_game.png";
    public static final String TEX_BTN_NEW_GAME_HOV  = "textures/menu/btn_new_game_hover.png";
    public static final String TEX_BTN_SETTINGS      = "textures/menu/btn_settings.png";
    public static final String TEX_BTN_EXIT          = "textures/menu/btn_exit.png";

    // ── World / NPC ───────────────────────────────────────────────────────────
    public static final String TEX_WORLD_ARTARTEL    = "textures/world/artartel_bg.png";
    public static final String TEX_NPC_ZAK           = "textures/characters/npc/zak.png";
    public static final String TEX_NPC_HAZAN         = "textures/characters/npc/hazan.png";
    public static final String TEX_NPC_TENKAI        = "textures/characters/npc/tenkai.png";

    // ── Academy ───────────────────────────────────────────────────────────────
    public static final String TEX_ACADEMY_BG        = "textures/academy/bg_academy.png";

    // ── Cutscene slides ───────────────────────────────────────────────────────
    /** Dark forest — "darkness spreads across the kingdom" */
    public static final String SLIDE_01 = "textures/cutscene/slide_01.png";
    /** Medieval city at night — "the kingdom of Valmerion" */
    public static final String SLIDE_02 = "textures/cutscene/slide_02.png";
    /** Cave/dungeon — "ancient evil awakens" */
    public static final String SLIDE_03 = "textures/cutscene/slide_03.png";
    /** Wizard's study — "the Academy awaits" */
    public static final String SLIDE_04 = "textures/cutscene/slide_04.png";

    public static final String[] CUTSCENE_SLIDES = {
        SLIDE_01, SLIDE_02, SLIDE_03, SLIDE_04
    };

    // ── Character atlases ─────────────────────────────────────────────────────
    public static final String ATLAS_PLAYER = "textures/characters/player/player.atlas";
    public static final String ATLAS_GOBLIN = "textures/characters/goblin/goblin.atlas";

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final String FONT_MAIN  = "main_font.ttf";
    public static final String FONT_TITLE = "title_font.ttf";

    // ── Music ─────────────────────────────────────────────────────────────────
    public static final String MUSIC_MENU    = "audio/music/menu.ogg";
    public static final String MUSIC_ACADEMY = "audio/music/academy.ogg";

    // ── SFX ───────────────────────────────────────────────────────────────────
    public static final String SFX_BTN_CLICK = "audio/sfx/btn_click.ogg";
    public static final String SFX_JUMP      = "audio/sfx/jump.ogg";
    public static final String SFX_ATTACK    = "audio/sfx/attack.ogg";
    public static final String SFX_HIT       = "audio/sfx/hit.ogg";

    // ── Internal ─────────────────────────────────────────────────────────────
    private final AssetManager manager;

    public AssetLoader() {
        manager = new AssetManager();
        InternalFileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void loadAll() {
        // Textures
        safeLoad(TEX_MENU_BG,          Texture.class);
        safeLoad(TEX_BTN_NEW_GAME,     Texture.class);
        safeLoad(TEX_BTN_NEW_GAME_HOV, Texture.class);
        safeLoad(TEX_BTN_SETTINGS,     Texture.class);
        safeLoad(TEX_BTN_EXIT,         Texture.class);
        safeLoad(TEX_WORLD_ARTARTEL,   Texture.class);
        safeLoad(TEX_NPC_ZAK,         Texture.class);
        safeLoad(TEX_NPC_HAZAN,       Texture.class);
        safeLoad(TEX_NPC_TENKAI,      Texture.class);
        safeLoad(TEX_ACADEMY_BG,      Texture.class);

        for (String slide : CUTSCENE_SLIDES) {
            safeLoad(slide, Texture.class);
        }

        // Atlases
        safeLoad(ATLAS_PLAYER, TextureAtlas.class);
        safeLoad(ATLAS_GOBLIN, TextureAtlas.class);

        // Fonts
        loadFont(FONT_MAIN,  "fonts/main.ttf",  24);
        loadFont(FONT_TITLE, "fonts/title.ttf", 52);

        // Music
        safeLoad(MUSIC_MENU,    Music.class);
        safeLoad(MUSIC_ACADEMY, Music.class);

        // SFX
        safeLoad(SFX_BTN_CLICK, Sound.class);
        safeLoad(SFX_JUMP,      Sound.class);
        safeLoad(SFX_ATTACK,    Sound.class);
        safeLoad(SFX_HIT,       Sound.class);

        manager.finishLoading();
        applyLinearFilters();
        Gdx.app.log("AssetLoader", "All assets loaded.");
    }

    public Texture      texture(String key) { return get(key, Texture.class);      }
    public TextureAtlas atlas  (String key) { return get(key, TextureAtlas.class); }
    public BitmapFont   font   (String key) { return get(key, BitmapFont.class);   }
    public Music        music  (String key) { return get(key, Music.class);        }
    public Sound        sound  (String key) { return get(key, Sound.class);        }

    public AssetManager getManager() { return manager; }
    public void         dispose()    { manager.dispose(); }

    // ── Private ───────────────────────────────────────────────────────────────

    private <T> T get(String key, Class<T> type) {
        return manager.isLoaded(key) ? manager.get(key, type) : null;
    }

    private <T> void safeLoad(String path, Class<T> type) {
        if (Gdx.files.internal(path).exists()) {
            manager.load(path, type);
        } else {
            Gdx.app.log("AssetLoader", "SKIP (not found): " + path);
        }
    }

    private void loadFont(String key, String fontPath, int size) {
        if (!Gdx.files.internal(fontPath).exists()) {
            Gdx.app.log("AssetLoader", "SKIP font (not found): " + fontPath);
            return;
        }
        FreeTypeFontLoaderParameter p = new FreeTypeFontLoaderParameter();
        p.fontFileName            = fontPath;
        p.fontParameters.size     = size;
        p.fontParameters.minFilter = TextureFilter.Linear;
        p.fontParameters.magFilter = TextureFilter.Linear;
        manager.load(key, BitmapFont.class, p);
    }

    private void applyLinearFilters() {
        com.badlogic.gdx.utils.Array<Texture> textures = new com.badlogic.gdx.utils.Array<>();
        manager.getAll(Texture.class, textures);
        for (Texture t : textures) {
            t.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
    }
}
