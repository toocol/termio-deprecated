package com.toocol.termio.utilities.anis;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/2 14:32
 */
public class Color {
    public final String name;
    public final String shortcut;
    public int color;

    public Color(String shortcut, int color, String name) {
        this.name = name;
        this.shortcut = shortcut;
        this.color = color;
    }

    public static Color of(String shortcut, int color, String name) {
        return new Color(shortcut, color, name);
    }
}
