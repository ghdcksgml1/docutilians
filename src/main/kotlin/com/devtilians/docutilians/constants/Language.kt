package com.devtilians.docutilians.constants

enum class Language {
    EN,
    KO,
}

enum class SupportDevLanguage(val extension: String, val displayName: String) {
    KOTLIN("kt", "Kotlin"),
    JAVA("java", "Java"),
    TYPESCRIPT("ts", "TypeScript"),
    JAVASCRIPT("js", "JavaScript"),
    PYTHON("py", "Python"),
    GO("go", "Go"),
    UNKNOWN("", "Unknown");

    companion object {
        fun fromExtension(ext: String): SupportDevLanguage {
            return entries.find { it.extension == ext } ?: UNKNOWN
        }
    }
}
