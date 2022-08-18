package com.toocol.termio.desktop.ui.terminal;

import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import javafx.scene.control.ScrollPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/11 23:15
 * @version: 0.0.1
 */
public class TerminalScrollPane extends VirtualizedScrollPane<TerminalConsoleTextArea> implements IStyleAble, IComponent {

    protected final long id;

    public TerminalScrollPane(long id, TerminalConsoleTextArea terminalConsoleTextArea) {
        super(terminalConsoleTextArea);
        this.id = id;
    }

    @Override
    public String[] styleClasses() {
        return new String[]{
                "terminal-scroll-pane"
        };
    }

    @Override
    public void initialize() {
        styled();
        setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        totalHeightEstimateProperty().addListener((ob, oldVal, newVal) -> getContent().requestFollowCaret());
    }

    @Override
    public long id() {
        return id;
    }

}
