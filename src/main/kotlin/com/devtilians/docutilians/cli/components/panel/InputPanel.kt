package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt

data class InputPanelRequest(
    val label: String,
    val hint: String? = null,
    val default: String? = null,
)

object InputPanel {

    fun prompt(t: Terminal, inputPanelRequest: InputPanelRequest): String? {
        val p = Colors.Raw.primary
        val s = Colors.Raw.secondary
        val m = Colors.Raw.textMuted

        t.println(m("────────────────────────────────────────────"))
        t.println(s(TextStyles.bold(inputPanelRequest.label)))

        if (inputPanelRequest.hint != null) {
            t.println(m(inputPanelRequest.hint))
        }

        t.print(s("▶ "))
        val result = t.prompt("") ?: inputPanelRequest.default

        t.println(m("────────────────────────────────────────────"))

        return result
    }

    fun confirm(t: Terminal, message: String, default: Boolean = true): Boolean {
        val p = Colors.Raw.primary
        val s = Colors.Raw.secondary
        val m = Colors.Raw.textMuted

        t.println(m("────────────────────────────────────────────"))
        t.println(s("?") + " " + message)
        t.print(s("▶ "))

        val input = t.prompt(m(if (default) "(Y/n)" else "(y/N)"))?.trim()?.lowercase()

        t.println(m("────────────────────────────────────────────"))

        return when (input) {
            "y",
            "yes" -> true
            "n",
            "no" -> false
            else -> default
        }
    }
}
