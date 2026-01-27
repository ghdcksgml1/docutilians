package com.devtilians.docutilians.common

import com.devtilians.docutilians.llm.TokenUsage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GlobalState {
    private val mutex = Mutex()

    private val _tokenUsages = mutableListOf<TokenUsage>()
    val tokenUsage: TokenUsage
        get() =
            _tokenUsages.reduceOrNull { acc, tokenUsage -> acc + tokenUsage }
                ?: TokenUsage(0, 0, 0, 0.0)

    suspend fun addTokenUsage(tokenUsage: TokenUsage) =
        mutex.withLock { _tokenUsages.add(tokenUsage) }

    suspend fun clear() = mutex.withLock { _tokenUsages.clear() }
}
