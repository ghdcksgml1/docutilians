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

data class TextPanelRequest(
    val title: String? = null,
    val content: String,
    val lineColor: TextStyle = Colors.Raw.primary,
    val titleColor: TextStyle = Colors.Raw.secondary,
    val contentColor: TextStyle = Colors.Raw.textWhite,
    val width: Int = 77,
    val truncate: TruncateSide = TruncateSide.RIGHT,
    val ellipsis: String = "...",
)

object TextPanel {

    fun of(request: TextPanelRequest): Widget {
        val line = "═".repeat(request.width)

        val topLine =
            if (request.title != null) {
                val titleWithIcon = "🧾 ${request.title}"
                val padding = (request.width - titleWithIcon.length - 2) / 2
                "═".repeat(padding) +
                    " " +
                    request.titleColor(titleWithIcon) +
                    " " +
                    "═".repeat(padding)
            } else {
                line
            }

        val truncatedContent =
            request.content.lines().joinToString("\n") {
                truncateLine(it, request.width, request.truncate, request.ellipsis)
            }

        val output = buildString {
            appendLine(request.lineColor(topLine))
            appendLine(request.contentColor(truncatedContent))
            append(request.lineColor(line))
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
