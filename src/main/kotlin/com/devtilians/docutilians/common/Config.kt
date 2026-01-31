package com.devtilians.docutilians.common

import com.devtilians.docutilians.constants.Language
import com.devtilians.docutilians.constants.SupportDevLanguage
import java.nio.file.Path

data class Config(
    val projectDirPath: Path,
    val outputDirPath: Path = Path.of(".docutilians"),
    val outputFilename: String = "openapi",
    val language: Language = Language.EN,
    val primaryDevLanguage: SupportDevLanguage = SupportDevLanguage.UNKNOWN,
    val languageStats: Map<String, Int> = emptyMap(),
) {
    fun getPartialYamlOutputFilePath(filename: String): Path {
        return projectDirPath.resolve(outputDirPath).resolve("components").resolve(filename)
    }

    fun getErrorLogFilePath(): Path {
        return projectDirPath.resolve(outputDirPath).resolve(Path.of("logs", "error.json"))
    }

    fun getExecutionLogFilePath(): Path {
        return projectDirPath.resolve(outputDirPath).resolve(Path.of("logs", "execution_log.json"))
    }

    fun getMergeYamlOutputFilePath(): Path {
        return projectDirPath
            .resolve(outputDirPath)
            .resolve(Path.of("openapi", "$outputFilename.yaml"))
    }

    fun getScalarHtmlFilePath(): Path {
        return projectDirPath
            .resolve(outputDirPath)
            .resolve(Path.of("openapi", "$outputFilename.html"))
    }

    /** Update config with scan results */
    fun withScanResult(byLanguage: Map<String, Int>): Config {
        val primary =
            byLanguage.maxByOrNull { it.value }?.key?.let { SupportDevLanguage.fromExtension(it) }
                ?: SupportDevLanguage.UNKNOWN

        return copy(primaryDevLanguage = primary, languageStats = byLanguage)
    }
}
