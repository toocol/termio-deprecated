package com.toocol.ssh.core.router;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/3/19 16:04
 */
@AllArgsConstructor
public enum MessageRouter {
    /**
     * Unable to find the next message address for the current message.
     */
    ERROR_ROUTER(null, null)
    ;
    private final String currentMsg;

    private final String nextMsg;

    public static String nextMsg(String currentMsg) {
        Optional<MessageRouter> first = Arrays.stream(MessageRouter.values()).filter(router -> router.currentMsg.equals(currentMsg)).findFirst();
        return first.orElse(ERROR_ROUTER).nextMsg;
    }
}
