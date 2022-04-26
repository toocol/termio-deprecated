package com.toocol.ssh.utilities.anis;

import com.toocol.ssh.utilities.utils.ASCIIStrCache;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/26 19:14
 */
public final class AnisStringBuilder {

    private final StringBuilder builder = new StringBuilder();

    private int background = -1;
    private int front = -1;

    public AnisStringBuilder() {
    }

    public AnisStringBuilder front(int color) {
        this.front = color;
        return this;
    }

    public AnisStringBuilder background(int color) {
        this.background = color;
        return this;
    }

    public AnisStringBuilder deFront() {
        this.front = -1;
        return this;
    }

    public AnisStringBuilder deBackground() {
        this.background = -1;
        return this;
    }

    public AnisStringBuilder append(String str) {
        if (StringUtils.isEmpty(str)) {
            return this;
        }
        if (this.front != - 1) {
            str = ColorHelper.front(str, this.front);
        }
        if (this.background != -1) {
            str = ColorHelper.background(str, this.background);
        }
        builder.append(str);
        return this;
    }

    public AnisStringBuilder append(char ch) {
        String str = ASCIIStrCache.toString(ch);
        return append(str);
    }

    public AnisStringBuilder append(int integer) {
        String str = String.valueOf(integer);
        return append(str);
    }

    public AnisStringBuilder append(long l) {
        String str = String.valueOf(l);
        return append(str);
    }

    public AnisStringBuilder append(StringBuilder sb) {
        if (sb.length() == 0) {
            return this;
        }
        String str = sb.toString();
        return append(str);
    }

    public AnisStringBuilder append(AnisStringBuilder ansiSb) {
        builder.append(ansiSb.toString());
        return this;
    }

    public AnisStringBuilder clearStr() {
        builder.delete(0, builder.length());
        return this;
    }

    public AnisStringBuilder clearColor() {
        front = -1;
        background = -1;
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public int length() {
        return builder.length();
    }
}
