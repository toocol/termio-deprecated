package com.toocol.termio.utilities.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Char util class<br>
 * some util comes from Apache Commons
 *
 * @author Joezeo
 */
public final class CharUtil {
    /**
     * {@code up-arrow} translated
     */
    public static final char UP_ARROW = '\uAAAA';
    /**
     * {@code down-arrow} translated
     */
    public static final char DOWN_ARROW = '\uBBBB';
    /**
     * {@code down-arrow} translated
     */
    public static final char RIGHT_ARROW = '\uCCCC';
    /**
     * {@code up-arrow} translated
     */
    public static final char LEFT_ARROW = '\uDDDD';
    /**
     * {@code Ctrl+A}
     */
    public static final char CTRL_A = '\u0001';
    /**
     * {@code Ctrl+B}
     */
    public static final char CTRL_B = '\u0002';
    /**
     * {@code Ctrl+C}
     */
    public static final char CTRL_C = '\u0003';
    /**
     * {@code Ctrl+D}
     */
    public static final char CTRL_D = '\u0004';
    /**
     * {@code Ctrl+E}
     */
    public static final char CTRL_E = '\u0005';
    /**
     * {@code Ctrl+F}
     */
    public static final char CTRL_F = '\u0006';
    /**
     * {@code Ctrl+G}
     */
    public static final char CTRL_G = '\u0007';
    /**
     * {@code Ctrl+H}
     */
    public static final char CTRL_H = '\b';
    /**
     * {@code Ctrl+I}
     */
    public static final char CTRL_I = '\t';
    /**
     * {@code Ctrl+J}
     */
    public static final char CTRL_J = '\n';
    /**
     * {@code Ctrl+K}
     */
    public static final char CTRL_K = '\u000B';
    /**
     * {@code Ctrl+L}
     */
    public static final char CTRL_L = '\f';
    /**
     * {@code Ctrl+M}
     */
    public static final char CTRL_M = '\r';
    /**
     * {@code Ctrl+N}
     */
    public static final char CTRL_N = '\u000E';
    /**
     * {@code Ctrl+O}
     */
    public static final char CTRL_O = '\u000F';
    /**
     * {@code Ctrl+P}
     */
    public static final char CTRL_P = '\u0010';
    /**
     * {@code Ctrl+Q}
     */
    public static final char CTRL_Q = '\u0011';
    /**
     * {@code Ctrl+R}
     */
    public static final char CTRL_R = '\u0012';
    /**
     * {@code Ctrl+S}
     */
    public static final char CTRL_S = '\u0013';
    /**
     * {@code Ctrl+T}
     */
    public static final char CTRL_T = '\u0014';
    /**
     * {@code Ctrl+U}
     */
    public static final char CTRL_U = '\u0015';
    /**
     * {@code Ctrl+V}
     */
    public static final char CTRL_V = '\u0016';
    /**
     * {@code Ctrl+W}
     */
    public static final char CTRL_W = '\u0017';
    /**
     * {@code Ctrl+X}
     */
    public static final char CTRL_X = '\u0018';
    /**
     * {@code Ctrl+Y}
     */
    public static final char CTRL_Y = '\u0019';
    /**
     * {@code Ctrl+Z}
     */
    public static final char CTRL_Z = '\u001A';
    /**
     * {@code backspace}
     */
    public static final char BACKSPACE = '\b';
    /**
     * {@code ' '}
     */
    public static final char SPACE = ' ';
    /**
     * {@code '\t'}
     */
    public static final char TAB = '\t';
    /**
     * {@code '.'}
     */
    public static final char DOT = '.';
    /**
     * {@code '/'}
     */
    public static final char SLASH = '/';
    /**
     * {@code '\\'}
     */
    public static final char BACKSLASH = '\\';
    /**
     * {@code '\r'}
     */
    public static final char CR = '\r';
    /**
     * {@code '\n'}
     */
    public static final char LF = '\n';
    /**
     * {@code '�'}
     */
    public static final char UNKNOWN = '�';
    /**
     * {@code '-'}
     */
    public static final char DASHED = '-';
    /**
     * {@code '_'}
     */
    public static final char UNDERLINE = '_';
    /**
     * {@code ','}
     */
    public static final char COMMA = ',';
    /**
     * <code>'{'</code>
     */
    public static final char DELIM_START = '{';
    /**
     * <code>'}'</code>
     */
    public static final char DELIM_END = '}';
    /**
     * <code>Esc</code>
     */
    public static final char ESCAPE = '\u001B';
    /**
     * {@code '['}
     */
    public static final char BRACKET_START = '[';
    /**
     * {@code ']'}
     */
    public static final char BRACKET_END = ']';
    /**
     * {@code '"'}
     */
    public static final char DOUBLE_QUOTES = '"';
    /**
     * {@code '\''}
     */
    public static final char SINGLE_QUOTE = '\'';
    /**
     * {@code '&'}
     */
    public static final char AMP = '&';
    /**
     * {@code ':'}
     */
    public static final char COLON = ':';
    /**
     * <code>'@'</code>
     */
    public static final char AT = '@';

