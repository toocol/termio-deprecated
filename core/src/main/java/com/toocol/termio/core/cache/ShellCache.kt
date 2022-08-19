package com.toocol.termio.core.cache

import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.shell.core.ShellProtocol
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/29 22:35
 * @version: 0.0.1
 */
class ShellCache private constructor() {
    companion object Instance {
        /**
         * the map stored all alive ssh session shell's object.
         */
        private val shellMap: java.util.AbstractMap<Long, Shell> = ConcurrentHashMap()
        operator fun contains(sessionId: Long): Boolean {
            return shellMap.containsKey(sessionId)
        }

        fun putShell(sessionId: Long, shell: Shell) {
            shellMap[sessionId] = shell
        }

        fun getShell(sessionId: Long): Shell? {
            return shellMap[sessionId]
        }

        fun stop(sessionId: Long) {
            shellMap.computeIfPresent(sessionId) { _: Long?, v: Shell? ->
                when (v?.protocol) {
                    ShellProtocol.SSH -> SshSessionCache.stop(sessionId)
                    ShellProtocol.MOSH -> MoshSessionCache.stop(sessionId)
                    else -> {}
                }
                null
            }
        }

        fun initializeQuickSessionSwitchHelper() {
            shellMap.forEach { (_: Long?, shell: Shell) -> shell.initializeSwitchSessionHelper() }
        }

        fun stop(sessionId: Long, protocol: ShellProtocol?) {
            shellMap.computeIfPresent(sessionId) { _: Long?, _: Shell? ->
                when (protocol) {
                    ShellProtocol.SSH -> SshSessionCache.stop(sessionId)
                    ShellProtocol.MOSH -> MoshSessionCache.stop(sessionId)
                    else -> {}
                }
                null
            }
        }
    }
}