package com.devtilians.docutilians.cli.components.table

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.BorderType.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.Padding
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.log10
import kotlin.math.pow

/**
 * CHUCK_UI File Table
 * Cyberpunk-styled data grid with neon accents
 */
class FileTable(private val terminal: Terminal) {

    private data class FileRenderItem(
        val name: String,
        val extension: String,
        val size: String,
        val date: String,
        val isDir: Boolean,
    )

    fun render(paths: List<Path>) {
        val items = paths.map { toRenderItem(it) }

        terminal.println(
            table {
                borderType = SQUARE_DOUBLE_SECTION_SEPARATOR
                borderStyle = Colors.Raw.secondary  // Neon Pink border
                tableBorders = Borders.TOP_BOTTOM

                align = TextAlign.LEFT
                column(0) { width = ColumnWidth.Auto }
                column(1) { padding = Padding(0, 2, 0, 2) }
                column(2) { padding = Padding(0, 2, 0, 2) }
                column(3) { padding = Padding(0, 2, 0, 2) }

                header {
                    style = Colors.textBlack + Colors.Raw.primary.bg + TextStyles.bold
                    row {
                        cell("  ◈ NAME  ")
                        cell("EXT") { align = TextAlign.CENTER }
                        cell("SIZE") { align = TextAlign.RIGHT }
                        cell("MODIFIED") { align = TextAlign.RIGHT }
                    }
                }

                body {
                    cellBorders = Borders.BOTTOM

                    items.forEach { item ->
                        val (icon, colorStyle) = getStyleForExtension(item.extension, item.isDir)

                        row {
                            // Column 1: File name
                            cell(" $icon  ${item.name}") {
                                style = colorStyle + TextStyles.bold
                                if (item.isDir) style = style!! + TextStyles.underline
                            }

                            // Column 2: Extension
                            cell(item.extension.uppercase()) {
                                align = TextAlign.CENTER
                                style = colorStyle
                            }

                            // Column 3: Size
                            cell(item.size) {
                                align = TextAlign.RIGHT
                                style = Colors.tableCellMuted
                            }

                            // Column 4: Date
                            cell(item.date) {
                                align = TextAlign.RIGHT
                                style = Colors.tableCellMuted
                            }
                        }
                    }
                }

                footer {
                    style = Colors.Raw.primary + TextStyles.bold  // Neon Blue footer
                    row {
                        cell("  ▲ TOTAL: ${items.size} FILES  ") {
                            columnSpan = 4
                            align = TextAlign.RIGHT
                        }
                    }
                }
            }
        )
    }

    private fun getStyleForExtension(ext: String, isDir: Boolean): Pair<String, TextStyle> {
        // CHUCK_UI Cyberpunk color scheme for file types
        if (isDir) return "▶" to Colors.Raw.primary  // Neon Blue for directories

        return when (ext) {
            // JVM Ecosystem - Purple/Pink tones
            "kt" -> "◆" to TextColors.rgb("#bc13fe")  // Kotlin - Neon Pink
            "java" -> "◆" to TextColors.rgb("#ff6600")  // Java - Neon Orange
            "gradle" -> "◇" to Colors.Raw.textMuted

            // Web / Node - Cyan/Yellow tones
            "js" -> "◆" to TextColors.rgb("#fefe00")  // JS - Neon Yellow
            "ts" -> "◆" to TextColors.rgb("#00f3ff")  // TS - Neon Blue
            "jsx",
            "tsx" -> "◆" to TextColors.rgb("#00ccff")  // React - Cyan
            "html" -> "◇" to TextColors.rgb("#ff5500")  // HTML - Orange
            "css" -> "◇" to TextColors.rgb("#00f3ff")  // CSS - Neon Blue
            "vue" -> "◆" to TextColors.rgb("#0aff0a")  // Vue - Neon Green

            // Backend / System
            "rs" -> "◆" to TextColors.rgb("#ff6633")  // Rust - Orange
            "go" -> "◆" to TextColors.rgb("#00f3ff")  // Go - Neon Cyan
            "py" -> "◆" to TextColors.rgb("#fefe00")  // Python - Yellow
            "c",
            "cpp",
            "h" -> "◇" to Colors.Raw.textMuted

            // Config / Data - Accent colors
            "json" -> "◇" to TextColors.rgb("#fefe00")  // JSON - Yellow
            "yaml",
            "yml" -> "◇" to Colors.Raw.primary  // YAML - Neon Blue (primary)
            "xml" -> "◇" to Colors.Raw.textMuted
            "md" -> "◇" to Colors.Raw.textWhite

            // Graphics - Pink tones
            "png",
            "jpg",
            "jpeg",
            "svg",
            "ico" -> "◇" to TextColors.rgb("#bc13fe")  // Neon Pink

            // Archives - Yellow/Gold
            "zip",
            "tar",
            "gz",
            "7z" -> "◇" to TextColors.rgb("#fefe00")

            // Default
            else -> "◇" to Colors.Raw.textMuted
        }
    }

    private fun toRenderItem(path: Path): FileRenderItem {
        val fileName = path.fileName.toString()
        val isDir = Files.isDirectory(path)
        val ext = if (isDir) "DIR" else fileName.substringAfterLast('.', "").lowercase()
        val sizeStr = if (isDir) "-" else formatSize(Files.size(path))
        val dateStr = formatDate(Files.getLastModifiedTime(path))
        return FileRenderItem(fileName, ext, sizeStr, dateStr, isDir)
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.1f %s",
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups],
        )
    }

    private fun formatDate(fileTime: FileTime): String {
        return fileTime
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("YY-MM-dd HH:mm"))
    }
}
