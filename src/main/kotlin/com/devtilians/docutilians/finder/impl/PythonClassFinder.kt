package com.devtilians.docutilians.finder.impl

import com.devtilians.docutilians.finder.ClassFinder
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TreeSitterPython

class PythonClassFinder : ClassFinder() {

    override val language: TSLanguage = TreeSitterPython()
    override val fileExtension: String = "py"
    override val declarationTypes: Set<String> =
        setOf("class_definition", "function_definition", "decorated_definition")
    override val importNodeTypes = setOf("import_statement", "import_from_statement")

    override fun findDeclarationName(node: TSNode, source: String): String? {
        return findChildByFieldName(node, "name", source)
            ?: findChildByType(node, setOf("identifier"), source)
    }
}
