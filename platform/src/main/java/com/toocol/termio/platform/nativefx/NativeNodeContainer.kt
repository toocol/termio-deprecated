package com.toocol.termio.platform.nativefx

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/19 23:00
 * @version: 0.0.1
 */
class NativeNodeContainer {
    companion object {
        private val map: MutableMap<Int, NativeNode> = mutableMapOf()

        fun addNode(key: Int, node: NativeNode) {
            map[key] = node
        }

        fun deleteNode(key: Int) {
            map.remove(key)
        }

        fun nodes(): Collection<NativeNode> {
            return map.values
        }
    }
}