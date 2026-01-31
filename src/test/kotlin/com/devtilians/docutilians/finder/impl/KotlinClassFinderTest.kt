package com.devtilians.docutilians.finder.impl

import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir

class KotlinClassFinderTest {

    private lateinit var finder: KotlinClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = KotlinClassFinder()
    }

    private fun createKotlinFile(fileName: String, content: String): Path {
        val file = tempDir.resolve(fileName)
        file.writeText(content)
        return file
    }

    @Nested
    @DisplayName("클래스 찾기")
    inner class FindClass {

        @Test
        fun `기본 클래스를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "MyClass.kt",
                    """
                    package com.example

                    class MyClass {
                        fun hello() = "Hello"
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "MyClass")

            // then
            assertNotNull(result)
            assertEquals("MyClass", result.className)
            assertEquals(3, result.lineNumber)
            assertContains(result.sourceCode, "class MyClass")
        }

        @Test
        fun `data class를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "User.kt",
                    """
                    package com.example

                    data class User(
                        val id: Long,
                        val name: String,
                    )
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "User")

            // then
            assertNotNull(result)
            assertEquals("User", result.className)
            assertContains(result.sourceCode, "data class User")
        }

        @Test
        fun `여러 클래스 중 특정 클래스를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "Models.kt",
                    """
                    package com.example

                    class First
                    class Second
                    class Third
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Second")

            // then
            assertNotNull(result)
            assertEquals("Second", result.className)
            assertEquals(4, result.lineNumber)
        }
    }

    @Nested
    @DisplayName("object 찾기")
    inner class FindObject {

        @Test
        fun `object를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "Singleton.kt",
                    """
                    package com.example

                    object Singleton {
                        val instance = "single"
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Singleton")

            // then
            assertNotNull(result)
            assertEquals("Singleton", result.className)
            assertContains(result.sourceCode, "object Singleton")
        }

        @Test
        fun `companion object도 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "WithCompanion.kt",
                    """
                    package com.example

                    class WithCompanion {
                        companion object Factory {
                            fun create() = WithCompanion()
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Factory")

            // then
            assertNotNull(result)
        }
    }

    @Nested
    @DisplayName("interface 찾기")
    inner class FindInterface {

        @Test
        fun `interface를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "Repository.kt",
                    """
                    package com.example

                    interface Repository<T> {
                        fun findById(id: Long): T?
                        fun save(entity: T): T
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Repository")

            // then
            assertNotNull(result)
            assertEquals("Repository", result.className)
            assertContains(result.sourceCode, "interface Repository")
        }
    }

    @Nested
    @DisplayName("중첩 클래스 찾기")
    inner class FindNestedClass {

        @Test
        fun `중첩 클래스를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "Outer.kt",
                    """
                    package com.example

                    class Outer {
                        class Inner {
                            fun inner() = "inner"
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Inner")

            // then
            assertNotNull(result)
            assertEquals("Inner", result.className)
        }

        @Test
        fun `sealed class의 하위 클래스를 찾는다`() {
            // given
            val file =
                createKotlinFile(
                    "Result.kt",
                    """
                    package com.example

                    sealed class Result {
                        data class Success(val data: String) : Result()
                        data class Error(val message: String) : Result()
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Success")

            // then
            assertNotNull(result)
            assertEquals("Success", result.className)
        }
    }

    @Nested
    @DisplayName("찾기 실패 케이스")
    inner class FindFailure {

        @Test
        fun `존재하지 않는 클래스는 null을 반환한다`() {
            // given
            val file =
                createKotlinFile(
                    "MyClass.kt",
                    """
                    package com.example

                    class MyClass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "NotExist")

            // then
            assertNull(result)
        }

        @Test
        fun `빈 파일에서는 null을 반환한다`() {
            // given
            val file = createKotlinFile("Empty.kt", "")

            // when
            val result = finder.findClassByName(file, "Empty")

            // then
            assertNull(result)
        }

        @Test
        fun `함수만 있는 파일에서 클래스를 찾으면 null을 반환한다`() {
            // given
            val file =
                createKotlinFile(
                    "Utils.kt",
                    """
                    package com.example

                    fun hello() = "Hello"
                    fun world() = "World"
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("파일 확장자 검증")
    inner class FileExtensionValidation {

        @Test
        fun `kt가 아닌 파일은 예외를 던진다`() {
            // given
            val javaFile = tempDir.resolve("MyClass.java")
            javaFile.writeText("public class MyClass {}")

            // when & then
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    finder.findClassByName(javaFile, "MyClass")
                }
            assertContains(exception.message!!, "Expected .kt file")
        }
    }

    @Nested
    @DisplayName("ClassLocation 검증")
    inner class ClassLocationValidation {

        @Test
        fun `ClassLocation의 모든 필드가 올바르게 설정된다`() {
            // given
            val file =
                createKotlinFile(
                    "Person.kt",
                    """
                    package com.example

                    class Person(
                        val name: String,
                        val age: Int,
                    ) {
                        fun greet() = "Hello, I'm ${'$'}name"
                    }
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
            assertContains(result.sourceCode, "class Person")
            assertContains(result.sourceCode, "fun greet()")
        }
    }

    @Nested
    @DisplayName("import 추출")
    inner class ExtractImports {

        @Test
        fun `단일 import를 추출한다`() {
            // given
            val file =
                createKotlinFile(
                    "UserService.kt",
                    """
                    package com.example

                    import com.example.domain.User

                    class UserService {
                        fun getUser(): User? = null
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "com.example.domain.User")
        }

        @Test
        fun `여러 import를 추출한다`() {
            // given
            val file =
                createKotlinFile(
                    "UserService.kt",
                    """
                    package com.example

                    import com.example.domain.User
                    import com.example.repository.UserRepository
                    import kotlinx.coroutines.flow.Flow

                    class UserService(private val repo: UserRepository) {
                        fun findAll(): Flow<User> = repo.findAll()
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            println("Imports: ${result?.imports}")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "com.example.domain.User")
            assertContains(result.imports[0], "com.example.repository.UserRepository")
            assertContains(result.imports[0], "kotlinx.coroutines.flow.Flow")
        }

        @Test
        fun `와일드카드 import를 추출한다`() {
            // given
            val file =
                createKotlinFile(
                    "Service.kt",
                    """
                    package com.example

                    import com.example.domain.*
                    import java.util.*

                    class Service
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Service")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "import com.example.domain.*")
            assertContains(result.imports[0], "import java.util.*")
        }

        @Test
        fun `alias import를 추출한다`() {
            // given
            val file =
                createKotlinFile(
                    "Handler.kt",
                    """
                    package com.example

                    import java.util.Date as JavaDate
                    import kotlinx.datetime.LocalDate as KotlinDate

                    class Handler
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Handler")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "import java.util.Date as JavaDate")
            assertContains(result.imports[0], "import kotlinx.datetime.LocalDate as KotlinDate")
        }

        @Test
        fun `import가 없으면 빈 리스트를 반환한다`() {
            // given
            val file =
                createKotlinFile(
                    "Simple.kt",
                    """
                    package com.example

                    class Simple
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Simple")

            // then
            assertNotNull(result)
            assertEquals(0, result.imports.size)
        }
    }
}
