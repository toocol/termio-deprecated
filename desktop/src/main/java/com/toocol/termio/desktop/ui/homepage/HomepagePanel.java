package com.toocol.termio.desktop.ui.homepage;

import com.toocol.termio.platform.ui.TAnchorPane;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:43
 * @version: 0.0.1
 */
public class HomepagePanel extends TAnchorPane {
    public HomepagePanel(long id) {
        super(id);
    }

    @Override
    public String[] styleClasses() {
        return new String[] {
                "homepage-panel"
        };
    }

    @Override
    public void initialize() {
        styled();
    }

    @Override
    public void actionAfterShow() {

    }

}
