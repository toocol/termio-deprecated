package com.toocol.coroutines.core

import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 20:25
 * @version: 0.0.1
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RegisterApis(vararg val clazz: KClass<out AbstractApi>)
