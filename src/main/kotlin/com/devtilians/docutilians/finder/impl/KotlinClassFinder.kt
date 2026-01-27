package com.devtilians.docutilians.finder.impl

import com.devtilians.docutilians.finder.ClassFinder
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TreeSitterKotlin

class KotlinClassFinder : ClassFinder() {

    override val language: TSLanguage = TreeSitterKotlin()
    override val fileExtension: String = "kt"
    override val declarationTypes: Set<String> =
        setOf(
            "class_declaration",
            "object_declaration",
            "interface_declaration",
            "companion_object",
            "enum_class_body",
            "typealias_declaration",
        )
    override val importNodeTypes = setOf("import_header", "import_list")

    override fun findDeclarationName(node: TSNode, source: String): String? {
        return findChildByFieldName(node, "name", source)
            ?: findChildByType(node, setOf("type_identifier", "simple_identifier"), source)
    }
}
