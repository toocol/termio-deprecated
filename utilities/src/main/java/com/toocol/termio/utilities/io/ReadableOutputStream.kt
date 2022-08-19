package com.toocol.termio.utilities.io

import kotlin.Throws
import java.io.IOException
import java.io.OutputStream

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 11:01
 */
abstract class ReadableOutputStream<T> : OutputStream() {
    @Throws(IOException::class)
    abstract fun read(): T

    @Throws(IOException::class)
    abstract fun available(): Int
}