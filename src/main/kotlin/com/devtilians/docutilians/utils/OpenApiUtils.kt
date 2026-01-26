package com.devtilians.docutilians.utils

import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions

object OpenApiUtils {

    fun validateOpenApi(yamlContent: String): Boolean {
        val options = ParseOptions().apply { isResolve = true }

        val result = OpenAPIV3Parser().readContents(yamlContent, null, options)

        if (result.messages.isEmpty()) {
            return true
        } else {
            return false
        }
    }
}
