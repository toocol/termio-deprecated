package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.Color;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:11
 * @version: 0.0.1
 */
public enum TermTheme {
    DARK_THEME(
            "dark",
            Color.of("gabc", 234, "group active background color"),
            Color.of("gibc", 233, "group idle background color"),
            Color.of("gsbc", 233, "group split background color"),
            Color.of("pzbc", 0, "properties zone background color"),
            Color.of("ebc", 234, "execute background color"),
            Color.of("tbc", 233, "transition background color"),
            Color.of("dbc", 233, "display background color"),
            Color.of("chc", 43, "command highlight color"),
            Color.of("sac", 43, "session alive color"),
            Color.of("ifc", 245, "index front color"),
            Color.of("uhc", 253, "user highlight color"),
            Color.of("ahc", 229, "at highlight color"),
            Color.of("hhc", 229, "host highlight color"),
            Color.of("ibfc", 0, "info bar front color"),
            Color.of("ibbc", 255, "info bar background color"),
            Color.of("cpc", 105, "connection prompt color"),
            Color.of("efc", 231, "execute front color"),
            Color.of("emc", 167, "error msg color"),
            Color.of("asmc", 36, "active success msg color"),
            Color.of("afmc", 167, "active failed msg color"),
            Color.of("lcehc", 231, "lc cmd execute highlight color"),
            Color.of("sshfc", 231, "switch session head front color"),
            Color.of("sshbc", 234, "switch session head background color"),
            Color.of("sspbbc", 234, "switch session panel body background color"),
            Color.of("ssbmbc", 235, "switch session panel bottom background color"),
            Color.of("sscbc", 216, "switch session chosen background color"),
            Color.of("sscfc", 232, "switch session chosen front color")
    ),
    GRUVBOX_THEME(
            "gruvbox",
            Color.of("gabc", 234, "group active background color"),
            Color.of("gibc", 233, "group idle background color"),
            Color.of("gsbc", 233, "group split background color"),
            Color.of("pzbc", 0, "properties zone background color"),
            Color.of("ebc", 235, "execute background color"),
            Color.of("tbc", 233, "transition background color"),
            Color.of("dbc", 233, "display background color"),
            Color.of("chc", 72, "command highlight color"),
            Color.of("sac", 72, "session alive color"),
            Color.of("ifc", 245, "index front color"),
            Color.of("uhc", 167, "user highlight color"),
            Color.of("ahc", 223, "at highlight color"),
            Color.of("hhc", 223, "host highlight color"),
            Color.of("ibfc", 0, "info bar front color"),
            Color.of("ibbc", 250, "info bar background color"),
            Color.of("cpc", 105, "connection prompt color"),
            Color.of("efc", 231, "execute front color"),
            Color.of("emc", 167, "error msg color"),
            Color.of("asmc", 36, "active success msg color"),
            Color.of("afmc", 167, "active failed msg color"),
            Color.of("lcehc", 231, "lc cmd execute highlight color"),
            Color.of("sshfc", 231, "switch session head front color"),
            Color.of("sshbc", 234, "switch session head background color"),
            Color.of("sspbbc", 234, "switch session panel body background color"),
            Color.of("ssbmbc", 235, "switch session panel bottom background color"),
            Color.of("sscbc", 216, "switch session chosen background color"),
            Color.of("sscfc", 232, "switch session chosen front color")
    ),
    LIGHT_THEME(
            "light",
            Color.of("gabc", 231, "group active background color"),
            Color.of("gibc", 231, "group idle background color"),
            Color.of("gsbc", 230, "group split background color"),
            Color.of("pzbc", 231, "properties zone background color"),
            Color.of("ebc", 231, "execute background color"),
            Color.of("tbc", 231, "transition background color"),
            Color.of("dbc", 231, "display background color"),
            Color.of("chc", 36, "command highlight color"),
            Color.of("sac", 36, "session alive color"),
            Color.of("ifc", 250, "index front color"),
            Color.of("uhc", 167, "user highlight color"),
            Color.of("ahc", 31, "at highlight color"),
            Color.of("hhc", 31, "host highlight color"),
            Color.of("ibfc", 15, "info bar front color"),
            Color.of("ibbc", 0, "info bar background color"),
            Color.of("cpc", 105, "connection prompt color"),
            Color.of("efc", 235, "execute front color"),
            Color.of("emc", 167, "error msg color"),
            Color.of("asmc", 36, "active success msg color"),
            Color.of("afmc", 167, "active failed msg color"),
            Color.of("lcehc", 235, "lc cmd execute highlight color"),
            Color.of("sshfc", 235, "switch session head front color"),
            Color.of("sshbc", 231, "switch session head background color"),
            Color.of("sspbbc", 231, "switch session panel body background color"),
            Color.of("ssbmbc", 231, "switch session panel bottom background color"),
            Color.of("sscbc", 216, "switch session chosen background color"),
            Color.of("sscfc", 232, "switch session chosen front color")
    ),
    ;
    public final String name;
    public final Color groupActiveBgColor;
    public final Color groupIdleBgColor;
    public final Color groupSplitBgColor;
    public final Color propertiesZoneBgColor;
    public final Color executeBackgroundColor;
    public final Color transitionBackgroundColor;
    public final Color displayBackGroundColor;
    public final Color commandHighlightColor;
    public final Color sessionAliveColor;
    public final Color indexFrontColor;
    public final Color userHighlightColor;
    public final Color atHighlightColor;
    public final Color hostHighlightColor;
    public final Color infoBarFrontColor;
    public final Color infoBarBackgroundColor;
    public final Color connectionPromptColor;
    public final Color executeFrontColor;
    public final Color errorMsgColor;
    public final Color activeSuccessMsgColor;
    public final Color activeFailedMsgColor;
    public final Color lcCmdExecuteHighlightColor;
    public final Color switchSessionHeadFrontColor;
    public final Color switchSessionHeadBgColor;
    public final Color switchSessionPanelBodyBgColor;
    public final Color switchSessionPanelBottomBgColor;
    public final Color switchSessionChosenBgColor;
    public final Color switchSessionChosenFrontColor;

