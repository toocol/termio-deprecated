package com.toocol.termio.core.shell.core

import com.jcraft.jsch.ChannelExec
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.utilities.utils.Castable

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:48
 * @version: 0.0.1
 */
class ExecChannelProvider {
    companion object Instance : Castable {
        private val sshSessionCache = SshSessionCache

        @Throws(Exception::class)
        fun getChannelExec(sessionId: Long): ChannelExec {
            val session = sshSessionCache.getSession(sessionId)
                ?: throw RuntimeException("Session is null, sessionId = $sessionId")
            if (!session.isConnected) {
                throw RuntimeException("Session is not connected, sessionId = $sessionId")
            }
            return cast(session.openChannel("exec"))
        }

    }
}