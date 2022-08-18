package com.toocol.termio.core.term.core

import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import java.util.Arrays
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.utilities.utils.OsUtil
import com.toocol.termio.utilities.utils.PomUtil
import com.toocol.termio.utilities.utils.StrUtil
import java.lang.Runtime
import kotlin.jvm.Volatile
import java.io.PrintStream
import java.lang.ProcessBuilder
import org.apache.commons.lang3.StringUtils
import java.lang.Exception

/**
 * The print area of the term screen, from top to bottom is:
 * 1. information bar
 * 2. scene
 * 3. execution
 * 4. display
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/22 22:03
 * @version: 0.0.1
 */
class TermPrinter(private val term: Term) {
    companion object {
        private val credentialCache = CredentialCache.Instance
        private val runtime = Runtime.getRuntime()

        @Volatile
        private var PRINT_STREAM: PrintStream? = null

        @JvmField
        @Volatile
        var displayBuffer = StrUtil.EMPTY

        @Volatile
        var commandBuffer = StrUtil.EMPTY
        @JvmStatic
        fun registerPrintStream(printStream: PrintStream?) {
            PRINT_STREAM = printStream
        }

        private fun totalMemory(): Long {
            return runtime.totalMemory() / 1024 / 1024
        }

        private fun maxMemory(): Long {
            return runtime.maxMemory() / 1024 / 1024
        }

        private fun freeMemory(): Long {
            return runtime.freeMemory() / 1024 / 1024
        }

        private fun usedMemory(): Long {
            return totalMemory() - freeMemory()
        }

        fun clear() {
            try {
                ProcessBuilder(OsUtil.getExecution(), OsUtil.getExecuteMode(), OsUtil.getClearCmd())
                    .inheritIO()
                    .start()
                    .waitFor()
            } catch (e: Exception) {
                // do nothing
            }
        }
    }

    private fun printInformationBar() {
        val windowWidth = Term.CONSOLE.windowWidth
        Term.CONSOLE.setCursorPosition(0, 0)
        val termioVersion = " termio: V${PomUtil.getVersion()}"
        val memoryUse = "memory-use: ${usedMemory()}MB"
        val active = "alive: ${SshSessionCache.getAlive()}"
        val website = "https://github.com/Joezeo/termio "
        val totalLen = termioVersion.length + website.length + memoryUse.length + active.length
        if (totalLen >= windowWidth) {
            return
        }
        val space = " ".repeat((windowWidth - totalLen) / 3)
        var merge = termioVersion + space + memoryUse + space + active + space + website
        val fulfil = windowWidth - merge.length
        if (fulfil != 0) {
            merge = merge.replace(website.toRegex(), " ".repeat(fulfil)) + website
        }
        val builder = AnsiStringBuilder()
            .background(Term.theme.infoBarBackgroundColor.color)
            .front(Term.theme.infoBarFrontColor.color)
            .append(merge)
        PRINT_STREAM!!.println(builder)
    }

