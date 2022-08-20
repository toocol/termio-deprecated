package com.toocol.termio.utilities.sync

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/12 16:43
 */
object SharedCountdownLatch {
    private val sharedCountdownLatchMap: java.util.AbstractMap<String, CountDownLatch?> = HashMap()
    private val lock = ReentrantLock()

    @JvmStatic
    fun countdown(awaitClass: Class<*>?, workClass: Class<*>?) {
        lock.lock()
        try {
            sharedCountdownLatchMap.computeIfPresent(transferKey(awaitClass!!,
                workClass!!)) { _: String?, v: CountDownLatch? ->
                v!!.countDown()
                v
            }
        } catch (e: Exception) {
            // do nothing
        } finally {
            lock.unlock()
        }
    }

    @JvmStatic
    fun await(runBeforeAwait: Runnable, awaitClass: Class<*>?, vararg workClasses: Class<*>?) {
        var latch: CountDownLatch? = null
        lock.lock()
        try {
            latch = CountDownLatch(workClasses.size)
            for (workClass in workClasses) {
                sharedCountdownLatchMap[transferKey(awaitClass!!, workClass!!)] = latch
            }
        } catch (e: Exception) {
            // do nothing
        } finally {
            lock.unlock()
        }
        runBeforeAwait.run()
        if (latch == null) {
            return
        }
        try {
            latch.await()
            for (workClass in workClasses) {
                sharedCountdownLatchMap[transferKey(awaitClass!!, workClass!!)] = null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun await(runBeforeAwait: Runnable, wait: Long, awaitClass: Class<*>?, vararg workClasses: Class<*>?) {
        var latch: CountDownLatch? = null
        lock.lock()
        try {
            latch = CountDownLatch(workClasses.size)
            for (workClass in workClasses) {
                sharedCountdownLatchMap[transferKey(awaitClass!!, workClass!!)] = latch
            }
        } catch (e: Exception) {
            // do nothing
        } finally {
            lock.unlock()
        }
        runBeforeAwait.run()
        if (latch == null) {
            return
        }
        try {
            val await = latch.await(wait, TimeUnit.MILLISECONDS)
            for (workClass in workClasses) {
                sharedCountdownLatchMap[transferKey(awaitClass!!, workClass!!)] = null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun transferKey(vararg classes: Class<*>): String {
        val keyBuilder = StringBuilder()
        for (aClass in classes) {
            keyBuilder.append(aClass.name).append(":")
        }
        keyBuilder.deleteCharAt(keyBuilder.length - 1)
        return keyBuilder.toString()
    }
}