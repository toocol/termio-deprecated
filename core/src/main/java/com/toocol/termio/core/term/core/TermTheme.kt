package com.toocol.termio.core.term.core

import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.Color
import com.toocol.termio.utilities.ansi.Color.Companion.of
import java.util.*
import java.util.stream.Collectors

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:11
 * @version: 0.0.1
 */
enum class TermTheme(
    @JvmField val themeName: String,
    @JvmField val groupActiveBgColor: Color,
    @JvmField val groupIdleBgColor: Color,
    @JvmField val groupSplitBgColor: Color,
    @JvmField val propertiesZoneBgColor: Color,
    @JvmField val executeBackgroundColor: Color,
    @JvmField val transitionBackgroundColor: Color,
    @JvmField val displayBackGroundColor: Color,
    @JvmField val commandHighlightColor: Color,
    @JvmField val sessionAliveColor: Color,
    @JvmField val indexFrontColor: Color,
    @JvmField val userHighlightColor: Color,
    @JvmField val atHighlightColor: Color,
    @JvmField val hostHighlightColor: Color,
    @JvmField val infoBarFrontColor: Color,
    @JvmField val infoBarBackgroundColor: Color,
    @JvmField val connectionPromptColor: Color,
    @JvmField val executeFrontColor: Color,
    @JvmField val errorMsgColor: Color,
    @JvmField val activeSuccessMsgColor: Color,
    @JvmField val activeFailedMsgColor: Color,
    @JvmField val lcCmdExecuteHighlightColor: Color,
    @JvmField val switchSessionHeadFrontColor: Color,
    @JvmField val switchSessionHeadBgColor: Color,
    @JvmField val switchSessionPanelBodyBgColor: Color,
    @JvmField val switchSessionPanelBottomBgColor: Color,
    @JvmField val switchSessionChosenBgColor: Color,
    @JvmField val switchSessionChosenFrontColor: Color
) {
    DARK_THEME(
        "dark",
        of("gabc", 234, "group active background color"),
        of("gibc", 233, "group idle background color"),
        of("gsbc", 233, "group split background color"),
        of("pzbc", 0, "properties zone background color"),
        of("ebc", 234, "execute background color"),
        of("tbc", 233, "transition background color"),
        of("dbc", 233, "display background color"),
        of("chc", 43, "command highlight color"),
        of("sac", 43, "session alive color"),
        of("ifc", 245, "index front color"),
        of("uhc", 253, "user highlight color"),
        of("ahc", 229, "at highlight color"),
        of("hhc", 229, "host highlight color"),
        of("ibfc", 0, "info bar front color"),
        of("ibbc", 255, "info bar background color"),
        of("cpc", 105, "connection prompt color"),
        of("efc", 231, "execute front color"),
        of("emc", 167, "error msg color"),
        of("asmc", 36, "active success msg color"),
        of("afmc", 167, "active failed msg color"),
        of("lcehc", 231, "lc cmd execute highlight color"),
        of("sshfc", 231, "switch session head front color"),
        of("sshbc", 234, "switch session head background color"),
        of("sspbbc", 234, "switch session panel body background color"),
        of("ssbmbc", 235, "switch session panel bottom background color"),
        of("sscbc", 216, "switch session chosen background color"),
        of("sscfc", 232, "switch session chosen front color")
    ),
    GRUVBOX_THEME(
        "gruvbox",
        of("gabc", 234, "group active background color"),
        of("gibc", 233, "group idle background color"),
        of("gsbc", 233, "group split background color"),
        of("pzbc", 0, "properties zone background color"),
        of("ebc", 235, "execute background color"),
        of("tbc", 233, "transition background color"),
        of("dbc", 233, "display background color"),
        of("chc", 72, "command highlight color"),
        of("sac", 72, "session alive color"),
        of("ifc", 245, "index front color"),
        of("uhc", 167, "user highlight color"),
        of("ahc", 223, "at highlight color"),
        of("hhc", 223, "host highlight color"),
        of("ibfc", 0, "info bar front color"),
        of("ibbc", 250, "info bar background color"),
        of("cpc", 105, "connection prompt color"),
        of("efc", 231, "execute front color"),
        of("emc", 167, "error msg color"),
        of("asmc", 36, "active success msg color"),
        of("afmc", 167, "active failed msg color"),
        of("lcehc", 231, "lc cmd execute highlight color"),
        of("sshfc", 231, "switch session head front color"),
        of("sshbc", 234, "switch session head background color"),
        of("sspbbc", 234, "switch session panel body background color"),
        of("ssbmbc", 235, "switch session panel bottom background color"),
        of("sscbc", 216, "switch session chosen background color"),
        of("sscfc", 232, "switch session chosen front color")
    ),
    LIGHT_THEME(
        "light",
        of("gabc", 231, "group active background color"),
        of("gibc", 231, "group idle background color"),
        of("gsbc", 230, "group split background color"),
        of("pzbc", 231, "properties zone background color"),
        of("ebc", 231, "execute background color"),
        of("tbc", 231, "transition background color"),
        of("dbc", 231, "display background color"),
        of("chc", 36, "command highlight color"),
        of("sac", 36, "session alive color"),
        of("ifc", 250, "index front color"),
        of("uhc", 167, "user highlight color"),
        of("ahc", 31, "at highlight color"),
        of("hhc", 31, "host highlight color"),
        of("ibfc", 15, "info bar front color"),
        of("ibbc", 0, "info bar background color"),
        of("cpc", 105, "connection prompt color"),
        of("efc", 235, "execute front color"),
        of("emc", 167, "error msg color"),
        of("asmc", 36, "active success msg color"),
        of("afmc", 167, "active failed msg color"),
        of("lcehc", 235, "lc cmd execute highlight color"),
        of("sshfc", 235, "switch session head front color"),
        of("sshbc", 231, "switch session head background color"),
        of("sspbbc", 231, "switch session panel body background color"),
        of("ssbmbc", 231, "switch session panel bottom background color"),
        of("sscbc", 216, "switch session chosen background color"),
        of("sscfc", 232, "switch session chosen front color")
    );

    fun colorPanel(): String {
        val colorSet: MutableSet<Int> = TreeSet()
        try {
            for (field in this.javaClass.declaringClass.declaredFields) {
                if (field.name.endsWith("Color")) {
                    field.isAccessible = true
                    val color = field[this] as Color
                    colorSet.add(color.color)
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
        val builder = AnsiStringBuilder()
        return colorSet.stream().map { color: Int? ->
            val cp = builder.background(color!!).space(2).toString()
            builder.clearColor().clearStr()
            cp
        }.collect(Collectors.joining())
    }

    companion object {
        @JvmStatic
        fun nameOf(name: String): TermTheme? {
            for (theme in values()) {
                if (theme.themeName == name) {
                    return theme
                }
            }
            return null
        }

        @JvmStatic
        fun listTheme(): String {
            val builder = AnsiStringBuilder()
            return Arrays.stream(values()).map { theme: TermTheme ->
                val str = builder.append(theme.themeName)
                    .space(12 - theme.themeName.length)
                    .append(theme.colorPanel())
                    .toString()
                builder.clearStr().clearColor()
                str
            }.collect(Collectors.joining("\n"))
        }
    }
}