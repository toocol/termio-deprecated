package com.toocol.termio.platform.console

import com.toocol.termio.utilities.log.Loggable
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/27 14:36
 * @version: 0.0.1
 */
class TerminalConsolePrintStream(out: OutputStream) : PrintStream(out), Loggable {
    private val lock =  ReentrantLock()
    private val condition = lock.newCondition()

    override fun print(s: String?) {
        super.print(s)
        await()
    }

    override fun print(b: Boolean) {
        super.print(b)
        await()
    }

    override fun print(c: Char) {
        super.print(c)
        await()
    }

    override fun print(d: Double) {
        super.print(d)
        await()
    }

    override fun print(f: Float) {
        super.print(f)
        await()
    }

    override fun print(i: Int) {
        super.print(i)
        await()
    }

    override fun print(l: Long) {
        super.print(l)
        await()
    }

    override fun print(obj: Any?) {
        super.print(obj)
        await()
    }

    override fun print(s: CharArray) {
        super.print(s)
        await()
    }

    override fun println(x: Any?) {
        super.println(x)
        await()
    }

    override fun println(x: Boolean) {
        super.println(x)
        await()
    }

    override fun println(x: Char) {
        super.println(x)
        await()
    }

    override fun println(x: CharArray) {
        super.println(x)
        await()
    }

    override fun println(x: Double) {
        super.println(x)
        await()
    }

    override fun println(x: Float) {
        super.println(x)
        await()
    }

    override fun println(x: Int) {
        super.println(x)
        await()
    }

    override fun println(x: Long) {
        super.println(x)
        await()
    }

    override fun println(x: String?) {
        super.println(x)
        await()
    }

    override fun println() {
        super.println()
        await()
    }

    private fun await() {
        lock.withLock {
            condition.await()
        }
    }

    fun signal() {
        lock.withLock {
            condition.signalAll()
        }
    }
}