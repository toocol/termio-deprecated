package com.toocol.termio.core.term.core

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/7 16:57
 */
class CommandTrie {
    private val root = Node()

    /**
     * Insert a cmd to CommandTrie
     */
    fun insert(cmd: String) {
        var p: Node? = root
        for (ch in cmd.toCharArray()) {
            val u = ch - 'a'
            if (p!!.tns[u] == null) {
                p.tns[u] = Node()
            }
            p = p.tns[u]
        }
        p!!.end = true
        p.cmd = cmd
    }

    /**
     * Checking whether CommandTrie have the cmd equals input cmd.
     */
    operator fun contains(cmd: String): Boolean {
        var p: Node? = root
        for (ch in cmd.toCharArray()) {
            val u = ch - 'a'
            if (p!!.tns[u] == null) return false
            p = p.tns[u]
        }
        return p!!.end
    }

    /**
     * Checking whether CommandTrie have the cmd stars with the prefix or not.
     */
    fun startsWith(prefix: String): Boolean {
        var p: Node? = root
        for (ch in prefix.toCharArray()) {
            val u = ch - 'a'
            if (p!!.tns[u] == null) return false
            p = p.tns[u]
        }
        return true
    }

    /**
     * Auto-complex cmd.
     */
    fun complex(cmd: String): String? {
        var p: Node? = root
        for (ch in cmd.toCharArray()) {
            val u = ch - 'a'
            if (p!!.tns[u] == null) {
                return cmd
            }
            p = p.tns[u]
        }
        while (p != null && p.nextSize() > 1) {
            p = p.next()
        }
        return if (p != null && p.end) p.cmd else cmd
    }

    private class Node {
        val tns = arrayOfNulls<Node>(CHARS)
        var end = false
        var cmd: String? = null
        fun nextSize(): Int {
            var cnt = 0
            for (tn in tns) {
                if (tn != null) {
                    cnt++
                }
            }
            return cnt
        }

        operator fun next(): Node? {
            for (tn in tns) {
                if (tn != null) {
                    return tn
                }
            }
            return null
        }
    }

    companion object {
        private const val CHARS = 26
    }
}