package com.toocol.termio.platform.ui;

import com.toocol.termio.platform.component.IActionAfterShow;
import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import javafx.scene.layout.FlowPane;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:02
 */
public abstract class TFlowPane extends FlowPane implements IComponent, IStyleAble, IActionAfterShow {

    protected final long id;

    public TFlowPane(long id) {
        this.id = id;
        registerComponent(id);
    }

    @Override
    public long id() {
        return id;
    }
}
