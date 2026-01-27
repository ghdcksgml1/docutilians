package com.devtilians.docutilians.finder

import com.devtilians.docutilians.constants.SupportDevLanguage
import com.devtilians.docutilians.finder.impl.*

object ClassFinderRouter {

    // 팩토리 함수로 관리
    private val finderFactories: Map<SupportDevLanguage, () -> ClassFinder> =
        mapOf(
            SupportDevLanguage.KOTLIN to { KotlinClassFinder() },
            SupportDevLanguage.JAVA to { JavaClassFinder() },
            SupportDevLanguage.TYPESCRIPT to { TypeScriptClassFinder() },
            SupportDevLanguage.JAVASCRIPT to { JavaScriptClassFinder() },
            SupportDevLanguage.PYTHON to { PythonClassFinder() },
            SupportDevLanguage.GO to { GoClassFinder() },
        )

    fun getFinderByExtension(extension: String): ClassFinder? {
        val language = SupportDevLanguage.entries.find { it.extension == extension }
        return language?.let { finderFactories[it]?.invoke() }
    }
}
