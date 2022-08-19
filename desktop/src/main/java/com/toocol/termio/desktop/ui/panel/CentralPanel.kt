package com.toocol.termio.desktop.ui.panel

import com.toocol.termio.platform.ui.TBorderPane

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:13
 */
class CentralPanel(id: Long) : TBorderPane(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "central-panel"
        )
    }

    override fun initialize() {
        styled()
        setPrefSize(1280.0, 800.0)
    }

    override fun actionAfterShow() {}
}