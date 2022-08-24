package com.toocol.termio.utilities.bundle

import com.google.common.collect.ImmutableMap
import java.util.*
import javax.annotation.Nonnull

/**
 * A DynamicBundle should be annotated with @BindPath
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:10
 * @version: 0.0.1
 */
abstract class DynamicBundle protected constructor() {
    private var bundleMessages: Map<Locale, BundleMessage>? = null
    private var initialize = false

    init {
        val bindPath = this.javaClass.getAnnotation(BindPath::class.java)

        takeIf { bindPath.languages.isNotEmpty() }?.let {
            val map: MutableMap<Locale, BundleMessage> = HashMap()
            var sucCnt = 0
            for (language in bindPath.languages) {
                val locale = Locale.forLanguageTag(language) ?: continue
                val bundleMessage = BundleMessage(bindPath.bundlePath + "_" + language, locale)
                val suc = bundleMessage.load()
                if (!suc) {
                    continue
                }
                sucCnt++
                map[locale] = bundleMessage
            }
            takeIf { sucCnt != 0 }?.let {
                bundleMessages = ImmutableMap.copyOf(map)
                initialize = true
            }
        }
    }

    fun message(@Nonnull locale: Locale = Locale.getDefault(), @Nonnull key: String, vararg fillParams: Any?): String? {
        if (!initialize) {
            return null
        }
        val bundleMessage = bundleMessages!![locale] ?: return null
        return bundleMessage.get(key, *fillParams)
    }
}