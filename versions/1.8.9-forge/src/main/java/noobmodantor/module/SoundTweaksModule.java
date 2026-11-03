package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraftforge.client.event.sound.PlaySoundEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Sound Tweaks (scoped attempt): per-category volume sliders for a
 * few common sounds — UI click, level-up, explosion, player hurt —
 * applied on Forge's PlaySoundEvent by replacing the sound with an
 * identical one at the scaled volume (the event's public
 * {@code result} field exists exactly for this). Matching is by
 * substring of the sound's name, so it works across the naming
 * variants of this version ("random.click", "random.levelup",
 * "random.explode", "game.player.hurt", ...). Categories that match
 * nothing are left completely alone.
 *
 * <p>Known simplification: the replacement record uses the standard
 * linear attenuation model; a sound originally recorded with a
 * different attenuation type keeps its position/pitch but falls back
 * to linear falloff. Ships disabled by default.
 */
public class SoundTweaksModule extends Module {

    public SoundTweaksModule() {
        super("sound_tweaks", "Sound Tweaks");
    }

    @Override
    public boolean hasHud() {
        return false;
    }

    @Override
    public List<SliderOpt> sliders() {
        return Arrays.asList(
                new SliderOpt("click", "UI click volume %", 0, 200, 10, 100),
                new SliderOpt("levelup", "Level-up volume %", 0, 200, 10, 100),
                new SliderOpt("explosion", "Explosion volume %", 0, 200, 10, 100),
                new SliderOpt("hurt", "Player hurt volume %", 0, 200, 10, 100));
    }

    private double factorFor(String name) {
        if (name == null) return 1.0;
        String n = name.toLowerCase();
        List<SliderOpt> s = sliders();
        if (n.contains("click")) return sliderVal(s.get(0)) / 100.0;
        if (n.contains("levelup")) return sliderVal(s.get(1)) / 100.0;
        if (n.contains("explode") || n.contains("explosion")) return sliderVal(s.get(2)) / 100.0;
        if (n.contains("hurt") && n.contains("player")) return sliderVal(s.get(3)) / 100.0;
        return 1.0;
    }

    public static void onPlaySound(PlaySoundEvent event) {
        SoundTweaksModule self = (SoundTweaksModule) Modules.get("sound_tweaks");
        if (self == null || !self.enabled()) return;
        ISound sound = event.sound;
        if (sound == null) return;
        double factor = self.factorFor(event.name);
        if (factor == 1.0) return;
        event.result = new PositionedSoundRecord(sound.getSoundLocation(),
                sound.getVolume() * (float) factor, sound.getPitch(),
                sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
    }

    // Non-HUD module.
    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{0, 0};
    }

    @Override
    public int width(Minecraft mc) {
        return 0;
    }

    @Override
    public int height(Minecraft mc) {
        return 0;
    }

    @Override
    public void render(Minecraft mc, int x, int y) {
    }
}
