package com.devtilians.docutilians.finder.impl

import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir

class PythonClassFinderTest {

    private lateinit var finder: PythonClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = PythonClassFinder()
    }

    private fun createPythonFile(fileName: String, content: String): Path {
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
                createPythonFile(
                    "user.py",
                    """
                    class User:
                        def __init__(self, name: str):
                            self.name = name
                        
                        def greet(self) -> str:
                            return f"Hello, {self.name}"
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "User")

            // then
            assertNotNull(result)
            assertEquals("User", result.className)
            assertEquals(1, result.lineNumber)
            assertContains(result.sourceCode, "class User")
        }

        @Test
        fun `빈 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "empty.py",
                    """
                    class Empty:
                        pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Empty")

            // then
            assertNotNull(result)
            assertEquals("Empty", result.className)
        }

        @Test
        fun `여러 class 중 특정 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "models.py",
                    """
                    class First:
                        pass

                    class Second:
                        pass

                    class Third:
                        pass
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
    @DisplayName("상속 class 찾기")
    inner class FindInheritedClass {

        @Test
        fun `단일 상속 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "dog.py",
                    """
                    class Animal:
                        pass

                    class Dog(Animal):
                        def speak(self):
                            return "Bark!"
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Dog")

            // then
            assertNotNull(result)
            assertEquals("Dog", result.className)
            assertContains(result.sourceCode, "class Dog(Animal)")
        }

        @Test
        fun `다중 상속 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "flyingfish.py",
                    """
                    class Flyer:
                        pass

                    class Swimmer:
                        pass

                    class FlyingFish(Flyer, Swimmer):
                        pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "FlyingFish")

            // then
            assertNotNull(result)
            assertEquals("FlyingFish", result.className)
            assertContains(result.sourceCode, "class FlyingFish(Flyer, Swimmer)")
        }

        @Test
        fun `ABC 상속 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "repository.py",
                    """
                    from abc import ABC, abstractmethod

                    class Repository(ABC):
                        @abstractmethod
                        def find_by_id(self, id: int):
                            pass
                        
                        @abstractmethod
                        def save(self, entity):
                            pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Repository")

            // then
            assertNotNull(result)
            assertEquals("Repository", result.className)
        }
    }

    @Nested
    @DisplayName("데코레이터가 있는 class 찾기")
    inner class FindDecoratedClass {

        @Test
        fun `dataclass를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "point.py",
                    """
                    from dataclasses import dataclass

                    @dataclass
                    class Point:
                        x: int
                        y: int
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Point")

            // then
            assertNotNull(result)
            assertEquals("Point", result.className)
        }

        @Test
        fun `여러 데코레이터가 있는 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "user_dto.py",
                    """
                    from dataclasses import dataclass

                    @dataclass(frozen=True)
                    @total_ordering
                    class UserDto:
                        name: str
                        age: int
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserDto")

            // then
            assertNotNull(result)
            assertEquals("UserDto", result.className)
        }

        @Test
        fun `Pydantic BaseModel을 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "schema.py",
                    """
                    from pydantic import BaseModel

                    class UserSchema(BaseModel):
                        name: str
                        age: int
                        
                        class Config:
                            orm_mode = True
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserSchema")

            // then
            assertNotNull(result)
            assertEquals("UserSchema", result.className)
        }
    }

    @Nested
    @DisplayName("중첩 class 찾기")
    inner class FindNestedClass {

        @Test
        fun `중첩 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "outer.py",
                    """
                    class Outer:
                        class Inner:
                            def hello(self):
                                return "inner"
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
        fun `Config 내부 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "model.py",
                    """
                    class User:
                        name: str
                        
                        class Config:
                            orm_mode = True
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Config")

            // then
            assertNotNull(result)
            assertEquals("Config", result.className)
        }
    }

    @Nested
    @DisplayName("특수 메서드가 있는 class 찾기")
    inner class FindClassWithDunderMethods {

        @Test
        fun `__init__이 있는 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "person.py",
                    """
                    class Person:
                        def __init__(self, name: str, age: int):
                            self.name = name
                            self.age = age
                        
                        def __str__(self) -> str:
                            return f"Person({self.name}, {self.age})"
                        
                        def __repr__(self) -> str:
                            return self.__str__()
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Person")

            // then
            assertNotNull(result)
            assertContains(result.sourceCode, "__init__")
            assertContains(result.sourceCode, "__str__")
        }

        @Test
        fun `context manager class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "filehandler.py",
                    """
                    class FileHandler:
                        def __init__(self, filename: str):
                            self.filename = filename
                        
                        def __enter__(self):
                            self.file = open(self.filename)
                            return self.file
                        
                        def __exit__(self, exc_type, exc_val, exc_tb):
                            self.file.close()
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "FileHandler")

            // then
            assertNotNull(result)
            assertContains(result.sourceCode, "__enter__")
            assertContains(result.sourceCode, "__exit__")
        }
    }

    @Nested
    @DisplayName("찾기 실패 케이스")
    inner class FindFailure {

        @Test
        fun `존재하지 않는 class는 null을 반환한다`() {
            // given
            val file =
                createPythonFile(
                    "user.py",
                    """
                    class User:
                        pass
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
            val file = createPythonFile("empty.py", "")

            // when
            val result = finder.findClassByName(file, "Empty")

            // then
            assertNull(result)
        }

        @Test
        fun `함수만 있는 파일에서 class를 찾으면 null을 반환한다`() {
            // given
            val file =
                createPythonFile(
                    "utils.py",
                    """
                    def hello():
                        return "Hello"

                    def world():
                        return "World"
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNull(result)
        }

        @Test
        fun `변수는 찾지 못한다`() {
            // given
            val file =
                createPythonFile(
                    "config.py",
                    """
                    Config = {
                        "port": 8080,
                        "host": "localhost"
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Config")

            // then
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("파일 확장자 검증")
    inner class FileExtensionValidation {

        @Test
        fun `py가 아닌 파일은 예외를 던진다`() {
            // given
            val javaFile = tempDir.resolve("MyClass.java")
            javaFile.writeText("public class MyClass {}")

            // when & then
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    finder.findClassByName(javaFile, "MyClass")
                }
            assertContains(exception.message!!, "Expected .py file")
        }
    }

    @Nested
    @DisplayName("ClassLocation 검증")
    inner class ClassLocationValidation {

        @Test
        fun `ClassLocation의 모든 필드가 올바르게 설정된다`() {
            // given
            val file =
                createPythonFile(
                    "person.py",
                    """
                    from dataclasses import dataclass

                    @dataclass
                    class Person:
                        name: str
                        age: int
                        
                        def greet(self) -> str:
                            return f"Hello, I'm {self.name}"
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Person")

            // then
            assertNotNull(result)
            assertEquals("Person", result.className)
            assertEquals(file.toAbsolutePath().toString(), result.filePath)
            assertEquals(4, result.lineNumber)
            assertContains(result.sourceCode, "class Person")
            assertContains(result.sourceCode, "def greet")
        }
    }

    @Nested
    @DisplayName("타입 힌트가 있는 class 찾기")
    inner class FindClassWithTypeHints {

        @Test
        fun `타입 힌트가 있는 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "typed.py",
                    """
                    from typing import List, Optional, Dict

                    class TypedClass:
                        items: List[str]
                        mapping: Dict[str, int]
                        optional_value: Optional[str] = None
                        
                        def get_items(self) -> List[str]:
                            return self.items
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "TypedClass")

            // then
            assertNotNull(result)
            assertEquals("TypedClass", result.className)
        }

        @Test
        fun `Generic class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "generic.py",
                    """
                    from typing import TypeVar, Generic

                    T = TypeVar('T')

                    class Container(Generic[T]):
                        def __init__(self, value: T):
                            self.value = value
                        
                        def get(self) -> T:
                            return self.value
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
    @DisplayName("메타클래스 class 찾기")
    inner class FindMetaClass {

        @Test
        fun `메타클래스를 사용하는 class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "singleton.py",
                    """
                    class SingletonMeta(type):
                        _instances = {}
                        
                        def __call__(cls, *args, **kwargs):
                            if cls not in cls._instances:
                                cls._instances[cls] = super().__call__(*args, **kwargs)
                            return cls._instances[cls]

                    class Singleton(metaclass=SingletonMeta):
                        def __init__(self):
                            self.value = None
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Singleton")

            // then
            assertNotNull(result)
            assertEquals("Singleton", result.className)
        }

        @Test
        fun `메타클래스 자체를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "meta.py",
                    """
                    class MyMeta(type):
                        def __new__(mcs, name, bases, namespace):
                            return super().__new__(mcs, name, bases, namespace)
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "MyMeta")

            // then
            assertNotNull(result)
            assertEquals("MyMeta", result.className)
        }
    }

    @Nested
    @DisplayName("Protocol class 찾기")
    inner class FindProtocol {

        @Test
        fun `Protocol class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "protocol.py",
                    """
                    from typing import Protocol

                    class Drawable(Protocol):
                        def draw(self) -> None:
                            ...
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Drawable")

            // then
            assertNotNull(result)
            assertEquals("Drawable", result.className)
        }
    }

    @Nested
    @DisplayName("Enum class 찾기")
    inner class FindEnumClass {

        @Test
        fun `Enum class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "status.py",
                    """
                    from enum import Enum

                    class Status(Enum):
                        PENDING = "pending"
                        ACTIVE = "active"
                        INACTIVE = "inactive"
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Status")

            // then
            assertNotNull(result)
            assertEquals("Status", result.className)
        }

        @Test
        fun `IntEnum class를 찾는다`() {
            // given
            val file =
                createPythonFile(
                    "priority.py",
                    """
                    from enum import IntEnum

                    class Priority(IntEnum):
                        LOW = 1
                        MEDIUM = 2
                        HIGH = 3
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Priority")

            // then
            assertNotNull(result)
            assertEquals("Priority", result.className)
        }
    }

    @Nested
    @DisplayName("import 추출")
    inner class ExtractImports {

        @Test
        fun `import 문을 추출한다`() {
            // given
            val file =
                createPythonFile(
                    "service.py",
                    """
                    import os
                    import sys

                    class Service:
                        pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Service")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "import os")
        }

        @Test
        fun `from import 문을 추출한다`() {
            // given
            val file =
                createPythonFile(
                    "user_service.py",
                    """
                    from domain.user import User
                    from repository.user_repository import UserRepository

                    class UserService:
                        def __init__(self, repo: UserRepository):
                            self.repo = repo
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "from domain.user import User")
        }

        @Test
        fun `여러 항목 from import를 추출한다`() {
            // given
            val file =
                createPythonFile(
                    "handler.py",
                    """
                    from typing import List, Optional, Dict
                    from dataclasses import dataclass, field

                    @dataclass
                    class Handler:
                        items: List[str] = field(default_factory=list)
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Handler")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "List, Optional, Dict")
        }

        @Test
        fun `와일드카드 import를 추출한다`() {
            // given
            val file =
                createPythonFile(
                    "service.py",
                    """
                    from os.path import *
                    from typing import *

                    class Service:
                        pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Service")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "import *")
        }

        @Test
        fun `alias import를 추출한다`() {
            // given
            val file =
                createPythonFile(
                    "utils.py",
                    """
                    import numpy as np
                    import pandas as pd
                    from datetime import datetime as dt

                    class Utils:
                        pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNotNull(result)
            assertEquals(3, result.imports.size)
            assertContains(result.imports[0], "as np")
        }

        @Test
        fun `상대 import를 추출한다`() {
            // given
            val file =
                createPythonFile(
                    "api.py",
                    """
                    from . import utils
                    from .. import config
                    from .models import User
                    from ..repository import UserRepository

                    class Api:
                        pass
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Api")

            // then
            assertNotNull(result)
            assertEquals(4, result.imports.size)
            assertContains(result.imports[0], "from .")
        }

        @Test
        fun `import가 없으면 빈 리스트를 반환한다`() {
            // given
            val file =
                createPythonFile(
                    "simple.py",
                    """
                    class Simple:
                        pass
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
