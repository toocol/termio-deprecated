package com.toocol.termio.platform.component

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import javafx.scene.Node
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.util.function.Consumer
import kotlin.system.exitProcess

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 1:00
 * @version: 0.0.1
 */
class ComponentsParser : Loggable, Castable {
    private val components: MutableMap<Class<*>, WeakReference<IComponent>> = HashMap()

    fun parse(clazz: Class<*>) {
        val register = clazz.getAnnotation(RegisterComponent::class.java) ?: return
        try {
            for (component in register.value) {
                val constructor: Constructor<out IComponent> = component.clazz.java.getDeclaredConstructor(Long::class.java)
                constructor.isAccessible = true
                val iComponent = constructor.newInstance(component.id)
                if (!component.initialVisible) {
                    if (iComponent is Node) {
                        iComponent.isVisible = false
                        iComponent.isManaged = false
                    }
                }
                components[component.clazz.java] = WeakReference(iComponent)
            }
        } catch (e: Exception) {
            error("Parse register components failed.")
            exitProcess(-1)
        }
    }

    fun initializeAll() {
        components.values.forEach(Consumer { obj: WeakReference<IComponent> -> obj.get()?.initialize() })
    }

    fun <T> get(clazz: Class<T>): T {
        return cast(components[clazz]!!.get())
    }

    fun getAsNode(clazz: Class<*>): Node? {
        return components[clazz]?.get() as Node?
    }

    fun getAsComponent(clazz: Class<*>): IComponent? {
        return components[clazz]?.get()
    }
}