package com.toocol.termio.core.term.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:11
 * @version: 0.0.1
 */
public enum TermTheme {
    DARK_THEME(
            "dark",
            234,
            233,
            233,
            0,
            235,
            233,
            233,
            72,
            72,
            245,
            167,
            223,
            223,
            0,
            250,
            105,
            231,
            167,
            36,
            167,
            231,
            231,
            234,
            234,
            235,
            216,
            232
    ),
    LIGHT_THEME(
            "light",
            231,
            231,
            230,
            231,
            231,
            231,
            231,
            36,
            36,
            250,
            167,
            31,
            31,
            15,
            0,
            105,
            235,
            167,
            36,
            167,
            235,
            235,
            231,
            231,
            231,
            216,
            235
    ),
    ;
    public final String name;
    public final int groupActiveBgColor;
    public final int groupIdleBgColor;
    public final int groupSplitBgColor;
    public final int propertiesZoneBgColor;
    public final int executeBackgroundColor;
    public final int transitionBackgroundColor;
    public final int displayBackGroundColor;
    public final int commandHighlightColor;
    public final int sessionAliveColor;
    public final int indexFrontColor;
    public final int userHighlightColor;
    public final int atHighlightColor;
    public final int hostHighlightColor;
    public final int infoBarFrontColor;
    public final int infoBarBackgroundColor;
    public final int connectionPromptColor;
    public final int executeFrontColor;
    public final int errorMsgColor;
    public final int activeSuccessMsgColor;
    public final int activeFailedMsgColor;
    public final int lcCmdExecuteHighlightColor;
    public final int switchSessionHeadFrontColor;
    public final int switchSessionHeadBgColor;
    public final int switchSessionPanelBodyBgColor;
    public final int switchSessionPanelBottomBgColor;
    public final int switchSessionChosenBgColor;
    public final int switchSessionChosenFrontColor;

    TermTheme(
            String name,
            int groupActiveBgColor,
            int groupIdleBgColor,
            int groupSplitBgColor, int propertiesZoneBgColor,
            int executeBackgroundColor,
            int transitionBackgroundColor,
            int displayBackGroundColor,
            int commandHighlightColor,
            int sessionAliveColor,
            int indexFrontColor,
            int userHighlightColor,
            int atHighlightColor,
            int hostHighlightColor,
            int infoBarFrontColor,
            int infoBarBackgroundColor,
            int connectionPromptColor,
            int executeFrontColor,
            int errorMsgColor,
            int activeSuccessMsgColor,
            int activeFailedMsgColor,
            int lcCmdExecuteHighlightColor,
            int switchSessionHeadFrontColor,
            int switchSessionHeadBgColor,
            int switchSessionPanelBodyBgColor,
            int switchSessionPanelBottomBgColor,
            int switchSessionChosenBgColor,
            int switchSessionChosenFrontColor
    ) {
        this.name = name;
        this.groupActiveBgColor = groupActiveBgColor;
        this.groupIdleBgColor = groupIdleBgColor;
        this.groupSplitBgColor = groupSplitBgColor;
        this.propertiesZoneBgColor = propertiesZoneBgColor;
        this.executeBackgroundColor = executeBackgroundColor;
        this.transitionBackgroundColor = transitionBackgroundColor;
        this.displayBackGroundColor = displayBackGroundColor;
        this.commandHighlightColor = commandHighlightColor;
        this.sessionAliveColor = sessionAliveColor;
        this.indexFrontColor = indexFrontColor;
        this.userHighlightColor = userHighlightColor;
        this.atHighlightColor = atHighlightColor;
        this.hostHighlightColor = hostHighlightColor;
        this.infoBarFrontColor = infoBarFrontColor;
        this.infoBarBackgroundColor = infoBarBackgroundColor;
        this.connectionPromptColor = connectionPromptColor;
        this.executeFrontColor = executeFrontColor;
        this.errorMsgColor = errorMsgColor;
        this.activeSuccessMsgColor = activeSuccessMsgColor;
        this.activeFailedMsgColor = activeFailedMsgColor;
        this.lcCmdExecuteHighlightColor = lcCmdExecuteHighlightColor;
        this.switchSessionHeadFrontColor = switchSessionHeadFrontColor;
        this.switchSessionHeadBgColor = switchSessionHeadBgColor;
        this.switchSessionPanelBodyBgColor = switchSessionPanelBodyBgColor;
        this.switchSessionPanelBottomBgColor = switchSessionPanelBottomBgColor;
        this.switchSessionChosenBgColor = switchSessionChosenBgColor;
        this.switchSessionChosenFrontColor = switchSessionChosenFrontColor;
    }

    public static TermTheme nameOf(String name) {
        for (TermTheme theme : values()) {
            if (theme.name.equals(name)) {
                return theme;
            }
        }
        return null;
    }
}
