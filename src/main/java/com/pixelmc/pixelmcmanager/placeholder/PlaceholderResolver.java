package com.pixelmc.pixelmcmanager.placeholder;

import com.pixelmc.pixelmcmanager.stats.PlayerStats;
import com.pixelmc.pixelmcmanager.util.TimeFormatUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlaceholderResolver {
    public String resolve(String template, PlaceholderContext context) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        ServerPlayer player = context.player();
        PlayerStats stats = context.stats();
        Map<String, String> values = new LinkedHashMap<>();
        MinecraftServer server = player.level().getServer();
        values.put("{player}", player.getGameProfile().name());
        values.put("{uuid}", player.getUUID().toString());
        values.put("{online}", server == null ? "0" : Integer.toString(server.getPlayerList().getPlayerCount()));
        values.put("{max}", server == null ? "0" : Integer.toString(server.getMaxPlayers()));
        values.put("{ping}", Integer.toString(Math.max(0, player.connection.latency())));
        values.put("{join_count}", stats == null ? "0" : Integer.toString(stats.joinCount));
        values.put("{playtime}", stats == null ? "0秒" : TimeFormatUtil.formatFriendly(stats.totalOnlineMillis));
        values.put("{first_join}", stats == null ? "未知" : TimeFormatUtil.formatDateTime(stats.firstJoinEpochMillis, context.config().timezone, context.config().dateFormat, context.config().timeFormat));
        values.put("{last_join}", TimeFormatUtil.formatDateTime(context.previousLastJoinEpochMillis(), context.config().timezone, context.config().dateFormat, context.config().timeFormat));

        String resolved = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            resolved = resolved.replace(entry.getKey(), entry.getValue());
        }
        return resolved;
    }
}
