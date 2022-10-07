package com.toocol.termio.utilities.config

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Asable
import com.toocol.termio.utilities.utils.Castable
import org.ini4j.Profile
import java.util.*

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:09
 */
abstract class Configure<T : ConfigInstance> : Loggable, Castable, Asable {
    private val subConfigureMap: MutableMap<String, SubConfigure<out ConfigInstance>> = HashMap()

    /**
     * the section's names that configure class should process.
     */
    abstract fun section(): String

    /**
     * assemble the ini config.
     */
    abstract fun assemble(section: Profile.Section)

    abstract fun subConfigures(): Array<SubConfigure<*>>

    fun assignAssembleJob(name: String, section: Profile.Section) {
        if (name.contains(".")) {
            Optional.ofNullable(subConfigureMap[name])
                .ifPresent { subConfigure: SubConfigure<out ConfigInstance> -> subConfigure.assemble(section) }
        } else {
            assemble(section)
        }
    }

    fun initSubConfigure() {
        subConfigures().forEach {
            try {
                subConfigureMap[it.section()] = it
                info("Initialize sub configure success, class = {}", it.javaClass.name)
            } catch (e: Exception) {
                error("Initialize sub configure failed, class = {}", it.javaClass.name)
            }
        }
    }
}