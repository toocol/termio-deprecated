package com.toocol.termio.core.shell.core

import com.jcraft.jsch.ChannelShell
import com.toocol.termio.core.cache.EXECUTE_CD_CMD
import com.toocol.termio.core.cache.HANGED_ENTER
import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.cache.SshSessionCache.Instance.getChannelShell
import com.toocol.termio.core.mosh.core.MoshSession
import com.toocol.termio.core.shell.ShellAddress
import com.toocol.termio.core.shell.handlers.BlockingDfHandler
import com.toocol.termio.core.term.core.EscapeHelper
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.action.AbstractDevice
import com.toocol.termio.utilities.ansi.AsciiControl
import com.toocol.termio.utilities.ansi.AsciiControl.clean
import com.toocol.termio.utilities.ansi.AsciiControl.cleanCursorMode
import com.toocol.termio.utilities.ansi.AsciiControl.extractCursorPosition
import com.toocol.termio.utilities.ansi.AsciiControl.setCursorToLineHead
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.console.Console
import com.toocol.termio.utilities.execeptions.RemoteDisconnectException
import com.toocol.termio.utilities.functional.Executable
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.CmdUtil
import com.toocol.termio.utilities.utils.MessageBox
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.AsyncResult
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import jline.console.ConsoleReader
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import kotlin.system.exitProcess

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 20:57
 */
class Shell : AbstractDevice, Loggable {
    @JvmField
    val term: Term = Term.getInstance()
    private val shellPrinter: ShellPrinter
    private val shellReader: ShellReader

    @JvmField
    val historyCmdHelper: ShellHistoryCmdHelper

    @JvmField
    val moreHelper: MoreHelper

    @JvmField
    val escapeHelper: EscapeHelper

    @JvmField
    val vimHelper: VimHelper

    @JvmField
    val quickSwitchHelper: SessionQuickSwitchHelper

    @JvmField
    val shellCharEventDispatcher: ShellCharEventDispatcher

    @JvmField
    val tabFeedbackRec: HashSet<String> = HashSet()

    @JvmField
    val cmd = StringBuilder()

    /**
     * the session's id that shell belongs to.
     */
    val sessionId: Long

    /**
     * vert.x system
     */
    val vertx: Vertx

    /**
     * the EventBus of vert.x system.
     */
    val eventBus: EventBus
    private val host: String

    @JvmField
    @Volatile
    var localLastCmd = StringBuffer()

    @JvmField
    @Volatile
    var remoteCmd = StringBuffer()

    @JvmField
    @Volatile
    var currentPrint = StringBuffer()

    @JvmField
    @Volatile
    var selectHistoryCmd = StringBuffer()

    @JvmField
    @Volatile
    var localLastInput = StringBuffer()

    @JvmField
    @Volatile
    var lastRemoteCmd = StringBuffer()

    @JvmField
    @Volatile
    var lastExecuteCmd = StringBuffer()

    @JvmField
    @Volatile
    var status = Status.NORMAL

    @JvmField
    @Volatile
    var protocol: ShellProtocol = ShellProtocol.SSH

    @JvmField
    @Volatile
    var prompt = AtomicReference<String>()

    @JvmField
    @Volatile
    var fullPath = AtomicReference<String>()

    @JvmField
    @Volatile
    var sshWelcome: StringBuilder? = StringBuilder()

    @JvmField
    @Volatile
    var moshWelcome: StringBuilder? = StringBuilder()

    @JvmField
    @Volatile
    var user: String

    @JvmField
    @Volatile
    var bottomLinePrint = StrUtil.EMPTY

    @JvmField
    @Volatile
    var tabAccomplishLastStroke = StrUtil.EMPTY

    private var promptCursorPattern: Regex? = null

    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    /**
     * JSch channel shell
     */
    var channelShell: ChannelShell? = null

    /**
     * Mosh session
     */
    private var moshSession: MoshSession? = null

    @Volatile
    private var jumpServer = false

    @Volatile
    private var returnWrite = false

    @Volatile
    private var promptNow = false

    constructor(sessionId: Long, vertx: Vertx, eventBus: EventBus, moshSession: MoshSession) {
        this.sessionId = sessionId
        host = moshSession.host
        user = moshSession.user
        this.vertx = vertx
        this.eventBus = eventBus
        this.moshSession = moshSession
        shellPrinter = ShellPrinter(this)
        shellReader = ShellReader(this, reader)
        historyCmdHelper = ShellHistoryCmdHelper(this)
        moreHelper = MoreHelper()
        escapeHelper = EscapeHelper()
        vimHelper = VimHelper()
        quickSwitchHelper = SessionQuickSwitchHelper(this)
        shellCharEventDispatcher = ShellCharEventDispatcher()
        resetIO(ShellProtocol.MOSH)
        shellReader.initReader()
    }

