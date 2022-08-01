package com.toocol.termio.utilities.functional;

import io.vertx.core.AbstractVerticle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 15:12
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
    /**
     * the verticle should be ignored.
     */
    Class<? extends AbstractVerticle>[] ignore();
}
