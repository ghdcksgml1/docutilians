package com.devtilians.docutilians.llm

data class TokenUsage(
    val inputTokens: Long,
    val outputTokens: Long,
    val cachedTokens: Long = 0L,
    val dollarCost: Double = 0.0,
) {
    operator fun plus(other: TokenUsage): TokenUsage {
        return TokenUsage(
            inputTokens + other.inputTokens,
            outputTokens + other.outputTokens,
            cachedTokens + other.cachedTokens,
            dollarCost + other.dollarCost,
        )
    }
}
