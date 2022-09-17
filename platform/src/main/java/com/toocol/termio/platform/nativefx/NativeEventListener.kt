package com.toocol.termio.platform.nativefx

fun interface NativeEventListener {
    fun event(key: Int, type: String?, evt: String?)
}