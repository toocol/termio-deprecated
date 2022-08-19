package com.toocol.termio.desktop.ui.homepage

import com.toocol.termio.platform.ui.TAnchorPane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:43
 * @version: 0.0.1
 */
class HomepagePanel(id: Long) : TAnchorPane(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "homepage-panel"
        )
    }

    override fun initialize() {
        styled()
    }

    override fun actionAfterShow() {}
}