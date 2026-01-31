package com.devtilians.docutilians

import ScalarHtmlGenerator
import com.anthropic.models.messages.Model
import com.devtilians.docutilians.cli.components.animation.ProgressAnimation
import com.devtilians.docutilians.cli.components.panel.*
import com.devtilians.docutilians.cli.components.table.FileTable
import com.devtilians.docutilians.cli.components.table.ScannedFileTable
import com.devtilians.docutilians.cli.components.text.Banner
import com.devtilians.docutilians.common.Config
import com.devtilians.docutilians.common.ExecutionLogger
import com.devtilians.docutilians.common.GlobalState
import com.devtilians.docutilians.constants.Colors
import com.devtilians.docutilians.constants.Language
import com.devtilians.docutilians.exceptions.ApiKeyNotFoundError
import com.devtilians.docutilians.exceptions.InvalidModelError
import com.devtilians.docutilians.exceptions.OpenApiYamlParsedException
import com.devtilians.docutilians.llm.AnthropicClient
import com.devtilians.docutilians.llm.LlmClient
import com.devtilians.docutilians.llm.prompt.FileCollectPromptBuilder
import com.devtilians.docutilians.llm.prompt.PartialOpenApiYamlPromptBuilder
import com.devtilians.docutilians.llm.prompt.PromptBuilder.RouterFileInfo
import com.devtilians.docutilians.scanner.CodeScanner
import com.devtilians.docutilians.scanner.CodeScanner.ScanResult
import com.devtilians.docutilians.scanner.CodeScanner.ScannedFile
import com.devtilians.docutilians.utils.FileUtils
import com.devtilians.docutilians.utils.OpenApiMerger
import com.devtilians.docutilians.utils.retry
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.platform.MultiplatformSystem.exitProcess
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.google.common.base.CaseFormat
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

class Docutilians : CliktCommand() {
    val claudeApiKey: String by
        option(
                "-k",
                "--claude-api-key",
                help =
                    "Anthropic Claude API Key. Alternatively, set the 'ANTHROPIC_API_KEY' environment variable.",
            )
            .default("")
    val claudeApiModel: String by
        option(
                "-m",
                "--claude-model",
                help =
                    "Anthropic Claude model to use (default: claude-haiku-4-5)\n" +
                        "Available models: claude-haiku-4-5, claude-sonnet-4-5",
            )
            .default("claude-haiku-4-5")
    private val openApiYamlOutputDirPath: Path by
        option("-o", "--openapi-output", help = "Output path for the generated OpenAPI YAML file")
            .path(mustExist = false, canBeFile = true, canBeDir = false)
            .default(Path.of(".docutilians/openapi"))
    private val language: String by
        option(
                "-l",
                "--language",
                help = "Language for prompts and messages (default: EN)\nAvailable: EN, KO",
            )
            .default("EN")

    private val projectDir: Path? by
        argument(name = "PROJECT_DIR", help = "Project directory to scan")
            .path(mustExist = true, canBeFile = false)
            .optional()

    private lateinit var llm: LlmClient
    private lateinit var config: Config

    private val executionLogger by lazy {
        ExecutionLogger(Paths.get(".docutilians/logs/execution_log.json"))
    }

    private fun logExecution(
        command: String,
        success: Boolean,
        message: String? = null,
        changedFiles: List<String> = emptyList(),
    ) {
        executionLogger.log(command, success, message, changedFiles)
    }

    fun initialize(t: Terminal) {
        printGreeting(t)

        runCatching { Model.of(claudeApiModel).validate() }
            .onFailure { throw InvalidModelError(claudeApiModel) }

        llm =
            when {
                claudeApiKey.isNotBlank() ->
                    AnthropicClient(apiKey = claudeApiKey, model = Model.of(claudeApiModel))
                System.getenv("ANTHROPIC_API_KEY") != null ->
                    AnthropicClient(model = Model.of(claudeApiModel))
                else -> throw ApiKeyNotFoundError()
            }

        val projectDirPath = determineProjectDir(t, projectDir)
        val languageEnum =
            runCatching { Language.valueOf(language.uppercase()) }.getOrDefault(Language.EN)

        config =
            Config(
                projectDirPath = projectDirPath,
                outputDirPath = openApiYamlOutputDirPath,
                language = languageEnum,
            )

        // Initialize global state with config
        GlobalState.initConfig(config)
    }

