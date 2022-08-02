package com.toocol.termio.utilities.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/12 18:21
 */
public class CmdUtil {
    public static boolean isCdCmd(String cmd) {
        String clearLastCmd = cmd
                .trim()
                .replaceAll(" {2,}", " ")
                .replaceAll(StrUtil.CR, StrUtil.EMPTY)
                .replaceAll(StrUtil.LF, StrUtil.EMPTY);

        return "cd".equals(clearLastCmd.replaceAll(StrUtil.SPACE, StrUtil.EMPTY)) || clearLastCmd.startsWith("cd ");
    }

    public static boolean isViVimCmd(String cmd) {
        cmd = cmd.trim();
        return StringUtils.startsWith(cmd, "vi ") || StringUtils.startsWith(cmd, "vim ")
                || StringUtils.startsWith(cmd, "sudo vi ") || StringUtils.startsWith(cmd, "sudo vim ")
                || "vi".equals(cmd) || "vim".equals(cmd);
    }

    public static boolean isMoreCmd(String cmd) {
        String clearLastCmd = cmd
                .trim()
                .replaceAll(" {2,}", " ")
                .replaceAll(StrUtil.CR, StrUtil.EMPTY)
                .replaceAll(StrUtil.LF, StrUtil.EMPTY);

        return "more".equals(clearLastCmd.replaceAll(StrUtil.SPACE, StrUtil.EMPTY)) || clearLastCmd.startsWith("more ");
    }
}
