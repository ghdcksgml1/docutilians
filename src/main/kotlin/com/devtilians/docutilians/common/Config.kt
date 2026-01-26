package com.devtilians.docutilians.common

import com.devtilians.docutilians.constants.Language
import java.nio.file.Path

data class Config(
    val projectDirPath: Path,
    val outputDirPath: Path = Path.of(".docutilians/openapi"),
    val outputFilename: String = "openapi",
    val language: Language = Language.EN,
) {
    fun getPartialYamlOutputFilePath(filename: String): Path {
        return projectDirPath.resolve(outputDirPath).resolve("components").resolve(filename)
    }

    fun getMergeYamlOutputFilePath(): Path {
        return projectDirPath.resolve(outputDirPath).resolve("$outputFilename.yaml")
    }

    fun getScalarHtmlFilePath(): Path {
        return projectDirPath.resolve(outputDirPath).resolve("$outputFilename.html")
    }
}
