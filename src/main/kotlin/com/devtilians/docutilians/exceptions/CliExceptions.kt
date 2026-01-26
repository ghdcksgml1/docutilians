package com.devtilians.docutilians.exceptions

import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.UsageError

class ApiKeyNotFoundError :
    UsageError("API key not found. Set ANTHROPIC_API_KEY or use --claude-api-key option.")

class InvalidModelError(model: String) :
    UsageError("Invalid model: $model. Available: claude-haiku-4-5, claude-sonnet-4-5")

class ProjectDirNotFoundError(path: String) : UsageError("Project directory not found: $path")

class OpenApiGenerationError(cause: Throwable) :
    PrintMessage(
        "Failed to generate OpenAPI YAML: ${cause.message}",
        statusCode = 1,
        printError = true,
    )
