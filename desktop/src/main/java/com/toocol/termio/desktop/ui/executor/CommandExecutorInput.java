package com.toocol.termio.desktop.ui.executor;

import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodRequests;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.StyleClassedTextField;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 23:31
 * @version: 0.0.1
 */
public class CommandExecutorInput extends StyleClassedTextField implements IStyleAble, IComponent {

    protected final long id;

    public CommandExecutorInput(long id) {
        this.id = id;
    }

    @Override
    public String[] styleClasses() {
        return new String[] {
                "command-executor-input"
        };
    }

    @Override
    public void initialize() {
        styled();
        setEditable(true);
        setShowCaret(Caret.CaretVisibility.ON);

        CommandExecutorPanel executorPanel = findComponent(CommandExecutorPanel.class, id);
        executorPanel.setTop(this);
        setInputMethodRequests(new InputMethodRequestsObject());

        prefWidthProperty().bind(executorPanel.prefWidthProperty().multiply(1));
        prefHeightProperty().bind(executorPanel.prefHeightProperty().multiply(0.1));

    }

    @Override
    public long id() {
        return id;
    }

    private static class InputMethodRequestsObject implements InputMethodRequests {
        @Override
        public String getSelectedText() {
            return "";
        }

        @Override
        public int getLocationOffset(int x, int y) {
            return 0;
        }

        @Override
        public void cancelLatestCommittedText() {

        }

        @Override
        public Point2D getTextLocation(int offset) {
            return new Point2D(0, 0);
        }
    }
}
