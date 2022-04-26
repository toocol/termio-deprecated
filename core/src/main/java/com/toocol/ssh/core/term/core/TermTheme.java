package com.toocol.ssh.core.term.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:11
 * @version: 0.0.1
 */
public enum TermTheme {
    DARK_THEME("dark", 234, 233, 43, 43, 229, 0, 15, 105),
    LIGHT_THEME("light",231, 231, 36, 36, 31, 15, 0, 105),
    ;
    public final String name;
    public final int backgroundColor;
    public final int transitionBackground;
    public final int commandHighlightColor;
    public final int sessionAliveColor;
    public final int hostHighlightColor;
    public final int infoBarFront;
    public final int infoBarBackground;
    public final int connectionPrompt;

    public static TermTheme nameOf(String name) {
        for (TermTheme theme : values()) {
            if (theme.name.equals(name)) {
                return theme;
            }
        }
        return null;
    }

    TermTheme(String name, int backgroundColor, int transitionBackground, int commandHighlightColor, int sessionAliveColor, int hostHighlightColor, int infoBarFront, int infoBarBackground, int connectionPrompt) {
        this.name = name;
        this.backgroundColor = backgroundColor;
        this.transitionBackground = transitionBackground;
        this.commandHighlightColor = commandHighlightColor;
        this.sessionAliveColor = sessionAliveColor;
        this.hostHighlightColor = hostHighlightColor;
        this.infoBarFront = infoBarFront;
        this.infoBarBackground = infoBarBackground;
        this.connectionPrompt = connectionPrompt;
    }
}
