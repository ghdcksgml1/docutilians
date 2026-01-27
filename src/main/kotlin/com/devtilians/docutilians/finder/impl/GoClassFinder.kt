package com.devtilians.docutilians.finder.impl

import com.devtilians.docutilians.finder.ClassFinder
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TreeSitterGo

class GoClassFinder : ClassFinder() {
    override val language: TSLanguage = TreeSitterGo()
    override val fileExtension: String = "go"
    override val declarationTypes: Set<String> =
        setOf(
            "type_declaration",
            "type_spec",
            "type_alias",
            "function_declaration",
            "method_declaration",
        )
    override val importNodeTypes = setOf("import_declaration", "import_spec")

    override fun findDeclarationName(node: TSNode, source: String): String? {
        return findChildByFieldName(node, "name", source)
    }
}
