package noobmodantor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Ships a small built-in FPS resource pack inside the mod jar and extracts
 * it into the game's {@code resourcepacks} folder as "Noob_mod_antor FPS
 * Boost" on first run. The pack files live in the jar under
 * {@code assets/noob_mod_antor/fpspack/} with a {@code packfiles.txt}
 * index (generated together with the textures by generate_pack.py). The
 * pack is never auto-enabled and never overwritten: if the target folder
 * already contains a pack, extraction is skipped entirely, so any changes
 * the user makes to it are safe. The user enables it manually in
 * Options -&gt; Resource Packs.
 */
public final class FpsPack {
    public static final String PACK_NAME = "Noob_mod_antor FPS Boost";
    private static final String RESOURCE_ROOT = "/assets/noob_mod_antor/fpspack/";

    private FpsPack() {
    }

    public static void extract(File gameDir) {
        try {
            File target = new File(new File(gameDir, "resourcepacks"), PACK_NAME);
            if (new File(target, "pack.mcmeta").exists()) {
                NoobModAntor.LOGGER.info("Built-in FPS resource pack already present at {} - skipping extraction.", target);
                return;
            }
            List<String> files = new ArrayList<String>();
            InputStream indexStream = FpsPack.class.getResourceAsStream(RESOURCE_ROOT + "packfiles.txt");
            if (indexStream == null) {
                NoobModAntor.LOGGER.warn("Built-in FPS resource pack index missing from the jar; nothing extracted.");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream, StandardCharsets.UTF_8));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) files.add(line);
                }
            } finally {
                reader.close();
            }

            int copied = 0;
            for (String rel : files) {
                InputStream in = FpsPack.class.getResourceAsStream(RESOURCE_ROOT + rel);
                if (in == null) {
                    NoobModAntor.LOGGER.warn("Built-in FPS pack file missing from the jar: {}", rel);
                    continue;
                }
                try {
                    File out = new File(target, rel);
                    if (out.getParentFile() != null) out.getParentFile().mkdirs();
                    Files.copy(in, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                } finally {
                    in.close();
                }
            }
            NoobModAntor.LOGGER.info("Extracted built-in FPS resource pack '{}' ({} files) to {}. Enable it in Options -> Resource Packs.",
                    PACK_NAME, copied, target);
        } catch (Exception e) {
            // A broken pack extraction must never break the mod itself.
            NoobModAntor.LOGGER.error("Failed to extract the built-in FPS resource pack.", e);
        }
    }
}
