package com.toocol.termio.utilities.event.core

import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 17:06
 * @version: 0.0.1
 */
class TestSyncEventListener : EventListener<TestSyncEvent>() {
    override fun watch(): KClass<TestSyncEvent> {
        return TestSyncEvent::class
    }

    override fun actOn(event: TestSyncEvent) {

    }
}