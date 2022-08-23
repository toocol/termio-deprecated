package com.toocol.termio.utilities.event.core

import io.vertx.core.buffer.Buffer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 17:13
 * @version:
 */
internal class EventCodecTest {
    @Test
    fun testEventCodec() {
        val codec = EventCodec()
        val buffer = Buffer.buffer()
        val event = TestSyncEvent(10)
        codec.encodeToWire(buffer, event)
        val decodeEvent = codec.decodeFromWire(0, buffer)
        if (decodeEvent is TestSyncEvent) {
            assertEquals(event.value, decodeEvent.value)
        }
    }
}