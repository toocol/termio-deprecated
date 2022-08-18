package com.toocol.termio.core.shell.core;

import com.toocol.termio.utilities.utils.ASCIIStrCache;

import java.nio.charset.StandardCharsets;

import static com.toocol.termio.utilities.utils.CharUtil.*;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/19 14:29
 */
public record VimHelper() {

    public byte[] transferVimInput(char inChar) {
        return switch (inChar) {
            case UP_ARROW -> "\u001B[1A".getBytes(StandardCharsets.UTF_8);
            case DOWN_ARROW -> "\u001B[1B".getBytes(StandardCharsets.UTF_8);
            case LEFT_ARROW -> "\u001B[1D".getBytes(StandardCharsets.UTF_8);
            case RIGHT_ARROW -> "\u001B[1C".getBytes(StandardCharsets.UTF_8);
            default -> ASCIIStrCache.toString(inChar).getBytes(StandardCharsets.UTF_8);
        };
    }

}
