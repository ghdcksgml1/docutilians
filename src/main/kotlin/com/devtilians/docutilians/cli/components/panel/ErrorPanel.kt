package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text

/** CHUCK_UI Error Panel Cyberpunk-styled error display with neon red glow */
data class ErrorPanelRequest(
    val title: String,
    val message: String,
    val exception: Throwable? = null,
)

object ErrorPanel {
    fun of(errorPanelRequest: ErrorPanelRequest): Panel {
        val errorContent = buildString {
            append(Colors.Raw.textWhite(errorPanelRequest.message))
            if (errorPanelRequest.exception != null) {
                appendLine()
                append(Colors.Raw.textMuted("◈ ${errorPanelRequest.exception::class.simpleName}"))
            }
        }

        return Panel(
            content = Text(errorContent),
            expand = false,
            title =
                Text(
                    "⚠ ${Colors.Raw.error("ERROR")} ${Colors.Raw.errorBright("▸")} ${Colors.Raw.textWhite(errorPanelRequest.title)}"
                ),
            borderStyle = Colors.Raw.error,
            padding = Padding(top = 1, left = 2, right = 2, bottom = 1),
        )
    }
}
