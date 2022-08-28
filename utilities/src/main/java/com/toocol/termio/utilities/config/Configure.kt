package com.toocol.termio.utilities.config

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.ClassScanner
import org.ini4j.Profile
import java.util.*
import java.util.function.Consumer

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:09
 */
abstract class Configure<T : ConfigInstance> : Loggable, Castable {
    private val subConfigureMap: MutableMap<String, SubConfigure<out ConfigInstance>> = HashMap()

    /**
     * the section's names that configure class should process.
     */
    abstract fun section(): String

    /**
     * assemble the ini config.
     */
    abstract fun assemble(section: Profile.Section)

    fun assignAssembleJob(name: String, section: Profile.Section) {
        if (name.contains(".")) {
            Optional.ofNullable(subConfigureMap[name])
                .ifPresent { subConfigure: SubConfigure<out ConfigInstance> -> subConfigure.assemble(section) }
        } else {
            assemble(section)
        }
    }

    fun initSubConfigure() {
        val packageName = this.javaClass.packageName
        ClassScanner(packageName) { clazz: Class<*> ->
            Optional.ofNullable(clazz.superclass)
                .map { superClazz: Class<*> -> superClazz == SubConfigure::class.java }
                .orElse(false)
        }.scan().forEach(Consumer { subClazz: Class<*> ->
            try {
                val constructor = subClazz.getDeclaredConstructor()
                constructor.isAccessible = true
                val subConfigure = cast<SubConfigure<out ConfigInstance>>(constructor.newInstance())
                subConfigureMap[subConfigure.section()] = subConfigure
                info("Initialize sub configure success, class = {}", subClazz.name)
            } catch (e: Exception) {
                error("Initialize sub configure failed, class = {}", subClazz.name)
            }
        })
    }
}