package com.toocol.termio.platform.component

import com.toocol.termio.utilities.log.Loggable
import javafx.scene.Node
import java.lang.reflect.Constructor
import kotlin.system.exitProcess

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 1:00
 * @version: 0.0.1
 */
class ComponentsAnnotationParser : Loggable {
    private val components: MutableList<IComponent> = ArrayList()
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
                components.add(iComponent)
            }
        } catch (e: Exception) {
            error("Parse register components failed.")
            exitProcess(-1)
        }
    }

    fun getComponents(): List<IComponent> {
        return components
    }
}