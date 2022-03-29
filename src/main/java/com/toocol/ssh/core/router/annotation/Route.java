package com.toocol.ssh.core.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/4/2 15:20
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
    /**
     * the next command address string of current command address.
     */
    String nextAddress();
}
