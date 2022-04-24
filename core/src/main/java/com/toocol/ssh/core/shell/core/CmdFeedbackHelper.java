package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.utils.StrUtil;

import java.io.InputStream;
import java.util.regex.Matcher;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/11 15:14
 */
public record CmdFeedbackHelper(InputStream inputStream, String cmd, Shell shell) {

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
                String cleanedMsg = msg.replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).replaceAll(StrUtil.SPACE, StrUtil.EMPTY);
                if (!matcher.find()
                        && !msg.contains(StrUtil.CRLF)
                        && !cleanedMsg.equals(cmd.replaceAll(StrUtil.SPACE, StrUtil.EMPTY))
                        && !cleanedMsg.equals(shell.getLastRemoteCmd().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).replaceAll(StrUtil.SPACE, StrUtil.EMPTY))
                        && !cleanedMsg.equals(shell.localLastCmd.toString().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).replaceAll(StrUtil.SPACE, StrUtil.EMPTY))) {
                    feedback = msg;
                } else if (matcher.find()){
                    shell.setPrompt(matcher.group(0) + StrUtil.SPACE);
                    shell.extractUserFromPrompt();
                }

                if (msg.contains(StrUtil.CRLF)) {
                    for (String split : msg.split(StrUtil.CRLF)) {
                        Matcher insideMatcher = Shell.PROMPT_PATTERN.matcher(split);
                        String cleanedSplit = split.replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).replaceAll(StrUtil.SPACE, StrUtil.EMPTY);
                        if (!insideMatcher.find()
                                && !cleanedSplit.equals(cmd.replaceAll(StrUtil.SPACE, StrUtil.EMPTY))
                                && !cleanedSplit.equals(shell.getLastRemoteCmd().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).replaceAll(StrUtil.SPACE, StrUtil.EMPTY))
                                && !cleanedSplit.equals(shell.localLastCmd.toString().replaceAll(StrUtil.CR, StrUtil.EMPTY).replaceAll(StrUtil.LF, StrUtil.EMPTY).replaceAll(StrUtil.SPACE, StrUtil.EMPTY))) {
                            feedback = split;
                        }
                    }
                }
            }
        } while (feedback == null);

        return feedback;
    }

}
