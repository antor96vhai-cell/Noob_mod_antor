package kutu.kutuclient;

import java.util.HashMap;
import java.util.Map;

/** Per-module persisted settings, serialized to config/kutuclient.json by Gson. */
public class ModuleConfig {
    public boolean enabled = true;
    /** HUD position; -1 means "use the module's default anchor". */
    public int x = -1;
    public int y = -1;
    /** RGB text color (alpha is forced to 0xFF at render time). */
    public int color = 0xFFFFFF;
    /** Whether text-style HUD elements draw their translucent background panel. */
    public boolean background = true;
    /** Free-form per-module option values, keyed by option id. */
    public Map<String, String> options = new HashMap<>();
}