    /**
     * whether is ascii，ascii between 0~127
     *
     * <pre>
     *   CharUtil.isAscii('a')  = true
     *   CharUtil.isAscii('A')  = true
     *   CharUtil.isAscii('3')  = true
     *   CharUtil.isAscii('-')  = true
     *   CharUtil.isAscii('\n') = true
     *   CharUtil.isAscii('&copy;') = false
     * </pre>
     *
     * @param ch char
     * @return true-ASCII，ASCII is between 0~127
     */
    public static boolean isAscii(char ch) {
        return ch < 128;
    }

    public static boolean isLeftOrRightArrow(char ch) {
        return ch == LEFT_ARROW || ch == RIGHT_ARROW;
    }

    /**
     * whether is visible ascii， visible ascii is between 0~127
     *
     * <pre>
     *   CharUtil.isAsciiPrintable('a')  = true
     *   CharUtil.isAsciiPrintable('A')  = true
     *   CharUtil.isAsciiPrintable('3')  = true
     *   CharUtil.isAsciiPrintable('-')  = true
     *   CharUtil.isAsciiPrintable('\n') = false
     *   CharUtil.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch char
     * @return true-visible ASCII，is between 32~126
     */
    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * whether is ASCII control char（invisible char），is between 0~31 nad 127
     *
     * <pre>
     *   CharUtil.isAsciiControl('a')  = false
     *   CharUtil.isAsciiControl('A')  = false
     *   CharUtil.isAsciiControl('3')  = false
     *   CharUtil.isAsciiControl('-')  = false
     *   CharUtil.isAsciiControl('\n') = true
     *   CharUtil.isAsciiControl('&copy;') = false
     * </pre>
     *
     * @param ch char
     * @return true-control char，is between 0~31 and 127
     */
    public static boolean isAsciiControl(final char ch) {
        return ch < 32 || ch == 127;
    }

    public static boolean isLetter(char ch) {
        return isLetterUpper(ch) || isLetterLower(ch);
    }

    public static boolean isLetterUpper(final char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isLetterLower(final char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isNumber(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isHexChar(char c) {
        return isNumber(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    public static boolean isLetterOrNumber(final char ch) {
        return isLetter(ch) || isNumber(ch);
    }

    public static String toString(char c) {
        return ASCIIStrCache.toString(c);
    }

    public static boolean isCharClass(Class<?> clazz) {
        return clazz == Character.class || clazz == char.class;
    }

    public static boolean isChar(Object value) {
        //noinspection ConstantConditions
        return value instanceof Character || value.getClass() == char.class;
    }

    public static boolean isBlankChar(char c) {
        return isBlankChar((int) c);
    }

    public static boolean isBlankChar(int c) {
        return Character.isWhitespace(c)
                || Character.isSpaceChar(c)
                || c == '\ufeff'
                || c == '\u202a';
    }

    public static boolean isEmoji(char c) {
        //noinspection ConstantConditions
        return !((c == 0x0) ||
                (c == 0x9) ||
                (c == 0xA) ||
                (c == 0xD) ||
                ((c >= 0x20) && (c <= 0xD7FF)) ||
                ((c >= 0xE000) && (c <= 0xFFFD)) ||
                ((c >= 0x100000) && (c <= 0x10FFFF)));
    }

    public static boolean isChinese(int c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    public static byte[] charToBytes(char[] chars) {
        Charset charset = StandardCharsets.UTF_8;
        CharBuffer charBuffer = CharBuffer.allocate(chars.length);
        charBuffer.put(chars);
        charBuffer.flip();
        ByteBuffer byteBuffer = charset.encode(charBuffer);
        return byteBuffer.array();
    }

    public static boolean isFileSeparator(char c) {
        return SLASH == c || BACKSLASH == c;
    }

    public static boolean equals(char c1, char c2, boolean ignoreCase) {
        if (ignoreCase) {
            return Character.toLowerCase(c1) == Character.toLowerCase(c2);
        }
        return c1 == c2;
    }

    public static int getType(int c) {
        return Character.getType(c);
    }

    public static int digit16(int b) {
        return Character.digit(b, 16);
    }
}
