package com.toocol.termio.utilities.bundle;

import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:34
 * @version: 0.0.1
 */
final class BundleMessage implements Loggable {
    private final String path;
    private final Locale locale;
    private final Properties messageProperties;

    BundleMessage(String path, Locale locale) {
        this.path = path;
        this.locale = locale;
        this.messageProperties = new Properties();
    }

    boolean load() {
        try (InputStream inputStream = BundleMessage.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                return false;
            }

            messageProperties.load(inputStream);
        } catch (IOException e) {
            warn("Load bundle message failed, path = {}, local = {}", path, locale.getLanguage());
            return false;
        }
        return true;
    }

    @Nullable
    String get(@Nonnull String key, Object... param) {
        String message = messageProperties.getProperty(key);
        if (StringUtils.isEmpty(message)) {
            return null;
        }

        return StrUtil.fullFillParam(message, param);
    }
}
