package com.devtilians.docutilians.utils

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.writeText

object FileUtils {

    // ✅ 앞서 정의한 '개발자 친화적' 확장자 목록을 기본값으로 설정
    val DEFAULT_EXTENSIONS =
        setOf(
            // JVM & Languages
            "kt",
            "java",
            "gradle",
            "rs",
            "go",
            "py",
            "c",
            "cpp",
            "h",
            "cs",
            "swift",
            // Web & Frontend
            "js",
            "ts",
            "jsx",
            "tsx",
            "html",
            "css",
            "scss",
            "vue",
            "svelte",
            // Config & Data
            "json",
            "yaml",
            "yml",
            "xml",
            "toml",
            "properties",
            "env",
            // Docs
            "md",
            "txt",
        )

    // ⛔ 탐색 시 무조건 건너뛸 디렉토리 (성능 및 노이즈 방지)
    private val ALWAYS_IGNORE_DIRS =
        setOf(
            ".git",
            ".idea",
            ".vscode",
            ".gradle",
            "node_modules",
            "build",
            "target",
            "dist",
            "out",
            "bin",
        )

    /**
     * 지정된 확장자를 가진 파일만 재귀적으로 탐색하여 반환합니다.
     *
     * @param dir 탐색을 시작할 루트 경로
     * @param extensions 찾을 파일의 확장자 목록 (기본값: 개발 관련 확장자)
     * @param maxDepth 재귀 탐색 최대 깊이
     */
    fun walkFiles(
        dir: Path,
        extensions: Set<String> = DEFAULT_EXTENSIONS,
        maxDepth: Int = Int.MAX_VALUE,
    ): Sequence<Path> {
        return walkRecursive(dir, extensions, maxDepth, 0)
    }

    private fun walkRecursive(
        dir: Path,
        extensions: Set<String>,
        maxDepth: Int,
        currentDepth: Int,
    ): Sequence<Path> = sequence {
        if (currentDepth > maxDepth) return@sequence
        if (!Files.exists(dir) || !dir.isDirectory()) return@sequence

        val children =
            try {
                Files.list(dir).use { it.toList() } // 스트림을 리스트로 변환 (Resource Leak 방지)
            } catch (e: Exception) {
                return@sequence
            }

        for (path in children) {
            val fileName = path.fileName.toString()

            // 1. 디렉토리 처리: 숨김 폴더나 무시 목록에 있으면 진입하지 않음
            if (path.isDirectory()) {
                if (!fileName.startsWith(".") && fileName !in ALWAYS_IGNORE_DIRS) {
                    yieldAll(walkRecursive(path, extensions, maxDepth, currentDepth + 1))
                }
                continue
            }

            // 2. 파일 처리: 확장자 검사
            if (path.isRegularFile()) {
                val ext = fileName.substringAfterLast('.', "").lowercase()
                if (ext in extensions) {
                    yield(path)
                }
            }
        }
    }

    fun writeFile(path: Path, content: String) {
        path.parent?.let { Files.createDirectories(it) }
        path.writeText(content)
    }
}
