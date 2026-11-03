package noobmodantor.module;

import noobmodantor.ModuleConfig;
import noobmodantor.NoobConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ordered registry of all modules (menu order). Same ids/order as every Noob_mod_antor version. */
public final class Modules {
    private static final Map<String, Module> BY_ID = new LinkedHashMap<String, Module>();
    private static final List<Module> ALL = new ArrayList<Module>();

    private Modules() {
    }

    public static void init() {
        if (!ALL.isEmpty()) return;
        register(new FpsModule());
        register(new CpsModule());
        register(new KeystrokesModule());
        register(new CoordsModule());
        register(new ArmorModule());
        register(new PotionModule());
        register(new PingModule());
        register(new ClockModule());
        register(new ToggleSprintModule());
        register(new ZoomModule());
        register(new FullbrightModule());
        register(new CrosshairModule());
        register(new FovModule());
        register(new ReachModule());
        register(new ComboModule());
        register(new ItemCounterModule());
        register(new PlaytimeModule());
        register(new MemoryModule());
        register(new ServerModule());
        register(new ScoreboardModule());
        register(new BossBarModule());
        register(new TimeChangerModule());
        register(new ToggleSneakModule());
        register(new NickHiderModule());
        register(new EntitySizeModule());
        register(new AnimationModule());
        register(new WeatherModule());
        register(new FreelookModule());
        register(new FreecamModule());
        register(new NameHighlightModule());
        register(new HeldItemModule());
        register(new HitboxModule());
        register(new ChatTweaksModule());
        register(new NametagModule());
        register(new ParticlesModule());
        register(new BehindYouModule());
        register(new SoundTweaksModule());
        register(new SmartCullingModule());
        applyDefaults();
    }

    private static void register(Module module) {
        BY_ID.put(module.id, module);
        ALL.add(module);
    }

    /** Modules that ship disabled by default (everything else ships enabled). */
    private static final String[] DEFAULT_OFF = {
            "toggle_sprint", "fullbright", "toggle_sneak", "time_changer", "nick_hider", "fov",
            "entity_size", "weather", "hitbox", "sound_tweaks"
    };

    private static void applyDefaults() {
        boolean fresh = NoobConfig.get().modules.isEmpty();
        for (Module m : ALL) {
            boolean existed = NoobConfig.get().modules.containsKey(m.id);
            ModuleConfig cfg = NoobConfig.module(m.id);
            if (!existed) {
                for (String off : DEFAULT_OFF) {
                    if (m.id.equals(off)) {
                        cfg.enabled = false;
                        break;
                    }
                }
            }
        }
        if (fresh) NoobConfig.save();
    }

    public static Module get(String id) {
        return BY_ID.get(id);
    }

    public static List<Module> all() {
        return ALL;
    }
}
