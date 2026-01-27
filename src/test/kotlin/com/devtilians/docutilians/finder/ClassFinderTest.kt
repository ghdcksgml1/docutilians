package com.devtilians.docutilians.finder

import com.devtilians.docutilians.finder.impl.KotlinClassFinder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.*

class ClassFinderTest {

    // KotlinClassFinder를 통해 ClassFinder 공통 로직 테스트
    private lateinit var finder: ClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = KotlinClassFinder()
    }

    private fun createFile(relativePath: String, content: String): Path {
        val file = tempDir.resolve(relativePath)
        file.parent.createDirectories()
        file.writeText(content)
        return file
    }

    @Nested
    @DisplayName("findClassByName - 단일 파일")
    inner class FindInSingleFile {

        @Test
        fun `파일에서 클래스를 찾는다`() {
            // given
            val file =
                createFile(
                    "User.kt",
                    """
                    package com.example

                    class User(val name: String)
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "User")

            // then
            assertNotNull(result)
            assertEquals("User", result.className)
            assertEquals(3, result.lineNumber)
        }

        @Test
        fun `존재하지 않는 클래스는 null 반환`() {
            // given
            val file = createFile("User.kt", "class User")

            // when
            val result = finder.findClassByName(file, "NotExist")

            // then
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("findClassByName - 디렉토리 탐색")
    inner class FindInDirectory {

        @Test
        fun `파일에 없으면 하위 디렉토리에서 찾는다`() {
            // given
            val entryFile = createFile("Main.kt", "class Main")
            createFile(
                "domain/User.kt",
                """
                package com.example.domain

                class User(val id: Long)
                """
                    .trimIndent(),
            )

            // when
            val result = finder.findClassByName(entryFile, "User")

            // then
            assertNotNull(result)
            assertEquals("User", result.className)
        }

        @Test
        fun `중첩된 디렉토리에서도 찾는다`() {
            // given
            val entryFile = createFile("App.kt", "class App")
            createFile("a/b/c/Deep.kt", "class Deep")

            // when
            val result = finder.findClassByName(entryFile, "Deep")

            // then
            assertNotNull(result)
            assertEquals("Deep", result.className)
        }
    }

    @Nested
    @DisplayName("파일 확장자 검증")
    inner class FileExtensionValidation {

        @Test
        fun `잘못된 확장자는 예외 발생`() {
            // given
            val javaFile = createFile("User.java", "public class User {}")

            // when & then
            val ex =
                assertFailsWith<IllegalArgumentException> {
                    finder.findClassByName(javaFile, "User")
                }
            assertContains(ex.message!!, "Expected .kt file")
        }
    }

    @Nested
    @DisplayName("ClassLocation 검증")
    inner class ClassLocationValidation {

        @Test
        fun `ClassLocation 필드가 올바르게 설정된다`() {
            // given
            val file =
                createFile(
                    "Person.kt",
                    """
                    package com.example

                    data class Person(
                        val name: String,
                        val age: Int
                    )
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Person")

            // then
            assertNotNull(result)
            assertEquals("Person", result.className)
            assertEquals(file.toAbsolutePath().toString(), result.filePath)
            assertEquals(3, result.lineNumber)
            assertContains(result.sourceCode, "data class Person")
            assertContains(result.sourceCode, "val name: String")
        }
    }
}
