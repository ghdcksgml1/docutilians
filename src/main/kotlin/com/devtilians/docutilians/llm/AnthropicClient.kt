package com.devtilians.docutilians.llm

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.beta.messages.*
import com.anthropic.models.beta.messages.BetaStopReason.Companion.TOOL_USE
import com.anthropic.models.messages.Model
import com.devtilians.docutilians.exceptions.OpenApiYamlParsedException
import com.devtilians.docutilians.llm.prompt.PromptBuilder.Prompt
import com.devtilians.docutilians.llm.tool.GetFile
import com.devtilians.docutilians.utils.OpenApiUtils
import com.devtilians.docutilians.utils.extensions.aggregateToken
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.jvm.optionals.asSequence
import kotlin.jvm.optionals.getOrNull

class AnthropicClient(
    private val apiKey: String = System.getenv("ANTHROPIC_API_KEY"),
    private val model: Model = Model.CLAUDE_HAIKU_4_5,
) : LlmClient {
    private val maxRecursiveCalls = 15

    private val client: AnthropicClient =
        AnthropicOkHttpClient.builder().apiKey(this.apiKey).maxRetries(3).build()
    private val cacheControl =
        BetaCacheControlEphemeral.builder().ttl(BetaCacheControlEphemeral.Ttl.TTL_5M).build()

    override suspend fun collectRelevantFiles(
        prompt: Prompt,
        useCacheControl: Boolean,
    ): RelevantFileResult {
        val textBlockParam = BetaTextBlockParam.builder().text(prompt.userPrompt).build()

        val params =
            MessageCreateParams.builder()
                .model(model)
                .putAdditionalHeader("anthropic-beta", "structured-outputs-2025-11-13")
                .maxTokens(5000)
                .system(
                    MessageCreateParams.System.ofBetaTextBlockParams(
                        listOf(
                            BetaTextBlockParam.builder()
                                .text(prompt.systemPrompt)
                                .apply {
                                    if (useCacheControl) {
                                        cacheControl(cacheControl)
                                    }
                                }
                                .build()
                        )
                    )
                )
                .addTool(GetFile::class.java)
                .addUserMessage(
                    BetaMessageParam.Content.ofBetaContentBlockParams(
                        listOf(BetaContentBlockParam.ofText(textBlockParam))
                    )
                )

        var recursiveCalls = 0
        val summaryBuilder = StringBuilder()
        val relevantFiles = mutableListOf<RelevantFile>()

        withContext(dispatcher) {
            while (recursiveCalls++ < maxRecursiveCalls) {
                val response =
                    client.beta().messages().create(params.build()).validate().apply {
                        this.aggregateToken()
                    }

                val toolUseBlocks = mutableListOf<BetaToolUseBlock>()

                response.content().asSequence().forEach { contentBlock ->
                    contentBlock.text().asSequence().forEach { textBlock ->
                        summaryBuilder.appendLine(textBlock.text())
                    }
                    contentBlock.toolUse().asSequence().forEach { toolUseBlock ->
                        toolUseBlocks.add(toolUseBlock)
                    }
                }

                if (toolUseBlocks.isNotEmpty()) {
                    val toolResults =
                        toolUseBlocks
                            .map { toolUseBlock ->
                                async { toolUseBlock to callGetFileTool(toolUseBlock) }
                            }
                            .awaitAll()

                    toolResults
                        .filter { (_, result) ->
                            result.absolutePath != null && result.content != null
                        }
                        .forEach { (_, result) ->
                            val relevantFile =
                                RelevantFile(
                                    absolutePath = result.absolutePath!!,
                                    sourceCode = result.content!!,
                                )
                            relevantFiles.add(relevantFile)
                        }

                    params
                        .addAssistantMessageOfBetaContentBlockParams(
                            toolUseBlocks.map { toolUseBlock ->
                                BetaContentBlockParam.ofToolUse(
                                    BetaToolUseBlockParam.builder()
                                        .name(toolUseBlock.name())
                                        .id(toolUseBlock.id())
                                        .input(toolUseBlock._input())
                                        .build()
                                )
                            }
                        )
                        .addUserMessageOfBetaContentBlockParams(
                            toolResults.map { (toolUseBlock, result) ->
                                BetaContentBlockParam.ofToolResult(
                                    BetaToolResultBlockParam.builder()
                                        .toolUseId(toolUseBlock.id())
                                        .contentAsJson(result)
                                        .build()
                                )
                            }
                        )
                }

                if (response.stopReason().getOrNull() != TOOL_USE || toolUseBlocks.isEmpty()) {
                    break
                }
            }
        }

        val summary = summaryBuilder.toString().trim()
        return RelevantFileResult(summary = summary, files = relevantFiles)
    }

    override suspend fun generateOpenApiYamlText(
        prompt: Prompt,
        useCacheControl: Boolean,
    ): OpenApi? {
        val textBlockParam = BetaTextBlockParam.builder().text(prompt.userPrompt).build()

        val params =
            MessageCreateParams.builder()
                .model(model)
                .putAdditionalHeader("anthropic-beta", "structured-outputs-2025-11-13")
                .maxTokens(20000)
                .system(
                    MessageCreateParams.System.ofBetaTextBlockParams(
                        listOf(
                            BetaTextBlockParam.builder()
                                .text(prompt.systemPrompt)
                                .apply {
                                    if (useCacheControl) {
                                        cacheControl(cacheControl)
                                    }
                                }
                                .build()
                        )
                    )
                )
                .addUserMessage(
                    BetaMessageParam.Content.ofBetaContentBlockParams(
                        listOf(BetaContentBlockParam.ofText(textBlockParam))
                    )
                )

        val openApiYaml = StringBuilder()

        withContext(dispatcher) {
            val response =
                client.beta().messages().create(params.build()).validate().apply {
                    this.aggregateToken()
                }

            response.content().asSequence().forEach { contentBlock ->
                contentBlock.text().asSequence().forEach { textBlock ->
                    openApiYaml.append(textBlock.text())
                }
            }
        }

        if (openApiYaml.isBlank()) {
            return null
        }

        return OpenApi(yaml = stripCodeBlock(openApiYaml.toString()))
    }

    override suspend fun aggregateOpenApiYamls(prompt: Prompt, useCacheControl: Boolean): OpenApi? {
        TODO("Not yet implemented")
    }

    private suspend fun callGetFileTool(toolUseBlock: BetaToolUseBlock): GetFile.Response {
        return when (toolUseBlock.name()) {
            GetFile.NAME -> {
                val getFile = toolUseBlock.input(GetFile::class.java)
                return if (getFile != null) {
                    GetFile(absolutePath = getFile.absolutePath, className = getFile.className)
                        .execute()
                } else {
                    GetFile.Response(result = "Error: Invalid input for GetFile tool.")
                }
            }
            else -> GetFile.Response(result = "Error: Unknown tool '${toolUseBlock.name()}'.")
        }
    }

    private fun stripCodeBlock(response: String): String {
        return response
            .trim()
            .removePrefix("```yaml")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun verifyOpenApi(yaml: String) {
        if (!OpenApiUtils.validateOpenApi(yaml)) {
            throw OpenApiYamlParsedException()
        }
    }
}
