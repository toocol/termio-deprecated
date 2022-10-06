package com.toocol.termio.utilities.event.core

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 17:04
 * @version:
 */
@RegisterListeners(value = [
    TestSyncEventListener::class
])
internal class EventTest {
    @Test
    fun testEventListenerContainer() {
        EventListenerContainer.init(this.javaClass)
        val listeners = EventListenerContainer.getListeners(TestSyncEvent::class)
        assertTrue { listeners!!.isNotEmpty() }
    }
}