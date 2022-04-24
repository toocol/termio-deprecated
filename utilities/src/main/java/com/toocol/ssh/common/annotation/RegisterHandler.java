package com.toocol.ssh.common.annotation;

import com.toocol.ssh.common.handler.AbstractMessageHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:59
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterHandler {
    /**
     * register the command handler to verticle.
     *
     * @return the command handlers which belong to this verticle
     */
    Class<? extends AbstractMessageHandler>[] handlers();
}
