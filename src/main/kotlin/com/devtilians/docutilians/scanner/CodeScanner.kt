package com.devtilians.docutilians.scanner

import java.io.File

/**
 * CodeScanner - API Controller/Router 파일 탐지기
 *
 * 지원 프레임워크:
 * - Kotlin/Java: Spring MVC, Spring WebFlux, Ktor
 * - TypeScript: NestJS, Express, Hono, Fastify
 * - JavaScript: Express, Koa, Hapi, Fastify
 * - Python: FastAPI, Flask, Django, Django REST Framework
 * - Go: net/http, Gin, Echo, Fiber, Chi, Mux
 */
class CodeScanner(
    private val rootDir: String,
    private val excludeDirs: Set<String> = DEFAULT_EXCLUDE_DIRS,
) {
    companion object {
        val DEFAULT_EXCLUDE_DIRS =
            setOf(
                // Build outputs
                "build", "dist", "out", "target", "bin", ".next", ".nuxt",
                // Dependencies
                "node_modules", "vendor", "Pods",
                // IDE/Tools
                ".gradle", ".idea", ".vscode", ".git", ".svn",
                // Python
                "__pycache__", "venv", ".venv", ".tox", "env",
                // Test directories
                "test", "tests", "__tests__", "spec", "specs",
                // Others
                "coverage", "docs", "documentation",
            )

        private const val MAX_FILE_SIZE = 500_000L  // 500KB

        /**
         * Controller/Router 감지 정규식
         * - 주석과 문자열 내부는 제외하도록 설계
         */
        private val CONTROLLER_REGEX =
            mapOf(
                // Kotlin/Java: Spring MVC, WebFlux, Ktor
                "kt" to listOf(
                    Regex("""@(Rest)?Controller\b"""),
                    Regex("""@(Get|Post|Put|Delete|Patch|Request)Mapping\s*\("""),
                    Regex("""@RouterOperation\b"""),  // WebFlux
                    Regex("""\brouting\s*\{"""),      // Ktor
                    Regex("""\bget\s*\(\s*["'/]"""),  // Ktor route
                    Regex("""\bpost\s*\(\s*["'/]"""), // Ktor route
                ),
                "java" to listOf(
                    Regex("""@(Rest)?Controller\b"""),
                    Regex("""@(Get|Post|Put|Delete|Patch|Request)Mapping\s*\("""),
                    Regex("""@RouterOperation\b"""),
                    Regex("""@Path\s*\("""),  // JAX-RS
                ),
                // TypeScript: NestJS, Express, Hono, Fastify
                "ts" to listOf(
                    Regex("""@Controller\s*\("""),
                    Regex("""@(Get|Post|Put|Delete|Patch)\s*\("""),
                    Regex("""\brouter\.(get|post|put|delete|patch)\s*\("""),
                    Regex("""\bapp\.(get|post|put|delete|patch)\s*\(["'/]"""),
                    Regex("""\.route\s*\(\s*["'/]"""),
                    Regex("""\bHono\s*\(\s*\)"""),
                    Regex("""\bfastify\.(get|post|put|delete)\s*\("""),
                ),
                // JavaScript: Express, Koa, Hapi, Fastify
                "js" to listOf(
                    Regex("""\brouter\.(get|post|put|delete|patch)\s*\(\s*["'/]"""),
                    Regex("""\bapp\.(get|post|put|delete|patch)\s*\(\s*["'/]"""),
                    Regex("""express\.Router\s*\(\s*\)"""),
                    Regex("""\bserver\.route\s*\(\s*\{"""),  // Hapi
                    Regex("""\bfastify\.(get|post|put|delete)\s*\("""),
                    Regex("""\brouter\.(get|post|put|delete)\s*\(\s*["'/]"""),  // Koa
                ),
                // Python: FastAPI, Flask, Django, DRF
                "py" to listOf(
                    Regex("""@(app|router)\.(get|post|put|delete|patch)\s*\("""),
                    Regex("""\bAPIRouter\s*\("""),
                    Regex("""@(app|blueprint)\.route\s*\("""),
                    Regex("""\bpath\s*\(\s*["']"""),           // Django urls
                    Regex("""\bre_path\s*\(\s*["']"""),        // Django urls
                    Regex("""@api_view\s*\(\s*\["""),          // DRF
                    Regex("""class\s+\w+\s*\(\s*\w*(APIView|ViewSet|ModelViewSet)"""),  // DRF
                ),
                // Go: net/http, Gin, Echo, Fiber, Chi, Mux
                "go" to listOf(
                    Regex("""\bhttp\.(HandleFunc|Handle)\s*\("""),
                    Regex("""\b(GET|POST|PUT|DELETE|PATCH)\s*\(\s*["']"""),  // Gin/Echo
                    Regex("""\.(Get|Post|Put|Delete|Patch)\s*\(\s*["']"""),  // Fiber/Chi
                    Regex("""\bmux\.(HandleFunc|Handle)\s*\("""),
                    Regex("""\becho\.New\s*\("""),
                    Regex("""\bgin\.(Default|New)\s*\("""),
                    Regex("""\bfiber\.New\s*\("""),
                    Regex("""\bchi\.(NewRouter|Router)\s*\("""),
                ),
            )

        /**
         * 엔드포인트 카운팅용 정규식
         */
        private val ENDPOINT_REGEX =
            mapOf(
                "kt" to Regex("""@(Get|Post|Put|Delete|Patch|Request)Mapping\s*\("""),
                "java" to Regex("""@(Get|Post|Put|Delete|Patch|Request)Mapping\s*\("""),
                "ts" to Regex("""(@(Get|Post|Put|Delete|Patch)\s*\()|(\.(?:get|post|put|delete|patch)\s*\(\s*["'/])"""),
                "js" to Regex("""\.(?:get|post|put|delete|patch)\s*\(\s*["'/]"""),
                "py" to Regex("""@(app|router)\.(get|post|put|delete|patch)\s*\(|path\s*\(\s*["']"""),
                "go" to Regex("""(\.(?:Get|Post|Put|Delete|Patch|HandleFunc)\s*\(\s*["'])|(http\.Handle)"""),
            )

        /**
         * 주석 제거용 정규식 (언어별)
         */
        private val COMMENT_REGEX =
            mapOf(
                "kt" to listOf(
                    Regex("""//.*$""", RegexOption.MULTILINE),
                    Regex("""/\*[\s\S]*?\*/"""),
                ),
                "java" to listOf(
                    Regex("""//.*$""", RegexOption.MULTILINE),
                    Regex("""/\*[\s\S]*?\*/"""),
                ),
                "ts" to listOf(
                    Regex("""//.*$""", RegexOption.MULTILINE),
                    Regex("""/\*[\s\S]*?\*/"""),
                ),
                "js" to listOf(
                    Regex("""//.*$""", RegexOption.MULTILINE),
                    Regex("""/\*[\s\S]*?\*/"""),
                ),
                "py" to listOf(
                    Regex("""#.*$""", RegexOption.MULTILINE),
                    Regex("""('''[\s\S]*?''')|(\"\"\"[\s\S]*?\"\"\")"""),
                ),
                "go" to listOf(
                    Regex("""//.*$""", RegexOption.MULTILINE),
                    Regex("""/\*[\s\S]*?\*/"""),
                ),
            )
    }

    data class ScannedFile(
        val absolutePath: String,
        val relativePath: String,
        val content: String,
        val language: String,
        val estimatedEndpoints: Int,
        val framework: String?,
    )

    data class ScanResult(val files: List<ScannedFile>, val summary: ScanSummary)

    data class ScanSummary(
        val totalFilesScanned: Int,
        val apiFilesFound: Int,
        val byLanguage: Map<String, Int>,
        val byFramework: Map<String, Int>,
        val estimatedTotalEndpoints: Int,
    )

    /** API 관련 파일 스캔 */
    fun scan(): ScanResult {
        var totalScanned = 0

        val apiFiles =
            File(rootDir)
                .walkTopDown()
                .onEnter { dir -> dir.name !in excludeDirs && !dir.name.startsWith(".") }
                .filter { file ->
                    file.isFile &&
                        file.extension in CONTROLLER_REGEX.keys &&
                        file.length() < MAX_FILE_SIZE
                }
                .onEach { totalScanned++ }
                .mapNotNull { file -> processFile(file) }
                .sortedByDescending { it.estimatedEndpoints }
                .toList()

        val summary =
            ScanSummary(
                totalFilesScanned = totalScanned,
                apiFilesFound = apiFiles.size,
                byLanguage = apiFiles.groupingBy { it.language }.eachCount(),
                byFramework = apiFiles.mapNotNull { it.framework }.groupingBy { it }.eachCount(),
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
        val patterns = CONTROLLER_REGEX[extension] ?: return null

        // 주석 제거한 코드로 검사
        val codeWithoutComments = removeComments(content, extension)

        // 컨트롤러 패턴 매칭
        if (patterns.none { it.containsMatchIn(codeWithoutComments) }) {
            return null
        }

        val framework = detectFramework(codeWithoutComments, extension)

        return ScannedFile(
            absolutePath = file.absolutePath,
            relativePath = file.relativeTo(File(rootDir)).path,
            content = content,
            language = extension,
            estimatedEndpoints = estimateEndpointCount(codeWithoutComments, extension),
            framework = framework,
        )
    }

    /** 주석 제거 */
    private fun removeComments(content: String, extension: String): String {
        val commentPatterns = COMMENT_REGEX[extension] ?: return content
        var result = content
        commentPatterns.forEach { pattern ->
            result = result.replace(pattern, "")
        }
        return result
    }

    /** 프레임워크 감지 */
    private fun detectFramework(content: String, extension: String): String? {
        return when (extension) {
            "kt" -> when {
                content.contains("@RestController") || content.contains("@Controller") -> "Spring"
                content.contains("routing {") -> "Ktor"
                else -> null
            }
            "java" -> when {
                content.contains("@RestController") || content.contains("@Controller") -> "Spring"
                content.contains("@Path(") -> "JAX-RS"
                else -> null
            }
            "ts" -> when {
                Regex("""@Controller\s*\(""").containsMatchIn(content) -> "NestJS"
                content.contains("Hono") -> "Hono"
                content.contains("fastify") -> "Fastify"
                content.contains("express") || content.contains("Router()") -> "Express"
                else -> null
            }
            "js" -> when {
                content.contains("express") -> "Express"
                content.contains("fastify") -> "Fastify"
                content.contains("server.route") -> "Hapi"
                content.contains("new Koa") -> "Koa"
                else -> null
            }
            "py" -> when {
                content.contains("FastAPI") || content.contains("APIRouter") -> "FastAPI"
                content.contains("Flask") || content.contains("@blueprint") -> "Flask"
                content.contains("APIView") || content.contains("ViewSet") -> "Django REST"
                content.contains("path(") || content.contains("re_path(") -> "Django"
                else -> null
            }
            "go" -> when {
                content.contains("gin.") -> "Gin"
                content.contains("echo.") -> "Echo"
                content.contains("fiber.") -> "Fiber"
                content.contains("chi.") -> "Chi"
                content.contains("mux.") -> "Gorilla Mux"
                content.contains("http.HandleFunc") -> "net/http"
                else -> null
            }
            else -> null
        }
    }

    /** 엔드포인트 수 추정 */
    private fun estimateEndpointCount(content: String, extension: String): Int {
        val regex = ENDPOINT_REGEX[extension] ?: return 0
        return regex.findAll(content).count()
    }

    /** 지원 언어 목록 */
    fun supportedLanguages(): Set<String> = CONTROLLER_REGEX.keys
}
