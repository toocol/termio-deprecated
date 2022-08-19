package com.toocol.termio.platform.ui;

import com.toocol.termio.platform.component.IComponent;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 12:00
 */
public class TScene extends Scene implements IComponent {

    protected final long id;

    public TScene(long id, Parent root) {
        super(root);
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