    @Synchronized
    fun printScene(resize: Boolean) {
        val term = Term.getInstance()
        val oldPosition = term.cursorPosition
        Term.CONSOLE.hideCursor()
        if (resize) {
            clear()
        }
        printInformationBar()
        val prompt = "Properties:"
        val builder = AnsiStringBuilder()
        val width = term.width
        PRINT_STREAM!!.println(
            builder.background(Term.theme.propertiesZoneBgColor.color)
                .space(width).crlf()
                .append(prompt).space(width - prompt.length)
                .toString()
        )
        builder.clearStr()
        if (credentialCache.credentialsSize() == 0) {
            val msg = "You have no connection properties, type 'help' to get more information."
            PRINT_STREAM!!.print(
                builder.append(msg)
                    .space(width - msg.length).crlf()
                    .space(width).crlf()
            )
        } else {
            credentialCache.showCredentials()
        }
        for (idx in 0 until Term.TOP_MARGIN) {
            PRINT_STREAM!!.println(builder.space(width).toString())
        }
        val msg = "'←'/'→' to change groups."
        builder.clearColor().clearStr()
        val groups = arrayOf("default", "group_1", "group_2", "group_3")
        val groupsLen = Arrays.stream(groups).mapToInt { obj: String -> obj.length }.sum()
        builder.background(Term.theme.groupActiveBgColor.color).space(5).append(groups[0]).space(5)
            .background(Term.theme.groupSplitBgColor.color).space()
            .background(Term.theme.groupIdleBgColor.color)
            .space(5).append(groups[1]).space(5).background(Term.theme.groupSplitBgColor.color).space()
            .background(Term.theme.groupIdleBgColor.color)
            .space(5).append(groups[2]).space(5).background(Term.theme.groupSplitBgColor.color).space()
            .background(Term.theme.groupIdleBgColor.color)
            .space(5).append(groups[3]).space(5)
            .deBackground()
            .space(width - (8 * 5 + 3) - groupsLen - msg.length)
            .append(msg)
        PRINT_STREAM!!.println(builder.toString())
        Term.executeLine = term.cursorPosition[1]
        term.printExecuteBackground()
        if (resize && oldPosition[0] != 0 && oldPosition[1] != 0) {
            term.printDisplayBuffer()
            printTermPrompt()
            term.printCommandBuffer()
        }
        if (!resize) {
            term.setCursorPosition(Term.getPromptLen(), Term.executeLine)
        }
        Term.CONSOLE.showCursor()
    }

