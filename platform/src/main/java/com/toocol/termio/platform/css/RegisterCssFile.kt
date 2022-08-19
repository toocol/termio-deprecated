package com.toocol.termio.platform.css;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 16:47
 * @version: 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterCssFile {
    /**
     * The name of css file, all css file should be located in directory same as package name of class which loading the resource.
     */
    String[] name();
}
