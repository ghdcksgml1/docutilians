package com.devtilians.docutilians.cli.components.animation

import com.devtilians.docutilians.common.Config
import com.devtilians.docutilians.common.GlobalState
import com.devtilians.docutilians.constants.Colors
import com.devtilians.docutilians.llm.TokenUsage
import com.github.ajalt.mordant.animation.Animation
import com.github.ajalt.mordant.animation.textAnimation
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

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
            val filled = "█".repeat(state.current)
            val empty = "░".repeat(state.total - state.current)
            val progress = "[$filled$empty]"
            val percent = if (state.total > 0) (state.current * 100 / state.total) else 0

            buildString {
                appendLine()
                appendLine(Colors.Raw.textMuted("  ${"─".repeat(60)}"))

                // Status line with spinner
                appendLine(
                    "  ${Colors.Raw.primary(state.spinner)} ${Colors.Raw.textWhite(state.status)}"
                )

                // File info
                val fileDisplay = truncatePath(state.fileName, 50)
                appendLine(
                    "    ${Colors.Raw.textMuted("◈ TARGET:")} ${Colors.Raw.accent(fileDisplay)}"
                )

                appendLine()

                // Log display area
                if (state.logs.isNotEmpty()) {
                    state.logs.forEach { log ->
                        val logDisplay = if (log.length > 55) log.take(52) + "..." else log
                        appendLine(
                            "    ${Colors.Raw.textMuted("▸")} ${Colors.Raw.textMuted(logDisplay)}"
                        )
                    }
                    appendLine()
                }

                // Progress bar
                appendLine(
                    "  ${Colors.Raw.primary(progress)} ${Colors.Raw.textWhite("$percent%")} " +
                        Colors.Raw.textMuted("(${state.current}/${state.total})")
                )

                appendLine()

                // Success/Fail counters
                appendLine(
                    "  ${Colors.Raw.success("▲ ${state.success}")} " +
                        "${Colors.Raw.error("▼ ${state.fail}")}  " +
                        formatTokenUsage(GlobalState.tokenUsage)
                )

                append(Colors.Raw.textMuted("  ${"─".repeat(60)}"))
            }
        }

    private fun formatTokenUsage(usage: TokenUsage): String {
        val input = formatNumber(usage.inputTokens)
        val output = formatNumber(usage.outputTokens)
        val cost = String.format("%.4f", usage.dollarCost)

        return buildString {
            append(Colors.Raw.textMuted("IN:"))
            append(Colors.Raw.accent(" $input"))
            append(Colors.Raw.textMuted("  OUT:"))
            append(Colors.Raw.accent(" $output"))
            append(Colors.Raw.textMuted("  \$"))
            append(Colors.Raw.warning(cost))
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
