package com.devtilians.docutilians.finder.impl

import com.devtilians.docutilians.finder.ClassFinder
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TreeSitterJavascript

class JavaScriptClassFinder : ClassFinder() {

    override val language: TSLanguage = TreeSitterJavascript()
    override val fileExtension: String = "js"

    override val declarationTypes: Set<String> =
        setOf("class_declaration", "function_declaration", "generator_function_declaration")

    override val importNodeTypes: Set<String> = setOf("import_statement")

    override fun findDeclarationName(node: TSNode, source: String): String? {
        return findChildByFieldName(node, "name", source)
    }
}
