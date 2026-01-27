package com.devtilians.docutilians.finder.impl

import com.devtilians.docutilians.finder.ClassFinder
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TreeSitterJava

class JavaClassFinder : ClassFinder() {

    override val language: TSLanguage = TreeSitterJava()
    override val fileExtension: String = "java"
    override val declarationTypes: Set<String> =
        setOf(
            "class_declaration",
            "interface_declaration",
            "enum_declaration",
            "record_declaration",
        )
    override val importNodeTypes = setOf("import_declaration")

    override fun findDeclarationName(node: TSNode, source: String): String? {
        return findChildByFieldName(node, "name", source)
    }
}
