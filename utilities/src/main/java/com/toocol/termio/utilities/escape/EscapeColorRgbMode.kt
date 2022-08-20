package com.toocol.termio.utilities.escape;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/9 15:18
 */
public enum EscapeColorRgbMode implements IEscapeMode {
    COLOR_RGB_MODE;

    private boolean foreground;

    public EscapeColorRgbMode setForeground(boolean foreground) {
        this.foreground = foreground;
        return this;
    }
}
