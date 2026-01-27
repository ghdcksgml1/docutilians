package com.devtilians.docutilians.utils.extensions

import com.anthropic.models.beta.messages.BetaMessage
import com.anthropic.models.messages.Model
import com.devtilians.docutilians.common.GlobalState
import com.devtilians.docutilians.llm.TokenUsage
import kotlin.jvm.optionals.getOrDefault

suspend fun BetaMessage.aggregateToken(): TokenUsage {
    val usage = this.usage()

    val dollarCost =
        when (this.model()) {
            Model.CLAUDE_HAIKU_4_5_20251001 -> {
                (usage.inputTokens().toDouble() / 1000) * 0.001 +
                    (usage.outputTokens().toDouble() / 1000) * 0.005 +
                    (usage.cacheCreationInputTokens().getOrDefault(0L).toDouble() / 1000) *
                        0.00125 +
                    (usage.cacheReadInputTokens().getOrDefault(0L).toDouble() / 1000) * 0.0001
            }
            Model.CLAUDE_SONNET_4_5_20250929 -> {
                (usage.inputTokens().toDouble() / 1000) * 0.003 +
                    (usage.outputTokens().toDouble() / 1000) * 0.015 +
                    (usage.cacheCreationInputTokens().getOrDefault(0L).toDouble() / 1000) *
                        0.00375 +
                    (usage.cacheReadInputTokens().getOrDefault(0L).toDouble() / 1000) * 0.0003
            }
            else -> {
                0.0
            }
        }

    val tokenUsage =
        TokenUsage(
            usage.inputTokens() + usage.cacheCreationInputTokens().getOrDefault(0L),
            usage.outputTokens(),
            usage.cacheReadInputTokens().getOrDefault(0L),
            dollarCost,
        )

    GlobalState.addTokenUsage(tokenUsage)

    return tokenUsage
}
