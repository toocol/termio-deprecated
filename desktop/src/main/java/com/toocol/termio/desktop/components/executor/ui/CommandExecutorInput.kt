package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.beans.property.StringProperty
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.text.Text

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 23:31
 * @version: 0.0.1
 */
class CommandExecutorInput(private val id: Long) : AnchorPane(), IStyleAble, IComponent {

    private val text = AnchorPane()
    private val textField = TextField()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-input"
        )
    }

    override fun initialize() {
        apply {
            styled()
            children.addAll(text, textField)

            setTopAnchor(text, 8.0)
            setTopAnchor(textField, 5.0)

            setBottomAnchor(textField, 0.0)

            setLeftAnchor(text, 5.0)
            setLeftAnchor(textField, 77.0)

            setRightAnchor(textField, 10.0)

            focusedProperty().addListener {_, _, nv ->
                takeIf { nv }.run { textField.requestFocus() }
            }
        }

        text.apply {
            styleClass.add("command-executor-input-text")
            val textEle = Text("termio >")
            textEle.styleClass.add("text")
            children.add(textEle)
            setTopAnchor(textEle, 0.0)
            setLeftAnchor(textEle, 0.0)
        }

        textField.apply {
            isEditable = true
        }
    }

    override fun id(): Long {
        return id
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        prefHeight = 30.0
        maxHeight = 30.0
        minHeight = 30.0
    }

    fun clear() {
        textField.clear()
    }

    fun text(): String {
        return textField.text
    }

    fun textProperty(): StringProperty {
        return textField.textProperty()
    }

    fun isFocus(): Boolean {
        return textField.isFocused
    }
}