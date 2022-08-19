package com.toocol.termio.utilities.io

import kotlin.Throws
import java.io.IOException
import java.io.InputStream

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 11:00
 */
abstract class WritableInputStream<T> : InputStream() {
    @Throws(IOException::class)
    abstract fun write(data: T)

    @Throws(IOException::class)
    abstract fun flush()
}