package com.toocol.termio.platform.font

import com.toocol.termio.utilities.log.Loggable
import javafx.scene.text.Font
import java.io.InputStream
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 16:53
 * @version: 0.0.1
 */
class FontFileAnnotationParser : Loggable {
    fun parse(loaderClazz: Class<*>) {
        val registerFont = loaderClazz.getAnnotation(RegisterFontFile::class.java) ?: return
        for (fontFileName in registerFont.name) {
            Optional.ofNullable(loaderClazz.getResourceAsStream("/" + loaderClazz.packageName + "/" + fontFileName))
                .ifPresentOrElse(
                    { font: InputStream ->
                        Font.loadFont(font, 1.0)
                        info("Load font file success {}", fontFileName)
                    }) { warn("Can not find font file {}", fontFileName) }
        }
    }
}