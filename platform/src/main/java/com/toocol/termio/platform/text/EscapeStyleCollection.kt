package com.toocol.termio.platform.text

import javafx.scene.paint.Color

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/30 23:09
 * @version: 0.0.1
 */
class EscapeStyleCollection(override val size: Int = 1) : MutableCollection<String> {
    override fun contains(element: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<String>): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun add(element: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAll(elements: Collection<String>): Boolean {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun iterator(): MutableIterator<String> {
        TODO("Not yet implemented")
    }

    override fun remove(element: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(elements: Collection<String>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<String>): Boolean {
        TODO("Not yet implemented")
    }

    fun updateBold(b: Boolean): EscapeStyleCollection {
        TODO("Not yet implemented")
    }

    fun updateItalic(b: Boolean): EscapeStyleCollection {
        TODO("Not yet implemented")
    }

    fun updateUnderline(b: Boolean): EscapeStyleCollection {
        TODO("Not yet implemented")
    }

    fun updateStrikethrough(b: Boolean): EscapeStyleCollection {
        TODO("Not yet implemented")
    }

    fun updateTextColor(valueOf: Color?): EscapeStyleCollection {
        TODO("Not yet implemented")
    }

    fun updateBackgroundColor(valueOf: Color?): EscapeStyleCollection {
        TODO("Not yet implemented")
    }
}