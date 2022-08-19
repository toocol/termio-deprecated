package com.toocol.termio.platform.css;

import com.toocol.termio.utilities.log.Loggable;
import javafx.scene.Scene;

import java.util.Optional;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 16:53
 * @version: 0.0.1
 */
public class CssFileAnnotationParser implements Loggable {

    public void parse(Class<?> loaderClazz, Scene scene) {
        RegisterCssFile registerCss = loaderClazz.getAnnotation(RegisterCssFile.class);
        if (registerCss == null) {
            return;
        }
        for (String cssFileName : registerCss.name()) {
            Optional.ofNullable(loaderClazz.getResource("/" + loaderClazz.getPackageName() + "/" + cssFileName)).ifPresentOrElse(css -> {
                scene.getStylesheets().addAll(css.toExternalForm());
                info("Load css file success {}", cssFileName);
            }, () -> warn("Can not find css file {}", cssFileName));
        }
    }

}
