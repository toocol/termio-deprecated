package com.toocol.termio.utilities.config

import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/7 1:46
 * @version: 0.0.1
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RegisterConfigure(vararg val value : KClass<out Configure<*>>)