    constructor(
        sessionId: Long,
        host: String,
        user: String,
        vertx: Vertx,
        eventBus: EventBus,
        channelShell: ChannelShell?,
    ) {
        this.sessionId = sessionId
        this.host = host
        this.user = user
        this.vertx = vertx
        this.eventBus = eventBus
        this.channelShell = channelShell
        shellPrinter = ShellPrinter(this)
        shellReader = ShellReader(this, reader)
        historyCmdHelper = ShellHistoryCmdHelper(this)
        moreHelper = MoreHelper()
        escapeHelper = EscapeHelper()
        vimHelper = VimHelper()
        quickSwitchHelper = SessionQuickSwitchHelper(this)
        shellCharEventDispatcher = ShellCharEventDispatcher()
        resetIO(ShellProtocol.SSH)
        shellReader.initReader()
    }

    fun resetIO(protocol: ShellProtocol) {
        this.protocol = protocol
        try {
            when (protocol) {
                ShellProtocol.SSH -> {
                    inputStream = channelShell!!.inputStream
                    outputStream = channelShell!!.outputStream
                }
                ShellProtocol.MOSH -> {
                    inputStream = moshSession!!.inputStream
                    outputStream = moshSession!!.outputStream
                }
            }
        } catch (e: Exception) {
            MessageBox.setExitMessage("Reset IO failed: " + e.message)
            exitProcess(-1)
        }
    }

    fun print(msg: String): Boolean {
        val matcher = PROMPT_PATTERN.find(msg.trim { it <= ' ' })
        if (matcher != null) {
            val oldPrompt = prompt.get()
            prompt.set(clean(matcher.value + StrUtil.SPACE))
            if (oldPrompt != prompt.get()) {
                generateCursorPosPattern()
            }
            extractUserFromPrompt()
            if (status == Status.VIM_UNDER) {
                status = Status.NORMAL
                localLastCmd.delete(0, localLastCmd.length)
            } else if (status == Status.MORE_BEFORE || status == Status.MORE_PROC || status == Status.MORE_EDIT || status == Status.MORE_SUB) {
                status = Status.NORMAL
                localLastCmd.delete(0, localLastCmd.length)
            }
        }
        if (status == Status.MORE_BEFORE) {
            status = Status.MORE_PROC
        }
        var hasPrint = false
        when (status) {
            Status.NORMAL -> hasPrint = shellPrinter.printInNormal(msg)
            Status.TAB_ACCOMPLISH -> shellPrinter.printInTabAccomplish(msg)
            Status.VIM_BEFORE, Status.VIM_UNDER -> shellPrinter.printInVim(msg)
            Status.MORE_BEFORE, Status.MORE_PROC, Status.MORE_EDIT, Status.MORE_SUB -> shellPrinter.printInMore(msg)
            else -> {}
        }
        if (protocol == ShellProtocol.MOSH) {
            CONSOLE.showCursor()
        }
        if (status == Status.VIM_BEFORE) {
            status = Status.VIM_UNDER
        }
        selectHistoryCmd.delete(0, selectHistoryCmd.length)
        return hasPrint
    }

    private fun generateCursorPosPattern() {
        val patternStr = StrUtil.fullFillParam(AsciiControl.ANIS_ESCAPE_CURSOR_LOCATION, prompt.get()!!.length + 1)
        promptCursorPattern = Regex(pattern = patternStr)
    }

    @Throws(Exception::class)
    fun readCmd(): String? {
        try {
            shellReader.readCmd()
        } catch (e: RuntimeException) {
            return null
        }
        val cmdStr = cmd.toString()
        val isVimCmd = (CmdUtil.isViVimCmd(localLastCmd.toString())
                || CmdUtil.isViVimCmd(cmdStr)
                || CmdUtil.isViVimCmd(selectHistoryCmd.toString()))
        if (isVimCmd) {
            status = Status.VIM_BEFORE
        }
        if (CmdUtil.isCdCmd(lastRemoteCmd.toString())
            || CmdUtil.isCdCmd(cmdStr)
            || CmdUtil.isCdCmd(selectHistoryCmd.toString())
        ) {
            EXECUTE_CD_CMD = true
        }
        val isMoreCmd =
            (CmdUtil.isMoreCmd(localLastCmd.toString()) && "more" != localLastCmd.toString().trim { it <= ' ' }
                    || CmdUtil.isMoreCmd(cmdStr) && "more" != cmdStr.trim { it <= ' ' }
                    || CmdUtil.isMoreCmd(selectHistoryCmd.toString()) && "more" != selectHistoryCmd.toString()
                .trim { it <= ' ' })
        if (isMoreCmd) {
            status = Status.MORE_BEFORE
        }
        lastRemoteCmd.delete(0, lastRemoteCmd.length)
        currentPrint.delete(0, currentPrint.length)
        return cmdStr
    }

