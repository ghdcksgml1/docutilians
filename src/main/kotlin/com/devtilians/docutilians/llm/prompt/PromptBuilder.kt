package com.devtilians.docutilians.llm.prompt

interface PromptBuilder {
    data class RouterFileInfo(val absolutePath: String, val sourceCode: String)

    data class Prompt(val systemPrompt: String, val userPrompt: String)

    fun build(): Prompt
}
