package com.devtilians.docutilians.common

import com.devtilians.docutilians.llm.TokenUsage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Global state container for Docutilians CLI
 * Thread-safe access to shared configuration and metrics
 */
object GlobalState {
    private val mutex = Mutex()

    // ============================================
    // Config State
    // ============================================

    @Volatile
    private var _config: Config? = null

    val config: Config
        get() = _config ?: throw IllegalStateException("Config not initialized. Call initConfig() first.")

    val configOrNull: Config?
        get() = _config

    fun initConfig(config: Config) {
        _config = config
    }

    suspend fun updateConfig(block: (Config) -> Config) = mutex.withLock {
        _config = block(config)
    }

    // ============================================
    // Token Usage State
    // ============================================

    private val _tokenUsages = mutableListOf<TokenUsage>()

    val tokenUsage: TokenUsage
        get() =
            _tokenUsages.reduceOrNull { acc, tokenUsage -> acc + tokenUsage }
                ?: TokenUsage(0, 0, 0, 0.0)

    suspend fun addTokenUsage(tokenUsage: TokenUsage) =
        mutex.withLock { _tokenUsages.add(tokenUsage) }

    // ============================================
    // Utility
    // ============================================

    suspend fun clear() = mutex.withLock {
        _tokenUsages.clear()
        _config = null
    }

    suspend fun clearTokenUsage() = mutex.withLock {
        _tokenUsages.clear()
    }
}
