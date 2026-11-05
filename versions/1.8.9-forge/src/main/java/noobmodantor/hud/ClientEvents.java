package noobmodantor.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import noobmodantor.NoobConfig;
import noobmodantor.NoobModAntor;
import noobmodantor.gui.NoobMenuScreen;
import noobmodantor.module.AnimationModule;
import noobmodantor.module.BehindYouModule;
import noobmodantor.module.BossBarModule;
import noobmodantor.module.ChatTweaksModule;
import noobmodantor.module.CrosshairModule;
import noobmodantor.module.EntitySizeModule;
import noobmodantor.module.FovModule;
import noobmodantor.module.FreecamModule;
import noobmodantor.module.FreelookModule;
import noobmodantor.module.FullbrightModule;
import noobmodantor.module.HeldItemModule;
import noobmodantor.module.HitboxModule;
import noobmodantor.module.Module;
import noobmodantor.module.Modules;
import noobmodantor.module.NameHighlightModule;
import noobmodantor.module.NametagModule;
import noobmodantor.module.NickHiderModule;
import noobmodantor.module.ParticlesModule;
import noobmodantor.module.ScoreboardModule;
import noobmodantor.module.SmartCullingModule;
import noobmodantor.module.SoundTweaksModule;
import noobmodantor.module.TimeChangerModule;
import noobmodantor.module.TooltipScrollModule;
import noobmodantor.module.ToggleSneakModule;
import noobmodantor.module.ToggleSprintModule;
import noobmodantor.module.WeatherModule;
import noobmodantor.module.ZoomModule;

import java.util.HashSet;
import java.util.Set;

/**
 * Forge event handlers for every module, plus the crash-guard: every
 * module entry point runs through {@link #guard(String, Runnable)} —
 * if a module ever throws, it is logged and disabled for the rest of
 * the session instead of crashing the game (CrashPatch's spirit,
 * applied to our own code).
 */
public class ClientEvents {
    private boolean cleanedUp = false;

    // ---------------------------------------------------------------
    // Crash-guard
    // ---------------------------------------------------------------

    private static final Set<String> SESSION_DISABLED = new HashSet<String>();

    public static boolean sessionDisabled(String moduleId) {
        return SESSION_DISABLED.contains(moduleId);
    }

    private static void disableForSession(String moduleId, Throwable t) {
        if (SESSION_DISABLED.add(moduleId)) {
            NoobModAntor.LOGGER.error("[Noob_mod_antor] Module '" + moduleId
                    + "' threw an error and was disabled for this session.", t);
        }
    }

    /** Runs a module entry point under the crash-guard. */
    private static void guard(String moduleId, Runnable r) {
        if (SESSION_DISABLED.contains(moduleId)) return;
        try {
            r.run();
        } catch (Throwable t) {
            disableForSession(moduleId, t);
        }
    }

