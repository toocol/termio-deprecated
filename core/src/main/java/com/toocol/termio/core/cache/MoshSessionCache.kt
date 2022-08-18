package com.toocol.termio.core.cache

import com.toocol.termio.core.mosh.core.MoshSession
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/29 22:24
 * @version: 0.0.1
 */
class MoshSessionCache private constructor() {
    companion object Instance {
        private val moshSessionMap: java.util.AbstractMap<Long, MoshSession> = ConcurrentHashMap()

        val sessionMap: Map<Long, MoshSession>
            get() = moshSessionMap

        fun put(moshSession: MoshSession) {
            moshSessionMap[moshSession.sessionId] = moshSession
        }

        operator fun get(sessionId: Long): MoshSession? {
            return moshSessionMap[sessionId]
        }

        fun isDisconnect(sessionId: Long): Boolean {
            return if (!containSession(sessionId)) {
                true
            } else !moshSessionMap[sessionId]!!.isConnected
        }

        fun stop(sessionId: Long) {
            moshSessionMap.computeIfPresent(sessionId) { k: Long?, v: MoshSession ->
                v.close()
                null
            }
        }

        fun stopAll() {
            moshSessionMap.forEach { (_: Long?, moshSession: MoshSession) -> moshSession.close() }
        }

        private fun containSession(sessionId: Long): Boolean {
            return moshSessionMap.containsKey(sessionId)
        }
    }
}