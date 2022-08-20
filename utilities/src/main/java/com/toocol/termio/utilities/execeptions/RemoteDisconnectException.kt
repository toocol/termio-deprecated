package com.toocol.termio.utilities.execeptions

import java.lang.RuntimeException

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/19 16:49
 */
class RemoteDisconnectException(message: String?) : RuntimeException(message)