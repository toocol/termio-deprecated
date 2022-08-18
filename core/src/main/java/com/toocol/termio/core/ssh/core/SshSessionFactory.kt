package com.toocol.termio.core.ssh.core

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.shell.core.SshUserInfo
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.SnowflakeGuidGenerator
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:54
 * @version: 0.0.1
 */
class SshSessionFactory private constructor() {
    companion object Instance : Castable, Loggable {
        val sshSessionCache = SshSessionCache

        private val guidGenerator = SnowflakeGuidGenerator.getInstance()
        private val jSch = JSch()

        @Throws(Exception::class)
        fun createSession(credential: SshCredential): Long {
            val session = jSch.getSession(credential.user, credential.host, credential.port)
            session.setPassword(credential.password)
            session.userInfo = SshUserInfo()
            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            session.setConfig(config)
            session.timeout = 10000
            session.connect()
            val sessionId: Long = guidGenerator.nextId()
            val channelShell = cast<ChannelShell>(session.openChannel("shell"))
            channelShell.setPtyType("xterm")
            channelShell.connect()
            val sshSession = SshSession(sessionId, session, channelShell)
            sshSessionCache.putSshSession(sessionId, sshSession)
            info("Establish ssh session, sessionId = {}, host = {}, user = {}",
                sessionId, credential.host, credential.user)
            return sessionId
        }

        @Throws(Exception::class)
        fun invokeSession(sessionIdConst: Long, credential: SshCredential): Long {
            var sessionId = sessionIdConst
            var reopenChannelShell = false
            var session = sshSessionCache.getSession(sessionId)
                ?: throw RuntimeException("Get session is null, sessionId = $sessionId")
            if (!session.isConnected) {
                try {
                    session.connect()
                } catch (e: Exception) {
                    sessionId = guidGenerator.nextId()
                    session = jSch.getSession(credential.user, credential.host, credential.port)
                    session.setPassword(credential.password)
                    session.userInfo = SshUserInfo()
                    val config = Properties()
                    config["StrictHostKeyChecking"] = "no"
                    session.setConfig(config)
                    session.timeout = 30000
                    session.connect()
                    sshSessionCache.setSession(sessionId, session)
                }
                reopenChannelShell = true
                warn("Invoke ssh session failed, re-establish ssh session, sessionId = {}, host = {}, user = {}",
                    sessionId, credential.host, credential.user)
            } else {
                info("Multiplexing ssh session, sessionId = {}, host = {}, user = {}",
                    sessionId, credential.host, credential.user)
            }
            var channelShell = sshSessionCache.getChannelShell(sessionId)
                ?: throw RuntimeException("Get ChannelShell is null, sessionId = $sessionId")
            if (reopenChannelShell) {
                sshSessionCache.stopChannelShell(sessionId)
                channelShell = cast(session.openChannel("shell"))
                channelShell.setPtyType("xterm")
                channelShell.connect()
                sshSessionCache.setChannelShell(sessionId, channelShell)
            } else if (channelShell.isClosed || !channelShell.isConnected) {
                channelShell = cast(session.openChannel("shell"))
                channelShell.setPtyType("xterm")
                channelShell.connect()
                sshSessionCache.setChannelShell(sessionId, channelShell)
            }
            return sessionId
        }
    }
}