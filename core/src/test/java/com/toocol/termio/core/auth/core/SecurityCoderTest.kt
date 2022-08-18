package com.toocol.termio.core.auth.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 20:09
 */
internal class SecurityCoderTest {
    @Test
    fun get() {
        Assertions.assertThrows(RuntimeException::class.java) { SecurityCoder.get() }
        Thread { Assertions.assertThrows(RuntimeException::class.java, Executable { SecurityCoder.get() }) }
            .start()
        Assertions.assertThrows(RuntimeException::class.java) { SecurityCoder(UUID.randomUUID().toString()) }
        try {
            val declaredConstructor = SecurityCoder::class.java.getDeclaredConstructor(
                String::class.java
            )
            declaredConstructor.isAccessible = true
            Assertions.assertThrows(InvocationTargetException::class.java) {
                declaredConstructor.newInstance(
                    UUID.randomUUID().toString()
                )
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
    }

    @Test
    fun encodeThenDecode() {
        val coderGet = SecurityCoder.get()
        for (idx in 0..999) {
            val origin = UUID.randomUUID().toString()
            val decode = coderGet.decode(coder.encode(origin))
            Assertions.assertEquals(origin, decode)
        }
    }

    companion object {
        val coder = SecurityCoder("Fq3Jf5Li3-Lf2Tz3Jn0")
    }
}