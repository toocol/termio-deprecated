package com.toocol.termio.core.mosh.core.network

import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/2 0:24
 * @version:
 */
internal class TransportFragmentTest {
    @Test
    fun testPool() {
        val runtime = Runtime.getRuntime()
        val loop = 100000000
        val usePool: Long
        val notPool: Long
        val useMem: Long
        val notMem: Long
        val list: MutableList<TransportFragment.Fragment> = ArrayList(100000000)
        var start = System.currentTimeMillis()
        val pool = TransportFragment.Pool()
        pool.init()
        var sm = runtime.totalMemory() - runtime.freeMemory()
        for (i in 0 until loop) {
            list.add(pool.getObject())
            pool.recycle()
        }
        var end = System.currentTimeMillis()
        var em = runtime.totalMemory() - runtime.freeMemory()
        usePool = end - start
        useMem = em - sm
        list.clear()
        start = System.currentTimeMillis()
        sm = runtime.totalMemory() - runtime.freeMemory()
        for (i in 0 until loop) {
            list.add(TransportFragment.Fragment())
        }
        end = System.currentTimeMillis()
        em = runtime.totalMemory() - runtime.freeMemory()
        notPool = end - start
        notMem = em - sm
        println("usePoll: $usePool")
        println("useMem: " + useMem / 1024 / 1024)
        println("notPoll: $notPool")
        println("notMem: " + notMem / 1024 / 1024)
    }
}