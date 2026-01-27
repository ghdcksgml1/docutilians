package com.devtilians.docutilians.finder.impl

import com.devtilians.docutilians.finder.ClassFinder
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TreeSitterTypescript

class TypeScriptClassFinder : ClassFinder() {

    override val language: TSLanguage = TreeSitterTypescript()
    override val fileExtension: String = "ts"
    override val declarationTypes: Set<String> =
        setOf(
            "class_declaration",
            "abstract_class_declaration",
            "interface_declaration",
            "type_alias_declaration",
            "ambient_declaration",
        )
    override val importNodeTypes = setOf("import_statement")

    override fun findDeclarationName(node: TSNode, source: String): String? {
        return findChildByFieldName(node, "name", source)
    }
}
