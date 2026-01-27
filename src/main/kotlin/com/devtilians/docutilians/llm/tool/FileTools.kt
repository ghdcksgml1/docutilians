package com.devtilians.docutilians.llm.tool

import com.devtilians.docutilians.finder.ClassFinderRouter
import com.devtilians.docutilians.llm.tool.GetFile.Response
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import java.nio.file.Path
import kotlin.io.path.extension

@JsonClassDescription("Retrieves the content of a file given its absolute path.")
class GetFile(
    @JsonPropertyDescription("The absolute path of the file to retrieve.") val absolutePath: String,
    @JsonPropertyDescription("Class Name To find.") val className: String,
) : FunctionCallTool<Response> {
    companion object {
        const val NAME = "get_file"
    }

    class Response(
        @JsonPropertyDescription("tool call result") val result: String,
        @JsonPropertyDescription("The absolute path of the file.") val absolutePath: String? = null,
        @JsonPropertyDescription("Class Name To find.") val className: String? = null,
        @JsonPropertyDescription("The content of the file.") val content: String? = null,
        @JsonPropertyDescription("relevant imports of the class.") val imports: List<String>? = null,
    )

    override suspend fun execute(): Response {
        val path = Path.of(this.absolutePath)

        return ClassFinderRouter.getFinderByExtension(path.extension)
            ?.findClassByName(path, className)
            ?.let {
                Response(
                    result = "Success",
                    absolutePath = it.filePath,
                    className = it.className,
                    content = it.sourceCode,
                    imports = it.imports,
                )
            }
            ?: Response(
                result = "Error: File not found at path '${this.absolutePath}'.",
                absolutePath = this.absolutePath,
                content = null,
                imports = null,
            )
    }
}
