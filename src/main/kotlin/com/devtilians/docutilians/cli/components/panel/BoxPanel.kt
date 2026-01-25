package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.Panel
import com.github.ajalt.mordant.widgets.Text

data class BoxPanelRequest(
    val title: String,
    val titleColor: TextStyle = Colors.Raw.primary,
    val content: String,
    val contentColor: TextStyle = Colors.Raw.textWhite,
    val borderColor: TextStyle = Colors.Raw.borderPrimary,
    val expand: Boolean = true,
)

object BoxPanel {

    fun of(boxPanelRequest: BoxPanelRequest): Panel {
        return Panel(
            title = Text("🧾 " + boxPanelRequest.titleColor(boxPanelRequest.title)),
            content = Text(boxPanelRequest.contentColor(boxPanelRequest.content)),
            expand = boxPanelRequest.expand,
            padding = Padding(all = 1),
            borderStyle = boxPanelRequest.borderColor,
        )
    }
}
