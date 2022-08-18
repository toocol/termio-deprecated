package com.toocol.termio.utilities.bundle;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A DynamicBundle should be annotated with @BindPath
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:10
 * @version: 0.0.1
 */
public abstract class DynamicBundle {
    private final Map<Locale, BundleMessage> bundleMessages;

    private boolean initialize = false;

    protected DynamicBundle() {
        BindPath bindPath = this.getClass().getAnnotation(BindPath.class);
        int length = bindPath.languages().length;
        if (length == 0) {
            bundleMessages = null;
            return;
        }

        Map<Locale, BundleMessage> map = new HashMap<>();
        int sucCnt = 0;
        for (String language : bindPath.languages()) {
            Locale locale = Locale.forLanguageTag(language);
            if (locale == null) {
                continue;
            }
            BundleMessage bundleMessage = new BundleMessage(bindPath.bundlePath() + "_" + language, locale);
            boolean suc = bundleMessage.load();
            if (!suc) {
                continue;
            }
            sucCnt++;
            map.put(locale, bundleMessage);
        }
        if (sucCnt == 0) {
            bundleMessages = null;
            return;
        }
        bundleMessages = ImmutableMap.copyOf(map);
        initialize = true;
    }

    @Nullable
    public String message(@Nonnull Locale locale, @Nonnull String key, Object... params) {
        if (!initialize) {
            return null;
        }
        BundleMessage bundleMessage = bundleMessages.get(locale);
        if (bundleMessage == null) {
            return null;
        }
        return bundleMessage.get(key, params);
    }

    @Nullable
    public String message(@Nonnull String key, Object... params) {
        return message(Locale.ENGLISH, key, params);
    }

}