    override fun run() = runBlocking {
        val t = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true)
        runCatching { initialize(t) }
            .onFailure { e ->
                logExecution("initialize", false, e.message)
                when (e) {
                    is CliktError -> throw e
                    else -> throw UsageError("Initialization failed: ${e.message}")
                }
            }
        logExecution("initialize", true, "Initialization success")

        val scanResult =
            runCatching { scanProjectTargetDir(t, config.projectDirPath) }
                .onFailure { e ->
                    logExecution("scanProjectTargetDir", false, e.message)
                    GlobalState.logError(e)
                }
                .getOrNull()
        if (scanResult == null) return@runBlocking

        logExecution(
            "scanProjectTargetDir",
            true,
            "Scan success",
            scanResult.files.map { it.absolutePath },
        )

        val selectedFiles = selectFilesToProcess(t, scanResult.files)
        logExecution(
            "selectFilesToProcess",
            true,
            "File selection",
            selectedFiles.map { it.absolutePath },
        )

        if (selectedFiles.isEmpty()) {
            logExecution("selectFilesToProcess", false, "No files selected")
            t.println(Colors.warning("No files selected. Exiting."))
            return@runBlocking
        }

        val (generatedYamls, progress) =
            runCatching { generateFileToOpenApiYaml(t, selectedFiles) }
                .onFailure { e ->
                    logExecution(
                        "generateFileToOpenApiYaml",
                        false,
                        e.message,
                        selectedFiles.map { it.absolutePath },
                    )
                    GlobalState.logError(e)
                }
                .getOrNull() ?: return@runBlocking

        logExecution(
            "generateFileToOpenApiYaml",
            true,
            "YAML generation success",
            selectedFiles.map { it.absolutePath },
        )

        val mergedYaml = OpenApiMerger.mergeOpenApiYamls(generatedYamls, "Generated API", "1.0.0")
        FileUtils.writeFile(config.getMergeYamlOutputFilePath(), mergedYaml)
        FileUtils.writeFile(
            config.getScalarHtmlFilePath(),
            ScalarHtmlGenerator.generateHtmlContent(mergedYaml),
        )
        logExecution(
            "mergeOpenApiYamls",
            true,
            "Merged YAML written",
            listOf(config.getMergeYamlOutputFilePath().toString()),
        )
        logExecution(
            "generateScalarHtml",
            true,
            "Scalar HTML written",
            listOf(config.getScalarHtmlFilePath().toString()),
        )

