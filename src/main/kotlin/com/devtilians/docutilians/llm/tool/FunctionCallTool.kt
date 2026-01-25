package com.devtilians.docutilians.llm.tool

interface FunctionCallTool<T> {
    suspend fun execute(): T
}
