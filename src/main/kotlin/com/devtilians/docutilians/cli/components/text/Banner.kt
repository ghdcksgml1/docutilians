package com.devtilians.docutilians.cli.components.text

import com.devtilians.docutilians.constants.Colors

object Banner {
    fun generate(): String {
        val primary = Colors.Raw.primary
        val secondary = Colors.Raw.secondary
        val accent = Colors.Raw.accent
        val muted = Colors.Raw.textMuted
        val success = Colors.Raw.success

        val logo =
            """
            ┌────────────────────────────────────────────────────────────────────────────────────────┐
            │                                                                                        │
            │  ██████╗  ██████╗  ██████╗██╗   ██╗████████╗██╗██╗     ██╗ █████╗ ███╗   ██╗███████╗   │
            │  ██╔══██╗██╔═══██╗██╔════╝██║   ██║╚══██╔══╝██║██║     ██║██╔══██╗████╗  ██║██╔════╝   │
            │  ██║  ██║██║   ██║██║     ██║   ██║   ██║   ██║██║     ██║███████║██╔██╗ ██║███████╗   │
            │  ██║  ██║██║   ██║██║     ██║   ██║   ██║   ██║██║     ██║██╔══██║██║╚██╗██║╚════██║   │
            │  ██████╔╝╚██████╔╝╚██████╗╚██████╔╝   ██║   ██║███████╗██║██║  ██║██║ ╚████║███████║   │
            │  ╚═════╝  ╚═════╝  ╚═════╝ ╚═════╝    ╚═╝   ╚═╝╚══════╝╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝   │
            │                                                                                        │
            │                      Auto-generate OpenAPI specs from source code                      │
            │                                                                                        │
            └────────────────────────────────────────────────────────────────────────────────────────┘
            """
                .trimIndent()

        return logo
            .replace("█", primary("█"))
            .replace("╔", primary("╔"))
            .replace("╗", primary("╗"))
            .replace("╚", primary("╚"))
            .replace("╝", primary("╝"))
            .replace("═", primary("═"))
            .replace("║", primary("║"))
            .replace("┌", muted("┌"))
            .replace("┐", muted("┐"))
            .replace("└", muted("└"))
            .replace("┘", muted("┘"))
            .replace("─", muted("─"))
            .replace("│", muted("│"))
            .replace(
                "Auto-generate OpenAPI specs from source code",
                secondary("Auto-generate OpenAPI specs from source code"),
            )
    }
}
