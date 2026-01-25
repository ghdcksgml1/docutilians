package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text

data class ErrorPanelRequest(
    val title: String,
    val message: String,
    val exception: Throwable? = null,
)

object ErrorPanel {
    fun of(errorPanelRequest: ErrorPanelRequest): Panel {
        return Panel(
            content = Text(errorPanelRequest.message),
            expand = false,
            title = Text("❗ ${Colors.Raw.error(errorPanelRequest.title)}"),
            borderStyle = Colors.Raw.errorBright,
            padding = Padding(top = 1, left = 3, right = 3, bottom = 1),
        )
    }
}
