package com.pixelmc.pixelmcmanager.text;

import com.pixelmc.pixelmcmanager.config.WelcomeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextTemplateParser {
    private static final Pattern GRADIENT = Pattern.compile("<gradient:(#[0-9A-Fa-f]{6}):(#[0-9A-Fa-f]{6})>(.*?)</gradient>");

    private final Logger logger;

    public TextTemplateParser(Logger logger) {
        this.logger = logger;
    }

    public Component parse(String input, WelcomeConfig config) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        MutableComponent root = Component.empty();
        Matcher matcher = GRADIENT.matcher(input);
        int cursor = 0;
        while (matcher.find()) {
            if (matcher.start() > cursor) {
                root.append(parseLegacy(input.substring(cursor, matcher.start()), Style.EMPTY));
            }
            root.append(parseGradient(matcher.group(1), matcher.group(2), matcher.group(3)));
            cursor = matcher.end();
        }
        if (cursor < input.length()) {
            root.append(parseLegacy(input.substring(cursor), Style.EMPTY));
        }
        return root;
    }

    private Component parseGradient(String startHex, String endHex, String content) {
        try {
            int start = parseHex(startHex);
            int end = parseHex(endHex);
            List<Integer> codePoints = content.codePoints().boxed().toList();
            MutableComponent result = Component.empty();
            int denominator = Math.max(1, codePoints.size() - 1);
            for (int index = 0; index < codePoints.size(); index++) {
                double ratio = index / (double) denominator;
                int color = interpolate(start, end, ratio);
                String ch = new String(Character.toChars(codePoints.get(index)));
                result.append(Component.literal(ch).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
            }
            return result;
        } catch (RuntimeException exception) {
            logger.warn("Failed to parse PixelMC Manager gradient text.", exception);
            return parseLegacy(content, Style.EMPTY);
        }
    }

    private Component parseLegacy(String input, Style initialStyle) {
        MutableComponent root = Component.empty();
        Style style = initialStyle;
        StringBuilder buffer = new StringBuilder();
        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (current == '&' && index + 1 < input.length()) {
                appendBuffer(root, buffer, style);
                char code = Character.toLowerCase(input.charAt(++index));
                if (code == '#') {
                    int end = Math.min(index + 7, input.length());
                    if (end - index == 7) {
                        String hex = input.substring(index, end);
                        try {
                            style = style.withColor(TextColor.fromRgb(parseHex(hex)));
                            index += 6;
                            continue;
                        } catch (IllegalArgumentException ignored) {
                            buffer.append('&').append(code);
                            continue;
                        }
                    }
                }
                style = applyCode(style, code);
                continue;
            }
            buffer.append(current);
        }
        appendBuffer(root, buffer, style);
        return root;
    }

    private static void appendBuffer(MutableComponent root, StringBuilder buffer, Style style) {
        if (!buffer.isEmpty()) {
            root.append(Component.literal(buffer.toString()).withStyle(style));
            buffer.setLength(0);
        }
    }

    private static Style applyCode(Style style, char code) {
        ChatFormatting formatting = ChatFormatting.getByCode(code);
        if (formatting == null) {
            return style;
        }
        if (formatting == ChatFormatting.RESET) {
            return Style.EMPTY;
        }
        if (isColor(formatting)) {
            return Style.EMPTY.withColor(formatting);
        }
        return switch (formatting) {
            case BOLD -> style.withBold(true);
            case ITALIC -> style.withItalic(true);
            case UNDERLINE -> style.withUnderlined(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case OBFUSCATED -> style.withObfuscated(true);
            default -> style;
        };
    }

    private static boolean isColor(ChatFormatting formatting) {
        return switch (formatting) {
            case BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
                    DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE -> true;
            default -> false;
        };
    }

    private static int parseHex(String hex) {
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        return Integer.parseInt(clean.toUpperCase(Locale.ROOT), 16);
    }

    private static int interpolate(int start, int end, double ratio) {
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;
        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;
        int r = (int) Math.round(sr + (er - sr) * ratio);
        int g = (int) Math.round(sg + (eg - sg) * ratio);
        int b = (int) Math.round(sb + (eb - sb) * ratio);
        return (r << 16) | (g << 8) | b;
    }
}
