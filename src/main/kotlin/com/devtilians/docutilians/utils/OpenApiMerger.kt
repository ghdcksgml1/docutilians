package com.devtilians.docutilians.utils

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

object OpenApiMerger {
    fun mergeOpenApiYamls(yamls: List<String>, title: String, version: String): String {
        val yaml = Yaml()
        val allPaths = sortedMapOf<String, Any>()
        val allSchemas = sortedMapOf<String, Any>()

        yamls.forEach { content ->
            val parsed = yaml.load<Map<String, Any>>(content) ?: return@forEach

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
