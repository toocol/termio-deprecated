package com.toocol.ssh.utilities.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All the message handler which annotated this annotation is running in order.
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/15 16:38
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

}