    TermTheme(
            String name,
            Color groupActiveBgColor,
            Color groupIdleBgColor,
            Color groupSplitBgColor,
            Color propertiesZoneBgColor,
            Color executeBackgroundColor,
            Color transitionBackgroundColor,
            Color displayBackGroundColor,
            Color commandHighlightColor,
            Color sessionAliveColor,
            Color indexFrontColor,
            Color userHighlightColor,
            Color atHighlightColor,
            Color hostHighlightColor,
            Color infoBarFrontColor,
            Color infoBarBackgroundColor,
            Color connectionPromptColor,
            Color executeFrontColor,
            Color errorMsgColor,
            Color activeSuccessMsgColor,
            Color activeFailedMsgColor,
            Color lcCmdExecuteHighlightColor,
            Color switchSessionHeadFrontColor,
            Color switchSessionHeadBgColor,
            Color switchSessionPanelBodyBgColor,
            Color switchSessionPanelBottomBgColor,
            Color switchSessionChosenBgColor,
            Color switchSessionChosenFrontColor
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

    public static String listTheme() {
        AnsiStringBuilder builder = new AnsiStringBuilder();
        return Arrays.stream(values()).map(theme -> {
            String str = builder.append(theme.name)
                    .space(12 - theme.name.length())
                    .append(theme.colorPanel())
                    .toString();
            builder.clearStr().clearColor();
            return str;
        }).collect(Collectors.joining("\n"));
    }

    public String colorPanel() {
        Set<Integer> colorSet = new TreeSet<>();
        try {
            for (Field field : this.getDeclaringClass().getDeclaredFields()) {
                if (field.getName().endsWith("Color")) {
                    field.setAccessible(true);
                    Color color = (Color) field.get(this);
                    colorSet.add(color.color);
                }
            }
        } catch (Exception e) {
            // do nothing
        }
        AnsiStringBuilder builder = new AnsiStringBuilder();
        return colorSet.stream().map(color -> {
            String cp = builder.background(color).space(2).toString();
            builder.clearColor().clearStr();
            return cp;
        }).collect(Collectors.joining());
    }
}
