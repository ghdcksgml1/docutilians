package com.devtilians.docutilians.cli.components.text

import com.devtilians.docutilians.constants.Colors

/** CHUCK_UI Cyberpunk Banner Clean neon aesthetic */
object Banner {
    fun generate(): String {
        val cyan = Colors.Raw.primary
        val pink = Colors.Raw.secondary
        val dim = Colors.Raw.textMuted
        val white = Colors.Raw.textWhite

        return buildString {
            appendLine()
            appendLine(dim("  ${"─".repeat(70)}"))
            appendLine()
            appendLine(
                "  ${cyan("██████╗  ██████╗  ██████╗██╗   ██╗████████╗██╗██╗     ██╗ █████╗ ███╗   ██╗███████╗")}"
            )
            appendLine(
                "  ${cyan("██╔══██╗██╔═══██╗██╔════╝██║   ██║╚══██╔══╝██║██║     ██║██╔══██╗████╗  ██║██╔════╝")}"
            )
            appendLine(
                "  ${pink("██║  ██║██║   ██║██║     ██║   ██║   ██║   ██║██║     ██║███████║██╔██╗ ██║███████╗")}"
            )
            appendLine(
                "  ${pink("██║  ██║██║   ██║██║     ██║   ██║   ██║   ██║██║     ██║██╔══██║██║╚██╗██║╚════██║")}"
            )
            appendLine(
                "  ${cyan("██████╔╝╚██████╔╝╚██████╗╚██████╔╝   ██║   ██║███████╗██║██║  ██║██║ ╚████║███████║")}"
            )
            appendLine(
                "  ${cyan("╚═════╝  ╚═════╝  ╚═════╝ ╚═════╝    ╚═╝   ╚═╝╚══════╝╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝")}"
            )
            appendLine()
            appendLine("  ${white("Auto-generate OpenAPI specs from source code")}")
            appendLine("  ${dim("LLM-Powered Multi-Agent Pipeline")}")
            appendLine()
            append(dim("  ${"─".repeat(70)}"))
        }
    }
}
