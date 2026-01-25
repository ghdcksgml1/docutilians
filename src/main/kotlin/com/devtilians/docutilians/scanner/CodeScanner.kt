package com.devtilians.docutilians.scanner

import java.io.File

class CodeScanner(
    private val rootDir: String,
    private val excludeDirs: Set<String> = DEFAULT_EXCLUDE_DIRS,
) {
    companion object {
        val DEFAULT_EXCLUDE_DIRS =
            setOf(
                "build",
                "dist",
                "out",
                "target",
                "node_modules",
                ".gradle",
                ".idea",
                ".git",
                "__pycache__",
                "venv",
                ".venv",
                "vendor",
                "test",
                "tests",
                "__tests__",
            )

        private val CONTROLLER_PATTERNS =
            mapOf(
                "kt" to
                    listOf(
                        "@Controller",
                        "@RestController",
                        "@RequestMapping",
                        "@GetMapping",
                        "@PostMapping",
                    ),
                "java" to
                    listOf(
                        "@Controller",
                        "@RestController",
                        "@RequestMapping",
                        "@GetMapping",
                        "@PostMapping",
                    ),
                "ts" to listOf("@Controller", "@Get(", "@Post(", "@Put(", "@Delete(", "Router("),
                "js" to
                    listOf(
                        "router.",
                        "app.get",
                        "app.post",
                        "app.put",
                        "app.delete",
                        "express.Router",
                    ),
                "py" to listOf("@app.", "@router.", "APIRouter", "@blueprint.", "Flask"),
                "go" to listOf("HandleFunc", "gin.", "echo.", "mux.", "http.Handle"),
            )

        private val ENDPOINT_PATTERNS =
            mapOf(
                "kt" to
                    listOf(
                        "@GetMapping",
                        "@PostMapping",
                        "@PutMapping",
                        "@DeleteMapping",
                        "@PatchMapping",
                        "@RequestMapping",
                    ),
                "java" to
                    listOf(
                        "@GetMapping",
                        "@PostMapping",
                        "@PutMapping",
                        "@DeleteMapping",
                        "@PatchMapping",
                        "@RequestMapping",
                    ),
                "ts" to listOf("@Get(", "@Post(", "@Put(", "@Delete(", "@Patch("),
                "js" to listOf(".get(", ".post(", ".put(", ".delete(", ".patch("),
                "py" to listOf("@app.get", "@app.post", "@router.get", "@router.post", ".route("),
                "go" to listOf("\"GET\"", "\"POST\"", "\"PUT\"", "\"DELETE\"", "HandleFunc"),
            )
    }

    data class ScannedFile(
        val absolutePath: String,
        val relativePath: String,
        val content: String,
        val language: String,
        val estimatedEndpoints: Int,
    )

    data class ScanResult(val files: List<ScannedFile>, val summary: ScanSummary)

    data class ScanSummary(
        val totalFilesScanned: Int,
        val apiFilesFound: Int,
        val byLanguage: Map<String, Int>,
        val estimatedTotalEndpoints: Int,
    )

    /** API 관련 파일 스캔 */
    fun scan(): ScanResult {
        var totalScanned = 0

        val apiFiles =
            File(rootDir)
                .walkTopDown()
                .onEnter { dir -> dir.name !in excludeDirs && !dir.name.startsWith(".") }
                .filter { file -> file.isFile && file.extension in CONTROLLER_PATTERNS.keys }
                .onEach { totalScanned++ }
                .mapNotNull { file -> processFile(file) }
                .sortedByDescending { it.estimatedEndpoints }
                .toList()

        val summary =
            ScanSummary(
                totalFilesScanned = totalScanned,
                apiFilesFound = apiFiles.size,
                byLanguage = apiFiles.groupingBy { it.language }.eachCount(),
                estimatedTotalEndpoints = apiFiles.sumOf { it.estimatedEndpoints },
            )

        return ScanResult(apiFiles, summary)
    }

    /** 특정 파일 처리 */
    private fun processFile(file: File): ScannedFile? {
        val content =
            try {
                file.readText()
            } catch (e: Exception) {
                return null
            }

        val extension = file.extension
        val patterns = CONTROLLER_PATTERNS[extension] ?: return null

        if (patterns.none { content.contains(it) }) {
            return null
        }

        return ScannedFile(
            absolutePath = file.absolutePath,
            relativePath = file.relativeTo(File(rootDir)).path,
            content = content,
            language = extension,
            estimatedEndpoints = estimateEndpointCount(content, extension),
        )
    }

    /** 엔드포인트 수 추정 */
    private fun estimateEndpointCount(content: String, extension: String): Int {
        val patterns = ENDPOINT_PATTERNS[extension] ?: return 0
        return patterns.sumOf { pattern -> content.split(pattern).size - 1 }
    }

    /** 지원 언어 목록 */
    fun supportedLanguages(): Set<String> = CONTROLLER_PATTERNS.keys
}
