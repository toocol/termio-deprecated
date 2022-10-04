package com.toocol.termio.utilities.config

import com.google.common.collect.ImmutableMap
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.ClassScanner
import com.toocol.termio.utilities.utils.FileUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.ini4j.Ini
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/3 15:07
 */
object IniConfigLoader : Castable, Loggable{
    private var sectionConfigureMap: Map<String, Configure<out ConfigInstance>?>? = null
    private var configFileRootPath: String? = null
    private var configurePaths: Array<String>? = null

    fun setConfigFileRootPath(rootPath: String) {
        configFileRootPath = FileUtil.relativeToFixed(rootPath)
    }

    fun setConfigurePaths(configurePaths: Array<String>) {
        IniConfigLoader.configurePaths = configurePaths
    }

    @JvmStatic
    fun loadConfig() {
        if (StringUtils.isEmpty(configFileRootPath)) {
            warn("Config file root path is empty, skip config loading.")
            return
        }
        if (configurePaths == null || configurePaths!!.isEmpty()) {
            warn("Configure paths is empty, skip config loading.")
            return
        }
        val configures: MutableSet<Class<Configure<out ConfigInstance>>> = HashSet()
        for (configurePath in configurePaths!!) {
            configures.addAll(cast(ClassScanner(configurePath) { clazz: Class<*> ->
                Optional.of(clazz.superclass)
                    .map { superClass: Class<*> -> superClass == Configure::class.java }
                    .orElse(false)
            }.scan()))
        }

        sectionConfigureMap = ImmutableMap.copyOf(configures.stream()
            .map { clazz: Class<*> ->
                try {
                    val configure = clazz.getDeclaredConstructor().newInstance() as Configure<out ConfigInstance>
                    configure.initSubConfigure()
                    return@map configure
                } catch (e: Exception) {
                    error("Failed create Configure object, clazz = {}", clazz.name)
                }
                null
            }
            .filter { obj: Configure<out ConfigInstance>? -> Objects.nonNull(obj) }
            .collect(Collectors.toMap({ obj: Configure<out ConfigInstance>? -> obj!!.section() }, { configure: Configure<out ConfigInstance>? -> configure })))

        info("Initialize configures, size = ${sectionConfigureMap!!.size}")
        info("Reading ini config file, path = $configFileRootPath")
        read().forEach(Consumer { ini: Ini? ->
            for (section in ini!!.values) {
                val key = if (section.name.contains(".")) section.name.split("\\.").toTypedArray()[0] else section.name
                Optional.ofNullable(sectionConfigureMap!![key])
                    .ifPresent { configure: Configure<out ConfigInstance> ->
                        configure.assignAssembleJob(section.name, section)
                        info("Assemble ini config section, section = {}", section.name)
                    }
            }
        })
    }

    internal fun read(): Collection<Ini?> {
        return FileUtils.listFiles(configFileRootPath?.let { File(it) }, arrayOf("ini"), true)
            .stream()
            .map { file: File ->
                try {
                    return@map Ini(file)
                } catch (e: IOException) {
                    error("Read ini file failed, fileName = ", file.name)
                }
                null
            }
            .filter { obj: Ini? -> Objects.nonNull(obj) }
            .toList()
    }
}