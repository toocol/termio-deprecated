package com.toocol.termio.platform.watcher

import com.toocol.termio.utilities.event.core.SyncEvent

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/19 22:52
 * @version: 0.0.1
 */
object WindowResizeStartSync : SyncEvent()

object WindowResizingSync : SyncEvent()

object WindowResizeEndSync : SyncEvent()
