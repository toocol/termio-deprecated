package com.toocol.termio.platform.css

import com.toocol.termio.utilities.log.Loggable
import javafx.scene.Scene
import java.net.URL
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 16:53
 * @version: 0.0.1
 */
class CssFileAnnotationParser : Loggable {
    fun parse(loaderClazz: Class<*>, scene: Scene) {
        val registerCss = loaderClazz.getAnnotation(RegisterCssFile::class.java) ?: return
        for (cssFileName in registerCss.name) {
            Optional.ofNullable(loaderClazz.getResource("/" + loaderClazz.packageName + "/" + cssFileName))
                .ifPresentOrElse(
                    { css: URL ->
                        scene.stylesheets.addAll(css.toExternalForm())
                        info("Load css file success {}", cssFileName)
                    }) { warn("Can not find css file {}", cssFileName) }
        }
    }
}