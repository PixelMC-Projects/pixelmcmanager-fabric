package com.pixelmc.pixelmcmanager;

import com.mojang.logging.LogUtils;
import com.pixelmc.pixelmcmanager.audit.AuditService;
import com.pixelmc.pixelmcmanager.command.PixelMCManagerCommands;
import com.pixelmc.pixelmcmanager.config.WelcomeConfigManager;
import com.pixelmc.pixelmcmanager.event.AnnouncementScheduler;
import com.pixelmc.pixelmcmanager.event.DelayedMessageScheduler;
import com.pixelmc.pixelmcmanager.maintenance.MaintenanceScheduler;
import com.pixelmc.pixelmcmanager.maintenance.StopServerScheduler;
import com.pixelmc.pixelmcmanager.placeholder.PlaceholderResolver;
import com.pixelmc.pixelmcmanager.runtime.ServerRuntimeStats;
import com.pixelmc.pixelmcmanager.stats.PlayerLoginSnapshot;
import com.pixelmc.pixelmcmanager.stats.PlayerStatsStore;
import com.pixelmc.pixelmcmanager.text.TextTemplateParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public final class PixelMCManager implements ModInitializer {
    public static final String MOD_ID = "pixelmcmanager";
    public static final Logger LOGGER = LogUtils.getLogger();

    private WelcomeConfigManager configManager;
    private PlayerStatsStore statsStore;
    private AuditService auditService;
    private DelayedMessageScheduler delayedMessages;
    private AnnouncementScheduler announcements;
    private StopServerScheduler stopServerScheduler;
    private MaintenanceScheduler maintenanceScheduler;
    private ServerRuntimeStats runtimeStats;
    private long lastStatsCheckpointMillis = 0L;

    @Override
    public void onInitialize() {
        configManager = new WelcomeConfigManager(LOGGER);
        statsStore = new PlayerStatsStore(LOGGER);
        auditService = new AuditService(LOGGER);
        PlaceholderResolver placeholderResolver = new PlaceholderResolver();
        TextTemplateParser textParser = new TextTemplateParser(LOGGER);
        delayedMessages = new DelayedMessageScheduler(configManager, statsStore, placeholderResolver, textParser);
        announcements = new AnnouncementScheduler(configManager, placeholderResolver, textParser);
        stopServerScheduler = new StopServerScheduler(configManager, textParser);
        maintenanceScheduler = new MaintenanceScheduler(configManager, textParser);
        runtimeStats = new ServerRuntimeStats();
        PixelMCManagerCommands commands = new PixelMCManagerCommands(configManager, statsStore, auditService, placeholderResolver, textParser, stopServerScheduler, maintenanceScheduler, runtimeStats);

        configManager.loadOrCreate();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> commands.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            configManager.loadOrCreate();
            statsStore.load(server);
            auditService.load(server);
            lastStatsCheckpointMillis = System.currentTimeMillis();
            runtimeStats.markServerStarted(lastStatsCheckpointMillis);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            statsStore.settleAll(System.currentTimeMillis());
            statsStore.save();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerDisconnect(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            stopServerScheduler.tick(server);
            maintenanceScheduler.tick(server);
            delayedMessages.tick(server);
            announcements.tick(server);
            checkpointStatsIfDue(server);
        });

        LOGGER.info("PixelMC Manager Fabric loaded.");
    }

    private void onPlayerJoin(ServerPlayer player) {
        if (stopServerScheduler.rejectMaintenanceJoin(player)) {
            return;
        }
        if (maintenanceScheduler.rejectMaintenanceJoin(player)) {
            return;
        }

        long nowMillis = System.currentTimeMillis();
        if (!statsStore.ensureLoaded(player.level().getServer())) {
            return;
        }
        PlayerLoginSnapshot snapshot = statsStore.recordLogin(player, nowMillis);
        statsStore.save();
        delayedMessages.schedule(player, snapshot.firstJoin(), snapshot.previousLastJoinEpochMillis());
    }

    private void onPlayerDisconnect(ServerPlayer player) {
        delayedMessages.cancel(player.getUUID());
        statsStore.recordLogout(player, System.currentTimeMillis());
        statsStore.save();
    }

    private void checkpointStatsIfDue(net.minecraft.server.MinecraftServer server) {
        if (!statsStore.hasOnlineSessions()) {
            return;
        }

        long nowMillis = System.currentTimeMillis();
        long intervalMillis = configManager.getConfig().statsAutoSaveSeconds * 1_000L;
        if (lastStatsCheckpointMillis <= 0L || nowMillis - lastStatsCheckpointMillis >= intervalMillis) {
            statsStore.checkpointOnlinePlayers(server, nowMillis, true);
            lastStatsCheckpointMillis = nowMillis;
        }
    }
}
