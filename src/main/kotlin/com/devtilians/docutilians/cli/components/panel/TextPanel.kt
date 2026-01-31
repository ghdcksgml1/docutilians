package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.widgets.Text

enum class TruncateSide {
    /** .../project/src/main/kotlin/UserCustomTemplateController.kt */
    LEFT,

    /** This is a very long content that will be truncated at the ri... */
    RIGHT,
}

/**
 * CHUCK_UI Text Panel
 * Simple text display with separator lines (no box frame)
 */
data class TextPanelRequest(
    val title: String? = null,
    val content: String,
    val lineColor: TextStyle = Colors.Raw.textMuted,
    val titleColor: TextStyle = Colors.Raw.primary,
    val contentColor: TextStyle = Colors.Raw.textWhite,
    val width: Int = 70,
    val truncate: TruncateSide = TruncateSide.RIGHT,
    val ellipsis: String = "...",
)

object TextPanel {

    fun of(request: TextPanelRequest): Widget {
        val output = buildString {
            appendLine()

            // Title line
            if (request.title != null) {
                appendLine(
                    "  ${Colors.Raw.primary("◈")} " +
                        request.titleColor(request.title) +
                        " ${request.lineColor("─".repeat(maxOf(0, request.width - request.title.length - 5)))}"
                )
            } else {
                appendLine("  ${request.lineColor("─".repeat(request.width))}")
            }

            appendLine()

            // Content
            request.content.lines().forEach { line ->
                val truncated = truncateLine(line, request.width, request.truncate, request.ellipsis)
                appendLine("  ${request.contentColor(truncated)}")
            }

            appendLine()
            append("  ${request.lineColor("─".repeat(request.width))}")
        }

        return Text(output)
    }

    private fun truncateLine(
        line: String,
        width: Int,
        side: TruncateSide,
        ellipsis: String,
    ): String {
        if (line.length <= width) return line

        return when (side) {
            TruncateSide.RIGHT -> line.take(width - ellipsis.length) + ellipsis
            TruncateSide.LEFT -> ellipsis + line.takeLast(width - ellipsis.length)
        }
    }
}
