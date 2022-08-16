package com.toocol.termio.desktop.ui.sidebar;

import com.toocol.termio.desktop.ui.panel.CentralPanel;
import com.toocol.termio.desktop.ui.terminal.DesktopTerminalPanel;
import com.toocol.termio.platform.ui.TAnchorPane;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:26
 */
public class SessionManageSidebar extends TAnchorPane {

    public SessionManageSidebar(long id) {
        super(id);
    }

    @Override
    public String[] styleClasses() {
        return new String[]{
                "session-manage-sidebar"
        };
    }

    @Override
    public void initialize() {
        CentralPanel centralPanel = findComponent(CentralPanel.class, 1);

        styled();
        prefHeightProperty().bind(centralPanel.heightProperty());
        prefWidthProperty().bind(centralPanel.widthProperty().multiply(0.15));

        setOnMouseClicked(event -> {
            hide();
            findComponent(DesktopTerminalPanel.class, 1).prefWidthProperty().bind(centralPanel.widthProperty());
        });

        centralPanel.setLeft(this);
    }

    @Override
    public void actionAfterShow() {

    }
}