        val (successCount, failCount) = progress.getResult()
        printCompletionSummary(t, config.projectDirPath, successCount, failCount)
    }

    private fun printCompletionSummary(t: Terminal, projectDir: Path, success: Int, fail: Int) {
        t.println()
        t.println(Colors.Raw.primary("═".repeat(60)))
        t.println(Colors.done("  ✓ OpenAPI YAML generation completed!"))
        t.println()
        t.println(
            Colors.info("  Results: ") +
                Colors.success("$success success") +
                Colors.textMuted(" / ") +
                Colors.error("$fail failed")
        )
        t.println(Colors.hint("  Output: ${config.getMergeYamlOutputFilePath()}"))
        t.println(Colors.Raw.primary("═".repeat(60)))
    }

    private fun printGreeting(t: Terminal) {
        val message = Banner.generate()

        t.println(
            BoxPanel.of(
                BoxPanelRequest(
                    title = "🚀 " + Colors.brand("Docutilians") + " is running!",
                    content = message,
                    expand = false,
                )
            )
        )
        t.println(Colors.info("Hello World!"))
    }

    private fun determineProjectDir(t: Terminal, providedPath: Path?): Path {
        if (providedPath != null && providedPath.exists() && providedPath.isDirectory()) {
            return providedPath
        }

        val projectDirNotFoundMessage =
            ErrorPanel.of(
                ErrorPanelRequest(
                    title = "PROJECT_DIR Not Provided",
                    message =
                        """
                        Please specify a project directory.
                        |   Usage: docutilians <PROJECT_DIR>
                        |   Example: docutilians ./my-project
                        """
                            .trimIndent(),
                )
            )
        t.println(projectDirNotFoundMessage)

        while (true) {
            val userInputPanelRequest =
                InputPanel.prompt(
                    t,
                    InputPanelRequest(
                        label = "Enter project directory",
                        hint = "e.g., ./my-project",
                    ),
                )

            if (userInputPanelRequest.isNullOrBlank()) {
                t.println(
                    Colors.warning("Invalid directory. Please enter a valid project directory.")
                )
                continue
            }

            val targetDir = Path.of(userInputPanelRequest)
            if (!targetDir.toFile().exists() || !targetDir.toFile().isDirectory) {
                t.println(Colors.error("Directory does not exist: $userInputPanelRequest"))
                continue
            }

            t.println(Colors.info("Scanning directory: ") + Colors.accent(userInputPanelRequest))
            t.println(Colors.hint("Directory preview (max 10 files)"))
            FileTable(t)
                .render(
                    FileUtils.walkFiles(targetDir).filter { !it.isDirectory() }.take(10).toList()
                )

            val selection =
                InputPanel.prompt(
                    t,
                    InputPanelRequest(label = "Proceed with this directory? (y/n)"),
                )
            if (!selection.isNullOrEmpty() && selection.trim().lowercase() == "y") {
                return targetDir
            } else {
                t.println(Colors.warning("Please enter another directory."))
            }
        }
    }

    private suspend fun scanProjectTargetDir(t: Terminal, projectTargetDir: Path): ScanResult {
        t.println(Colors.progress("⬤ Starting directory scan..."))
        val scanResult = CodeScanner(projectTargetDir.normalize().pathString).scan()
        t.println(Colors.info("  └─ Found ${scanResult.files.size} controller file(s)."))

        // Update global config with detected language stats
        if (scanResult.summary.byLanguage.isNotEmpty()) {
            GlobalState.updateConfig { it.withScanResult(scanResult.summary.byLanguage) }
            config = GlobalState.config

            val primary = config.primaryDevLanguage
            t.println(
                Colors.info("  └─ Primary language: ") +
                    Colors.accent(primary.displayName) +
                    Colors.textMuted(" (${scanResult.summary.byLanguage})")
            )

            // Show detected frameworks
            if (scanResult.summary.byFramework.isNotEmpty()) {
                t.println(
                    Colors.info("  └─ Frameworks: ") +
                        Colors.textMuted(
                            scanResult.summary.byFramework.entries.joinToString(", ") {
                                "${it.key}(${it.value})"
                            }
                        )
                )
            }
        }

        return scanResult
    }

    /** Display detected files and allow user to add/remove files */
    private fun selectFilesToProcess(
        t: Terminal,
        detectedFiles: List<ScannedFile>,
    ): List<ScannedFile> {
        val selectedFiles = detectedFiles.toMutableList()

        while (true) {
            // Show current file list
            ScannedFileTable(t).render(selectedFiles, "DETECTED CONTROLLERS")

            // Show options
            t.println(Colors.Raw.textMuted("  ${"─".repeat(75)}"))
            t.println("  ${Colors.Raw.primary("Commands:")}")
            t.println(
                "    ${Colors.Raw.accent("y")}        ${Colors.Raw.textMuted("─ Proceed with these files")}"
            )
            t.println(
                "    ${Colors.Raw.accent("-N")}       ${Colors.Raw.textMuted("─ Remove file by number (e.g., -1, -3)")}"
            )
            t.println(
                "    ${Colors.Raw.accent("+path")}    ${Colors.Raw.textMuted("─ Add file by path (e.g., +src/Controller.kt)")}"
            )
            t.println("    ${Colors.Raw.accent("q")}        ${Colors.Raw.textMuted("─ Quit")}")
            t.println(Colors.Raw.textMuted("  ${"─".repeat(75)}"))

            val input =
                InputPanel.prompt(
                        t,
                        InputPanelRequest(
                            label = "Enter command",
                            hint = "y to proceed, -N to remove, +path to add, q to quit",
                        ),
                    )
                    ?.trim() ?: ""

            when {
                // Proceed
                input.lowercase() == "y" || input.isEmpty() -> {
                    return selectedFiles
                }

                // Quit
                input.lowercase() == "q" -> {
                    return emptyList()
                }

                // Remove file by number
                input.startsWith("-") && input.length > 1 -> {
                    val numbers =
                        input.drop(1).split(",", " ").mapNotNull { it.trim().toIntOrNull() }
                    if (numbers.isEmpty()) {
                        t.println(Colors.error("Invalid format. Use -N (e.g., -1 or -1,2,3)"))
                        continue
                    }

                    // Remove in reverse order to maintain indices
                    numbers.sortedDescending().forEach { num ->
                        val index = num - 1
                        if (index in selectedFiles.indices) {
                            val removed = selectedFiles.removeAt(index)
                            t.println(Colors.warning("  ✗ Removed: ${removed.relativePath}"))
                        } else {
                            t.println(Colors.error("  Invalid number: $num"))
                        }
                    }
                }

                // Add file by path
                input.startsWith("+") && input.length > 1 -> {
                    val path = input.drop(1).trim()
                    val file = config.projectDirPath.resolve(path).toFile()

                    if (!file.exists()) {
                        t.println(Colors.error("  File not found: $path"))
                        continue
                    }

                    if (file.isDirectory) {
                        t.println(Colors.error("  Cannot add directory: $path"))
                        continue
                    }

                    // Check if already added
                    if (selectedFiles.any { it.absolutePath == file.absolutePath }) {
                        t.println(Colors.warning("  Already in list: $path"))
                        continue
                    }

                    val content =
                        try {
                            file.readText()
                        } catch (e: Exception) {
                            t.println(Colors.error("  Cannot read file: ${e.message}"))
                            continue
                        }

                    val newFile =
                        ScannedFile(
                            absolutePath = file.absolutePath,
                            relativePath = path,
                            content = content,
                            language = file.extension,
                            estimatedEndpoints = 0,
                            framework = null,
                        )
                    selectedFiles.add(newFile)
                    t.println(Colors.success("  ✓ Added: $path"))
                }

                else -> {
                    t.println(Colors.error("Unknown command: $input"))
                }
            }
        }
    }

    private suspend fun generateFileToOpenApiYaml(
        t: Terminal,
        files: List<ScannedFile>,
    ): Pair<List<String>, ProgressAnimation> {
        val generatedYamls = mutableListOf<String>()

        val progress = ProgressAnimation(t, config)
        progress.start(files.size)

        files.forEach { scanFile ->
            runCatching {
                    val file = Path.of(scanFile.absolutePath)
                    if (!file.exists() || file.isDirectory()) return@forEach

                    val fileName = file.fileName.toString()
                    progress.clearLogs()

                    retry(retryOn = { it is OpenApiYamlParsedException }) { attempt ->
                        if (attempt > 0)
                            progress.updateStatus(
                                "[Retry: $attempt] Collecting relevant files...",
                                fileName,
                            )
                        else progress.updateStatus("Collecting relevant files...", fileName)
                        val fileCollectPrompt =
                            FileCollectPromptBuilder(
                                    fileInfo =
                                        RouterFileInfo(scanFile.absolutePath, scanFile.content),
                                    language = config.language,
                                )
                                .build()

                        val relevantFileResult = llm.collectRelevantFiles(fileCollectPrompt, false)

                        relevantFileResult.files.forEach {
                            progress.addLog("[relevant file] :${it.absolutePath}")
                        }

                        progress.updateStatus("Generating OpenAPI spec...", fileName)

                        val partialOpenApiYamlPrompt =
                            PartialOpenApiYamlPromptBuilder(
                                    summary = relevantFileResult.summary,
                                    fileInfo =
                                        RouterFileInfo(scanFile.absolutePath, scanFile.content),
                                    referenceFiles =
                                        relevantFileResult.files.map {
                                            RouterFileInfo(it.absolutePath, it.sourceCode)
                                        },
                                    language = config.language,
                                )
                                .build()

                        val openApi = llm.generateOpenApiYamlText(partialOpenApiYamlPrompt, false)

                        if (openApi != null) {
                            val filename =
                                "${
                            CaseFormat.LOWER_CAMEL.to(
                                CaseFormat.LOWER_UNDERSCORE,
                                file.nameWithoutExtension,
                            )
                        }.yaml"
                            generatedYamls.add(openApi.yaml)

                            val targetFilePath = config.getPartialYamlOutputFilePath(filename)

                            FileUtils.writeFile(targetFilePath, openApi.yaml)
                            progress.incrementSuccess()
                        } else {
                            progress.incrementFail("No OpenAPI YAML generated.")
                        }
                    }
                }
                .onFailure { e -> progress.incrementFail(e) }
        }

        progress.stop()
        return Pair(generatedYamls, progress)
    }
}

fun main(args: Array<String>) {
    val command = Docutilians()
    try {
        command.parse(args)
    } catch (e: CliktError) {
        command.echoFormattedHelp(e)
        exitProcess(e.statusCode)
    } catch (e: Exception) {
        Terminal().println(Colors.error("Unexpected error: ${e.message}"))
        exitProcess(1)
    }
}
