package com.toocol.termio.platform.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:35
 * @version: 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Component {
    Class<? extends IComponent> clazz();

    long id();

    boolean initialVisible() default false;
}
