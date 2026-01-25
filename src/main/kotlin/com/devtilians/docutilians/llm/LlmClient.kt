package com.devtilians.docutilians.llm

import com.devtilians.docutilians.llm.prompt.PromptBuilder.Prompt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class RelevantFile(val absolutePath: String, val sourceCode: String)

data class RelevantFileResult(val files: List<RelevantFile>, val summary: String)

data class OpenApi(val yaml: String)

interface LlmClient {
    val dispatcher: CoroutineDispatcher
        get() = Dispatchers.IO.limitedParallelism(10)

    /** Collect relevant files based on the provided prompt. */
    suspend fun collectRelevantFiles(
        prompt: Prompt,
        useCacheControl: Boolean = true,
    ): RelevantFileResult

    /** Generate OpenAPI YAML text based on the provided prompt. */
    suspend fun generateOpenApiYamlText(prompt: Prompt, useCacheControl: Boolean = true): OpenApi?

    /** Aggregate multiple OpenAPI YAML snippets into a single comprehensive OpenAPI */
    suspend fun aggregateOpenApiYamls(prompt: Prompt, useCacheControl: Boolean = true): OpenApi?
}