    // ---------------------------------------------------------------
    // Tick
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        final Minecraft mc = Minecraft.getMinecraft();
        if (!cleanedUp) {
            cleanedUp = true;
            ZoomModule.cleanupOnStart(mc);
            FullbrightModule.cleanupOnStart(mc);
            NoobConfig.save();
        }
        while (NoobModAntor.menuKey.isPressed()) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new NoobMenuScreen());
            }
        }
        // Freecam runs before Freelook: while it is active it owns the
        // player's rotation/input, and Freelook stays out of the way.
        guard("freecam", () -> FreecamModule.tick(mc));
        guard("freelook", () -> FreelookModule.tick(mc));
        guard("behind_you", () -> BehindYouModule.tick(mc));
        // The FOV module runs before Zoom so Zoom captures the module's
        // value as its base and restores exactly to it.
        guard("fov", () -> FovModule.tick(mc));
        ZoomModule.keyHeld = NoobModAntor.zoomKey.isKeyDown();
        guard("zoom", () -> ZoomModule.tick(mc));
        guard("fullbright", () -> FullbrightModule.tick(mc));
        guard("toggle_sprint", () -> ToggleSprintModule.tick(mc));
        guard("toggle_sneak", () -> ToggleSneakModule.tick(mc));
        guard("time_changer", () -> TimeChangerModule.tick(mc));
        guard("weather", () -> WeatherModule.tick(mc));
        guard("scoreboard", () -> ScoreboardModule.apply(mc));
        guard("chat_tweaks", () -> ChatTweaksModule.tick(mc));
        guard("particles", () -> ParticlesModule.tick(mc));
        guard("smart_culling", () -> SmartCullingModule.tick(mc));
        DynamicFps.tick(mc);
    }

    // ---------------------------------------------------------------
    // Overlay / HUD
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            if (CrosshairModule.shouldHideVanilla()) {
                event.setCanceled(true);
            }
        } else if (event.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
            if (BossBarModule.shouldHide()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (event.type == RenderGameOverlayEvent.ElementType.CHAT) {
            if (mc.thePlayer != null) {
                guard("name_highlight", () -> NameHighlightModule.drawHighlights(mc, event.resolution));
            }
            return;
        }
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        CpsTracker.poll();
        ComboTracker.poll(mc);
        guard("particles", () -> ParticlesModule.poll(mc));
        for (final Module module : Modules.all()) {
            if (!module.enabled() || !module.hasHud()) continue;
            if (SESSION_DISABLED.contains(module.id)) continue;
            final int[] pos = module.pos(mc);
            guard(module.id, () -> module.render(mc, pos[0], pos[1]));
        }
    }

    // ---------------------------------------------------------------
    // Tooltip Scroll (capture during the screen draw, draw in Post)
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        TooltipScrollModule.frameStart();
    }

    /** LOW priority: capture the tooltip lines after other mods have had their say, since capturing an overflowing tooltip also empties the event list. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        guard("tooltip_scroll", () -> TooltipScrollModule.onTooltip(event));
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        guard("tooltip_scroll", () -> TooltipScrollModule.onDrawPost(event));
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
        guard("nick_hider", () -> NickHiderModule.onChat(event));
        guard("name_highlight", () -> NameHighlightModule.onChat(event));
        guard("chat_tweaks", () -> ChatTweaksModule.onChat(event));
    }

    // ---------------------------------------------------------------
    // Sound tweaks
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        guard("sound_tweaks", () -> SoundTweaksModule.onPlaySound(event));
    }

    // ---------------------------------------------------------------
    // FOV effects (inside the FOV Changer module)
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event) {
        FovModule.onFovUpdate(event);
    }

    @SubscribeEvent
    public void onFovModifier(EntityViewRenderEvent.FOVModifier event) {
        FovModule.onFovModifier(event);
    }

    // ---------------------------------------------------------------
    // Camera substitution (Freelook orbit, Behind You snap)
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        guard("freelook", () -> FreelookModule.onCameraSetup(event));
        guard("behind_you", () -> BehindYouModule.onCameraSetup(event));
    }

    // ---------------------------------------------------------------
    // Visual-only render hooks
    // ---------------------------------------------------------------

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        // Smart Culling / Performance: entity render distance.
        int limit = SmartCullingModule.effectiveEntityLimit();
        if (limit > 0 && event.entity != mc.thePlayer
                && event.entity.getDistanceToEntity(mc.thePlayer) > limit) {
            event.setCanceled(true);
            return;
        }
        guard("animation", () -> AnimationModule.applyPre(event.entity));
        guard("entity_size", () -> EntitySizeModule.applyPre(event));
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post event) {
        guard("entity_size", () -> EntitySizeModule.applyPost(event));
        guard("animation", () -> AnimationModule.applyPost(event.entity));
    }

    @SubscribeEvent
    public void onRenderLivingSpecialsPre(RenderLivingEvent.Specials.Pre event) {
        guard("nametag", () -> NametagModule.applyScalePre(event));
    }

    @SubscribeEvent
    public void onRenderLivingSpecialsPost(RenderLivingEvent.Specials.Post event) {
        guard("nametag", () -> NametagModule.applyScalePost(event));
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        guard("held_item", () -> HeldItemModule.apply());
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        final Minecraft mc = Minecraft.getMinecraft();
        final float partialTicks = event.partialTicks;
        guard("hitbox", () -> HitboxModule.render(mc, partialTicks));
        guard("nametag", () -> NametagModule.renderOwn(mc, partialTicks));
    }
}