    @Synchronized
    fun printTermPrompt() {
        term.printExecuteBackground()
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine)
    }

    @Synchronized
    fun printExecuteBackground() {
        term.setCursorPosition(Term.LEFT_MARGIN, Term.executeLine)
        val builder = AnsiStringBuilder()
            .background(Term.theme.executeBackgroundColor.color)
            .front(Term.theme.executeFrontColor.color)
            .append(Term.PROMPT + " ".repeat(term.width - Term.getPromptLen() - Term.LEFT_MARGIN))
        PRINT_STREAM!!.print(builder.toString())
        term.showCursor()
    }

    @Synchronized
    fun printExecution(msg: String) {
        commandBuffer = msg
        term.hideCursor()
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine)
        val builder = AnsiStringBuilder()
            .background(Term.theme.executeBackgroundColor.color)
            .front(Term.theme.executeFrontColor.color)
            .append(" ".repeat(term.width - Term.getPromptLen() - Term.LEFT_MARGIN))
        PRINT_STREAM!!.print(builder.toString())
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine)
        builder.clearStr().append(msg)
        PRINT_STREAM!!.print(builder.toString())
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine)
        term.showCursor()
    }

    @Synchronized
    fun cleanDisplay() {
        term.setCursorPosition(0, Term.executeLine + 1)
        val windowWidth: Int = term.width
        while (term.cursorPosition[1] < term.height - 3) {
            PRINT_STREAM!!.println(" ".repeat(windowWidth))
        }
    }

    @Synchronized
    fun printDisplayBackground(lines: Int) {
        term.setCursorPosition(0, Term.executeLine + 1)
        val builder = AnsiStringBuilder()
            .background(Term.theme.displayBackGroundColor.color)
            .append(" ".repeat(term.width - Term.LEFT_MARGIN - Term.LEFT_MARGIN))
        for (idx in 0 until lines + 2) {
            PRINT_STREAM!!.println(builder.toString())
        }
    }

    @Synchronized
    fun printDisplay(msg: String) {
        if (StringUtils.isEmpty(msg)) {
            displayBuffer = StrUtil.EMPTY
            cleanDisplay()
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length, Term.executeLine)
            return
        }
        displayBuffer = msg
        term.hideCursor()
        cleanDisplay()
        val split = msg.split("\n").toTypedArray()
        printDisplayBackground(split.size)
        for ((idx, line) in split.withIndex()) {
            term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx)
            PRINT_STREAM!!.println(
                AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
                    .append(line)
                    .toString()
            )
        }
        term.displayZoneBottom = term.cursorPosition[1] + 1
        term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length, Term.executeLine)
        term.showCursor()
    }

    @Synchronized
    fun printDisplay(msg: String, judgment: Boolean) {
        if (judgment) {
            if (StringUtils.isEmpty(msg)) {
                displayBuffer = StrUtil.EMPTY
                cleanDisplay()
                term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length, Term.executeLine)
                return
            }
            displayBuffer = msg
            term.hideCursor()
            cleanDisplay()
            val split = msg.split("\n").toTypedArray()
            printDisplayBackground(split.size)
            for ((idx, line) in split.withIndex()) {
                term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx)
                PRINT_STREAM!!.println(
                    AnsiStringBuilder()
                        .background(Term.theme.displayBackGroundColor.color)
                        .append(line)
                        .toString()
                )
            }
            term.displayZoneBottom = term.cursorPosition[1] + 1
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length, Term.executeLine)
            term.showCursor()
        }
    }

    @Synchronized
    fun printDisplayBuffer() {
        if (StringUtils.isEmpty(displayBuffer)) {
            cleanDisplay()
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length, Term.executeLine)
            return
        }
        cleanDisplay()
        val split = displayBuffer.split("\n").toTypedArray()
        printDisplayBackground(split.size)
        for ((idx, line) in split.withIndex()) {
            term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx)
            PRINT_STREAM!!.println(
                AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
                    .append(line)
                    .toString()
            )
        }
        term.displayZoneBottom = term.cursorPosition[1] + 1
        term.setCursorPosition(0, Term.executeLine)
    }

    @Synchronized
    fun printDisplayEcho(msg: String) {
        if (StringUtils.isEmpty(msg)) {
            displayBuffer = StrUtil.EMPTY
            cleanDisplay()
            term.setCursorPosition(Term.getPromptLen() + term.lineBuilder.length, Term.executeLine)
            return
        }
        displayBuffer = msg
        term.hideCursor()
        cleanDisplay()
        val split = msg.split("\n").toTypedArray()
        printDisplayBackground(split.size)
        for ((idx, line) in split.withIndex()) {
            term.setCursorPosition(Term.TEXT_LEFT_MARGIN, Term.executeLine + 2 + idx)
            PRINT_STREAM!!.println(
                AnsiStringBuilder()
                    .background(Term.theme.displayBackGroundColor.color)
                    .append(line)
                    .toString()
            )
        }
        term.displayZoneBottom = term.cursorPosition[1] + 1
        term.setCursorPosition(term.executeCursorOldX.get(), Term.executeLine)
        term.showCursor()
    }

    @Synchronized
    fun printCommandBuffer() {
        term.setCursorPosition(Term.getPromptLen(), Term.executeLine)
        PRINT_STREAM!!.print(
            AnsiStringBuilder().background(Term.theme.executeBackgroundColor.color)
                .front(Term.theme.executeFrontColor.color).append(
                commandBuffer
            ).toString()
        )
    }

    @Synchronized
    fun printTest() {
        clear()
        //        PRINT_STREAM.print("aaaaaaaaaaaaaaaaaaa");
//        PRINT_STREAM.print("\u001b[Hbbbbbbbbbbbbbbbbbbb");
//        PRINT_STREAM.println();
//        PRINT_STREAM.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        PRINT_STREAM!!.println("aaaaaaaaaaaaaaaaaaa")
        PRINT_STREAM!!.print("bbb")
        PRINT_STREAM!!.print("\u001b[2;0Hc")
        //        String msg = "\u001B[0m\u001B[1;49r\u001B[49;1H\n" +
//                "\u001B[r\u001B[48;1H[root@vultrguest /]# ls\n" +
//                "\u001B[0;1;36mbin\u001B[0m  \u001B[0;1;34mboot\u001B[0m  \u001B[0;1;34mdata\u001B[0m  \u001B[0;1;34mdev\u001B[0m  \u001B[0;1;34metc\u001B[0m  \u001B[0;1;34mhome\u001B[0m  \u001B[0;1;36mlib\u001B[0m  \u001B[0;1;36mlib64\u001B[0m  \u001B[0;1;34mlost+found\u001B[0m  \u001B[0;1;34mmedia\u001B[0m  \u001B[0;1;34mmnt\u001B[0m  \u001B[0;1;34mopt\u001B[0m  \u001B[0;1;34mproc\u001B[0m  \u001B[0;1;34mroot\u001B[0m  \u001B[0;1;34mrun\u001B[0m  \u001B[0;1;36msbin\u001B[0m  \u001B[0;1;34msrv\u001B[0m  \u001B[0;1;34msys\u001B[0m  \u001B[0;30;42mtmp\u001B[0m  \u001B[0;1;34musr\u001B[0m  \u001B[0;1;34mvar\u001B[50;22H\u001B[0m";
//        PRINT_STREAM.print(msg);
//        CONSOLE.rollingProcessing(msg);

//        msg = "\u001B[0m\u001B[1;49r\u001B[49;1H\n" +
//                "\u001B[r\u001B[25;1H[root@vultrguest /]# ll -a\n" +
//                "total 80\n" +
//                "dr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m.\n" +
//                "\u001B[0mdr-xr-xr-x.  19 root root  4096 Jul  2 21:35 \u001B[0;1;34m..\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mbin\u001B[0m -> \u001B[0;1;34musr/bin\n" +
//                "\u001B[0mdr-xr-xr-x.   5 root root  4096 Jun 18 16:54 \u001B[0;1;34mboot\n" +
//                "\u001B[0mdrwxr-xr-x.   3 root root  4096 Jan 24  2021 \u001B[0;1;34mdata\n" +
//                "\u001B[0mdrwxr-xr-x.  19 root root  2920 Jun 22  2021 \u001B[0;1;34mdev\n" +
//                "\u001B[0mdrwxr-xr-x. 107 root root 12288 Jul 10 23:46 \u001B[0;1;34metc\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mhome\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     7 Jun 22  2021 \u001B[0;1;36mlib\u001B[0m -> \u001B[0;1;34musr/lib\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     9 Jun 22  2021 \u001B[0;1;36mlib64\u001B[0m -> \u001B[0;1;34musr/lib64\n" +
//                "\u001B[0mdrwx------.   2 root root 16384 Feb 13  2020 \u001B[0;1;34mlost+found\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmedia\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34mmnt\n" +
//                "\u001B[0mdrwxr-xr-x.   4 root root  4096 Jun 22  2021 \u001B[0;1;34mopt\n" +
//                "\u001B[0mdr-xr-xr-x. 101 root root     0 Apr  6 10:19 \u001B[0;1;34mproc\n" +
//                "\u001B[0mdr-xr-x---.  10 root root  4096 Jul 25 20:01 \u001B[0;1;34mroot\n" +
//                "\u001B[0mdrwxr-xr-x.  35 root root  1000 Jul 24 03:06 \u001B[0;1;31;45,0mrun\n" +
//                "\u001B[0mlrwxrwxrwx.   1 root root     8 Jun 22  2021 \u001B[0;1;36msbin\u001B[0m -> \u001B[0;1;34musr/sbin\n" +
//                "\u001B[0mdrwxr-xr-x.   2 root root  4096 Jun 22  2021 \u001B[0;1;34msrv\n" +
//                "\u001B[0mdr-xr-xr-x.  13 root root     0 Apr  6 10:19 \u001B[0;1;34msys\n" +
//                "\u001B[0mdrwxrwxrwt.   4 root root  4096 Jul 25 22:26 \u001B[0;30;42mtmp\n" +
//                "\u001B[0mdrwxr-xr-x.  13 root root  4096 Jun 18 16:48 \u001B[0;1;34musr\n" +
//                "\u001B[0mdrwxr-xr-x.  21 root root  4096 Jun 18 16:42 \u001B[0;1;34mvar\u001B[50;22H\u001B[0m";
//        PRINT_STREAM.print(msg);
    }

    fun printColorPanel() {}

}