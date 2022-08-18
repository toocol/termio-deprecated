package com.toocol.termio.utilities.utils

import kotlin.system.measureTimeMillis

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/18 20:41
 * @version: 0.0.1
 */
class StringWithKotlin {
    fun processString() {
        var str = String(StringBuilder())
        var time = measureTimeMillis {
            for (i in 0..50000) {
                str += "Hello world~${i}"
            }
        }
        println("Kotlin String use time: $time")

        val builder = StringBuilder()
        time = measureTimeMillis {
            for (i in 0..100000000) {
                builder.append("Hello world~${i}")
            }
        }
        println("Kotlin StringBuilder use time: $time")
    }
}