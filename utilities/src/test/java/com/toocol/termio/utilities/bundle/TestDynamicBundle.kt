package com.toocol.termio.utilities.bundle

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:46
 * @version: 0.0.1
 */
@BindPath(bundlePath = "/message/testDynamicBundle1.properties", languages = ["en", "zh"])
object TestDynamicBundle1 : DynamicBundle()

@BindPath(bundlePath = "/message/testDynamicBundle2.properties", languages = ["en", "zh"])
object TestDynamicBundle2 : DynamicBundle()

@BindPath(bundlePath = "/message/testDynamicBundle3.properties", languages = ["en", "zh"])
object TestDynamicBundle3 : DynamicBundle()

@BindPath(bundlePath = "/message/testDynamicBundle4.properties", languages = ["en", "zh"])
object TestDynamicBundle4 : DynamicBundle()