    fun extractUserFromPrompt() {
        val preprocess = prompt.get()!!.trim { it <= ' ' }.replace("\\[".toRegex(), "")
            .replace("]".toRegex(), "")
            .replace("#".toRegex(), "")
            .trim { it <= ' ' }
        user = preprocess.split("@").toTypedArray()[0]
    }

    fun resize(width: Int, height: Int, sessionId: Long) {
        when (protocol) {
            ShellProtocol.SSH -> {
                val channelShell = getChannelShell(sessionId) ?: return
                channelShell.setPtySize(width, height, width, height)
            }
            ShellProtocol.MOSH -> {
                val moshSession = MoshSessionCache[sessionId] ?: return
                moshSession.resize(width, height)
            }
        }
        localLastCmd.delete(0, localLastCmd.length).append(RESIZE_COMMAND)
    }

    fun fillPrompt(msgConst: String): String {
        var msg = msgConst
        if (protocol == ShellProtocol.SSH) {
            return msg
        }
        val matcher = promptCursorPattern!!.find(msg)
        if (matcher != null) {
            val doubleCursorMatcher: MatchResult? = AsciiControl.ANIS_ESCAPE_DOUBLE_CURSOR_PATTERN.find(msg)
            if (doubleCursorMatcher != null) {
                val changePathStr = doubleCursorMatcher.value
                val pos = extractCursorPosition(changePathStr)
                val setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + setCursorToLineHead(pos[0])
                msg = msg.replace(changePathStr, setToHead + prompt.get() + changePathStr)
                val col = pos[1]
                prompt.set(prompt.get()!!.substring(0, col - 1) + cleanCursorMode(changePathStr) + "]# ")
                generateCursorPosPattern()
                return msg
            }
            val matcherPrompt = PROMPT_PATTERN.find(msg)
            if (matcherPrompt == null) {
                val group = matcher.value
                val pos = extractCursorPosition(group)
                val setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + setCursorToLineHead(pos[0])
                msg = msg.replace(group, setToHead + prompt.get() + group)
                return msg
            } else {
                val group = matcher.value
                val lastPos = extractCursorPosition(msg)
                val pos = extractCursorPosition(group)
                if (pos[0] - lastPos[0] == 1) {
                    val setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + setCursorToLineHead(pos[0])
                    msg = msg + setToHead + prompt.get()
                    return msg
                }
            }
        }
        val cursorBracketKMatcher: MatchResult? = AsciiControl.ANIS_ESCAPE_CURSOR_BRACKET_K_PATTERN.find(msg)
        if (cursorBracketKMatcher != null) {
            val changePathStr = cursorBracketKMatcher.value
            val pos = extractCursorPosition(changePathStr)
            val setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + setCursorToLineHead(pos[0])
            msg = msg.replace(changePathStr, setToHead + prompt.get() + changePathStr)
            val col = pos[1]
            // there still have problem
            if (col >= prompt.get()!!.length) {
                return msg
            }
            prompt.set(prompt.get()!!.substring(0, col - 1) + cleanCursorMode(changePathStr))
            generateCursorPosPattern()
            return msg
        }
        return msg
    }

    fun hasWelcome(): Boolean {
        return when (protocol) {
            ShellProtocol.SSH -> sshWelcome != null
            ShellProtocol.MOSH -> moshWelcome != null
        }
    }

    fun printWelcome() {
        when (protocol) {
            ShellProtocol.SSH -> Printer.print(sshWelcome.toString())
            ShellProtocol.MOSH -> Printer.print(moshWelcome.toString())
        }
    }

    fun printAfterEstablish() {
        clear()
        if (HANGED_ENTER) {
            println("Invoke hanged session: $user@$host")
        } else {
            println("Session established: $user@$host")
        }
        println("""
    
    Use protocol ${protocol.name}.
    
    """.trimIndent())
    }

