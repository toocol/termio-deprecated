package com.toocol.termio.utilities.bundle

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:23
 * @version: 0.0.1
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BindPath(
    /**
     * The path to bundle message properties
     */
    val bundlePath: String,

    /**
     * the path to the message properties
     */
    val languages: Array<String>
)