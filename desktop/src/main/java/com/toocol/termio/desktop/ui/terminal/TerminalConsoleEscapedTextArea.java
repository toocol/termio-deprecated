package com.toocol.termio.desktop.ui.terminal;

import com.toocol.termio.platform.component.IComponent;
import com.toocol.termio.platform.component.IStyleAble;
import com.toocol.termio.platform.text.Cursor;
import com.toocol.termio.platform.text.TextStyle;
import com.toocol.termio.platform.text.EscapedTextStyleClassArea;
import com.toocol.termio.utilities.utils.Castable;
import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import javafx.geometry.Point2D;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.Caret;

/**
 * A logical line may become multiple physical rows when the window size changes:<br>
 * (logical line, physical line)
 * <p>
 * (1,1): ##################################<br>
 * (2,2): xxx<br>
 * | <br>
 * | When window resize<br>
 * ↓<br>
 * (1,1): #######################<br>
 * (1,2): ###########<br>
 * (2,3): xxx<br><br>
 * <p>
 * <b>Each logical line must be strictly separated by \n</b><br>
 * <br>
 * And by the way, the number of line controlled by Ansi Escape Sequence is actually <b>physical line</b>.<br>
 * For example, suppose we have such screen output now <b>(logical line, physical line)</b>:<br>
 * <p>
 * (1,1): #######################<br>
 * (1,2): ###########<br>
 * (1,3): xxx<br>
 * |<br>
 * | When input `ESC[2,0Ha`<br>
 * ↓<br>
 * (1,1): #######################<br>
 * (1,2): a##########<br>
 * (2,3): xxx<br>
 * | <br>
 * | When window resize<br>
 * ↓<br>
 * (1,1): #######################a##########<br>
 * (2,2): xxx<br>
 * </p>
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/6 14:06
 */
@SuppressWarnings("all")
public class TerminalConsoleEscapedTextArea extends EscapedTextStyleClassArea implements Castable, IComponent, IStyleAble {
    protected final long id;

    private final Cursor cursor;

    public TerminalConsoleEscapedTextArea(long id) {
        this.id = id;
        cursor = new Cursor(id);
    }

    @Override
    public String[] styleClasses() {
        return new String[] {
                "console-text-area"
        };
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public void initialize() {
        styled();
        setWrapText(true);
        setEditable(false);
        setUseInitialStyleForInsertion(true);
        setShowCaret(Caret.CaretVisibility.OFF);
        setInputMethodRequests(new InputMethodRequestsObject());

        updateDefaultChineseStyle(TextStyle.EMPTY.updateFontFamily("\"宋体\"").updateTextColor(Color.valueOf("#cccccc")).updateFontSize(9));
        updateDefaultEnglishStyle(TextStyle.EMPTY.updateFontFamily("\"Consolas\"").updateTextColor(Color.valueOf("#cccccc")).updateFontSize(10));

        DesktopTerminalPanel desktopTerminalPanel = findComponent(DesktopTerminalPanel.class, id);
        prefWidthProperty().bind(desktopTerminalPanel.prefWidthProperty().multiply(1));
        prefHeightProperty().bind(desktopTerminalPanel.prefHeightProperty().multiply(0.99));

        /*
         * Prevent auto caret movement when user pressed '←', '→', '↑', '↓', 'Home', 'PgUp', 'PgDn', instead of setting the caret manually
         */
        addEventFilter(KeyEvent.ANY, event -> {
            switch (event.getCode()) {
                case LEFT -> {
                    event.consume();
                    if (cursor.getInlinePosition() > getCurrentLineStartInParargraph()) {
                        cursor.moveLeft();
                    }
                }
                case RIGHT -> {
                    event.consume();
                    int index = getParagraphs().size() - 1;
                    if (cursor.getInlinePosition() < getParagraphLength(index)) {
                        cursor.moveRight();
                    }
                }
                case END -> {
                    cursor.setTo(getLength());
                }
                case UP, DOWN, PAGE_DOWN, PAGE_UP, HOME -> event.consume();
            }
        });

        textProperty().addListener((ob, oldVal, newVal) -> {
            cursor.update(newVal.length() - oldVal.length());
        });
    }

    public void append(String text) {
        if (StrUtil.isNewLine(text)) {
            cursor.setTo(getLength());
        }
        for (String splitText : StrUtil.splitSequenceByChinese(text)) {
            replace(
                    cursor.getInlinePosition(), cursor.getInlinePosition(),
                    (StrUtil.join(splitText.toCharArray(), CharUtil.INVISIBLE_CHAR) + CharUtil.INVISIBLE_CHAR).replaceAll(StrUtil.SPACE, StrUtil.NONE_BREAKING_SPACE),
                    StrUtil.isChineseSequenceByHead(splitText) ? defaultChineseTextStyle : defaultEnglishTextStyle
            );
        }
    }

    public int getPhysicLines() {
        return getParagraphs().size();
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
