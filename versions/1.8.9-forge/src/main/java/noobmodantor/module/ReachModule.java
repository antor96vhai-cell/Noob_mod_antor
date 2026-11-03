package noobmodantor.module;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

import java.util.Collections;
import java.util.List;

/**
 * Shows the distance (in blocks, two decimals) from the player's eyes to
 * whatever the crosshair is currently pointing at — an entity or a block —
 * read from {@code Minecraft.objectMouseOver}. Shows "--" when the
 * crosshair is over nothing within reach.
 */
public class ReachModule extends TextModule {

    public ReachModule() {
        super("reach", "Reach Display");
    }

    @Override
    protected List<String> lines(Minecraft mc) {
        String text = "Reach: --";
        if (mc.thePlayer != null && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.MISS
                && mc.objectMouseOver.hitVec != null) {
            double dist = mc.thePlayer.getPositionEyes(1.0f).distanceTo(mc.objectMouseOver.hitVec);
            text = String.format("Reach: %.2f", dist);
        }
        return Collections.singletonList(text);
    }

    @Override
    public int[] defaultPos(Minecraft mc) {
        return new int[]{4, 84};
    }
}
