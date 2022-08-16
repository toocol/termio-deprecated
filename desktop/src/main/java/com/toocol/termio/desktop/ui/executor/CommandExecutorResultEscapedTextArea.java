package com.toocol.termio.desktop.ui.executor;

import com.toocol.termio.platform.text.Cursor;
import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import com.toocol.termio.platform.text.TextStyle;
import com.toocol.termio.platform.text.EscapedTextStyleClassArea;
import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import javafx.scene.paint.Color;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 23:33
 * @version: 0.0.1
 */
public class CommandExecutorResultEscapedTextArea extends EscapedTextStyleClassArea implements IStyleAble, IComponent {

    protected final long id;

    private final Cursor cursor;

    public CommandExecutorResultEscapedTextArea(long id) {
        this.id = id;
        cursor = new Cursor(id);
    }

    @Override
    public String[] styleClasses() {
        return new String[] {
                "command-executor-result-text-area"
        };
    }

    @Override
    public void initialize() {
        styled();
        setWrapText(true);
        setEditable(false);

        CommandExecutorPanel executorPanel = findComponent(CommandExecutorPanel.class, id);
        prefWidthProperty().bind(executorPanel.prefWidthProperty().multiply(1));
        prefHeightProperty().bind(executorPanel.prefHeightProperty().multiply(0.9));

        updateDefaultChineseStyle(TextStyle.EMPTY.updateFontFamily("\"宋体\"").updateTextColor(Color.valueOf("#cccccc")).updateFontSize(9));
        updateDefaultEnglishStyle(TextStyle.EMPTY.updateFontFamily("\"Consolas\"").updateTextColor(Color.valueOf("#cccccc")).updateFontSize(10));
    }

    public void append(String text) {
        if (StrUtil.isNewLine(text)) {
            cursor.setTo(getLength());
        }
        for (String splitText : StrUtil.splitSequenceByChinese(text)) {
            replace(
                    getCaretPosition(), getCaretPosition(),
                    (StrUtil.join(splitText.toCharArray(), CharUtil.INVISIBLE_CHAR) + CharUtil.INVISIBLE_CHAR).replaceAll(StrUtil.SPACE, StrUtil.NONE_BREAKING_SPACE),
                    StrUtil.isChineseSequenceByHead(splitText) ? defaultChineseTextStyle :defaultEnglishTextStyle
            );
        }
    }

    @Override
    public long id() {
        return id;
    }
}
