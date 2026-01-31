package com.devtilians.docutilians.cli.components.text

import com.devtilians.docutilians.constants.Colors

/**
 * CHUCK_UI Cyberpunk Banner
 * Neon glow aesthetic with glassmorphism-inspired frame
 */
object Banner {
    fun generate(): String {
        val neonBlue = Colors.Raw.primary      // #00f3ff - Neon Cyan
        val neonPink = Colors.Raw.secondary    // #bc13fe - Neon Pink
        val dim = Colors.Raw.textMuted         // #8888aa - Dim text

        val logo =
            """
            ╔══════════════════════════════════════════════════════════════════════════════════════╗
            ║  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ║
            ║  ▓                                                                                ▓  ║
            ║  ▓   ██████╗  ██████╗  ██████╗██╗   ██╗████████╗██╗██╗     ██╗ █████╗ ███╗   ██╗███████╗  ▓  ║
            ║  ▓   ██╔══██╗██╔═══██╗██╔════╝██║   ██║╚══██╔══╝██║██║     ██║██╔══██╗████╗  ██║██╔════╝  ▓  ║
            ║  ▓   ██║  ██║██║   ██║██║     ██║   ██║   ██║   ██║██║     ██║███████║██╔██╗ ██║███████╗  ▓  ║
            ║  ▓   ██║  ██║██║   ██║██║     ██║   ██║   ██║   ██║██║     ██║██╔══██║██║╚██╗██║╚════██║  ▓  ║
            ║  ▓   ██████╔╝╚██████╔╝╚██████╗╚██████╔╝   ██║   ██║███████╗██║██║  ██║██║ ╚████║███████║  ▓  ║
            ║  ▓   ╚═════╝  ╚═════╝  ╚═════╝ ╚═════╝    ╚═╝   ╚═╝╚══════╝╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝  ▓  ║
            ║  ▓                                                                                ▓  ║
            ║  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  ║
            ║                                                                                      ║
            ║              ⚡ Auto-generate OpenAPI specs from source code ⚡                      ║
            ║                          [ LLM-Powered Multi-Agent Pipeline ]                        ║
            ║                                                                                      ║
            ╚══════════════════════════════════════════════════════════════════════════════════════╝
            """
                .trimIndent()

        return logo
            // Main logo text - Neon Blue glow
            .replace("█", neonBlue("█"))
            .replace("╔", neonBlue("╔"))
            .replace("╗", neonBlue("╗"))
            .replace("╚", neonBlue("╚"))
            .replace("╝", neonBlue("╝"))
            // Outer frame - Neon Pink accent
            .replace("═", neonPink("═"))
            .replace("║", neonPink("║"))
            // Glass panel effect
            .replace("▓", dim("▓"))
            // Tagline highlight
            .replace(
                "⚡ Auto-generate OpenAPI specs from source code ⚡",
                neonBlue("⚡ Auto-generate OpenAPI specs from source code ⚡"),
            )
            .replace(
                "[ LLM-Powered Multi-Agent Pipeline ]",
                dim("[ LLM-Powered Multi-Agent Pipeline ]"),
            )
    }
}
