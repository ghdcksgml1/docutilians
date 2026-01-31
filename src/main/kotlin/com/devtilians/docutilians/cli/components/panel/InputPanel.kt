package com.devtilians.docutilians.cli.components.panel

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt

/**
 * CHUCK_UI Input Panel
 * Cyberpunk-styled input prompts with neon accents
 */
data class InputPanelRequest(
    val label: String,
    val hint: String? = null,
    val default: String? = null,
)

object InputPanel {

    fun prompt(t: Terminal, inputPanelRequest: InputPanelRequest): String? {
        val neonBlue = Colors.Raw.primary     // Neon Blue
        val neonPink = Colors.Raw.secondary   // Neon Pink
        val dim = Colors.Raw.textMuted        // Dim text

        t.println(neonPink("╔${"═".repeat(50)}╗"))
        t.println(neonPink("║") + " ${neonBlue("◈")} ${Colors.Raw.textWhite(TextStyles.bold(inputPanelRequest.label))}")

        if (inputPanelRequest.hint != null) {
            t.println(neonPink("║") + "   ${dim(inputPanelRequest.hint)}")
        }

        t.println(neonPink("╠${"─".repeat(50)}╣"))
        t.print(neonPink("║") + " ${neonBlue("▸")} ")
        val result = t.prompt("") ?: inputPanelRequest.default

        t.println(neonPink("╚${"═".repeat(50)}╝"))

        return result
    }

    fun confirm(t: Terminal, message: String, default: Boolean = true): Boolean {
        val neonBlue = Colors.Raw.primary
        val neonPink = Colors.Raw.secondary
        val neonYellow = Colors.Raw.warning
        val dim = Colors.Raw.textMuted

        t.println(neonPink("╔${"═".repeat(50)}╗"))
        t.println(neonPink("║") + " ${neonYellow("?")} ${Colors.Raw.textWhite(message)}")
        t.println(neonPink("╠${"─".repeat(50)}╣"))
        t.print(neonPink("║") + " ${neonBlue("▸")} ")

        val input = t.prompt(dim(if (default) "(Y/n)" else "(y/N)"))?.trim()?.lowercase()

        t.println(neonPink("╚${"═".repeat(50)}╝"))

        return when (input) {
            "y",
            "yes" -> true
            "n",
            "no" -> false
            else -> default
        }
    }
}
