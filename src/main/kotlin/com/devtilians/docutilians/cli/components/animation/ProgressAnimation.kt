package com.devtilians.docutilians.cli.components.animation

import com.devtilians.docutilians.common.Config
import com.devtilians.docutilians.constants.Colors
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
            val progress =
                "[${"█".repeat(state.current)}${"░".repeat(state.total - state.current)}]"
            val percent = if (state.total > 0) (state.current * 100 / state.total) else 0

            buildString {
                appendLine(Colors.Raw.primary("─".repeat(60)))
                appendLine("${Colors.Raw.secondary(state.spinner)} ${state.status}")
                appendLine(
                    "   ${Colors.Raw.textMuted("File:")} ${Colors.Raw.accent(state.fileName)}"
                )
                appendLine()

                // 로그 표시 영역
                if (state.logs.isNotEmpty()) {
                    state.logs.forEach { log ->
                        appendLine("   ${Colors.Raw.textMuted("→")} ${Colors.Raw.textMuted(log)}")
                    }
                    appendLine()
                }

                appendLine(
                    "   ${Colors.Raw.primary(progress)} ${percent}% (${state.current}/${state.total})"
                )
                appendLine()
                appendLine(
                    "   ${Colors.Raw.success("✓ ${state.success}")}  ${Colors.Raw.error("✗ ${state.fail}")}"
                )
                append(Colors.Raw.primary("─".repeat(60)))
            }
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
