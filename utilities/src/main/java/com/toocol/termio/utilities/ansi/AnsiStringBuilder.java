package com.toocol.termio.utilities.ansi;

import com.toocol.termio.utilities.utils.ASCIIStrCache;
import com.toocol.termio.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/26 19:14
 */
public final class AnsiStringBuilder {
    private final StringBuilder builder = new StringBuilder();

    private ColorMode colorMode = ColorMode.COLOR_256;

    private int bg256 = -1;
    private int ft256 = -1;
    private int bgR = -1;
    private int bgG = -1;
    private int bgB = -1;
    private int ftR = -1;
    private int ftG = -1;
    private int ftB = -1;

    public AnsiStringBuilder() {
    }

    public AnsiStringBuilder front(int color) {
        if (color < 0 || color > 255) {
            return this;
        }
        this.colorMode = ColorMode.COLOR_256;
        this.ft256 = color;
        return this;
    }

    public AnsiStringBuilder background(int color) {
        if (color < 0 || color > 255) {
            return this;
        }
        this.colorMode = ColorMode.COLOR_256;
        this.bg256 = color;
        return this;
    }

    public AnsiStringBuilder front(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            return this;
        }
        this.colorMode = ColorMode.COLOR_RGB;
        this.ftR = r;
        this.ftG = g;
        this.ftB = b;
        return this;
    }

    public AnsiStringBuilder background(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            return this;
        }
        this.colorMode = ColorMode.COLOR_RGB;
        this.bgR = r;
        this.bgG = g;
        this.bgB = b;
        return this;
    }

    public AnsiStringBuilder deFront() {
        this.ft256 = -1;
        this.ftR = -1;
        this.ftG = -1;
        this.ftB = -1;
        return this;
    }

    public AnsiStringBuilder deBackground() {
        this.bg256 = -1;
        this.bgR = -1;
        this.bgG = -1;
        this.bgB = -1;
        return this;
    }

    public AnsiStringBuilder append(String str) {
        if (StringUtils.isEmpty(str)) {
            return this;
        }
        builder.append(fillColor(str));
        return this;
    }

    public AnsiStringBuilder append(String str, int line, int column) {
        if (StringUtils.isEmpty(str)) {
            return this;
        }
        str = CursorPositionHelper.cursorMove(fillColor(str), line, column);
        builder.append(str);
        return this;
    }

    public AnsiStringBuilder append(char ch) {
        String str = ASCIIStrCache.toString(ch);
        return append(str);
    }

    public AnsiStringBuilder append(int integer) {
        String str = String.valueOf(integer);
        return append(str);
    }

    public AnsiStringBuilder append(long l) {
        String str = String.valueOf(l);
        return append(str);
    }

    public AnsiStringBuilder append(StringBuilder sb) {
        if (sb.length() == 0) {
            return this;
        }
        String str = sb.toString();
        return append(str);
    }

    public AnsiStringBuilder append(AnsiStringBuilder ansiSb) {
        builder.append(ansiSb.toString());
        return this;
    }

    public AnsiStringBuilder crlf() {
        builder.append(StrUtil.CRLF);
        return this;
    }

    public AnsiStringBuilder tab() {
        return append(StrUtil.TAB);
    }

    public AnsiStringBuilder space() {
        return append(StrUtil.SPACE);
    }

    public AnsiStringBuilder space(int cnt) {
        return append(StrUtil.SPACE.repeat(cnt));
    }

    public AnsiStringBuilder clearStr() {
        builder.delete(0, builder.length());
        return this;
    }

    public AnsiStringBuilder clearColor() {
        ft256 = -1;
        bg256 = -1;
        ftR = -1;
        ftG = -1;
        ftB = -1;
        bgR = -1;
        bgG = -1;
        bgB = -1;
        return this;
    }

    private String fillColor(String str) {
        if (this.colorMode.equals(ColorMode.COLOR_256)) {
            if (this.ft256 != -1) {
                str = ColorHelper.front(str, this.ft256);
            }
            if (this.bg256 != -1) {
                str = ColorHelper.background(str, this.bg256);
            }
        } else if (this.colorMode.equals(ColorMode.COLOR_RGB)) {
            if (this.ftR != -1 && this.ftG != -1 && this.ftB != -1) {
                str = ColorHelper.front(str, ftR, ftG, ftB);
            }
            if (this.bgR != -1 && this.bgG != -1 && this.bgB != -1) {
                str = ColorHelper.background(str, bgR, bgG, bgB);
            }
        }
        return str;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public int length() {
        return builder.length();
    }

    public enum ColorMode {
        COLOR_256,
        COLOR_RGB
    }
}
