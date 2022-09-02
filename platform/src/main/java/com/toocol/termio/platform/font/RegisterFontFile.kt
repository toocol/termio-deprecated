package com.toocol.termio.platform.font

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/2 17:47
 * @version: 0.0.1
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RegisterFontFile (
    /**
     * The names of font file
     */
    val name: Array<String>
)
