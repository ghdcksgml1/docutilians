package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.widgets.Text

/**
 * CHUCK_UI Box Panel
 * Simple panel with separator lines (no box frame)
 */
data class BoxPanelRequest(
    val title: String,
    val titleColor: TextStyle = Colors.Raw.primary,
    val content: String,
    val contentColor: TextStyle = Colors.Raw.textWhite,
    val lineColor: TextStyle = Colors.Raw.textMuted,
    val expand: Boolean = true,
)

object BoxPanel {

    fun of(boxPanelRequest: BoxPanelRequest): Widget {
        val output = buildString {
            appendLine()
            appendLine(
                "  ${Colors.Raw.primary("◈")} " +
                    boxPanelRequest.titleColor(boxPanelRequest.title) +
                    " ${boxPanelRequest.lineColor("─".repeat(50))}"
            )
            appendLine()
            boxPanelRequest.content.lines().forEach { line ->
                appendLine("  ${boxPanelRequest.contentColor(line)}")
            }
            appendLine()
        }
        return Text(output)
    }
}
