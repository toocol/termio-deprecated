package com.toocol.termio.platform.component

import java.util.concurrent.ConcurrentHashMap

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:54
 */
object ComponentsContainer {
    private val componentsMap: MutableMap<String, IComponent> = ConcurrentHashMap()
    @JvmStatic
    fun put(clazz: Class<*>, id: Long, component: IComponent) {
        componentsMap[generateFullId(clazz, id)] = component
    }

    @JvmStatic
    operator fun <T : IComponent?> get(clazz: Class<*>, id: Long): T {
        return componentsMap[generateFullId(clazz, id)]!!.`as`()
    }

    @JvmStatic
    fun getComponents(): MutableCollection<IComponent> {
        return componentsMap.values
    }

    private fun generateFullId(clazz: Class<*>, id: Long): String {
        return clazz.name + "." + id
    }
}