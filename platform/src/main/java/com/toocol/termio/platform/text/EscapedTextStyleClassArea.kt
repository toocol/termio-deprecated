package com.toocol.termio.platform.text

import com.google.common.collect.ImmutableMap
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.utilities.escape.AnsiEscapeSearchEngine
import com.toocol.termio.utilities.escape.EscapeCodeSequenceSupporter
import com.toocol.termio.utilities.escape.EscapeCursorControlMode
import com.toocol.termio.utilities.escape.IEscapeMode
import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction
import javafx.scene.Node
import javafx.scene.text.TextFlow
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.StyledTextArea
import org.fxmisc.richtext.TextExt
import org.fxmisc.richtext.model.Codec
import org.fxmisc.richtext.model.SegmentOps
import org.fxmisc.richtext.model.StyledSegment
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 17:43
 * @version: 0.0.1
 */
abstract class EscapedTextStyleClassArea(private val id: Long) : GenericStyledArea<ParagraphStyle, String, TextStyle>(
    // default paragraph style
    ParagraphStyle.EMPTY,
    // paragraph style setter
    BiConsumer { paragraph: TextFlow, style: ParagraphStyle ->
        paragraph.style = style.toCss()
    },
    // default segment style
    TextStyle.EMPTY,
    // segment operations
    styledTextOps,
    // Node creator and segment style setter
    Function<StyledSegment<String, TextStyle>, Node> { seg: StyledSegment<String, TextStyle> ->
        createNode(seg) { text: TextExt, style: TextStyle ->
            text.style = style.toCss()
        }
    }
), EscapeCodeSequenceSupporter<EscapedTextStyleClassArea>, IComponent {

    protected var paragraphStyle: ParagraphStyle = ParagraphStyle.EMPTY

    @JvmField
    protected var defaultChineseTextStyle: TextStyle

    @JvmField
    protected var defaultEnglishTextStyle: TextStyle

    protected var cursor: Cursor

    private var ansiEscapeSearchEngine: AnsiEscapeSearchEngine<EscapedTextStyleClassArea>? = null
    private var actionMap: Map<Class<out IEscapeMode>, AnsiEscapeAction<EscapedTextStyleClassArea>>? = null

    fun updateDefaultChineseStyle(style: TextStyle) {
        defaultChineseTextStyle = style
    }

    fun updateDefaultEnglishStyle(style: TextStyle) {
        defaultEnglishTextStyle = style
    }

    override fun getActionMap(): Map<Class<out IEscapeMode>, AnsiEscapeAction<EscapedTextStyleClassArea>>? {
        return actionMap
    }

    override fun printOut(text: String) {}

    private fun registerActions() {
        val actions: MutableList<AnsiEscapeAction<EscapedTextStyleClassArea>> = ArrayList()
        actions.add(EscapeCursorControlAction())
        val map: MutableMap<Class<out IEscapeMode>, AnsiEscapeAction<EscapedTextStyleClassArea>> = HashMap()
        for (action in actions) {
            map[action.focusMode()] = action
        }
        actionMap = ImmutableMap.copyOf(map)
    }

    private class EscapeCursorControlAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeCursorControlMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>) {}
    }

    init {
        this.setStyleCodecs(
            ParagraphStyle.CODEC,
            Codec.styledSegmentCodec(Codec.STRING_CODEC, TextStyle.CODEC)
        )
        registerActions()

        defaultChineseTextStyle = TextStyle.EMPTY
        defaultEnglishTextStyle = TextStyle.EMPTY
        cursor = Cursor(id)
        ansiEscapeSearchEngine = AnsiEscapeSearchEngine()
    }

    override fun id(): Long {
        return id
    }

    companion object {
        private val styledTextOps = SegmentOps.styledTextOps<TextStyle>()
        private fun createNode(
            seg: StyledSegment<String, TextStyle>,
            applyStyle: BiConsumer<in TextExt, TextStyle>,
        ): Node {
            return StyledTextArea.createStyledTextNode(seg.segment, seg.style, applyStyle)
        }
    }
}