package com.toocol.termio.core.term

import com.toocol.termio.utilities.module.IAddress

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
enum class TermAddress(
    // the address string of message
    private val address: String
) : IAddress {

    /**
     * execute console outside command
     */
    EXECUTE_OUTSIDE("ssh.command.execute.outside"),

    /**
     * to accept the user command input
     */
    ACCEPT_COMMAND("ssh.command.accept"),

    /**
     * monitor the terminal program's status,
     * include terminal's size and ssh/mosh connection status.
     */
    MONITOR_TERMINAL("ssh.term.listen.size.change"),

    /**
     * deal with the termio command echo and prompt display.
     */
    TERMINAL_ECHO("ssh.term.echo"),

    /**
     * deal with the termio command echo and prompt display.
     */
    TERMINAL_ECHO_CLEAN_BUFFER("ssh.term.echo.clean.buffer");

    override fun address(): String {
        return address
    }
}