package com.toocol.coroutines.core

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/20 23:39
 * @version: 0.0.1
 */
interface Asable {
    fun <T> `as`(): T {
        return this as T
    }
}