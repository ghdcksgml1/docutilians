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
                borderStyle = Colors.Raw.tableBorder
                tableBorders = Borders.TOP_BOTTOM

                align = TextAlign.LEFT
                column(0) { width = ColumnWidth.Auto }
                column(1) { padding = Padding(0, 2, 0, 2) }
                column(2) { padding = Padding(0, 2, 0, 2) }
                column(3) { padding = Padding(0, 2, 0, 2) }

                header {
                    style = Colors.tableHeaderText + Colors.Raw.tableHeaderBg.bg + TextStyles.bold
                    row {
                        cell("  NAME  ")
                        cell("EXT") { align = TextAlign.CENTER }
                        cell("SIZE") { align = TextAlign.RIGHT }
                        cell("LAST MODIFIED") { align = TextAlign.RIGHT }
                    }
                }

                body {
                    cellBorders = Borders.BOTTOM

                    items.forEach { item ->
                        val (icon, colorStyle) = getStyleForExtension(item.extension, item.isDir)

                        row {
                            // 1열: 파일명
                            cell(" $icon  ${item.name}") {
                                style = colorStyle + TextStyles.bold
                                if (item.isDir) style = style!! + TextStyles.underline
                            }

                            // 2열: 확장자
                            cell(item.extension.uppercase()) {
                                align = TextAlign.CENTER
                                style = colorStyle
                            }

                            // 3열: 크기
                            cell(item.size) {
                                align = TextAlign.RIGHT
                                style = Colors.tableCellMuted
                            }

                            // 4열: 날짜
                            cell(item.date) {
                                align = TextAlign.RIGHT
                                style = Colors.tableCellMuted + TextStyles.italic
                            }
                        }
                    }
                }

                footer {
                    style = Colors.tableFooter + TextStyles.bold
                    row {
                        cell("  TOTAL: ${items.size} FILES  ") {
                            columnSpan = 4
                            align = TextAlign.RIGHT
                        }
                    }
                }
            }
        )
    }

    private fun getStyleForExtension(ext: String, isDir: Boolean): Pair<String, TextStyle> {
        if (isDir) return "📂" to Colors.accent

        return when (ext) {
            // JVM Ecosystem
            "kt" -> "🦄" to TextColors.rgb("#7F52FF") // Kotlin Purple
            "java" -> "☕" to TextColors.rgb("#ED8B00") // Java Orange
            "gradle" -> "🐘" to TextColors.rgb("#02303A") // Gradle Dark

            // Web / Node
            "js" -> "✨" to TextColors.rgb("#F7DF1E") // JS Yellow
            "ts" -> "📘" to TextColors.rgb("#3178C6") // TS Blue
            "jsx",
            "tsx" -> "⚛️" to TextColors.rgb("#61DAFB") // React Cyan
            "html" -> "🌐" to TextColors.rgb("#E34F26") // HTML Orange
            "css" -> "🎨" to TextColors.rgb("#1572B6") // CSS Blue
            "vue" -> "🟩" to TextColors.rgb("#4FC08D") // Vue Green

            // Backend / System
            "rs" -> "🦀" to TextColors.rgb("#DEA584") // Rust
            "go" -> "🐹" to TextColors.rgb("#00ADD8") // Go Cyan
            "py" -> "🐍" to TextColors.rgb("#3776AB") // Python Blue
            "c",
            "cpp",
            "h" -> "Ⓜ️" to TextColors.rgb("#A8B9CC") // C++ Metal

            // Config / Data
            "json" -> "📦" to TextColors.rgb("#F0E68C") // Khaki
            "yaml",
            "yml" -> "⚙️" to Colors.Raw.secondary // Docutilians Cyan
            "xml" -> "📑" to Colors.Raw.textMuted
            "md" -> "📝" to Colors.Raw.textWhite

            // Graphics
            "png",
            "jpg",
            "jpeg",
            "svg",
            "ico" -> "🖼️" to TextColors.rgb("#FF69B4")

            // Archives
            "zip",
            "tar",
            "gz",
            "7z" -> "🗜️" to TextColors.rgb("#FFD700")

            // Default
            else -> "📄" to Colors.Raw.textMuted
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