    fun initializeSwitchSessionHelper() {
        quickSwitchHelper.initialize()
    }

    fun switchSession(): Boolean {
        return quickSwitchHelper.switchSession()
    }

    fun initialFirstCorrespondence(protocol: ShellProtocol, executable: Executable) {
        this.protocol = protocol
        try {
            if (jumpServer) {
                prompt.set(StrUtil.EMPTY)
                resetIO(protocol)
                executable.execute()
                return
            }
            val request = JsonObject()
            request.put("sessionId", sessionId)
            request.put("remotePath", "/$user/.bash_history")
            request.put("type", BlockingDfHandler.DF_TYPE_BYTE)
            eventBus.request(ShellAddress.START_DF_COMMAND.address(),
                request) { result: AsyncResult<Message<Any?>?>? ->
                if (result?.result() == null) {
                    return@request
                }
                val bytes = result.result()!!.body() as ByteArray?
                val data = String(bytes!!, StandardCharsets.UTF_8)
                historyCmdHelper.initialize(data.split(StrUtil.LF).toTypedArray())
            }
            vertx.executeBlocking({ promise: Promise<Any?> ->
                try {
                    do {
                        if (returnWrite) {
                            return@executeBlocking
                        }
                    } while (!promptNow)
                    if (protocol == ShellProtocol.SSH) {
                        outputStream!!.write(StrUtil.LF.toByteArray(StandardCharsets.UTF_8))
                        outputStream!!.flush()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    promise.complete()
                }
            }, false)
            vertx.executeBlocking({ promise: Promise<Any?> ->
                try {
                    val tmp = ByteArray(1024)
                    val startTime = System.currentTimeMillis()
                    if (channelShell == null) {
                        promptNow = true
                    }
                    while (true) {
                        while (inputStream!!.available() > 0) {
                            val i = inputStream!!.read(tmp, 0, 1024)
                            if (i < 0) {
                                break
                            }
                            val inputStr = String(tmp, 0, i)
                            val matcher = PROMPT_PATTERN.find(inputStr)
                            returnWrite = if (matcher != null) {
                                prompt.set(matcher.value.replace("\\[\\?1034h".toRegex(), "") + StrUtil.SPACE)
                                true
                            } else {
                                if (this.protocol == ShellProtocol.SSH) {
                                    sshWelcome!!.append(inputStr)
                                } else if (this.protocol == ShellProtocol.MOSH) {
                                    moshWelcome!!.append(inputStr)
                                }
                                true
                            }
                        }
                        if (System.currentTimeMillis() - startTime >= 1000) {
                            promptNow = true
                        }
                        if (StringUtils.isNoneEmpty(prompt.get())) {
                            break
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    assert(prompt.get() != null)
                    generateCursorPosPattern()
                    extractUserFromPrompt()
                    fullPath.set("/$user")
                    resetIO(protocol)
                    executable.execute()
                    promise.complete()
                }
            }, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkConnection() {
        when (protocol) {
            ShellProtocol.SSH -> {
                if (SshSessionCache.isDisconnect(sessionId)) {
                    throw RemoteDisconnectException("SSH session disconnect.")
                }
            }
            ShellProtocol.MOSH -> {
                if (MoshSessionCache.isDisconnect(sessionId)) {
                    throw RemoteDisconnectException("Mosh session disconnect.")
                }
            }
        }
    }

    fun flush() {
        checkConnection()
        try {
            outputStream!!.flush()
        } catch (e: IOException) {
            throw RemoteDisconnectException(e.message)
        }
    }

    fun write(bytes: ByteArray) {
        checkConnection()
        try {
            outputStream!!.write(bytes)
        } catch (e: IOException) {
            throw RemoteDisconnectException(e.message)
        }
    }

    fun write(bytes: Char) {
        checkConnection()
        try {
            outputStream!!.write(bytes.code)
        } catch (e: IOException) {
            throw RemoteDisconnectException(e.message)
        }
    }

    fun writeAndFlush(bytes: ByteArray) {
        checkConnection()
        try {
            outputStream!!.write(bytes)
            outputStream!!.flush()
        } catch (e: IOException) {
            throw RemoteDisconnectException(e.message)
        }
    }

    fun writeAndFlush(inChar: Char) {
        checkConnection()
        try {
            outputStream!!.write(inChar.code)
            outputStream!!.flush()
        } catch (e: IOException) {
            throw RemoteDisconnectException(e.message)
        }
    }

    fun clearShellLineWithPrompt() {
        val promptLen = prompt.get()!!.length
        val position = term.cursorPosition
        val cursorX = position[0]
        val cursorY = position[1]
        term.hideCursor()
        term.setCursorPosition(promptLen, cursorY)
        Printer.print(" ".repeat(cursorX - promptLen))
        term.setCursorPosition(promptLen, cursorY)
        term.showCursor()
    }

    fun cleanUp() {
        remoteCmd.delete(0, remoteCmd.length)
        currentPrint.delete(0, currentPrint.length)
        selectHistoryCmd.delete(0, selectHistoryCmd.length)
        localLastCmd.delete(0, localLastCmd.length)
    }

    fun printErr(err: String?) {
        shellPrinter.printErr(err)
    }

    val isClosed: Boolean
        get() {
            if (channelShell != null) {
                return channelShell!!.isClosed
            }
            return if (moshSession != null) {
                !moshSession!!.isConnected
            } else true
        }

    fun setJumpServer(isJumpServer: Boolean) {
        jumpServer = isJumpServer
    }

    fun clearRemoteCmd() {
        remoteCmd.delete(0, remoteCmd.length)
    }

    fun setLocalLastCmd(cmd: String?) {
        localLastCmd.delete(0, localLastCmd.length).append(cmd)
    }

    fun getSshWelcome(): String? {
        return if (StringUtils.isEmpty(sshWelcome)) null else sshWelcome.toString()
    }

    fun getPrompt(): String {
        return prompt.get()
    }

    fun setPrompt(prompt: String?) {
        this.prompt.set(prompt)
    }

    fun getLastRemoteCmd(): String {
        return lastRemoteCmd.toString()
    }

    fun getRemoteCmd(): String {
        return remoteCmd.toString()
    }

    fun getCurrentPrint(): String {
        return currentPrint.toString()
    }

    fun getOutputStream(protocol: ShellProtocol): OutputStream? {
        try {
            return when (protocol) {
                this.protocol -> {
                    outputStream
                }
                ShellProtocol.MOSH -> {
                    moshSession!!.outputStream
                }
                ShellProtocol.SSH -> {
                    channelShell!!.outputStream
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
        return null
    }

    fun getInputStream(protocol: ShellProtocol): InputStream? {
        try {
            return when (protocol) {
                this.protocol -> {
                    inputStream
                }
                ShellProtocol.MOSH -> {
                    moshSession!!.inputStream
                }
                ShellProtocol.SSH -> {
                    channelShell!!.inputStream
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
        return null
    }

    fun getInputStream(): InputStream? {
        try {
            return when (protocol) {
                this.protocol -> {
                    inputStream
                }
                ShellProtocol.MOSH -> {
                    moshSession!!.inputStream
                }
                ShellProtocol.SSH -> {
                    channelShell!!.inputStream
                }
            }
        } catch (e: Exception) {
            // do nothing
        }
        return null
    }

    fun uri(): String {
        return "$user@$host"
    }

    fun shellReader(): ShellReader {
        return shellReader
    }

    enum class Status(val status: Int, val comment: String) {
        /**
         * The status of Shell.
         */
        NORMAL(1, "Shell is under normal cmd input status."), TAB_ACCOMPLISH(2,
            "Shell is under tab key to auto-accomplish address status."),
        VIM_BEFORE(3, "Shell is before Vim/Vi edit status."), VIM_UNDER(4,
            "Shell is under Vim/Vi edit status."),
        MORE_BEFORE(5, "Shell is before more cmd process status."), MORE_PROC(6,
            "Shell is under more cmd process status."),
        MORE_EDIT(7, "Shell is under more regular expression or cmd edit status."), MORE_SUB(8,
            "Shell is under more :sub cmd status."),
        QUICK_SWITCH(9, "List all connection properties to quick switch session.");
    }

    companion object {
        @JvmField
        val PROMPT_PATTERN: Regex = Regex(pattern = """(\[(\w*?)@(.*?)][$#])""")
        val CONSOLE: Console = Console.get()
        const val RESIZE_COMMAND = AsciiControl.DC2 + CharUtil.BRACKET_START + "resize"
        var reader: ConsoleReader? = null

        @JvmStatic
        fun initializeReader(`in`: InputStream?) {
            try {
                reader = ConsoleReader(`in`, null, null)
            } catch (e: Exception) {
                MessageBox.setExitMessage("Create console reader failed.")
                exitProcess(-1)
            }
        }
    }
}