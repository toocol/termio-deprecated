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
class TerminalConsolePrintStream(out: OutputStream) : PrintStream(out, false), Loggable {
    private val lock =  ReentrantLock()
    private val condition = lock.newCondition()

    override fun print(s: String?) {
        s?: return
        if (s.isEmpty()) return
        super.print(s)
        flush()
        await()
    }

    override fun print(b: Boolean) {
        super.print(b)
        flush()
        await()
    }

    override fun print(c: Char) {
        super.print(c)
        flush()
        await()
    }

    override fun print(d: Double) {
        super.print(d)
        flush()
        await()
    }

    override fun print(f: Float) {
        super.print(f)
        flush()
        await()
    }

    override fun print(i: Int) {
        super.print(i)
        flush()
        await()
    }

    override fun print(l: Long) {
        super.print(l)
        flush()
        await()
    }

    override fun print(obj: Any?) {
        obj?: return
        super.print(obj)
        flush()
        await()
    }

    override fun print(s: CharArray) {
        if (s.isEmpty()) return
        super.print(s)
        flush()
        await()
    }

    override fun println(x: Any?) {
        x?: return
        super.println(x)
        flush()
        await()
    }

    override fun println(x: Boolean) {
        super.println(x)
        flush()
        await()
    }

    override fun println(x: Char) {
        super.println(x)
        flush()
        await()
    }

    override fun println(x: CharArray) {
        if (x.isEmpty()) return
        super.println(x)
        flush()
        await()
    }

    override fun println(x: Double) {
        super.println(x)
        flush()
        await()
    }

    override fun println(x: Float) {
        super.println(x)
        flush()
        await()
    }

    override fun println(x: Int) {
        super.println(x)
        flush()
        await()
    }

    override fun println(x: Long) {
        super.println(x)
        flush()
        await()
    }

    override fun println(x: String?) {
        x?: return
        if (x.isEmpty()) return
        super.println(x)
        flush()
        await()
    }

    override fun println() {
        super.println()
        flush()
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