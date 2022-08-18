package com.toocol.termio.utilities.bundle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:25
 * @version: 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface BundleLanguage {
    /**
     * The language of bundle message properties
     */
    String language();
}
