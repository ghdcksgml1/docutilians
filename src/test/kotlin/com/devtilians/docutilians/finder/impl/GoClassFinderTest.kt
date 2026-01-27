package com.devtilians.docutilians.finder.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.*

class GoClassFinderTest {

    private lateinit var finder: GoClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = GoClassFinder()
    }

    private fun createGoFile(fileName: String, content: String): Path {
        val file = tempDir.resolve(fileName)
        file.writeText(content)
        return file
    }

    @Nested
    @DisplayName("struct 찾기")
    inner class FindStruct {

        @Test
        fun `기본 struct를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "user.go",
                    """
                    package model

                    type User struct {
                        ID   int
                        Name string
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "User")

            // then
            assertNotNull(result)
            assertEquals("User", result.className)
            assertContains(result.sourceCode, "User struct")
        }

        @Test
        fun `빈 struct를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "empty.go",
                    """
                    package model

                    type Empty struct{}
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
        fun `여러 struct 중 특정 struct를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "models.go",
                    """
                    package model

                    type First struct {
                        Value int
                    }

                    type Second struct {
                        Value string
                    }

                    type Third struct {
                        Value bool
                    }
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
        fun `embedded field가 있는 struct를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "embedded.go",
                    """
                    package model

                    type Base struct {
                        ID int
                    }

                    type Extended struct {
                        Base
                        Name string
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Extended")

            // then
            assertNotNull(result)
            assertEquals("Extended", result.className)
            assertContains(result.sourceCode, "Base")
        }
    }

    @Nested
    @DisplayName("interface 찾기")
    inner class FindInterface {

        @Test
        fun `interface를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "repository.go",
                    """
                    package repository

                    type Repository interface {
                        FindByID(id int) (*Entity, error)
                        Save(entity *Entity) error
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Repository")

            // then
            assertNotNull(result)
            assertEquals("Repository", result.className)
            assertContains(result.sourceCode, "interface")
        }

        @Test
        fun `빈 interface를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "any.go",
                    """
                    package types

                    type Any interface{}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Any")

            // then
            assertNotNull(result)
            assertEquals("Any", result.className)
        }

        @Test
        fun `embedded interface가 있는 interface를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "readwriter.go",
                    """
                    package io

                    type Reader interface {
                        Read(p []byte) (n int, err error)
                    }

                    type Writer interface {
                        Write(p []byte) (n int, err error)
                    }

                    type ReadWriter interface {
                        Reader
                        Writer
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "ReadWriter")

            // then
            assertNotNull(result)
            assertEquals("ReadWriter", result.className)
        }
    }

    @Nested
    @DisplayName("type alias 찾기")
    inner class FindTypeAlias {

        @Test
        fun `type alias를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "types.go",
                    """
                    package types

                    type UserID = int64
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserID")

            // then
            assertNotNull(result)
            assertEquals("UserID", result.className)
        }

        @Test
        fun `custom type을 찾는다`() {
            // given
            val file =
                createGoFile(
                    "status.go",
                    """
                    package types

                    type Status int

                    const (
                        StatusPending Status = iota
                        StatusActive
                        StatusInactive
                    )
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
        fun `function type을 찾는다`() {
            // given
            val file =
                createGoFile(
                    "handler.go",
                    """
                    package http

                    type HandlerFunc func(w ResponseWriter, r *Request)
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "HandlerFunc")

            // then
            assertNotNull(result)
            assertEquals("HandlerFunc", result.className)
        }
    }

    @Nested
    @DisplayName("찾기 실패 케이스")
    inner class FindFailure {

        @Test
        fun `존재하지 않는 타입은 null을 반환한다`() {
            // given
            val file =
                createGoFile(
                    "user.go",
                    """
                    package model

                    type User struct {
                        Name string
                    }
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
            val file = createGoFile("empty.go", "package empty")

            // when
            val result = finder.findClassByName(file, "Empty")

            // then
            assertNull(result)
        }

        @Test
        fun `함수만 있는 파일에서 타입을 찾으면 null을 반환한다`() {
            // given
            val file =
                createGoFile(
                    "utils.go",
                    """
                    package utils

                    func Hello() string {
                        return "Hello"
                    }

                    func World() string {
                        return "World"
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNull(result)
        }

        @Test
        fun `변수 선언은 찾지 않는다`() {
            // given
            val file =
                createGoFile(
                    "vars.go",
                    """
                    package config

                    var Config = struct {
                        Port int
                    }{
                        Port: 8080,
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
        fun `go가 아닌 파일은 예외를 던진다`() {
            // given
            val kotlinFile = tempDir.resolve("MyClass.kt")
            kotlinFile.writeText("class MyClass")

            // when & then
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    finder.findClassByName(kotlinFile, "MyClass")
                }
            assertContains(exception.message!!, "Expected .go file")
        }
    }

    @Nested
    @DisplayName("ClassLocation 검증")
    inner class ClassLocationValidation {

        @Test
        fun `ClassLocation의 모든 필드가 올바르게 설정된다`() {
            // given
            val file =
                createGoFile(
                    "person.go",
                    """
                    package model

                    type Person struct {
                        Name string
                        Age  int
                    }

                    func (p *Person) Greet() string {
                        return "Hello, I'm " + p.Name
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
            assertContains(result.sourceCode, "Person struct")
        }
    }

    @Nested
    @DisplayName("제네릭 타입 찾기")
    inner class FindGenericType {

        @Test
        fun `제네릭 struct를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "generic.go",
                    """
                    package container

                    type Container[T any] struct {
                        Value T
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

        @Test
        fun `제네릭 interface를 찾는다`() {
            // given
            val file =
                createGoFile(
                    "comparable.go",
                    """
                    package types

                    type Comparable[T any] interface {
                        CompareTo(other T) int
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Comparable")

            // then
            assertNotNull(result)
            assertEquals("Comparable", result.className)
        }
    }

    @Nested
    @DisplayName("import 추출")
    inner class ExtractImports {

        @Test
        fun `단일 import를 추출한다`() {
            // given
            val file =
                createGoFile(
                    "service.go",
                    """
                    package service

                    import "fmt"

                    type Service struct{}

                    func (s *Service) Print() {
                        fmt.Println("hello")
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Service")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "fmt")
        }

        @Test
        fun `여러 import를 추출한다 (괄호 형식)`() {
            // given
            val file =
                createGoFile(
                    "user_service.go",
                    """
                    package service

                    import (
                        "context"
                        "errors"
                        "time"
                    )

                    type UserService struct{}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertContains(result.imports[0], "context")
            assertContains(result.imports[0], "errors")
            assertContains(result.imports[0], "time")
        }

        @Test
        fun `외부 패키지 import를 추출한다`() {
            // given
            val file =
                createGoFile(
                    "handler.go",
                    """
                    package handler

                    import (
                        "net/http"
                        
                        "github.com/gin-gonic/gin"
                        "github.com/google/uuid"
                    )

                    type Handler struct{}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Handler")

            // then
            assertNotNull(result)
            assertContains(result.imports[0], "net/http")
            assertContains(result.imports[0], "github.com/gin-gonic/gin")
        }

        @Test
        fun `alias import를 추출한다`() {
            // given
            val file =
                createGoFile(
                    "client.go",
                    """
                    package client

                    import (
                        "context"
                        
                        pb "github.com/example/proto"
                        _ "github.com/lib/pq"
                        . "github.com/onsi/gomega"
                    )

                    type Client struct{}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Client")

            // then
            assertNotNull(result)
            assertContains(result.imports[0], "pb \"github.com/example/proto\"")
            assertContains(result.imports[0], "_ \"github.com/lib/pq\"")
        }

        @Test
        fun `내부 패키지 import를 추출한다`() {
            // given
            val file =
                createGoFile(
                    "app.go",
                    """
                    package main

                    import (
                        "myproject/internal/domain"
                        "myproject/internal/repository"
                        "myproject/pkg/utils"
                    )

                    type App struct{}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "App")

            // then
            assertNotNull(result)
            assertContains(result.imports[0], "myproject/internal/domain")
        }

        @Test
        fun `import가 없으면 빈 리스트를 반환한다`() {
            // given
            val file =
                createGoFile(
                    "simple.go",
                    """
                    package simple

                    type Simple struct{}
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
