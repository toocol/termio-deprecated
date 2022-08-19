package com.toocol.termio.platform.ui;

import com.toocol.termio.platform.component.IActionAfterShow;
import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import javafx.scene.layout.AnchorPane;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 10:37
 */
public abstract class TAnchorPane extends AnchorPane implements IComponent, IStyleAble, IActionAfterShow {

    protected final long id;

    public TAnchorPane(long id) {
        this.id = id;
        registerComponent(id);
    }

    @Override
    public long id() {
        return id;
    }
}
