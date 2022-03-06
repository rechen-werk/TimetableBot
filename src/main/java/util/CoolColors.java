package util;

import java.awt.*;

/**
 * A data-class that just holds static Colors that I liked and thought might be useful to paint the schedule
 * This class holds 6 dark, 6 neon and 6 pastel colors.
 * This class extends Color so the colors in Color can be accessed as well.
 * There is also a method to get a random pastel/neon/dark color.
 */
public class CoolColors extends Color {
    // Neon colors.
    public final static Color NEON_PINK = new Color(0xDA34B1);
    public final static Color NEON_AQUA = new Color(0x08F7FE);
    public final static Color NEON_GREEN = new Color(0x0CFF0C);
    public final static Color YELLOW = new Color(0xE2DD0B);
    public final static Color SOME_BLUE = new Color(0x0743EC);
    public final static Color ORANGE = new Color(0xFF8702);

    @SuppressWarnings("unused")
    public static Color getRandomNeon() { return getRandomColor(NEON_PINK, NEON_AQUA, NEON_GREEN, YELLOW, SOME_BLUE, ORANGE); }

    // Pastel colors.
    public final static Color AQUAMARINE = new Color(0x8DE8C0);
    public final static Color SKYBLUE = new Color(0x87ceeb);
    public final static Color PASTEL_GREEN = new Color(0xA5CE85);
    public final static Color LIGHTPINK = new Color(0xFFB6C1);
    public final static Color VIOLET = new Color(0xEE82EE);
    public final static Color WHEAT = new Color(0xFFE7BA);

    @SuppressWarnings("unused")
    public static Color getRandomPastel() { return getRandomColor(AQUAMARINE, SKYBLUE, PASTEL_GREEN, LIGHTPINK, VIOLET, WHEAT); }

    // Dark colors.
    public final static Color PURPLE = new Color(0x500057);
    public final static Color DEEP_OCEAN = new Color(0x125366);
    public final static Color BROWN = new Color(0x493B20);
    public final static Color BRICK = new Color(0x870E0E);
    public final static Color PRUSSIAN = new Color(0x003153);
    public final static Color OLIVE = new Color(0x0F6600);

    @SuppressWarnings("unused")
    public static Color getRandomDark() { return getRandomColor(PURPLE, DEEP_OCEAN, BROWN, BRICK, PRUSSIAN, OLIVE); }

    public CoolColors(int rgb) {
        super(rgb);
    }

    // Helper method to pick a "random" color
    // with the touch that it is not completely random, because the same color can't be picked twice in a row.
    private static int lastColor;
    private static Color getRandomColor(Color a, Color b, Color c, Color d, Color e, Color f) {
        int col;
        if(Math.random() >0.7 ) {
            col = ++lastColor % 6;
        } else {
            col = (int) (Math.random()*6);
            if(col == lastColor) col = (col + 1) % 6;
        }
        lastColor = col;
        return switch (col) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            case 4 -> e;
            case 5 -> f;
            default -> BLACK;
        };
    }
}
