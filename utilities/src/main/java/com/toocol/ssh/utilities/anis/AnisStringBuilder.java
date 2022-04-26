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

    public AnisStringBuilder clearFront() {
        this.front = -1;
        return this;
    }

    public AnisStringBuilder clearBackground() {
        this.background = -1;
        return this;
    }

    public AnisStringBuilder append(String str) {
        if (StringUtils.isEmpty(str)) {
            return this;
        }
        if (this.front != - 1) {
            str = HighlightHelper.assembleColor(str, this.front);
        }
        if (this.background != -1) {
            str = HighlightHelper.assembleColorBackground(str, this.background);
        }
        builder.append(str);
        return this;
    }

    public AnisStringBuilder append(char ch) {
        String str = ASCIIStrCache.toString(ch);
        if (this.front != - 1) {
            str = HighlightHelper.assembleColor(str, this.front);
        }
        if (this.background != -1) {
            str = HighlightHelper.assembleColorBackground(str, this.background);
        }
        builder.append(str);
        return this;
    }

    public AnisStringBuilder append(int integer) {
        String str = String.valueOf(integer);
        if (this.front != - 1) {
            str = HighlightHelper.assembleColor(str, this.front);
        }
        if (this.background != -1) {
            str = HighlightHelper.assembleColorBackground(str, this.background);
        }
        builder.append(str);
        return this;
    }

    public AnisStringBuilder append(long l) {
        String str = String.valueOf(l);
        if (this.front != - 1) {
            str = HighlightHelper.assembleColor(str, this.front);
        }
        if (this.background != -1) {
            str = HighlightHelper.assembleColorBackground(str, this.background);
        }
        builder.append(str);
        return this;
    }

    public AnisStringBuilder append(StringBuilder sb) {
        if (sb.length() == 0) {
            return this;
        }
        String str = sb.toString();
        if (this.front != - 1) {
            str = HighlightHelper.assembleColor(str, this.front);
        }
        if (this.background != -1) {
            str = HighlightHelper.assembleColorBackground(str, this.background);
        }
        builder.append(str);
        return this;
    }

    public AnisStringBuilder append(AnisStringBuilder ansiSb) {
        builder.append(ansiSb.toString());
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
