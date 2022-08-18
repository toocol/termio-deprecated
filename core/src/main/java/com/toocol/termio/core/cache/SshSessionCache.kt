package com.toocol.termio.core.cache

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import com.toocol.termio.core.ssh.core.SshSession
import com.toocol.termio.utilities.functional.Switchable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:12
 */
class SshSessionCache private constructor() {
    companion object Instance {
        /**
         * the map stored all alive ssh session(include Session and ChannelShell)
         */
        private val sshSessionMap: AbstractMap<Long, SshSession> = ConcurrentHashMap()

        @get:Synchronized
        val sessionMap: Map<Long, SshSession>
            get() = sshSessionMap

        fun getAlive(): Int {
            return sshSessionMap.entries.stream()
                .filter { (_, value): Map.Entry<Long?, SshSession> -> value.alive() }
                .toList()
                .size
        }

        fun isAlive(ip: String): Boolean {
            val sessionId = containSession(ip)
            return if (sessionId == 0L) {
                false
            } else sshSessionMap[sessionId]!!.alive()
        }

        fun containSessionId(sessionId: Long): Boolean {
            return sshSessionMap.containsKey(sessionId)
        }

        fun containSession(ip: String): Long {
            return sshSessionMap.entries.stream()
                .filter { (_, value): Map.Entry<Long, SshSession> -> ip == value.host }
                .map { (key, _) -> key }
                .findAny()
                .orElse(0L)
        }

        fun isDisconnect(sessionId: Long): Boolean {
            return if (!sshSessionMap.containsKey(sessionId)) {
                false
            } else !sshSessionMap[sessionId]!!.alive()
        }

        fun putSshSession(sessionId: Long, session: SshSession) {
            sshSessionMap[sessionId] = session
        }

        fun setSession(sessionId: Long, session: Session?) {
            sshSessionMap.computeIfPresent(sessionId) { _: Long?, v: SshSession ->
                v.session = session
                v
            }
        }

        fun setChannelShell(sessionId: Long, channelShell: ChannelShell?) {
            sshSessionMap.computeIfPresent(sessionId) { _: Long?, v: SshSession ->
                v.channelShell = channelShell
                v
            }
        }

        fun getSession(sessionId: Long): Session? {
            return Optional.ofNullable(sshSessionMap[sessionId]).map { obj: SshSession -> obj.session }
                .orElse(null)
        }

        fun getChannelShell(sessionId: Long): ChannelShell? {
            return Optional.ofNullable(sshSessionMap[sessionId]).map { obj: SshSession -> obj.channelShell }
                .orElse(null)
        }

        @Synchronized
        fun stopChannelShell(sessionId: Long) {
            sshSessionMap.computeIfPresent(sessionId) { _: Long?, v: SshSession ->
                v.stopChannelShell()
                v
            }
        }

        @Synchronized
        fun stop(sessionId: Long) {
            sshSessionMap.computeIfPresent(sessionId) { _: Long?, v: SshSession ->
                v.stop()
                null
            }
        }

        @Synchronized
        fun stop(host: String) {
            val sessionId = containSession(host)
            sshSessionMap.computeIfPresent(sessionId) { _: Long?, v: SshSession ->
                v.stop()
                null
            }
            ShellCache.stop(sessionId)
        }

        @Synchronized
        fun stopAll() {
            sshSessionMap.forEach { (_: Long?, v: SshSession) -> v.stop() }
        }

        val allSwitchable: Collection<Switchable?>
            get() = sshSessionMap.values
                .stream()
                .map { sshSession: SshSession? -> sshSession }
                .toList()
    }
}