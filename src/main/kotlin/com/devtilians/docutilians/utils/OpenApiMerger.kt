package com.devtilians.docutilians.utils

import com.devtilians.docutilians.common.GlobalState
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

object OpenApiMerger {

    /** Validates if content looks like valid OpenAPI YAML */
    private fun isValidOpenApiYaml(content: String): Boolean {
        val trimmed = content.trim()

        // Must start with valid YAML key (paths:, schemas:, openapi:, etc.)
        val validStarts = listOf("paths:", "schemas:", "openapi:", "info:", "components:")
        if (!validStarts.any { trimmed.startsWith(it) }) {
            return false
        }

        // Should not contain markdown patterns
        val invalidPatterns =
            listOf(
                Regex("""^\d+\.\s+\*\*""", RegexOption.MULTILINE), // 1. **filename**
                Regex("""^#+\s+""", RegexOption.MULTILINE), // # Header
                Regex("""^-\s+\*\*""", RegexOption.MULTILINE), // - **item**
                Regex("""```"""), // code blocks
            )
        if (invalidPatterns.any { it.containsMatchIn(trimmed) }) {
            return false
        }

        return true
    }

    suspend fun mergeOpenApiYamls(yamls: List<String>, title: String, version: String): String {
        val yaml = Yaml()
        val allPaths = sortedMapOf<String, Any>()
        val allSchemas = sortedMapOf<String, Any>()

        yamls.forEach { content ->
            // Skip invalid YAML content
            if (!isValidOpenApiYaml(content)) {
                GlobalState.logError("[WARN] Skipping invalid YAML content: ${content.take(50)}...")
                return@forEach
            }

            val parsed =
                runCatching { yaml.load<Map<String, Any>>(content) }
                    .getOrElse { e ->
                        GlobalState.logError("[WARN] Failed to parse YAML: ${e.message}")
                        null
                    } ?: return@forEach

            (parsed["paths"] as? Map<String, Any>)?.forEach { (path, methods) ->
                allPaths.merge(path, methods) { old, new ->
                    (old as MutableMap<String, Any>).apply { putAll(new as Map<String, Any>) }
                }
            }

            val schemas =
                (parsed["schemas"] as? Map<String, Any>)
                    ?: ((parsed["components"] as? Map<String, Any>)?.get("schemas")
                        as? Map<String, Any>)

            schemas?.forEach { (name, schema) -> allSchemas.putIfAbsent(name, schema) }
        }

        val merged =
            linkedMapOf(
                "openapi" to "3.0.3",
                "info" to mapOf("title" to title, "version" to version),
                "paths" to allPaths,
                "components" to mapOf("schemas" to allSchemas),
            )

        return Yaml(
                DumperOptions().apply {
                    defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                    indent = 2
                }
            )
            .dump(merged)
    }
}
