package com.toocol.termio.utilities.event.core

import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 17:08
 * @version: 0.0.1
 */
class TestAsyncEventListener : EventListener<TestAsyncEvent>() {
    override fun watch(): KClass<TestAsyncEvent> {
        return TestAsyncEvent::class
    }

    override fun actOn(event: TestAsyncEvent) {
    }
}