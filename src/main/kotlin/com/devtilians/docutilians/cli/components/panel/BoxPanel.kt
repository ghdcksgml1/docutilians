package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text

/**
 * CHUCK_UI Glass Card Panel
 * Cyberpunk-styled panel with neon glow borders
 */
data class BoxPanelRequest(
    val title: String,
    val titleColor: TextStyle = Colors.Raw.primary,       // Neon Blue
    val content: String,
    val contentColor: TextStyle = Colors.Raw.textWhite,   // Light text
    val borderColor: TextStyle = Colors.Raw.secondary,    // Neon Pink border
    val expand: Boolean = true,
)

object BoxPanel {

    fun of(boxPanelRequest: BoxPanelRequest): Panel {
        return Panel(
            title = Text("◈ " + boxPanelRequest.titleColor(boxPanelRequest.title)),
            content = Text(boxPanelRequest.contentColor(boxPanelRequest.content)),
            expand = boxPanelRequest.expand,
            padding = Padding(top = 1, left = 2, right = 2, bottom = 1),
            borderStyle = boxPanelRequest.borderColor,
        )
    }
}
