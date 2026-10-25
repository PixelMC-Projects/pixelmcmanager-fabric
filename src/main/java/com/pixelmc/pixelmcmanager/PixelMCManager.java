package com.pixelmc.pixelmcmanager;

import com.mojang.logging.LogUtils;
import com.pixelmc.pixelmcmanager.audit.AuditService;
import com.pixelmc.pixelmcmanager.bridge.BridgeAgent;
import com.pixelmc.pixelmcmanager.command.PixelMCManagerCommands;
import com.pixelmc.pixelmcmanager.config.WelcomeConfigManager;
import com.pixelmc.pixelmcmanager.event.*;
import com.pixelmc.pixelmcmanager.maintenance.*;
import com.pixelmc.pixelmcmanager.placeholder.PlaceholderResolver;
import com.pixelmc.pixelmcmanager.restart.IdleRestartScheduler;
import com.pixelmc.pixelmcmanager.runtime.ServerRuntimeStats;
import com.pixelmc.pixelmcmanager.stats.PlayerStatsStore;
import com.pixelmc.pixelmcmanager.text.TextTemplateParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public final class PixelMCManager implements ModInitializer {
    public static final String MOD_ID = "pixelmcmanager";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String VERSION = version(MOD_ID);
    public static final String MINECRAFT_VERSION = version("minecraft");
    public static final String LOADER_VERSION = version("fabricloader");
    private static String version(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
    }

    @Override public void onInitialize() {
        WelcomeConfigManager config = new WelcomeConfigManager(LOGGER);
        PlayerStatsStore stats = new PlayerStatsStore(LOGGER);
        AuditService audit = new AuditService(LOGGER);
        PlaceholderResolver placeholders = new PlaceholderResolver();
        TextTemplateParser text = new TextTemplateParser(LOGGER);
        DelayedMessageScheduler delayed = new DelayedMessageScheduler(config, stats, placeholders, text);
        AnnouncementScheduler announcements = new AnnouncementScheduler(config, placeholders, text);
        StopServerScheduler stop = new StopServerScheduler(config, text);
        MaintenanceScheduler maintenance = new MaintenanceScheduler(config, text);
        IdleRestartScheduler restart = new IdleRestartScheduler(config, stop, audit);
        ServerRuntimeStats runtime = new ServerRuntimeStats();
        BridgeAgent bridge = new BridgeAgent(stats, config, maintenance, stop, restart);
        WelcomeEventHandler events = new WelcomeEventHandler(config, stats, audit, delayed, announcements, stop, maintenance, runtime, restart, bridge);
        PixelMCManagerCommands commands = new PixelMCManagerCommands(config, stats, audit, placeholders, text, stop, maintenance, runtime, restart);
        config.loadOrCreate();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> commands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> { events.onServerStarted(server); bridge.started(server); });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> { events.onServerStopping(server); bridge.stopping(server); });
        ServerLifecycleEvents.SERVER_STOPPED.register(bridge::stopped);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> events.onPlayerLoggedIn(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> events.onPlayerLoggedOut(handler.player));
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> bridge.chunkLoaded(level, chunk));
        ServerChunkEvents.CHUNK_UNLOAD.register(bridge::chunkUnloaded);
        // Surround normal Fabric listeners like Forge's HIGHEST START / LOWEST END priorities.
        Identifier early = Identifier.fromNamespaceAndPath(MOD_ID, "telemetry_start");
        Identifier late = Identifier.fromNamespaceAndPath(MOD_ID, "telemetry_end");
        ServerTickEvents.START_SERVER_TICK.addPhaseOrdering(early, Event.DEFAULT_PHASE);
        ServerTickEvents.START_SERVER_TICK.register(early, bridge::beginTick);
        ServerTickEvents.END_SERVER_TICK.register(events::onServerTick);
        ServerTickEvents.END_SERVER_TICK.addPhaseOrdering(Event.DEFAULT_PHASE, late);
        ServerTickEvents.END_SERVER_TICK.register(late, bridge::endTick);
        ServerTickEvents.START_LEVEL_TICK.addPhaseOrdering(early, Event.DEFAULT_PHASE);
        ServerTickEvents.START_LEVEL_TICK.register(early, bridge::beginLevel);
        ServerTickEvents.END_LEVEL_TICK.addPhaseOrdering(Event.DEFAULT_PHASE, late);
        ServerTickEvents.END_LEVEL_TICK.register(late, bridge::endLevel);
        LOGGER.info("PixelMC Manager Fabric {} loaded.", VERSION);
    }
}
