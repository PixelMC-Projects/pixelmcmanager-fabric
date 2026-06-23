package com.pixelmc.pixelmcmanager.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeFormatUtil {
    private TimeFormatUtil() {
    }

    public static String formatFriendly(long millis) {
        long seconds = Math.max(0L, millis / 1_000L);
        long days = seconds / 86_400L;
        seconds %= 86_400L;
        long hours = seconds / 3_600L;
        seconds %= 3_600L;
        long minutes = seconds / 60L;
        seconds %= 60L;
        if (days > 0L) {
            return String.format(Locale.ROOT, "%d天%d小时%d分钟", days, hours, minutes);
        }
        if (hours > 0L) {
            return String.format(Locale.ROOT, "%d小时%d分钟", hours, minutes);
        }
        if (minutes > 0L) {
            return String.format(Locale.ROOT, "%d分钟%d秒", minutes, seconds);
        }
        return seconds + "秒";
    }

    public static String formatDateTime(long epochMillis, String timezone, String dateFormat, String timeFormat) {
        if (epochMillis <= 0L) {
            return "未知";
        }
        try {
            ZoneId zone = ZoneId.of(timezone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat + " " + timeFormat).withZone(zone);
            return formatter.format(Instant.ofEpochMilli(epochMillis));
        } catch (Exception ignored) {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(epochMillis));
        }
    }
}
