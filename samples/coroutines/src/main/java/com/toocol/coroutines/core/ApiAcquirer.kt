package com.toocol.coroutines.core

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 20:49
 * @version: 0.0.1
 */
interface AbstractApi : Asable

interface ApiAcquirer {
    @DelicateCoroutinesApi
    suspend fun <T : AbstractApi, R, Z> R.api(
        api: T,
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend T.() -> Z
    ) : Z {
        return withContext(context) {
            block(api)
        }
    }
}