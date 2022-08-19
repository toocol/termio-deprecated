package com.toocol.termio.utilities.bundle

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.StrUtil
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.util.*
import javax.annotation.Nonnull

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:34
 * @version: 0.0.1
 */
internal class BundleMessage(private val path: String, private val locale: Locale) : Loggable {

    private val messageProperties: Properties = Properties()

    fun load(): Boolean {
        try {
            BundleMessage::class.java.getResourceAsStream(path).use { inputStream ->
                if (inputStream == null) {
                    return false
                }
                messageProperties.load(inputStream)
            }
        } catch (e: IOException) {
            warn("Load bundle message failed, path = {}, local = {}", path, locale.language)
            return false
        }
        return true
    }

    operator fun get(@Nonnull key: String, vararg fillParam: Any?): String? {
        val message = messageProperties.getProperty(key)
        return if (StringUtils.isEmpty(message)) null else StrUtil.fullFillParam(message, *fillParam)
    }
}