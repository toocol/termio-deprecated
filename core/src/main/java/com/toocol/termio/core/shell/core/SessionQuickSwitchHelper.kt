package com.toocol.termio.core.shell.core

import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.cache.CredentialCache.Instance.indexOf
import com.toocol.termio.core.cache.SWITCH_SESSION_WAIT_HANG_PREVIOUS
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.ssh.SshAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.AsciiControl
import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.ansi.Printer.println
import com.toocol.termio.utilities.execeptions.IStacktraceParser
import com.toocol.termio.utilities.functional.Switchable
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.Promise
import java.util.*
import kotlin.math.min

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:39
 */
class SessionQuickSwitchHelper(private val shell: Shell) : Loggable, IStacktraceParser {
    private val recordCursorPos = IntArray(2)

    @Volatile
    private var quit = false

    @Volatile
    private var changeSession = false
    private var switchableList: List<Switchable> = ArrayList()
    private var chosen: Switchable? = null
    private var indicator = 0
    private var viewportStart = 0
    private var helpInfoLine = 0
    private var bottomLineOffset = 0

    fun initialize() {
        val credentialSwitchable: Collection<Switchable> = CredentialCache.allSwitchable
            .asSequence()
            .filter { switchable: Switchable -> switchable.uri() != shell.uri() }
            .toList()
        val sessionSwitchable: Collection<Switchable> = SshSessionCache.allSwitchable
            .asSequence()
            .filter { switchable: Switchable -> switchable.uri() != shell.uri() }
            .toList()
        val removalSet: MutableSet<Switchable> = HashSet(sessionSwitchable)
        removalSet.addAll(credentialSwitchable)
        val list: ArrayList<Switchable> = ArrayList()
        for (switchable in removalSet) {
            list.add(switchable)
        }
        val toTypedArray = list.toTypedArray()
        Arrays.sort(toTypedArray)
        switchableList = listOf(*toTypedArray)
        reset()
    }

    fun switchSession(): Boolean {
        try {
            shell.status = Shell.Status.QUICK_SWITCH
            reset()
            var cursorPosition = term.cursorPosition
            var offset = 0
            val height = term.height
            if (cursorPosition[1] > height - bottomLineOffset + 1) {
                while (offset < bottomLineOffset - 1) {
                    println(AsciiControl.ANIS_ERASE_LINE)
                    offset++
                }
            }
            val promptBase = AnsiStringBuilder()
                .append(shell.getPrompt())
                .front(Term.theme.lcCmdExecuteHighlightColor.color)
                .append("> {} <")
            cursorPosition = term.cursorPosition
            term.hideCursor()
            recordCursorPos[0] = shell.getPrompt().length
            recordCursorPos[1] = cursorPosition[1] - 1 - offset
            term.setCursorPosition(0, recordCursorPos[1] + helpInfoLine)
            val width = term.width
            print(
                AnsiStringBuilder().background(Term.theme.switchSessionPanelBottomBgColor.color)
                    .append(HELP_INFO)
                    .space(width - HELP_INFO.length)
                    .toString()
            )
            while (!quit) {
                term.setCursorPosition(0, recordCursorPos[1])
                println(StrUtil.fullFillParam(promptBase.toString(), indicator + viewportStart))
                term.setCursorPosition(0, recordCursorPos[1] + 1)
                printSwitchPanel()
                term.setCursorPosition(24, recordCursorPos[1] + 1)
                shell.shellReader().readCmd()
            }
            cleanSwitchPanel()
            term.setCursorPosition(recordCursorPos[0], recordCursorPos[1])
            term.showCursor()
            shell.status = Shell.Status.NORMAL
        } catch (e: Exception) {
            error("Catch exception when quick switch session, stackTrace = {}", parseStackTrace(e))
        }
        return changeSession
    }

    fun upSession() {
        if (indicator > SCROLLABLE || indicator > 1 && viewportStart == 0) {
            indicator--
            return
        }
        if (viewportStart > 0) {
            viewportStart--
        }
    }

