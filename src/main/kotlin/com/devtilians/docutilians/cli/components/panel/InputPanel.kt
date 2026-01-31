package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt

/** CHUCK_UI Input Panel Simple input prompts with separator lines (no box frame) */
data class InputPanelRequest(
    val label: String,
    val hint: String? = null,
    val default: String? = null,
)

object InputPanel {

    fun prompt(t: Terminal, inputPanelRequest: InputPanelRequest): String? {
        val dim = Colors.Raw.textMuted

        t.println()
        t.println(dim("  ${"─".repeat(50)}"))
        t.println(
            "  ${Colors.Raw.primary("◈")} ${Colors.Raw.textWhite(TextStyles.bold(inputPanelRequest.label))}"
        )

        if (inputPanelRequest.hint != null) {
            t.println("    ${dim(inputPanelRequest.hint)}")
        }

        t.println()
        t.print("  ${Colors.Raw.primary("▸")} ")
        val result = t.prompt("") ?: inputPanelRequest.default

        t.println(dim("  ${"─".repeat(50)}"))

        return result
    }

    fun confirm(t: Terminal, message: String, default: Boolean = true): Boolean {
        val dim = Colors.Raw.textMuted

        t.println()
        t.println(dim("  ${"─".repeat(50)}"))
        t.println("  ${Colors.Raw.warning("?")} ${Colors.Raw.textWhite(message)}")
        t.println()
        t.print("  ${Colors.Raw.primary("▸")} ")

        val input = t.prompt(dim(if (default) "(Y/n)" else "(y/N)"))?.trim()?.lowercase()

        t.println(dim("  ${"─".repeat(50)}"))

        return when (input) {
            "y",
            "yes" -> true
            "n",
            "no" -> false
            else -> default
        }
    }
}
