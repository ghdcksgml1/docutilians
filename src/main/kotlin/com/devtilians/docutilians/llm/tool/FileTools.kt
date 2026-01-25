package com.devtilians.docutilians.llm.tool

import com.devtilians.docutilians.llm.tool.GetFile.Response
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.useLines

@JsonClassDescription("Retrieves the content of a file given its absolute path.")
class GetFile(
    @JsonPropertyDescription("The absolute path of the file to retrieve.") val absolutePath: String
) : FunctionCallTool<Response> {
    companion object {
        const val NAME = "get_file"
    }

    class Response(
        @JsonPropertyDescription("tool call result") val result: String,
        @JsonPropertyDescription("The absolute path of the file.") val absolutePath: String? = null,
        @JsonPropertyDescription("The content of the file.") val content: String? = null,
    )

    override suspend fun execute(): Response {
        val path = Path.of(this.absolutePath)
        if (!path.exists() || !path.toFile().isFile) {
            return Response(
                result = "Error: File not found at path '${this.absolutePath}'.",
                absolutePath = this.absolutePath,
                content = null,
            )
        }

        val sourceCode = path.useLines { it.toList() }.joinToString("\n")
        return Response(result = "Success", absolutePath = this.absolutePath, content = sourceCode)
    }
}
