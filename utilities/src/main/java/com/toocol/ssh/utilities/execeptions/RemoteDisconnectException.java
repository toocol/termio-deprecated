package com.toocol.ssh.utilities.execeptions;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/19 16:49
 */
public class RemoteDisconnectException extends RuntimeException {

    public RemoteDisconnectException(String message) {
        super(message);
    }

}
