package com.toocol.termio.utilities.event.core

import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/6 22:20
 * @version: 0.0.1
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RegisterListeners(vararg val value: KClass<out EventListener<*>>)
