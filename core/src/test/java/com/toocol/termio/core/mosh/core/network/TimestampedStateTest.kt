package com.toocol.termio.core.mosh.core.network

import com.toocol.termio.core.mosh.core.statesnyc.UserEvent.Resize
import com.toocol.termio.core.mosh.core.statesnyc.UserStream
import com.toocol.termio.utilities.utils.Timestamp
import org.junit.jupiter.api.Test

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/30 14:20
 */
internal class TimestampedStateTest {
    private val currentState = UserStream()
    private val sentStates: MutableList<TimestampedState<UserStream>> = ArrayList()
    private var assumedReceiverState: TimestampedState<UserStream>? = null
    @Test
    fun testSubtract() {
        sentStates.add(TimestampedState(Timestamp.timestamp(), 0, currentState.copy()))
        assumedReceiverState = sentStates[0]
        var ackNum = 1
        for (i in 0..9999) {
            currentState.pushBack(Resize(100, 100))
            updateAssumedReceiverState()
            rationalizeStates()
            val bytes = currentState.diffFrom(assumedReceiverState!!.state)
            assert(bytes != null && bytes.size > 0)
            var newNum: Long
            val back = sentStates[sentStates.size - 1]
            newNum = if (currentState == back.state) {
                back.num
            } else {
                back.num + 1
            }
            addSentState(Timestamp.timestamp(), newNum, currentState)
            assert(newNum == back.num + 1)

            /* suppose we have received an ack packet from mosh-server */processAcknowledgmentThrough(ackNum++.toLong())
        }
    }

    private fun addSentState(theTimestamp: Long, num: Long, state: UserStream) {
        sentStates.add(TimestampedState(theTimestamp, num, state.copy()))
        if (sentStates.size > 32) {
            val iterator = sentStates.listIterator(sentStates.size - 1)
            for (i in 0..14) {
                if (iterator.hasPrevious()) {
                    iterator.previous()
                }
            }
            iterator.remove()
        }
    }

    private fun updateAssumedReceiverState() {
        val now = Timestamp.timestamp()
        assumedReceiverState = sentStates[sentStates.size - 1]
        for (i in 1 until sentStates.size) {
            val state = sentStates[i]
            assert(now >= state.timestamp)
            assumedReceiverState = if (now - state.timestamp < 1000 + NetworkConstants.ACK_DELAY) {
                state
            } else {
                return
            }
        }
    }

    private fun rationalizeStates() {
        val knownReceiverState = sentStates[0].state
        currentState.subtract(knownReceiverState)
        val iterator: ListIterator<TimestampedState<UserStream>> = sentStates.listIterator(sentStates.size)
        while (iterator.hasPrevious()) {
            iterator.previous().state.subtract(knownReceiverState)
        }
    }

    fun processAcknowledgmentThrough(ackNum: Long) {
        var iterator = sentStates.iterator()
        var i: TimestampedState<UserStream>
        var find = false
        while (iterator.hasNext()) {
            i = iterator.next()
            if (i.num == ackNum) {
                find = true
                break
            }
        }
        if (find) {
            iterator = sentStates.iterator()
            while (iterator.hasNext()) {
                i = iterator.next()
                if (i.num < ackNum) {
                    iterator.remove()
                }
            }
        }
    }
}