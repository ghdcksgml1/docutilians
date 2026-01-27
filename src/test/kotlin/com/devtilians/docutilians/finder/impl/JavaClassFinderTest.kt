package com.devtilians.docutilians.finder.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.*

class JavaClassFinderTest {

    private lateinit var finder: JavaClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = JavaClassFinder()
    }

    private fun createJavaFile(fileName: String, content: String): Path {
        val file = tempDir.resolve(fileName)
        file.writeText(content)
        return file
    }

    @Nested
    @DisplayName("class 찾기")
    inner class FindClass {

        @Test
        fun `기본 class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "User.java",
                    """
                    package com.example;

                    public class User {
                        private String name;
                        
                        public String getName() {
                            return name;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "User")

            // then
            assertNotNull(result)
            assertEquals("User", result.className)
            assertContains(result.sourceCode, "public class User")
        }

        @Test
        fun `abstract class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Animal.java",
                    """
                    package com.example;

                    public abstract class Animal {
                        public abstract void speak();
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Animal")

            // then
            assertNotNull(result)
            assertEquals("Animal", result.className)
            assertContains(result.sourceCode, "abstract class Animal")
        }

        @Test
        fun `여러 class 중 특정 class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Models.java",
                    """
                    package com.example;

                    class First {}
                    class Second {}
                    class Third {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Second")

            // then
            assertNotNull(result)
            assertEquals("Second", result.className)
        }

        @Test
        fun `제네릭 class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Container.java",
                    """
                    package com.example;

                    public class Container<T> {
                        private T value;
                        
                        public T getValue() {
                            return value;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Container")

            // then
            assertNotNull(result)
            assertEquals("Container", result.className)
        }
    }

    @Nested
    @DisplayName("interface 찾기")
    inner class FindInterface {

        @Test
        fun `interface를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Repository.java",
                    """
                    package com.example;

                    public interface Repository<T, ID> {
                        T findById(ID id);
                        T save(T entity);
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

        @Test
        fun `default method가 있는 interface를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Greeting.java",
                    """
                    package com.example;

                    public interface Greeting {
                        default String hello() {
                            return "Hello";
                        }
                        
                        String greet(String name);
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Greeting")

            // then
            assertNotNull(result)
            assertEquals("Greeting", result.className)
        }
    }

    @Nested
    @DisplayName("enum 찾기")
    inner class FindEnum {

        @Test
        fun `enum을 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Status.java",
                    """
                    package com.example;

                    public enum Status {
                        PENDING,
                        ACTIVE,
                        INACTIVE
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Status")

            // then
            assertNotNull(result)
            assertEquals("Status", result.className)
            assertContains(result.sourceCode, "enum Status")
        }

        @Test
        fun `메서드가 있는 enum을 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Operation.java",
                    """
                    package com.example;

                    public enum Operation {
                        ADD {
                            @Override
                            public int apply(int a, int b) { return a + b; }
                        },
                        SUBTRACT {
                            @Override
                            public int apply(int a, int b) { return a - b; }
                        };
                        
                        public abstract int apply(int a, int b);
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Operation")

            // then
            assertNotNull(result)
            assertEquals("Operation", result.className)
        }
    }

    @Nested
    @DisplayName("record 찾기")
    inner class FindRecord {

        @Test
        fun `record를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Point.java",
                    """
                    package com.example;

                    public record Point(int x, int y) {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Point")

            // then
            assertNotNull(result)
            assertEquals("Point", result.className)
            assertContains(result.sourceCode, "record Point")
        }

        @Test
        fun `메서드가 있는 record를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Person.java",
                    """
                    package com.example;

                    public record Person(String name, int age) {
                        public String greeting() {
                            return "Hello, I'm " + name;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Person")

            // then
            assertNotNull(result)
            assertEquals("Person", result.className)
        }
    }

    @Nested
    @DisplayName("중첩 클래스 찾기")
    inner class FindNestedClass {

        @Test
        fun `static nested class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Outer.java",
                    """
                    package com.example;

                    public class Outer {
                        public static class Nested {
                            public void hello() {}
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Nested")

            // then
            assertNotNull(result)
            assertEquals("Nested", result.className)
        }

        @Test
        fun `inner class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Outer.java",
                    """
                    package com.example;

                    public class Outer {
                        public class Inner {
                            public void hello() {}
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
        fun `nested interface를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Map.java",
                    """
                    package com.example;

                    public interface Map<K, V> {
                        interface Entry<K, V> {
                            K getKey();
                            V getValue();
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Entry")

            // then
            assertNotNull(result)
            assertEquals("Entry", result.className)
        }
    }

    @Nested
    @DisplayName("찾기 실패 케이스")
    inner class FindFailure {

        @Test
        fun `존재하지 않는 클래스는 null을 반환한다`() {
            // given
            val file =
                createJavaFile(
                    "User.java",
                    """
                    package com.example;

                    public class User {}
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
            val file = createJavaFile("Empty.java", "package com.example;")

            // when
            val result = finder.findClassByName(file, "Empty")

            // then
            assertNull(result)
        }

        @Test
        fun `메서드만 있는 파일에서 클래스를 찾으면 null을 반환한다`() {
            // given
            val file =
                createJavaFile(
                    "Utils.java",
                    """
                    package com.example;

                    public class Utils {
                        public static String hello() {
                            return "Hello";
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "hello")

            // then
            assertNull(result)
        }

        @Test
        fun `anonymous class는 찾지 못한다`() {
            // given
            val file =
                createJavaFile(
                    "Main.java",
                    """
                    package com.example;

                    public class Main {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {}
                        };
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Runnable")

            // then
            assertNull(result) // anonymous class는 이름이 없음
        }
    }

    @Nested
    @DisplayName("파일 확장자 검증")
    inner class FileExtensionValidation {

        @Test
        fun `java가 아닌 파일은 예외를 던진다`() {
            // given
            val kotlinFile = tempDir.resolve("MyClass.kt")
            kotlinFile.writeText("class MyClass")

            // when & then
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    finder.findClassByName(kotlinFile, "MyClass")
                }
            assertContains(exception.message!!, "Expected .java file")
        }
    }

    @Nested
    @DisplayName("ClassLocation 검증")
    inner class ClassLocationValidation {

        @Test
        fun `ClassLocation의 모든 필드가 올바르게 설정된다`() {
            // given
            val file =
                createJavaFile(
                    "Person.java",
                    """
                    package com.example;

                    public class Person {
                        private String name;
                        private int age;
                        
                        public Person(String name, int age) {
                            this.name = name;
                            this.age = age;
                        }
                        
                        public String greet() {
                            return "Hello, I'm " + name;
                        }
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
            assertContains(result.sourceCode, "public class Person")
            assertContains(result.sourceCode, "public String greet()")
        }
    }

    @Nested
    @DisplayName("어노테이션이 있는 클래스 찾기")
    inner class FindAnnotatedClass {

        @Test
        fun `어노테이션이 있는 class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "UserService.java",
                    """
                    package com.example;

                    @Service
                    @Transactional
                    public class UserService {
                        public void createUser() {}
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertEquals("UserService", result.className)
        }

        @Test
        fun `어노테이션이 있는 record를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "UserDto.java",
                    """
                    package com.example;

                    @JsonIgnoreProperties(ignoreUnknown = true)
                    public record UserDto(
                        @NotNull String name,
                        @Min(0) int age
                    ) {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserDto")

            // then
            assertNotNull(result)
            assertEquals("UserDto", result.className)
        }
    }

    @Nested
    @DisplayName("상속 관계 클래스 찾기")
    inner class FindInheritedClass {

        @Test
        fun `extends가 있는 class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "Dog.java",
                    """
                    package com.example;

                    public class Dog extends Animal {
                        @Override
                        public void speak() {
                            System.out.println("Bark!");
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Dog")

            // then
            assertNotNull(result)
            assertEquals("Dog", result.className)
            assertContains(result.sourceCode, "extends Animal")
        }

        @Test
        fun `implements가 있는 class를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "UserServiceImpl.java",
                    """
                    package com.example;

                    public class UserServiceImpl implements UserService, Auditable {
                        @Override
                        public void createUser() {}
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserServiceImpl")

            // then
            assertNotNull(result)
            assertEquals("UserServiceImpl", result.className)
            assertContains(result.sourceCode, "implements UserService")
        }

        @Test
        fun `extends interface를 찾는다`() {
            // given
            val file =
                createJavaFile(
                    "CrudRepository.java",
                    """
                    package com.example;

                    public interface CrudRepository<T, ID> extends Repository<T, ID> {
                        void delete(T entity);
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "CrudRepository")

            // then
            assertNotNull(result)
            assertEquals("CrudRepository", result.className)
        }
    }

    @Nested
    @DisplayName("import 추출")
    inner class ExtractImports {

        @Test
        fun `단일 import를 추출한다`() {
            // given
            val file =
                createJavaFile(
                    "UserService.java",
                    """
                    package com.example;

                    import com.example.domain.User;

                    public class UserService {
                        public User getUser() { return null; }
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
                createJavaFile(
                    "UserService.java",
                    """
                    package com.example;

                    import com.example.domain.User;
                    import com.example.repository.UserRepository;
                    import java.util.List;
                    import java.util.Optional;

                    public class UserService {
                        private final UserRepository repo;
                        
                        public List<User> findAll() { return null; }
                        public Optional<User> findById(Long id) { return Optional.empty(); }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertEquals(4, result.imports.size)
        }

        @Test
        fun `와일드카드 import를 추출한다`() {
            // given
            val file =
                createJavaFile(
                    "Service.java",
                    """
                    package com.example;

                    import com.example.domain.*;
                    import java.util.*;

                    public class Service {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Service")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], ".*")
        }

        @Test
        fun `static import를 추출한다`() {
            // given
            val file =
                createJavaFile(
                    "TestClass.java",
                    """
                    package com.example;

                    import static org.junit.jupiter.api.Assertions.assertEquals;
                    import static org.junit.jupiter.api.Assertions.*;

                    public class TestClass {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "TestClass")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "static")
        }

        @Test
        fun `import가 없으면 빈 리스트를 반환한다`() {
            // given
            val file =
                createJavaFile(
                    "Simple.java",
                    """
                    package com.example;

                    public class Simple {}
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
