package com.toocol.termio.platform.text

import javafx.scene.text.TextAlignment
import javafx.scene.paint.Color
import java.lang.StringBuilder
import java.lang.AssertionError
import org.fxmisc.richtext.model.Codec
import kotlin.Throws
import java.io.IOException
import java.io.DataOutputStream
import java.io.DataInputStream
import java.util.*
import kotlin.math.max

/**
 * Holds information about the style of a paragraph.
 */
class ParagraphStyle private constructor(
    val alignment: Optional<TextAlignment> = Optional.empty(),
    val backgroundColor: Optional<Color> = Optional.empty(),
    val indent: Optional<Indent> = Optional.empty(),
    val foldCount: Int = 0
) {
    override fun hashCode(): Int {
        return Objects.hash(alignment, backgroundColor, indent, foldCount)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ParagraphStyle) {
            alignment == other.alignment &&
                    backgroundColor == other.backgroundColor &&
                    indent == other.indent && foldCount == other.foldCount
        } else {
            false
        }
    }

    override fun toString(): String {
        return toCss()
    }

    fun toCss(): String {
        val sb = StringBuilder()
        alignment.ifPresent { al: TextAlignment? ->
            val cssAlignment: String = when (al) {
                TextAlignment.LEFT -> "left"
                TextAlignment.CENTER -> "center"
                TextAlignment.RIGHT -> "right"
                TextAlignment.JUSTIFY -> "justify"
                else -> throw AssertionError("unreachable code")
            }
            sb.append("-fx-text-alignment: $cssAlignment;")
        }
        backgroundColor.ifPresent { color: Color? -> sb.append("-fx-background-color: " + TextStyle.cssColor(color) + ";") }
        if (foldCount > 0) sb.append("visibility: collapse;")
        return sb.toString()
    }

    fun updateWith(mixin: ParagraphStyle): ParagraphStyle {
        return ParagraphStyle(
            if (mixin.alignment.isPresent) mixin.alignment else alignment,
            if (mixin.backgroundColor.isPresent) mixin.backgroundColor else backgroundColor,
            if (mixin.indent.isPresent) mixin.indent else indent,
            mixin.foldCount + foldCount)
    }

    fun updateAlignment(alignment: TextAlignment): ParagraphStyle {
        return ParagraphStyle(Optional.of(alignment), backgroundColor, indent, foldCount)
    }

    fun updateBackgroundColor(backgroundColor: Color): ParagraphStyle {
        return ParagraphStyle(alignment, Optional.of(backgroundColor), indent, foldCount)
    }

    private fun updateIndent(indent: Indent?): ParagraphStyle {
        return ParagraphStyle(alignment, backgroundColor, Optional.ofNullable(indent), foldCount)
    }

    fun increaseIndent(): ParagraphStyle {
        return updateIndent(indent.map { obj: Indent -> obj.increase() }
            .orElseGet { Indent() })
    }

    fun decreaseIndent(): ParagraphStyle {
        return updateIndent(indent.filter { `in`: Indent -> `in`.level > 1 }
            .map { obj: Indent -> obj.decrease() }.orElse(null))
    }

    fun getIndent(): Indent {
        return indent.get()
    }

    val isIndented: Boolean
        get() = indent.map { `in`: Indent -> `in`.level > 0 }.orElse(false)

    fun updateFold(fold: Boolean): ParagraphStyle {
        val foldLevels = if (fold) foldCount + 1 else max(0, foldCount - 1)
        return ParagraphStyle(alignment, backgroundColor, indent, foldLevels)
    }

    val isFolded: Boolean
        get() = foldCount > 0

    companion object {
        val EMPTY = ParagraphStyle()
        val CODEC: Codec<ParagraphStyle> = object : Codec<ParagraphStyle> {
            private val OPT_ALIGNMENT_CODEC = Codec.optionalCodec(Codec.enumCodec(
                TextAlignment::class.java))
            private val OPT_COLOR_CODEC = Codec.optionalCodec(Codec.COLOR_CODEC)
            override fun getName(): String {
                return "par-style"
            }

            @Throws(IOException::class)
            override fun encode(os: DataOutputStream, t: ParagraphStyle) {
                OPT_ALIGNMENT_CODEC.encode(os, t.alignment)
                OPT_COLOR_CODEC.encode(os, t.backgroundColor)
                os.writeInt(t.indent.map { i: Indent -> i.level }.orElse(0))
                os.writeInt(t.foldCount)
            }

            @Throws(IOException::class)
            override fun decode(`is`: DataInputStream): ParagraphStyle {
                return ParagraphStyle(
                    OPT_ALIGNMENT_CODEC.decode(`is`),
                    OPT_COLOR_CODEC.decode(`is`),
                    Optional.of(Indent(`is`.readInt())),
                    `is`.readInt())
            }
        }

        fun alignLeft(): ParagraphStyle {
            return EMPTY.updateAlignment(TextAlignment.LEFT)
        }

        fun alignCenter(): ParagraphStyle {
            return EMPTY.updateAlignment(TextAlignment.CENTER)
        }

        fun alignRight(): ParagraphStyle {
            return EMPTY.updateAlignment(TextAlignment.RIGHT)
        }

        fun alignJustify(): ParagraphStyle {
            return EMPTY.updateAlignment(TextAlignment.JUSTIFY)
        }

        fun backgroundColor(color: Color): ParagraphStyle {
            return EMPTY.updateBackgroundColor(color)
        }

        fun folded(): ParagraphStyle {
            return EMPTY.updateFold(java.lang.Boolean.TRUE)
        }

        fun unfolded(): ParagraphStyle {
            return EMPTY.updateFold(java.lang.Boolean.FALSE)
        }
    }
}