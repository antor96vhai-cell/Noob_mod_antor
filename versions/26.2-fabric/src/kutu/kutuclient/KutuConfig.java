package kutu.kutuclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root config, persisted as JSON at config/kutuclient.json.
 * Also remembers the user's own FOV / gamma while the Zoom / Fullbright
 * modules temporarily override them, so the originals can always be
 * restored (including after an unclean shutdown, via startup cleanup).
 */
public class KutuConfig {
    public Map<String, ModuleConfig> modules = new LinkedHashMap<>();

    /** User's own FOV captured when zoom first engages; null when not overriding. */
    public Integer baseFov = null;
    public boolean fovOverrideActive = false;

    /** User's own gamma captured when fullbright first engages; null when not overriding. */
    public Double baseGamma = null;
    public boolean gammaOverrideActive = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static KutuConfig INSTANCE;

    public static KutuConfig get() {
        if (INSTANCE == null) INSTANCE = new KutuConfig();
        return INSTANCE;
    }

    /** Returns the config for a module id, creating and registering a default one if absent. */
    public static ModuleConfig module(String id) {
        KutuConfig cfg = get();
        ModuleConfig mc = cfg.modules.get(id);
        if (mc == null) {
            mc = new ModuleConfig();
            cfg.modules.put(id, mc);
        }
        if (mc.options == null) mc.options = new HashMap<>();
        return mc;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("kutuclient.json");
    }

    public static void load() {
        Path path = file();
        try {
            if (Files.exists(path)) {
                KutuConfig loaded = GSON.fromJson(Files.readString(path), KutuConfig.class);
                if (loaded != null) INSTANCE = loaded;
            }
        } catch (IOException | RuntimeException ignored) {
            // Corrupt config: fall through with defaults rather than crashing the client.
        }
        if (INSTANCE == null) INSTANCE = new KutuConfig();
        if (INSTANCE.modules == null) INSTANCE.modules = new LinkedHashMap<>();
        for (ModuleConfig mc : INSTANCE.modules.values()) {
            if (mc != null && mc.options == null) mc.options = new HashMap<>();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(get()));
        } catch (IOException ignored) {
        }
    }
}
