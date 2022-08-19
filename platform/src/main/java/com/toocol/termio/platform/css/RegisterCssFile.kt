package com.toocol.termio.platform.css

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 16:47
 * @version: 0.0.1
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RegisterCssFile(
    /**
     * The name of css file, all css file should be located in directory same as package name of class which loading the resource.
     */
    val name: Array<String>,
)