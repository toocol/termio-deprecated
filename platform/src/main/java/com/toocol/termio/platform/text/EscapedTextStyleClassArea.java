package com.toocol.termio.platform.text;

import com.toocol.termio.utilities.escape.AnsiEscapeSearchEngine;
import com.toocol.termio.utilities.escape.EscapeCodeSequenceSupporter;
import com.toocol.termio.utilities.escape.EscapeCursorControlMode;
import com.toocol.termio.utilities.escape.IEscapeMode;
import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction;
import javafx.scene.Node;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Codec;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.StyledSegment;
import org.fxmisc.richtext.model.TextOps;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 17:43
 * @version: 0.0.1
 */
public class EscapedTextStyleClassArea extends GenericStyledArea<ParagraphStyle, String, TextStyle> implements EscapeCodeSequenceSupporter {

    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();

    private final AnsiEscapeSearchEngine<EscapedTextStyleClassArea> ansiEscapeSearchEngine = new AnsiEscapeSearchEngine<>(this);

    protected ParagraphStyle paragraphStyle;
    protected TextStyle defaultChineseTextStyle;
    protected TextStyle defaultEnglishTextStyle;
    protected Cursor cursor;

    public EscapedTextStyleClassArea() {
        super(
                ParagraphStyle.EMPTY,                                                   // default paragraph style
                (paragraph, style) -> paragraph.setStyle(style.toCss()),                // paragraph style setter
                TextStyle.EMPTY,                                                        // default segment style
                styledTextOps,                                                          // segment operations
                seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss()))   // Node creator and segment style setter
        );
        paragraphStyle = ParagraphStyle.EMPTY;
        defaultChineseTextStyle = TextStyle.EMPTY;
        defaultEnglishTextStyle = TextStyle.EMPTY;
    }

    public void updateDefaultChineseStyle(TextStyle style) {
        defaultChineseTextStyle = style;
    }

    public void updateDefaultEnglishStyle(TextStyle style) {
        defaultEnglishTextStyle = style;
    }

    private static Node createNode(StyledSegment<String, TextStyle> seg,
                                   BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return StyledTextArea.createStyledTextNode(seg.getSegment(), seg.getStyle(), applyStyle);
    }

    @Override
    public void printOut(String text) {

    }

    public static class EscapeCursorControlAction extends AnsiEscapeAction<EscapedTextStyleClassArea> {

        @Override
        public Class<? extends IEscapeMode> focusMode() {
            return EscapeCursorControlMode.class;
        }

        @Override
        public void action(EscapedTextStyleClassArea executeTarget, IEscapeMode escapeMode, List<Object> params) {

        }
    }

    {
        setStyleCodecs(
                ParagraphStyle.CODEC,
                Codec.styledSegmentCodec(Codec.STRING_CODEC, TextStyle.CODEC)
        );
        List<AnsiEscapeAction<EscapedTextStyleClassArea>> actionList = new ArrayList<>();
        actionList.add(new EscapeCursorControlAction());
        ansiEscapeSearchEngine.registerActions(actionList);
    }
}
