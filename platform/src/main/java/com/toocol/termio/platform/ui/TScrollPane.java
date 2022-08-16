package com.toocol.termio.platform.ui;

import com.toocol.termio.platform.component.IActionAfterShow;
import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import javafx.scene.control.ScrollPane;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/11 22:38
 * @version: 0.0.1
 */
public abstract class TScrollPane extends ScrollPane implements IComponent, IStyleAble, IActionAfterShow {

    protected final long id;

    public TScrollPane(long id) {
        this.id = id;
        registerComponent(id);
    }

    @Override
    public long id() {
        return id;
    }
}
