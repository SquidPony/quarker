package net.slashie.libjcsi;

import java.util.Objects;

public class CSIColor {
    private final int r;
    private final int g;
    private final int b;

    public static final CSIColor WHITE = new CSIColor(255, 255, 255);
    public static final CSIColor BLACK = new CSIColor(0, 0, 0);
    public static final CSIColor BLUE = new CSIColor(65, 105, 225);
    public static final CSIColor CYAN = new CSIColor(0, 200, 200);
    public static final CSIColor GREEN = new CSIColor(80, 200, 120);
    public static final CSIColor YELLOW = new CSIColor(255, 215, 0);
    public static final CSIColor AMBER = new CSIColor(255, 191, 0);
    public static final CSIColor ALICE_BLUE = new CSIColor(240, 248, 255);
    public static final CSIColor RED_PIGMENT = new CSIColor(237, 28, 36);
    public static final CSIColor MAGENTA_DYE = new CSIColor(202, 31, 123);
    public static final CSIColor ATOMIC_TANGERINE = new CSIColor(255, 153, 102);
    public static final CSIColor VEGAS_GOLD = new CSIColor(197, 179, 88);
    public static final CSIColor SAFETY_ORANGE = new CSIColor(255, 103, 0);
    public static final CSIColor VERMILION = new CSIColor(227, 66, 52);

    public CSIColor(int packedRgb) {
        this((packedRgb >> 16) & 0xFF, (packedRgb >> 8) & 0xFF, packedRgb & 0xFF);
    }

    public CSIColor(int r, int g, int b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getColor() {
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CSIColor)) {
            return false;
        }
        CSIColor c = (CSIColor) o;
        return r == c.r && g == c.g && b == c.b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b);
    }
}