    fun downSession() {
        if (indicator >= switchableList.size) {
            return
        }
        if (indicator < SCROLLABLE
            || viewportStart + indicator + SCROLLABLE > switchableList.size && viewportStart + indicator < switchableList.size
        ) {
            indicator++
            return
        }
        if (viewportStart + VIEWPORT_LEN < switchableList.size) {
            viewportStart++
        }
    }

    fun changeSession() {
        if (chosen == null) {
            return
        }
        val uri = chosen!!.uri().split("@").toTypedArray()
        val index = indexOf(uri[1], uri[0])
        shell.vertx.executeBlocking { promise: Promise<Any?> ->
            while (true) {
                if (SWITCH_SESSION_WAIT_HANG_PREVIOUS) {
                    shell.eventBus.send(SshAddress.ESTABLISH_SSH_SESSION.address(), index)
                    SWITCH_SESSION_WAIT_HANG_PREVIOUS = false
                    break
                }
            }
            promise.complete()
        }
        quit()
        changeSession = true
    }

    fun quit() {
        quit = true
    }

    private fun reset() {
        changeSession = false
        quit = false
        indicator = 1
        viewportStart = 0
        chosen = null
        helpInfoLine = min(VIEWPORT_LEN + 2, switchableList.size + 2)
        bottomLineOffset = min(VIEWPORT_LEN + 3, switchableList.size + 3)
    }

    private fun printSwitchPanel() {
        val builder = AnsiStringBuilder()
        val partLength = IntArray(5)
        val width = term.width
        var totalPartLength = 0
        for (i in 0..4) {
            partLength[i] = width * PART_PROPORTION[i] / 10
            builder.background(Term.theme.switchSessionHeadBgColor.color)
                .front(Term.theme.switchSessionHeadFrontColor.color)
                .append(PART_HEADS[i])
                .space(partLength[i] - PART_HEADS[i].length)
            totalPartLength += partLength[i]
        }
        println(builder.space(width - totalPartLength).toString())
        builder.clearStr().clearColor()
        val range = min(viewportStart + VIEWPORT_LEN, switchableList.size)
        for (i in viewportStart until range) {
            val switchable = switchableList[i]
            val idx = i + 1
            var prefix: String
            val chosenSession = indicator + viewportStart == idx
            if (chosenSession) {
                chosen = switchable
                prefix = " > "
            } else if (idx != switchableList.size && i == range - 1) {
                prefix = " .."
            } else {
                prefix = "   "
            }
            val index = "[" + (if (idx < 10) "0$idx" else idx) + "]"
            val uri = switchable.uri()
            val curPath = switchable.currentPath()
            val protocol = switchable.protocol()
            val alive = if (switchable.alive()) "alive" else "disconnect"
            builder.background(if (chosenSession) Term.theme.switchSessionChosenBgColor.color else Term.theme.switchSessionPanelBodyBgColor.color)
                .front(if (chosenSession) Term.theme.switchSessionChosenFrontColor.color else -1)
                .append(prefix)
                .append(index).space(partLength[0] - (prefix + index).length)
                .append(uri).space(partLength[1] - uri.length)
                .append(curPath).space(partLength[2] - curPath.length)
                .append(protocol).space(partLength[3] - protocol.length)
                .append(alive).space(partLength[4] - alive.length)
                .space(width - totalPartLength).crlf()
            print(builder.toString())
            builder.clearStr().clearColor()
        }
    }

    private fun cleanSwitchPanel() {
        term.setCursorPosition(0, recordCursorPos[1])
        for (i in 0 until bottomLineOffset) {
            if (i == 0) {
                print(AsciiControl.ANIS_ERASE_LINE)
                println(shell.getPrompt())
            } else {
                println(AsciiControl.ANIS_ERASE_LINE)
            }
        }
    }

    companion object {
        private const val VIEWPORT_LEN = 5
        private const val SCROLLABLE = VIEWPORT_LEN / 2 + 1
        private val PART_PROPORTION = intArrayOf(1, 3, 2, 2, 2)
        private val PART_HEADS = arrayOf("   No.", "address", "path", "protocol", "status")
        private const val HELP_INFO =
            " Press '↑'/'↓' key to choose session to switch, '←'/'→' to change group, 'Enter' to confirm, 'Esc' to quit."
        private val term = Term.getInstance()
    }
}