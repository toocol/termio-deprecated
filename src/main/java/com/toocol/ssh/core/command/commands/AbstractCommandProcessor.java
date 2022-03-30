package com.toocol.ssh.core.command.commands;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:32
 */
public abstract class AbstractCommandProcessor {
    /**
     * process a outside command
     *
     * @param param param
     */
    public abstract <T> void process(T param) throws Exception;
}
