package com.toocol.termio.desktop.ui.panel;

import com.toocol.termio.platform.ui.TBorderPane;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:13
 */
public class CentralPanel extends TBorderPane {

    public CentralPanel(long id) {
        super(id);
    }

    @Override
    public String[] styleClasses() {
        return new String[]{
                "central-panel"
        };
    }

    @Override
    public void initialize() {
        styled();
        setPrefSize(1280, 800);
    }

    @Override
    public void actionAfterShow() {

    }
}
