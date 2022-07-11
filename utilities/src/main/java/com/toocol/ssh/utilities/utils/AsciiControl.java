package com.toocol.ssh.utilities.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/2 19:00
 * @version: 0.0.1
 */
public class AsciiControl {
    public static final Pattern ESCAPE_CAPTURE_PATTERN = Pattern.compile("[\\w\\u001B\\[\\]].*");
    public static final Pattern BS_CAPTURE_PATTERN = Pattern.compile("[\\w\\u0008\\[\\]].*");
    public static final Pattern PRE_CLEAR_CAPTURE_PATTERN = Pattern.compile("[A-Z]\\u0012[A-Z]\".");

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

    public static final char[] REGEX_CHARS = new char[] {'[', ']', '?', '.', '^', '{', '}', '+', '/'};
    public static final String[][] REPLACES = new String[][]{
            {"\u001B[?25h", "\\u001B\\[\\?25h"},
            {"\u001B\u0012\u0019\"\u0017", "\\u001B\\u0012\\u0019\"\\u0017"}
    };
    public static final String[] IGNORES = new String[]{
            "\u001B[?25l"
    };
    public static final Set<Character> SUPPORTED_CHARACTER = new HashSet<>(){
        {
            add(CharUtil.ESCAPE);
            add(CharUtil.LF);
            add(CharUtil.CR);
        }
    };

    public static String preClear(String source) {
        Matcher matcher = PRE_CLEAR_CAPTURE_PATTERN.matcher(source);
        if (matcher.find()) {
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String match = matcher.group(i);
                for (char regexChar : REGEX_CHARS) {
                    int idx = match.indexOf(regexChar);
                    if (idx != -1) {
                        match = new StringBuilder(match).replace(idx, idx, "\\" + regexChar).toString();
                    }
                }
                source = source.replaceAll(match, "");
            }
            source = source.replaceAll("\\u001B\\[\\?25l", "");
        }
        return source;
    }

    public static String escapeMatch(String source) {
        Matcher matcher = ESCAPE_CAPTURE_PATTERN.matcher(source);
        if (matcher.find()) {
            source = matcher.group();
        }
        return source;
    }

    public static String bsMatch(String source) {
        Matcher matcher = BS_CAPTURE_PATTERN.matcher(source);
        if (matcher.find()) {
            source = matcher.group().replaceAll(BS, "");
        }
        return source;
    }

    public static boolean haveUnsupportedAsciiControl(String source) {
        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (!SUPPORTED_CHARACTER.contains(ch) && CharUtil.isAsciiControl(ch)) {
                return true;
            }
        }
        return false;
    }

    public static boolean haveEscape(String source) {
        return source.contains(ESCAPE);
    }

    public static boolean haveBs(String source) {
        return source.contains(BS);
    }

    public static String ignoreAndReplace(String source) {
        for (String[] replace : REPLACES) {
            if (source.endsWith(replace[0]))
                source = source.replaceAll(replace[1], StrUtil.EMPTY);
        }
        for (String ignore : IGNORES) {
            if (source.endsWith(ignore)) {
                source = StrUtil.EMPTY;
                break;
            }
        }
        return source;
    }
}
