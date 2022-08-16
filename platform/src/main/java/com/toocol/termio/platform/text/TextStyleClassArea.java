package com.toocol.termio.platform.text;

import javafx.scene.Node;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.TextExt;
import org.fxmisc.richtext.model.Codec;
import org.fxmisc.richtext.model.SegmentOps;
import org.fxmisc.richtext.model.StyledSegment;
import org.fxmisc.richtext.model.TextOps;

import java.util.function.BiConsumer;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 17:43
 * @version: 0.0.1
 */
public class TextStyleClassArea extends GenericStyledArea<ParagraphStyle, String, TextStyle> {

    private final static TextOps<String, TextStyle> styledTextOps = SegmentOps.styledTextOps();

    protected ParagraphStyle paragraphStyle;
    protected TextStyle textStyle;

    public TextStyleClassArea() {
        super(
                ParagraphStyle.EMPTY,                                                   // default paragraph style
                (paragraph, style) -> paragraph.setStyle(style.toCss()),                // paragraph style setter
                TextStyle.EMPTY,                                                        // default segment style
                styledTextOps,                                                          // segment operations
                seg -> createNode(seg, (text, style) -> text.setStyle(style.toCss()))   // Node creator and segment style setter
        );
        paragraphStyle = ParagraphStyle.EMPTY;
        textStyle = TextStyle.EMPTY;
    }

    private static Node createNode(StyledSegment<String, TextStyle> seg,
                                   BiConsumer<? super TextExt, TextStyle> applyStyle) {
        return StyledTextArea.createStyledTextNode(seg.getSegment(), seg.getStyle(), applyStyle);
    }

    {
        setStyleCodecs(
                ParagraphStyle.CODEC,
                Codec.styledSegmentCodec(Codec.STRING_CODEC, TextStyle.CODEC)
        );
    }
}
