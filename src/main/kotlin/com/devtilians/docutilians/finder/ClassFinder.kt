package com.devtilians.docutilians.finder

import com.devtilians.docutilians.utils.FileUtils
import org.treesitter.TSLanguage
import org.treesitter.TSNode
import org.treesitter.TSParser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.readText

abstract class ClassFinder {

    protected abstract val language: TSLanguage
    protected abstract val fileExtension: String
    protected abstract val declarationTypes: Set<String>
    protected abstract val importNodeTypes: Set<String>

    private val parser: TSParser by lazy {
        TSParser().apply { language = this@ClassFinder.language }
    }

    private data class ParseResult(
        val sourceCode: String,
        val classNode: TSNode?,
        val imports: List<String>,
    )

    /** 파일 경로와 클래스명으로 클래스 위치와 소스코드를 찾음 */
    fun findClassByName(filePath: Path, className: String): ClassLocation? {
        require(filePath.extension == fileExtension) {
            "Expected .$fileExtension file, but got .${filePath.extension}"
        }

        if (filePath.exists() && Files.isRegularFile(filePath)) {
            val (sourceCode, classNode, imports) = findClassNameInFile(filePath, className)

            if (classNode != null) {

                return ClassLocation(
                    className = className,
                    filePath = filePath.toAbsolutePath().toString(),
                    lineNumber = classNode.startPoint.row + 1,
                    sourceCode = extractNodeText(sourceCode, classNode),
                    imports = imports,
                )
            }
        }

        return FileUtils.walkFiles(filePath.parent, setOf(fileExtension), 10)
            .mapNotNull { path ->
                val (sourceCode, classNode, imports) = findClassNameInFile(path, className)

                classNode?.let {
                    ClassLocation(
                        className = className,
                        filePath = filePath.toAbsolutePath().toString(),
                        lineNumber = it.startPoint.row + 1,
                        sourceCode = extractNodeText(sourceCode, it),
                        imports = imports,
                    )
                }
            }
            .firstOrNull()
    }

    private fun findClassNameInFile(filePath: Path, className: String): ParseResult {
        val sourceCode = filePath.readText()
        val tree = parser.parseString(null, sourceCode)
        val classNode = findClassDeclaration(tree.rootNode, sourceCode, className)
        val imports = extractImports(tree.rootNode, sourceCode) // rootNode 사용!

        return ParseResult(sourceCode, classNode, imports)
    }

    private fun findClassDeclaration(node: TSNode, source: String, className: String): TSNode? {
        if (node.type in declarationTypes) {
            val name = findDeclarationName(node, source)
            if (name == className) {
                return node
            }
        }

        repeat(node.childCount) { index ->
            val child = node.getChild(index)
            if (!child.isNull) {
                findClassDeclaration(child, source, className)?.let {
                    return it
                }
            }
        }

        return null
    }

    private fun extractImports(root: TSNode, source: String): List<String> {
        val imports = mutableListOf<String>()

        repeat(root.childCount) { index ->
            val child = root.getChild(index)
            if (!child.isNull && child.type in importNodeTypes) {
                imports.add(extractNodeText(source, child))
            }
        }

        return imports
    }

    /** 언어별 클래스/타입 이름 추출 로직 */
    protected abstract fun findDeclarationName(node: TSNode, source: String): String?

    protected fun extractNodeText(source: String, node: TSNode): String {
        return source.substring(node.startByte, node.endByte)
    }

    protected fun findChildByType(node: TSNode, types: Set<String>, source: String): String? {
        repeat(node.childCount) { index ->
            val child = node.getChild(index)
            if (!child.isNull && child.type in types) {
                return extractNodeText(source, child)
            }
        }
        return null
    }

    protected fun findChildByFieldName(node: TSNode, fieldName: String, source: String): String? {
        val child = node.getChildByFieldName(fieldName)
        return if (child != null && !child.isNull) {
            extractNodeText(source, child)
        } else null
    }
}
