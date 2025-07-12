package com.prakhar.j2mepcemu;

import java.awt.Color;

public class ColorUtils {

    /**
     * Calculates the perceived luminance of a color.
     * Uses the formula for sRGB luminance.
     * @param color The color to calculate the luminance of.
     * @return A value between 0 (darkest) and 255 (lightest).
     */
    private static double getLuminance(Color color) {
        return (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue());
    }

    /**
     * Determines whether a color is "dark".
     * @param color The color to check.
     * @return true if the color is dark, false otherwise.
     */
    public static boolean isDark(Color color) {
        return getLuminance(color) < 128;
    }

    /**
     * Returns a contrasting color (black or white) for the given background color.
     * @param backgroundColor The background color.
     * @return Color.BLACK or Color.WHITE.
     */
    public static Color getContrastingTextColor(Color backgroundColor) {
        return isDark(backgroundColor) ? Color.WHITE : Color.BLACK;
    }
}
