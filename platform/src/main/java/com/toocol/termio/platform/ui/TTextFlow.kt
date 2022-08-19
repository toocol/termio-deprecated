package com.toocol.termio.platform.ui;

import com.toocol.termio.platform.component.IActionAfterShow;
import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import javafx.scene.text.TextFlow;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/12 14:28
 */
public abstract class TTextFlow extends TextFlow implements IComponent, IStyleAble, IActionAfterShow {

    protected final long id;

    public TTextFlow(long id) {
        this.id = id;
        registerComponent(id);
    }

    @Override
    public void initialize() {

    }

    @Override
    public long id() {
        return id;
    }
}
