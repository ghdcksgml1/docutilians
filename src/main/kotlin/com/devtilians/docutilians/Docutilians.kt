package com.devtilians.docutilians

import com.anthropic.models.messages.Model
import com.devtilians.docutilians.cli.components.animation.ProgressAnimation
import com.devtilians.docutilians.cli.components.panel.*
import com.devtilians.docutilians.cli.components.table.FileTable
import com.devtilians.docutilians.cli.components.text.Banner
import com.devtilians.docutilians.common.Config
import com.devtilians.docutilians.constants.Colors
import com.devtilians.docutilians.exceptions.OpenApiYamlParsedException
import com.devtilians.docutilians.llm.AnthropicClient
import com.devtilians.docutilians.llm.LlmClient
import com.devtilians.docutilians.llm.prompt.FileCollectPromptBuilder
import com.devtilians.docutilians.llm.prompt.PartialOpenApiYamlPromptBuilder
import com.devtilians.docutilians.llm.prompt.PromptBuilder.RouterFileInfo
import com.devtilians.docutilians.scanner.CodeScanner
import com.devtilians.docutilians.scanner.CodeScanner.ScanResult
import com.devtilians.docutilians.utils.FileUtils
import com.devtilians.docutilians.utils.OpenApiMerger
import com.devtilians.docutilians.utils.ScalarHtmlGenerator
import com.devtilians.docutilians.utils.retry
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import com.google.common.base.CaseFormat
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.*

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
    private val projectDir: Path? by
        argument(name = "PROJECT_DIR", help = "Project directory to scan")
            .path(mustExist = true, canBeFile = false)
            .optional()

    private lateinit var llm: LlmClient
    private lateinit var config: Config

    fun initialize(t: Terminal) {
        printGreeting(t)

        Model.of(claudeApiModel).validate()

        llm =
            if (claudeApiKey.isNotBlank())
                AnthropicClient(apiKey = claudeApiKey, model = Model.of(claudeApiModel))
            else AnthropicClient(model = Model.of(claudeApiModel))

        val projectDirPath = determineProjectDir(t, projectDir)

        config = Config(projectDirPath = projectDirPath, outputDirPath = openApiYamlOutputDirPath)
    }

    override fun run() = runBlocking {
        val t = Terminal(ansiLevel = AnsiLevel.TRUECOLOR, interactive = true)
        initialize(t)

        val scanResult = scanProjectTargetDir(t, config.projectDirPath)

        val (generatedYamls, progress) = generateFileToOpenApiYaml(t, scanResult)

        val mergedYaml = OpenApiMerger.mergeOpenApiYamls(generatedYamls, "Generated API", "1.0.0")

        FileUtils.writeFile(config.getMergeYamlOutputFilePath(), mergedYaml)
        FileUtils.writeFile(
            config.getScalarHtmlFilePath(),
            ScalarHtmlGenerator.generateHtmlContent(config.getMergeYamlOutputFilePath().name),
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

    private fun scanProjectTargetDir(t: Terminal, projectTargetDir: Path): ScanResult {
        t.println(Colors.progress("⬤ Starting directory scan..."))
        val scanResult = CodeScanner(projectTargetDir.normalize().pathString).scan()
        t.println(Colors.info("  └─ Found ${scanResult.files.size} controller file(s)."))

        return scanResult
    }

    private suspend fun generateFileToOpenApiYaml(
        t: Terminal,
        scanResult: ScanResult,
    ): Pair<List<String>, ProgressAnimation> {
        val generatedYamls = mutableListOf<String>()

        val progress = ProgressAnimation(t)
        progress.start(scanResult.files.size)

        scanResult.files.forEach { scanFile ->
            runCatching {
                    val file = Path.of(scanFile.absolutePath)
                    if (!file.exists() || file.isDirectory()) return@forEach

                    val fileName = file.fileName.toString()
                    progress.clearLogs()
                    progress.updateStatus("Collecting relevant files...", fileName)

                    retry(retryOn = { it is OpenApiYamlParsedException }) {
                        val fileCollectPrompt =
                            FileCollectPromptBuilder(
                                    RouterFileInfo(scanFile.absolutePath, scanFile.content)
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
                            progress.incrementFail()
                        }
                    }
                }
                .onFailure { _ -> progress.incrementFail() }
        }

        progress.stop()
        return Pair(generatedYamls, progress)
    }
}

fun main(args: Array<String>) = Docutilians().main(args)
