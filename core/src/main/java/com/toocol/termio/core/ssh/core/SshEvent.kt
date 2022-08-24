@file:JvmName("SshEvent")
package com.toocol.termio.core.ssh.core

import com.toocol.termio.utilities.event.core.AsyncEvent
import com.toocol.termio.utilities.event.core.SyncEvent

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/24 11:53
 * @version: 0.0.1
 */
class SessionEstablishedSync(val sessionId: Long = 0) : SyncEvent()

class SessionEstablishedAsync(val sessionId: Long = 0) : AsyncEvent()

class SessionClosedSync(val sessionId: Long = 0) : SyncEvent()

class SessionClosedAsync(val sessionId: Long = 0) : AsyncEvent()
