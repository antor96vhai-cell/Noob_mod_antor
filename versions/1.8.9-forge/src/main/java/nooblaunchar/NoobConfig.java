package nooblaunchar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root config, persisted as JSON at config/nooblaunchar.json.
 * Also remembers the user's own FOV / gamma while the Zoom / Fullbright
 * modules temporarily override them, so the originals can always be
 * restored (including after an unclean shutdown, via startup cleanup).
 * Same schema as the other Noob_launchar versions (see shared/CONFIG_SCHEMA.md).
 */
public class NoobConfig {
    public Map<String, ModuleConfig> modules = new LinkedHashMap<String, ModuleConfig>();

    /** User's own FOV captured when zoom first engages; null when not overriding. */
    public Double baseFov = null;
    public boolean fovOverrideActive = false;

    /** User's own gamma captured when fullbright first engages; null when not overriding. */
    public Double baseGamma = null;
    public boolean gammaOverrideActive = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NoobConfig INSTANCE;
    private static File file;

    public static NoobConfig get() {
        if (INSTANCE == null) INSTANCE = new NoobConfig();
        return INSTANCE;
    }

    /** Returns the config for a module id, creating and registering a default one if absent. */
    public static ModuleConfig module(String id) {
        NoobConfig cfg = get();
        ModuleConfig mc = cfg.modules.get(id);
        if (mc == null) {
            mc = new ModuleConfig();
            cfg.modules.put(id, mc);
        }
        if (mc.options == null) mc.options = new HashMap<String, String>();
        return mc;
    }

    public static void init(File configDir) {
        file = new File(configDir, "nooblaunchar.json");
        load();
    }

    public static void load() {
        try {
            if (file != null && file.exists()) {
                String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                NoobConfig loaded = GSON.fromJson(json, NoobConfig.class);
                if (loaded != null) INSTANCE = loaded;
            }
        } catch (Exception ignored) {
            // Corrupt config: fall through with defaults rather than crashing the client.
        }
        if (INSTANCE == null) INSTANCE = new NoobConfig();
        if (INSTANCE.modules == null) INSTANCE.modules = new LinkedHashMap<String, ModuleConfig>();
        for (ModuleConfig mc : INSTANCE.modules.values()) {
            if (mc != null && mc.options == null) mc.options = new HashMap<String, String>();
        }
    }

    public static void save() {
        if (file == null) return;
        try {
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            Files.write(file.toPath(), GSON.toJson(get()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
