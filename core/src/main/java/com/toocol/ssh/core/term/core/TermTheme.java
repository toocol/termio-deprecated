package com.toocol.ssh.core.term.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:11
 * @version: 0.0.1
 */
public enum TermTheme {
    DARK_THEME(
            "dark",
            234,
            234,
            233,
            43,
            43,
            229,
            0,
            15,
            105,
            231,
            167),
    LIGHT_THEME(
            "light",
            231,
            231,
            231,
            36,
            36,
            31,
            15,
            0,
            105,
            235,
            167),
    ;
    public final String name;
    public final int executeBackgroundColor;
    public final int transitionBackgroundColor;
    public final int displayBackGroundColor;
    public final int commandHighlightColor;
    public final int sessionAliveColor;
    public final int hostHighlightColor;
    public final int infoBarFrontColor;
    public final int infoBarBackgroundColor;
    public final int connectionPromptColor;
    public final int executeFrontColor;
    public final int errorMsgColor;

    public static TermTheme nameOf(String name) {
        for (TermTheme theme : values()) {
            if (theme.name.equals(name)) {
                return theme;
            }
        }
        return null;
    }

    TermTheme(
            String name,
            int executeBackgroundColor,
            int transitionBackgroundColor,
            int displayBackGroundColor,
            int commandHighlightColor,
            int sessionAliveColor,
            int hostHighlightColor,
            int infoBarFrontColor,
            int infoBarBackgroundColor,
            int connectionPromptColor,
            int executeFrontColor,
            int errorMsgColor
    ) {
        this.name = name;
        this.executeBackgroundColor = executeBackgroundColor;
        this.transitionBackgroundColor = transitionBackgroundColor;
        this.displayBackGroundColor = displayBackGroundColor;
        this.commandHighlightColor = commandHighlightColor;
        this.sessionAliveColor = sessionAliveColor;
        this.hostHighlightColor = hostHighlightColor;
        this.infoBarFrontColor = infoBarFrontColor;
        this.infoBarBackgroundColor = infoBarBackgroundColor;
        this.connectionPromptColor = connectionPromptColor;
        this.executeFrontColor = executeFrontColor;
        this.errorMsgColor = errorMsgColor;
    }
}
