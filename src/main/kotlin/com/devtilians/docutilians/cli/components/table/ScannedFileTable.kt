package com.devtilians.docutilians.cli.components.table

import com.devtilians.docutilians.constants.Colors
import com.devtilians.docutilians.scanner.CodeScanner.ScannedFile
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal

/**
 * ScannedFile Table
 * Displays detected API controller files with framework info
 */
class ScannedFileTable(private val terminal: Terminal) {

    fun render(files: List<ScannedFile>, title: String = "DETECTED CONTROLLERS") {
        terminal.println()
        terminal.println(
            "  ${Colors.Raw.primary("◈")} ${Colors.Raw.textWhite(title)} " +
                Colors.Raw.textMuted("─".repeat(maxOf(0, 55 - title.length)))
        )
        terminal.println()

        // Header
        terminal.println(
            "  ${Colors.Raw.textMuted("#")}   " +
                "${Colors.Raw.primary("FILE")}${" ".repeat(32)}" +
                "${Colors.Raw.textMuted("LANG")}    " +
                "${Colors.Raw.textMuted("FRAMEWORK")}     " +
                "${Colors.Raw.textMuted("ENDPOINTS")}"
        )
        terminal.println(Colors.Raw.textMuted("  ${"─".repeat(75)}"))

        // Body
        files.forEachIndexed { index, file ->
            val (icon, colorStyle) = getStyleForLanguage(file.language)
            val indexStr = Colors.Raw.textMuted(String.format("%02d", index + 1))
            val fileName = file.relativePath.substringAfterLast("/")
            val nameStr = (colorStyle + TextStyles.bold)("$icon ${truncateName(fileName, 32)}")
            val langStr = formatLangBadge(file.language)
            val frameworkStr = formatFramework(file.framework)
            val endpointStr = formatEndpoints(file.estimatedEndpoints)

            terminal.println("  $indexStr  $nameStr  $langStr  $frameworkStr  $endpointStr")
        }

        // Footer
        terminal.println(Colors.Raw.textMuted("  ${"─".repeat(75)}"))

        val totalEndpoints = files.sumOf { it.estimatedEndpoints }
        terminal.println(
            "  ${Colors.Raw.primary("▲")} " +
                "${Colors.Raw.success(files.size.toString())} ${Colors.Raw.textMuted("files")}  " +
                "${Colors.Raw.accent("~$totalEndpoints")} ${Colors.Raw.textMuted("endpoints")}"
        )
        terminal.println()
    }

    private fun truncateName(name: String, maxLen: Int): String {
        return if (name.length <= maxLen) {
            name + " ".repeat(maxLen - name.length)
        } else {
            name.take(maxLen - 3) + "..."
        }
    }

    private fun formatLangBadge(lang: String): String {
        val label = lang.uppercase().padEnd(4)
        return when (lang) {
            "kt" -> Colors.Raw.secondary("[$label]")
            "java" -> TextColors.rgb("#ff6600")("[$label]")
            "ts" -> Colors.Raw.primary("[$label]")
            "js" -> Colors.Raw.warning("[$label]")
            "py" -> Colors.Raw.warning("[$label]")
            "go" -> Colors.Raw.primary("[$label]")
            else -> Colors.Raw.textMuted("[$label]")
        }
    }

    private fun formatFramework(framework: String?): String {
        val label = (framework ?: "-").take(12).padEnd(12)
        return when (framework) {
            "Spring" -> Colors.Raw.success(label)
            "NestJS" -> Colors.Raw.error(label)
            "Express" -> Colors.Raw.warning(label)
            "FastAPI" -> Colors.Raw.primary(label)
            "Gin", "Echo", "Fiber" -> Colors.Raw.primary(label)
            "Django", "Flask" -> Colors.Raw.success(label)
            else -> Colors.Raw.textMuted(label)
        }
    }

    private fun formatEndpoints(count: Int): String {
        return when {
            count >= 10 -> Colors.Raw.error("$count".padStart(3))
            count >= 5 -> Colors.Raw.warning("$count".padStart(3))
            count > 0 -> Colors.Raw.success("$count".padStart(3))
            else -> Colors.Raw.textMuted("$count".padStart(3))
        }
    }

    private fun getStyleForLanguage(lang: String): Pair<String, TextStyle> {
        return when (lang) {
            "kt" -> "🟣" to TextColors.rgb("#bc13fe")
            "java" -> "☕" to TextColors.rgb("#ff6600")
            "ts" -> "💠" to TextColors.rgb("#00f3ff")
            "js" -> "⚡" to TextColors.rgb("#fefe00")
            "py" -> "🐍" to TextColors.rgb("#fefe00")
            "go" -> "🐹" to TextColors.rgb("#00f3ff")
            else -> "📄" to Colors.Raw.textMuted
        }
    }
}
