package com.devtilians.docutilians.cli.components.table

import com.devtilians.docutilians.constants.Colors
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.log10
import kotlin.math.pow

/** CHUCK_UI File Table Cyberpunk-styled data grid with neon accents and glassmorphism feel */
class FileTable(private val terminal: Terminal) {

    private data class FileRenderItem(
        val index: Int,
        val name: String,
        val extension: String,
        val size: String,
        val date: String,
        val isDir: Boolean,
    )

    fun render(paths: List<Path>) {
        val items = paths.mapIndexed { index, path -> toRenderItem(index, path) }

        // Title
        terminal.println()
        terminal.println(
            "  ${Colors.Raw.primary("◈")} ${Colors.Raw.textWhite("FILE SCANNER")} " +
                "${Colors.Raw.textMuted("─────────────────────────────────────────────────")}"
        )
        terminal.println()

        // Header
        terminal.println(
            "  ${Colors.Raw.textMuted("#")}   " +
                "${Colors.Raw.primary("FILE")}${" ".repeat(36)}" +
                "${Colors.Raw.textMuted("TYPE")}      " +
                "${Colors.Raw.textMuted("SIZE")}        " +
                "${Colors.Raw.textMuted("MODIFIED")}"
        )
        terminal.println(Colors.Raw.textMuted("  ${"─".repeat(75)}"))

        // Body
        items.forEach { item ->
            val (icon, colorStyle) = getStyleForExtension(item.extension, item.isDir)
            val indexStr = Colors.Raw.textMuted(String.format("%02d", item.index + 1))
            val nameStr = (colorStyle + TextStyles.bold)("$icon ${truncateName(item.name, 35)}")
            val extStr = formatExtBadge(item.extension, item.isDir)
            val sizeStr = Colors.Raw.accent(item.size.padStart(8))
            val dateStr = Colors.Raw.textMuted(item.date)

            terminal.println("  $indexStr  $nameStr  $extStr  $sizeStr    $dateStr")
        }

        // Footer
        terminal.println(Colors.Raw.textMuted("  ${"─".repeat(75)}"))
        terminal.println(
            "  ${Colors.Raw.primary("▲")} ${Colors.Raw.textWhite("COMPLETE")} " +
                "${Colors.Raw.success(items.size.toString())} ${Colors.Raw.textMuted("files")}"
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

    private fun formatExtBadge(ext: String, isDir: Boolean): String {
        val label = if (isDir) "DIR" else ext.uppercase()
        return when {
            isDir -> Colors.Raw.primary("[$label]")
            ext in listOf("kt", "java", "ts", "js", "py", "go", "rs") ->
                Colors.Raw.success("[$label]")
            ext in listOf("yaml", "yml", "json", "xml") -> Colors.Raw.warning("[$label]")
            else -> Colors.Raw.textMuted("[$label]")
        }
    }

    private fun getStyleForExtension(ext: String, isDir: Boolean): Pair<String, TextStyle> {
        if (isDir) return "📁" to Colors.Raw.primary

        return when (ext) {
            // JVM Ecosystem
            "kt" -> "🟣" to TextColors.rgb("#bc13fe")
            "java" -> "☕" to TextColors.rgb("#ff6600")
            "gradle" -> "🐘" to Colors.Raw.textMuted

            // Web / Node
            "js" -> "⚡" to TextColors.rgb("#fefe00")
            "ts" -> "💠" to TextColors.rgb("#00f3ff")
            "jsx",
            "tsx" -> "⚛️" to TextColors.rgb("#00ccff")
            "html" -> "🌐" to TextColors.rgb("#ff5500")
            "css" -> "🎨" to TextColors.rgb("#00f3ff")
            "vue" -> "💚" to TextColors.rgb("#0aff0a")

            // Backend / System
            "rs" -> "🦀" to TextColors.rgb("#ff6633")
            "go" -> "🐹" to TextColors.rgb("#00f3ff")
            "py" -> "🐍" to TextColors.rgb("#fefe00")
            "c",
            "cpp",
            "h" -> "⚙️" to Colors.Raw.textMuted

            // Config / Data
            "json" -> "📦" to TextColors.rgb("#fefe00")
            "yaml",
            "yml" -> "📋" to Colors.Raw.primary
            "xml" -> "📄" to Colors.Raw.textMuted
            "md" -> "📝" to Colors.Raw.textWhite

            // Graphics
            "png",
            "jpg",
            "jpeg",
            "svg",
            "ico" -> "🖼️" to TextColors.rgb("#bc13fe")

            // Archives
            "zip",
            "tar",
            "gz",
            "7z" -> "📦" to TextColors.rgb("#fefe00")

            // Default
            else -> "📄" to Colors.Raw.textMuted
        }
    }

    private fun toRenderItem(index: Int, path: Path): FileRenderItem {
        val fileName = path.fileName.toString()
        val isDir = Files.isDirectory(path)
        val ext = if (isDir) "DIR" else fileName.substringAfterLast('.', "").lowercase()
        val sizeStr = if (isDir) "-" else formatSize(Files.size(path))
        val dateStr = formatDate(Files.getLastModifiedTime(path))
        return FileRenderItem(index, fileName, ext, sizeStr, dateStr, isDir)
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
