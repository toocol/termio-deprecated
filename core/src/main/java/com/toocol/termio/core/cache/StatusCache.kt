@file:JvmName("StatusCache")
package com.toocol.termio.core.cache

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/1 0:08
 * @version: 0.0.1
 */
@JvmField
@Volatile
var STOP_ACCEPT_OUT_COMMAND = false

@JvmField
@Volatile
var HANGED_QUIT = false

@JvmField
@Volatile
var HANGED_ENTER = false

@JvmField
@Volatile
var JUST_CLOSE_EXHIBIT_SHELL = false

@JvmField
@Volatile
var SHOW_WELCOME = false

@JvmField
@Volatile
var ACCEPT_SHELL_CMD_IS_RUNNING = false

@JvmField
@Volatile
var ACCESS_EXHIBIT_SHELL_WITH_PROMPT = true

@JvmField
@Volatile
var STOP_PROGRAM = false

@JvmField
@Volatile
var EXECUTE_CD_CMD = false

@JvmField
@Volatile
var EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false

@JvmField
@Volatile
var SWITCH_SESSION = false

@JvmField
@Volatile
var SWITCH_SESSION_WAIT_HANG_PREVIOUS = false

@JvmField
@Volatile
var MONITOR_SESSION_ID: Long = 0