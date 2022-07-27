package com.toocol.ssh.utilities.anis;

import com.toocol.ssh.utilities.utils.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/2 19:00
 * @version: 0.0.1
 */
public class AsciiControl {
    public static final String SOH = "\u0001";
    public static final String STX = "\u0002";
    public static final String ETX = "\u0003";
    public static final String EOT = "\u0004";
    public static final String ENQ = "\u0005";
    public static final String ACK = "\u0006";
    public static final String BEL = "\u0007";
    public static final String BS = "\b";
    public static final String HT = "\u0009";
    public static final String LF = "\n";
    public static final String VT = "\u000B";
    public static final String FF = "\u000C";
    public static final String CR = "\r";
    public static final String SO = "\u000E";
    public static final String SI = "\u000F";
    public static final String DLE = "\u0010";
    public static final String DC1 = "\u0011";
    public static final String DC2 = "\u0012";
    public static final String DC3 = "\u0013";
    public static final String DC4 = "\u0014";
    public static final String NAK = "\u0015";
    public static final String SYN = "\u0016";
    public static final String ETB = "\u0017";
    public static final String CAN = "\u0018";
    public static final String EM = "\u0019";
    public static final String SUB = "\u001A";
    public static final String ESCAPE = "\u001B";
    public static final String FS = "\u001C";
    public static final String GS = "\u001D";
    public static final String RS = "\u001E";
    public static final String US = "\u001F";
    public static final String DEL = "\u007F";
    public static final String UNKNOWN = "�";

    // this two ANSI Escape Sequences was useless under Windows console.
    public static final String[][] IGNORES = new String[][]{
            {"\u001B[?25h", "\\u001B\\[\\?25h"},     // make cursor invisible
            {"\u001B[?25l", "\\u001B\\[\\?25l"}      // make cursor visible
    };

    public static final String[] CLEAN_PATTERNS = new String[]{
            "\\u001b\\[#?=?\\??[0-9]*[a-zA-Z]",
            "\\u001b\\[[0-9]+;[0-9]+[a-zA-Z]",
            "\\u001b\\[[0-9]+;[0-9]+;.+m"
    };

    public static final String ANIS_CLEAR_ALL_MODE = "\u001b[0m";

    public static final String ANIS_ESCAPE_POSITION = "\\u001b\\[#?\\??[0-9]*;?[0-9]*[fjklhrABCDEFGJH]";
    public static final String ANIS_ESCAPE_MOSH_ROLLING = "\\u001b\\[0m\\u001b\\[[0-9]*;[0-9]*r\\u001b\\[[0-9]*;[0-9]*H";
    public static final String ANIS_ESCAPE_CURSOR_LOCATION = "\\u001b\\[[0-9]+;{}H";
    public static final String ANIS_ESCAPE_DOUBLE_CURSOR_LOCATION = "\\u001b\\[[0-9]+;[0-9]+H[a-zA-Z0-9_~/]+\\u001b\\[[0-9]+;[0-9]+H";
    public static final String ANIS_ESCAPE_CURSOR_BRACKET_K = "\\u001b\\[[0-9]+;[0-9]+H[a-zA-Z0-9_~/\\]\\[# ]+\\u001b\\[K";
    public static final String ANIS_CURSOR_POSITION = "[0-9]+;[0-9]+";

    public static final Pattern ANIS_ESCAPE_MOSH_ROLLING_PATTERN = Pattern.compile(ANIS_ESCAPE_MOSH_ROLLING);
    public static final Pattern ANIS_ESCAPE_DOUBLE_CURSOR_PATTERN = Pattern.compile(ANIS_ESCAPE_DOUBLE_CURSOR_LOCATION);
    public static final Pattern ANIS_ESCAPE_CURSOR_BRACKET_K_PATTERN = Pattern.compile(ANIS_ESCAPE_CURSOR_BRACKET_K);
    public static final Pattern ANIS_CURSOR_POSITION_PATTERN = Pattern.compile(ANIS_CURSOR_POSITION);

    public static String ignore(String source) {
        for (String[] replace : IGNORES) {
            if (source.contains(replace[0]))
                source = source.replaceAll(replace[1], StrUtil.EMPTY);
        }
        return source;
    }

    public static boolean detectRolling(String msg) {
        Matcher matcher = ANIS_ESCAPE_MOSH_ROLLING_PATTERN.matcher(msg);
        return matcher.find();
    }

    public static String clean(String str) {
        for (String cleanPattern : CLEAN_PATTERNS) {
            str = str.replaceAll(cleanPattern, StrUtil.EMPTY);
        }
        return str;
    }

    public static String cleanPositionAnisEscape(String str) {
        return str.replaceAll(ANIS_ESCAPE_POSITION, StrUtil.EMPTY);
    }

    public static String setCursorToLineHead(int line) {
        return AsciiControl.ESCAPE + "[" + line + ";0H";
    }

    public static int[] extractCursorPosition(String str) {
        Matcher matcher = ANIS_CURSOR_POSITION_PATTERN.matcher(str);
        if (matcher.find()) {
            String[] split = matcher.group(0).split(";");
            return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1])};
        }
        return new int[]{0, 0};
    }

    public static String cleanCursorMode(String str) {
        return str.replaceAll("\\u001b\\[[0-9]+;[0-9]+H", "").replaceAll("\\u001b\\[K", "");
    }
}
