package com.devtilians.docutilians.finder.impl

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir

class JavaScriptClassFinderTest {

    private lateinit var finder: JavaScriptClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = JavaScriptClassFinder()
    }

    private fun createJsFile(relativePath: String, content: String): Path {
        val file = tempDir.resolve(relativePath)
        file.parent.createDirectories()
        file.writeText(content)
        return file
    }

    @Nested
    @DisplayName("class 선언")
    inner class ClassDeclaration {

        @Test
        fun `기본 class를 찾는다`() {
            // given
            val file =
                createJsFile(
                    "User.js",
                    """
                    class User {
                        constructor(name) {
                            this.name = name;
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
        }

        @Test
        fun `상속 class를 찾는다`() {
            // given
            val file =
                createJsFile(
                    "Admin.js",
                    """
                    class Admin extends User {
                        constructor(name, role) {
                            super(name);
                            this.role = role;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Admin")

            // then
            assertNotNull(result)
            assertEquals("Admin", result.className)
            assertContains(result.sourceCode, "extends User")
        }

        @Test
        fun `getter setter가 있는 class를 찾는다`() {
            // given
            val file =
                createJsFile(
                    "Product.js",
                    """
                    class Product {
                        #price;
                        
                        constructor(price) {
                            this.#price = price;
                        }
                        
                        get price() {
                            return this.#price;
                        }
                        
                        set price(value) {
                            this.#price = value;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Product")

            // then
            assertNotNull(result)
            assertContains(result.sourceCode, "get price()")
            assertContains(result.sourceCode, "set price(value)")
        }

        @Test
        fun `static 메서드가 있는 class를 찾는다`() {
            // given
            val file =
                createJsFile(
                    "Utils.js",
                    """
                    class Utils {
                        static formatDate(date) {
                            return date.toISOString();
                        }
                        
                        static async fetchData(url) {
                            return fetch(url);
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNotNull(result)
            assertContains(result.sourceCode, "static formatDate")
            assertContains(result.sourceCode, "static async fetchData")
        }
    }

    @Nested
    @DisplayName("function 선언")
    inner class FunctionDeclaration {

        @Test
        fun `function을 찾는다`() {
            // given
            val file =
                createJsFile(
                    "helpers.js",
                    """
                    function calculateTotal(items) {
                        return items.reduce((sum, item) => sum + item.price, 0);
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "calculateTotal")

            // then
            assertNotNull(result)
            assertEquals("calculateTotal", result.className)
        }

        @Test
        fun `async function을 찾는다`() {
            // given
            val file =
                createJsFile(
                    "api.js",
                    """
                    async function fetchUsers() {
                        const response = await fetch('/api/users');
                        return response.json();
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "fetchUsers")

            // then
            assertNotNull(result)
            assertContains(result.sourceCode, "async function")
        }

        @Test
        fun `generator function을 찾는다`() {
            // given
            val file =
                createJsFile(
                    "generator.js",
                    """
                    function* idGenerator() {
                        let id = 0;
                        while (true) {
                            yield id++;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "idGenerator")

            // then
            assertNotNull(result)
            assertContains(result.sourceCode, "function*")
        }
    }

    @Nested
    @DisplayName("import 추출")
    inner class ExtractImports {

        @Test
        fun `named import를 추출한다`() {
            // given
            val file =
                createJsFile(
                    "UserService.js",
                    """
                    import { User } from './domain/User.js';

                    class UserService {
                        getUser() { return new User(); }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "User")
        }

        @Test
        fun `여러 named import를 추출한다`() {
            // given
            val file =
                createJsFile(
                    "app.js",
                    """
                    import { useState, useEffect, useCallback } from 'react';
                    import { Router, Route } from 'react-router-dom';

                    function App() {
                        return null;
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "App")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
        }

        @Test
        fun `default import를 추출한다`() {
            // given
            val file =
                createJsFile(
                    "main.js",
                    """
                    import React from 'react';
                    import axios from 'axios';

                    class Main {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Main")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "React")
        }

        @Test
        fun `namespace import를 추출한다`() {
            // given
            val file =
                createJsFile(
                    "utils.js",
                    """
                    import * as fs from 'fs';
                    import * as path from 'path';

                    function Utils() {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "* as fs")
        }

        @Test
        fun `side-effect import를 추출한다`() {
            // given
            val file =
                createJsFile(
                    "app.js",
                    """
                    import './styles.css';
                    import 'normalize.css';

                    class App {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "App")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
        }

        @Test
        fun `dynamic import는 추출하지 않는다`() {
            // given
            val file =
                createJsFile(
                    "lazy.js",
                    """
                    class LazyLoader {
                        async load() {
                            const module = await import('./heavy-module.js');
                            return module;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "LazyLoader")

            // then
            assertNotNull(result)
            assertEquals(0, result.imports.size) // top-level import만 추출
        }

        @Test
        fun `import가 없으면 빈 리스트를 반환한다`() {
            // given
            val file =
                createJsFile(
                    "simple.js",
                    """
                    class Simple {
                        hello() { console.log('hello'); }
                    }
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

    @Nested
    @DisplayName("존재하지 않는 선언")
    inner class NotFound {

        @Test
        fun `존재하지 않는 class는 null 반환`() {
            // given
            val file = createJsFile("User.js", "class User {}")

            // when
            val result = finder.findClassByName(file, "NotExist")

            // then
            assertNull(result)
        }
    }
}
