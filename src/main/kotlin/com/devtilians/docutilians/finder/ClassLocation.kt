package com.devtilians.docutilians.finder

data class ClassLocation(
    val className: String,
    val filePath: String,
    val lineNumber: Int,
    val sourceCode: String,
    val imports: List<String> = emptyList(),
)
