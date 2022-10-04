package com.toocol.termio.core.term.core

import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.log.Loggable
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer

class HistoryOutputInfoHelper : Loggable {
    private var showIndex = 1
    private var totalPage = 1
    private val msgList: MutableMap<Int, MutableList<String>> = HashMap()
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss ")
    fun add(messageConst: String) {
        var message = messageConst
        var pageMsgs = msgList.getOrDefault(totalPage, ArrayList())
        message = """
               ${simpleDateFormat.format(Date())}
               $message
               
               """.trimIndent()
        if (pageMsgs.size < PAGE_SIZE) {
            pageMsgs.add(message)
        } else {
            pageMsgs = msgList.getOrDefault(++totalPage, ArrayList())
            pageMsgs.add(message)
        }
        showIndex = totalPage
        msgList[totalPage] = pageMsgs
    }

    fun displayInformation() {
        val msgs: List<String> = msgList[showIndex]!!
        val builder = AnsiStringBuilder()
        msgs.forEach(Consumer { str: String? -> builder.append(str!!) })
        builder.append("\n")
            .append("index:")
            .append(showIndex)
            .append(StringUtils.repeat(" ", 40))
            .append("Press '←'/'→' to change page,'Esc' to quit.")
        val term = Term
        term.cleanDisplay()
        term.printDisplayWithRecord(builder.toString())
    }

    fun pageLeft() {
        if (showIndex == 1) {
            return
        }
        showIndex--
        displayInformation()
    }

    fun pageRight() {
        if (showIndex >= totalPage) {
            return
        }
        showIndex++
        displayInformation()
    }

    companion object {
        private const val PAGE_SIZE = 5

        @JvmStatic
        @get:Synchronized
        val instance = HistoryOutputInfoHelper()
    }
}