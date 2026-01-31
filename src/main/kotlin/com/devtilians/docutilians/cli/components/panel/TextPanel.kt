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
 * Cyberpunk-styled text display with neon borders
 */
data class TextPanelRequest(
    val title: String? = null,
    val content: String,
    val lineColor: TextStyle = Colors.Raw.secondary,     // Neon Pink borders
    val titleColor: TextStyle = Colors.Raw.primary,      // Neon Blue title
    val contentColor: TextStyle = Colors.Raw.textWhite,  // Light content
    val width: Int = 77,
    val truncate: TruncateSide = TruncateSide.RIGHT,
    val ellipsis: String = "...",
)

object TextPanel {

    fun of(request: TextPanelRequest): Widget {
        val topBorder = "═".repeat(request.width)
        val bottomBorder = "═".repeat(request.width)

        val topLine =
            if (request.title != null) {
                val titleWithIcon = "◈ ${request.title}"
                val totalPadding = request.width - titleWithIcon.length - 2
                val leftPad = totalPadding / 2
                val rightPad = totalPadding - leftPad
                "╔" + "═".repeat(leftPad) +
                    " " +
                    request.titleColor(titleWithIcon) +
                    " " +
                    "═".repeat(rightPad) + "╗"
            } else {
                "╔$topBorder╗"
            }

        val truncatedContent =
            request.content.lines().joinToString("\n") { line ->
                val truncated = truncateLine(line, request.width - 4, request.truncate, request.ellipsis)
                "║ ${request.contentColor(truncated)}${" ".repeat(maxOf(0, request.width - truncated.length - 3))}║"
            }

        val output = buildString {
            appendLine(request.lineColor(topLine))
            appendLine(truncatedContent)
            append(request.lineColor("╚${bottomBorder}╝"))
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
