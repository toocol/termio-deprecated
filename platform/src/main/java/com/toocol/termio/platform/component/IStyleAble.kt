package com.toocol.termio.platform.component;

import javafx.scene.Node;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 14:00
 */
public interface IStyleAble {
    String[] styleClasses();

    /**
     * add styles to components
     */
    default void styled() {
        if (!(this instanceof Node)) {
            return;
        }
        ((Node) this).getStyleClass().addAll(styleClasses());
    }
}
