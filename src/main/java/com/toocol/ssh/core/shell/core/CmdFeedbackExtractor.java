package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.StrUtil;

import java.io.InputStream;
import java.util.regex.Matcher;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/11 15:14
 */
public record CmdFeedbackExtractor(InputStream inputStream, String cmd, Shell shell) {

    public String extractFeedback() throws Exception {
        String feedback = null;

        long startTime = System.currentTimeMillis();
        byte[] tmp = new byte[1024];
        do {
            if (System.currentTimeMillis() - startTime >= 10000) {
                feedback = StrUtil.EMPTY;
            }
            while (inputStream.available() > 0) {
                int i = inputStream.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                String msg = new String(tmp, 0, i);
                Matcher matcher = Shell.PROMPT_PATTERN.matcher(msg);
                String cleandMsg = msg.replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY);
                if (!matcher.find()
                        && !msg.contains(StrUtil.CRLF)
                        && !cleandMsg.equals(cmd)
                        && !cleandMsg.equals(shell.getLastRemoteCmd().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY))
                        && !cleandMsg.equals(shell.localLastCmd.get().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY))) {
                    feedback = msg;
                } else {
                    shell.setPrompt(matcher.group(0) + StrUtil.SPACE);
                    shell.extractUserFromPrompt();
                }

                if (msg.contains(StrUtil.CRLF)) {
                    for (String split : msg.split(StrUtil.CRLF)) {
                        Matcher insideMatcher = Shell.PROMPT_PATTERN.matcher(split);
                        if (!insideMatcher.find()
                                && !split.replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).equals(cmd)
                                && !split.equals(shell.getLastRemoteCmd().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY))
                                && !split.equals(shell.localLastCmd.get().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY))) {
                            feedback = split;
                        }
                    }
                }
            }
        } while (feedback == null);

        return feedback;
    }

}
