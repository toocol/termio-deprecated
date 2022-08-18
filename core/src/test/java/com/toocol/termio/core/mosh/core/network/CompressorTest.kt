package com.toocol.termio.core.mosh.core.network

import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import com.toocol.termio.core.mosh.core.proto.InstructionPB
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/14 22:17
 * @version:
 */
internal class CompressorTest {
    @Test
    fun compressorTest() {
        val compressor = Compressor.get()
        val builder = InstructionPB.Instruction.newBuilder()
        builder.protocolVersion = NetworkConstants.MOSH_PROTOCOL_VERSION
        builder.oldNum = 0
        builder.newNum = 1
        builder.ackNum = 1
        builder.throwawayNum = 0
        builder.diff = ByteString.copyFromUtf8(UUID.randomUUID().toString() + UUID.randomUUID())
        builder.chaff = ByteString.copyFromUtf8("CHAFF")
        val inst = builder.build()
        val bytes = inst.toByteArray()
        val compress = compressor.compress(bytes)
        Assertions.assertTrue(bytes.size > compress.size)
        val origin = compressor.decompress(compress)
        Assertions.assertEquals(String(origin, StandardCharsets.UTF_8), String(bytes, StandardCharsets.UTF_8))
        try {
            val instruction = InstructionPB.Instruction.parseFrom(origin)
            Assertions.assertEquals(instruction.toString(), inst.toString())
        } catch (e: InvalidProtocolBufferException) {
            e.printStackTrace()
        }
    }

    @Test
    fun decompressTest() {
        val src =
            "eJx1VcmO20YQvesrCnOJDcgCkmNyGgwMJEEMGLCNHAwfSs0i2VFv6IUa+evzqluaxUhuZLHW914V7wNJKXwhW/a0SJDMzl32xJSsGKE40znbasNCdeVKi92k4FGIW11j/qlQPAfivDQvoe7p2Gr/PMlsAwJjQGraeGmyp7hJdpySpjvbusIzll6EyUmtkntlTv1B01ZrnAyjTyt81D7hvaB4pVJjvhzovY5QaOVN6CgSqLTjO+O4FDtbgXuhOWbPrgfbMF5+vRllhHMWMitnNmjEfkfc8UJ3Bc+xFUotJzS7p8kuGAwYubhYg/CYFw72O+uwsEpY6rq/o/MqWXiAdSv5BPZ/FFI/DF5iUD8niie9KeLmd1k2cdf8Nkx2s1ODU+VSwYbOJI8ItRKMgEcTw2wnhFs4eQ5g9e2e1uYjYF1QVubmgN1Foc3sj04JKTU3U1uGrQWk2DSBNoNhMSMFsOf0cdaRvGBEqebw9edvu937FwhG72NwF2plII94qCpfyKiQjC1+Tyk6fR7t2VnAY1HoOAcNuqoJpngE/lsfvWjliS1SO4SgczHROTH949BFlvlmuPZ51emB7p2PpRLkTT5OksNL2lXjFbqxgVLuLKuOzzGfitogW8j0WV5TOx7R5zXBGzkshz0KyCN6kEwfYxIsxn0YwiTswMNt9t7myy8fOLw90N+rdUgOohXtVpqu4ViizvCotactuuZtUEGO7pw9Cf0Z10B/RXN6VfYhQg45KLe/N+BMX7S7UtGB2rSRz2v0oOgDu7q28kPPit7HbIOxyfUlxVxtyPDKdAugFlN7OJTDbvcHNi/66xfI8QkbTfUFWwPQPlW+ifaBA08Macqr/TWagqGMf6C6hBugtW/7MzXTOzh8/eUbfYJvmFRbpTYV/GCzclvW+izoK1MjB3xqJOvBM6qhMZufTlw5WefKb8STt7gd8ZVG4qwK6arGurZgVRWIky4R7KkKT3FNyUHaXb7j3PTxV6VguKu1RKPbWcxY2v8pBG6YztyXDtcMTuMS90vRxwnjSD/NP7WsLtCNogVqlJfPq3IC19SxhBTuer07QF6GpOURRSeUBDgRBTJ5mSzj4wUQ3xDCGiG18893DDzFzcr4O4zOoZiIW5k7z1M0fZeVJI30fBrHBsdnIDHDA5Mhj84+wBLVeRda/zf0e6M3CFeNtYe0xhpxydJqzbUXoz8X7afGBGP/veCvYsMJQ+kRl34SniPL6NkjtguPDYSXOFw6xMBDCxpO/Zocdv8Cge6byg=="
        val decode = Base64.getDecoder().decode(src)
        val compressor = Compressor.get()
        val decompress = compressor.decompress(decode)
        println(String(decompress))
    }
}