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

    @Nested
    @DisplayName("인코딩 및 특수문자 처리 (멀티바이트 지원)")
    inner class EncodingTest {

        @Test
        fun `한글과 이모지가 포함된 주석이 있어도 정확한 코드를 추출한다`() {
            // given
            // 💡 핵심: 클래스 정의 앞에 '한글'과 '이모지'를 배치하여
            //         String length와 Byte length의 차이를 유발시킴
            val file =
                createFile(
                    "Korean.kt",
                    """
                    package com.example

                    // 🛑 주의: 이곳에는 한글 주석이 있습니다.
                    // Tree-sitter는 이것을 바이트로 계산하고, String은 글자수로 계산합니다.
                    // 🚀 이모지도 4바이트를 차지합니다.

                    class KoreanClass(
                        val message: String = "안녕하세요"
                    )
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "KoreanClass")

            // then
            assertNotNull(result, "클래스를 찾지 못했습니다.")
            assertEquals("KoreanClass", result.className)

            // 만약 바이트 처리가 안 되었다면 여기서 IndexOutOfBoundsException이 발생하거나
            // 엉뚱한 문자열("ss KoreanCl" 등)이 잘려서 나옵니다.
            assertTrue(
                result.sourceCode.startsWith("class KoreanClass"),
                "추출된 소스코드의 시작이 올바르지 않습니다. (추출된 값: ${result.sourceCode.take(20)}...)",
            )

            assertContains(result.sourceCode, "val message: String")
        }
    }
}
