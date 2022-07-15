package com.toocol.ssh.utilities.functional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * annotated the verticle that need deployed in the main class(com.toocol.ssh.TerminalSystem)
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 11:21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerticleDeployment {
    /**
     * The executive weight of verticle, the bigger that number is, the more prior the verticle deployed.
     * If two verticle's weight is the same, the executive sequence is random.
     *
     * @return weight
     */
    int weight() default 0;

    /**
     * whether the verticle is worker verticle.
     *
     * @return is worker
     */
    boolean worker() default false;

    /**
     * the pool size of worker verticle.
     * take effect only the worker is true.
     *
     * @return pool size
     */
    int workerPoolSize() default 20;

    /**
     * the executor pool name
     * take effect only the worker is true.
     *
     * @return poll name
     */
    String workerPoolName() default "";
}
