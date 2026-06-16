package com.pixelmc.pixelmcmanager.maintenance;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeArgumentParser {
    private static final Pattern INPUT = Pattern.compile("^([1-9][0-9]*)([smh])$", Pattern.CASE_INSENSITIVE);
    private static final Duration MAX_DURATION = Duration.ofHours(24);

    private TimeArgumentParser() {
    }

    public static Result parse(String input) {
        if (input == null) {
            return Result.failure("时间格式无效，请使用 1s、30s、1m、15m 或 1h。");
        }
        Matcher matcher = INPUT.matcher(input.trim().toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            return Result.failure("时间格式无效，请使用正整数加 s/m/h，例如 30s、15m、1h。");
        }

        long value;
        try {
            value = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            return Result.failure("时间数值过大。");
        }

        Duration duration = switch (matcher.group(2)) {
            case "s" -> Duration.ofSeconds(value);
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            default -> throw new IllegalStateException("Unexpected unit");
        };
        if (duration.compareTo(MAX_DURATION) > 0) {
            return Result.failure("停机预定时间最长为 24h。");
        }
        return Result.success(duration);
    }

    public record Result(boolean success, Duration duration, String errorMessage) {
        public static Result success(Duration duration) {
            return new Result(true, duration, "");
        }

        public static Result failure(String message) {
            return new Result(false, Duration.ZERO, message);
        }
    }
}
