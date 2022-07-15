package com.toocol.ssh.utilities.functional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/1 23:06
 * @version: 0.0.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DiffThread {

}
