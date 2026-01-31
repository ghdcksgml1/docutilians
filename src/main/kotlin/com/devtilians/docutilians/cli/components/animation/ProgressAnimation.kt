package com.devtilians.docutilians.cli.components.animation

import com.devtilians.docutilians.common.Config
import com.devtilians.docutilians.common.GlobalState
import com.devtilians.docutilians.constants.Colors
import com.devtilians.docutilians.llm.TokenUsage
import com.github.ajalt.mordant.animation.Animation
import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

data class ProgressState(
    val spinner: String = "⠋",
    val current: Int = 0,
    val total: Int = 0,
    val fileName: String = "-",
    val status: String = "Initializing...",
    val success: Int = 0,
    val fail: Int = 0,
    val logs: List<String> = emptyList(),
)

class ProgressAnimation(private val t: Terminal, private val config: Config) {

    private val spinnerFrames = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    private var frameIndex = 0
    private var currentState = ProgressState()
    private var currentLogs = mutableListOf<String>()

    private val animation: Animation<ProgressState> =
        t.textAnimation { state ->
            // Cyberpunk neon progress bar
            val filled = "▓".repeat(state.current)
            val empty = "░".repeat(state.total - state.current)
            val progress = "[$filled$empty]"
            val percent = if (state.total > 0) (state.current * 100 / state.total) else 0

            buildString {
                // Top border - Neon Pink glow
                appendLine(Colors.Raw.secondary("╔${"═".repeat(58)}╗"))

                // Status line with spinner
                appendLine(
                    Colors.Raw.secondary("║") +
                        " ${Colors.Raw.primary(state.spinner)} ${Colors.Raw.textWhite(state.status)}" +
                        " ".repeat(maxOf(0, 56 - state.status.length)) +
                        Colors.Raw.secondary("║")
                )

                // File info
                val fileDisplay = truncatePath(state.fileName, 45)
                appendLine(
                    Colors.Raw.secondary("║") +
                        "   ${Colors.Raw.textMuted("◈ TARGET:")} ${Colors.Raw.accent(fileDisplay)}" +
                        " ".repeat(maxOf(0, 44 - fileDisplay.length)) +
                        Colors.Raw.secondary("║")
                )

                appendLine(Colors.Raw.secondary("╠${"─".repeat(58)}╣"))

                // Log display area
                if (state.logs.isNotEmpty()) {
                    state.logs.forEach { log ->
                        val logDisplay = if (log.length > 52) log.take(49) + "..." else log
                        appendLine(
                            Colors.Raw.secondary("║") +
                                "   ${Colors.Raw.textMuted("▸")} ${Colors.Raw.textMuted(logDisplay)}" +
                                " ".repeat(maxOf(0, 53 - logDisplay.length)) +
                                Colors.Raw.secondary("║")
                        )
                    }
                    appendLine(Colors.Raw.secondary("╠${"─".repeat(58)}╣"))
                }

                // Progress bar - Neon Blue
                val progressDisplay = "$progress $percent%"
                appendLine(
                    Colors.Raw.secondary("║") +
                        "   ${Colors.Raw.primary(progress)} ${Colors.Raw.textWhite("$percent%")}" +
                        " (${state.current}/${state.total})" +
                        " ".repeat(maxOf(0, 35 - state.total.toString().length * 2)) +
                        Colors.Raw.secondary("║")
                )

                appendLine(Colors.Raw.secondary("╠${"─".repeat(58)}╣"))

                // Success/Fail counters
                appendLine(
                    Colors.Raw.secondary("║") +
                        "   ${Colors.Raw.success("▲ SUCCESS: ${state.success}")}" +
                        "  ${Colors.Raw.error("▼ FAILED: ${state.fail}")}" +
                        " ".repeat(maxOf(0, 30 - state.success.toString().length - state.fail.toString().length)) +
                        Colors.Raw.secondary("║")
                )

                appendLine(Colors.Raw.secondary("╠${"─".repeat(58)}╣"))

                // Token usage panel
                append(formatTokenUsage(GlobalState.tokenUsage))

                // Bottom border
                appendLine()
                append(Colors.Raw.secondary("╚${"═".repeat(58)}╝"))
            }
        }

    private fun formatTokenUsage(usage: TokenUsage): String {
        val input = formatNumber(usage.inputTokens)
        val output = formatNumber(usage.outputTokens)
        val cached = formatNumber(usage.cachedTokens)
        val cost = String.format("%.4f", usage.dollarCost)

        return buildString {
            // Token Usage Header
            appendLine(
                Colors.Raw.secondary("║") +
                    "   ${Colors.Raw.primary("◆ TOKEN USAGE")}" +
                    " ".repeat(43) +
                    Colors.Raw.secondary("║")
            )

            // Input/Output tokens
            val tokensLine = "${Colors.Raw.textMuted("IN:")} ${Colors.Raw.accent(input)}  " +
                "${Colors.Raw.textMuted("OUT:")} ${Colors.Raw.accent(output)}"
            appendLine(
                Colors.Raw.secondary("║") +
                    "     $tokensLine" +
                    " ".repeat(maxOf(0, 35 - input.length - output.length)) +
                    Colors.Raw.secondary("║")
            )

            // Cache + Cost
            val cachePart = if (usage.cachedTokens > 0) {
                "${Colors.Raw.textMuted("CACHE:")} ${Colors.Raw.success(cached)}  "
            } else ""
            val costLine = "$cachePart${Colors.Raw.textMuted("COST:")} ${Colors.Raw.warning("\$$cost")}"
            append(
                Colors.Raw.secondary("║") +
                    "     $costLine" +
                    " ".repeat(maxOf(0, 35 - cached.length - cost.length)) +
                    Colors.Raw.secondary("║")
            )
        }
    }

    private fun formatNumber(n: Long): String =
        when {
            n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
            n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
            else -> n.toString()
        }

    fun start(total: Int) {
        currentState = ProgressState(total = total)
        animation.update(currentState)
    }

    fun updateStatus(status: String, fileName: String? = null) {
        frameIndex = (frameIndex + 1) % spinnerFrames.size
        currentState =
            currentState.copy(
                spinner = spinnerFrames[frameIndex],
                status = status,
                fileName = fileName ?: currentState.fileName,
                logs = currentLogs.toList(),
            )
        animation.update(currentState)
    }

    fun addLog(log: String, maxLogs: Int = 5) {
        currentLogs.add(truncatePath(log, 50))
        if (currentLogs.size > maxLogs) {
            currentLogs.removeAt(0)
        }
        updateStatus(currentState.status)
    }

    fun clearLogs() {
        currentLogs.clear()
    }

    fun incrementSuccess() {
        currentState =
            currentState.copy(
                current = currentState.current + 1,
                success = currentState.success + 1,
            )
        animation.update(currentState)
    }

    suspend fun incrementFail(e: Throwable) {
        this.incrementFail(e.message)
    }

    suspend fun incrementFail(message: String? = null) {
        message?.let { addLog("Error: ${Colors.error(message)}") }

        currentState =
            currentState.copy(current = currentState.current + 1, fail = currentState.fail + 1)
        animation.update(currentState)

        delay(3.seconds)
    }

    fun stop() {
        animation.stop()
    }

    fun getResult(): Pair<Int, Int> = currentState.success to currentState.fail

    private fun truncatePath(path: String, maxLen: Int): String {
        if (path.length <= maxLen) return path
        return "..." + path.takeLast(maxLen - 3)
    }
}
