package com.pixelmc.pixelmcmanager.text;

import com.pixelmc.pixelmcmanager.config.WelcomeConfig;
import net.minecraft.network.chat.Style;
import org.slf4j.LoggerFactory;

/** Exercises the actual MC26 chat API without starting a game instance. */
public final class TextParityTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        var config = WelcomeConfig.defaults();
        var parser = new TextTemplateParser(LoggerFactory.getLogger("text-check"));
        check(parser.parse("Hello &z世界 😀", config).getString().equals("Hello &z世界 😀"), "literal unsupported codes and Unicode survive");
        var gradient = GradientParser.render("A😀B", 0x112233, 0xAABBCC, Style.EMPTY.withBold(true));
        check(gradient.getString().equals("A😀B"), "gradient text preserved");
        check(gradient.getSiblings().size() == 3, "supplementary Unicode is one gradient character");
        check(gradient.getSiblings().get(0).getStyle().getColor().getValue() == 0x112233, "gradient first endpoint");
        check(gradient.getSiblings().get(2).getStyle().getColor().getValue() == 0xAABBCC, "gradient final endpoint");
        check(gradient.getSiblings().stream().allMatch(c -> c.getStyle().isBold()), "gradient inherits surrounding formatting");
        var text = parser.parse("&#123456A<gradient:#112233:#AABBCC>😀B</gradient>&rC", config);
        check(text.getString().equals("A😀BC"), "HEX, gradient and reset preserve exact text");
        check(text.getSiblings().get(0).getStyle().getColor().getValue() == 0x123456, "HEX foreground works");
        check(text.getSiblings().get(text.getSiblings().size()-1).getStyle().getColor() == null, "reset clears color");
        check(parser.parse("<gradient:invalid>keep</gradient>", config).getString().equals("<gradient:invalid>keep</gradient>"), "malformed markup is not lost");
        System.out.println("MC26 text parity checks passed: " + checks);
    }
}
