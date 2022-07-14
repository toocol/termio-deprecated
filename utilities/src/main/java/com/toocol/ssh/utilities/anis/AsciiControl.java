package com.toocol.ssh.utilities.anis;

import com.toocol.ssh.utilities.utils.StrUtil;

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

    public static String ignore(String source) {
        for (String[] replace : IGNORES) {
            if (source.endsWith(replace[0]))
                source = source.replaceAll(replace[1], StrUtil.EMPTY);
        }
        return source;
    }
}
