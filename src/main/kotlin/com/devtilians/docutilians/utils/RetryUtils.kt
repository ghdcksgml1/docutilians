package com.devtilians.docutilians.utils

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun <T> retry(
    times: Int = 3,
    delay: Duration = 1.seconds,
    retryOn: (Exception) -> Boolean = { true },
    block: suspend () -> T,
): T {
    var lastException: Exception? = null

    repeat(times) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (!retryOn(e)) {
                throw e // retry 대상 아니면 바로 throw
            }
            if (attempt < times - 1) {
                delay(delay)
            }
        }
    }

    throw lastException!!
}
