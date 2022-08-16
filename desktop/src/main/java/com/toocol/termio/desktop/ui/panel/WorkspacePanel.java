package com.toocol.termio.desktop.ui.panel;

import com.toocol.termio.platform.ui.TBorderPane;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:39
 * @version: 0.0.1
 */
public class WorkspacePanel extends TBorderPane {

    public WorkspacePanel(long id) {
        super(id);
    }

    @Override
    public String[] styleClasses() {
        return new String[]{
                "workspace-panel"
        };
    }

    @Override
    public void initialize() {
        styled();
        CentralPanel centralPanel = findComponent(CentralPanel.class, 1);
        maxHeightProperty().bind(centralPanel.heightProperty());
        maxWidthProperty().bind(centralPanel.widthProperty().multiply(0.85));
        prefHeightProperty().bind(centralPanel.heightProperty());
        prefWidthProperty().bind(centralPanel.widthProperty().multiply(0.85));
        centralPanel.setCenter(this);
    }

    @Override
    public void actionAfterShow() {

    }
}
