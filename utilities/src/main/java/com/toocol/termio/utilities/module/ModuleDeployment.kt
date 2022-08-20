package com.toocol.termio.utilities.module

/**
 * annotated the verticle that need deployed in the main class(com.toocol.ssh.TerminalSystem)
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 11:21
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleDeployment(
    /**
     * The executive weight of verticle, the bigger that number is, the more prior the verticle deployed.
     * If two verticle's weight is the same, the executive sequence is random.
     *
     * @return weight
     */
    val weight: Int = 0,
    /**
     * whether the verticle is worker verticle.
     *
     * @return is worker
     */
    val worker: Boolean = false,
    /**
     * the pool size of worker verticle.
     * take effect only the worker is true.
     *
     * @return pool size
     */
    val workerPoolSize: Int = 20,
    /**
     * the executor pool name
     * take effect only the worker is true.
     *
     * @return poll name
     */
    val workerPoolName: String = ""
)