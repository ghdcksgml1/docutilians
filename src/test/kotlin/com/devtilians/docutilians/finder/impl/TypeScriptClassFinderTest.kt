package com.devtilians.docutilians.finder.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.*

class TypeScriptClassFinderTest {

    private lateinit var finder: TypeScriptClassFinder

    @TempDir lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        finder = TypeScriptClassFinder()
    }

    private fun createTsFile(fileName: String, content: String): Path {
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
                createTsFile(
                    "User.ts",
                    """
                    class User {
                        private name: string;
                        
                        constructor(name: string) {
                            this.name = name;
                        }
                        
                        greet(): string {
                            return `Hello, ${'$'}{this.name}`;
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
            assertContains(result.sourceCode, "class User")
        }

        @Test
        fun `export class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Service.ts",
                    """
                    export class UserService {
                        private users: User[] = [];
                        
                        findAll(): User[] {
                            return this.users;
                        }
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
        fun `export default class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Main.ts",
                    """
                    export default class Main {
                        run(): void {
                            console.log("Running...");
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Main")

            // then
            assertNotNull(result)
            assertEquals("Main", result.className)
        }

        @Test
        fun `abstract class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Animal.ts",
                    """
                    abstract class Animal {
                        abstract speak(): string;
                        
                        move(): void {
                            console.log("Moving...");
                        }
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
        fun `제네릭 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Container.ts",
                    """
                    class Container<T> {
                        private value: T;
                        
                        constructor(value: T) {
                            this.value = value;
                        }
                        
                        getValue(): T {
                            return this.value;
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
                createTsFile(
                    "Repository.ts",
                    """
                    interface Repository<T, ID> {
                        findById(id: ID): T | undefined;
                        save(entity: T): T;
                        delete(id: ID): void;
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
        fun `export interface를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "User.ts",
                    """
                    export interface User {
                        id: number;
                        name: string;
                        email: string;
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
        fun `extends가 있는 interface를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "CrudRepository.ts",
                    """
                    interface Repository<T> {
                        findAll(): T[];
                    }

                    interface CrudRepository<T, ID> extends Repository<T> {
                        findById(id: ID): T | undefined;
                        save(entity: T): T;
                        delete(id: ID): void;
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

        @Test
        fun `optional property가 있는 interface를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Config.ts",
                    """
                    interface Config {
                        host: string;
                        port: number;
                        ssl?: boolean;
                        timeout?: number;
                    }
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
    @DisplayName("type alias 찾기")
    inner class FindTypeAlias {

        @Test
        fun `type alias를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "types.ts",
                    """
                    type UserId = string | number;
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserId")

            // then
            assertNotNull(result)
            assertEquals("UserId", result.className)
        }

        @Test
        fun `object type alias를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Point.ts",
                    """
                    type Point = {
                        x: number;
                        y: number;
                    };
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
        fun `union type alias를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Status.ts",
                    """
                    type Status = 'pending' | 'active' | 'inactive';
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
        fun `intersection type alias를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Combined.ts",
                    """
                    type HasName = { name: string };
                    type HasAge = { age: number };
                    type Person = HasName & HasAge;
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Person")

            // then
            assertNotNull(result)
            assertEquals("Person", result.className)
        }

        @Test
        fun `제네릭 type alias를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Response.ts",
                    """
                    type ApiResponse<T> = {
                        data: T;
                        status: number;
                        message: string;
                    };
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "ApiResponse")

            // then
            assertNotNull(result)
            assertEquals("ApiResponse", result.className)
        }

        @Test
        fun `conditional type alias를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Conditional.ts",
                    """
                    type NonNullable<T> = T extends null | undefined ? never : T;
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "NonNullable")

            // then
            assertNotNull(result)
            assertEquals("NonNullable", result.className)
        }
    }

    @Nested
    @DisplayName("데코레이터가 있는 class 찾기")
    inner class FindDecoratedClass {

        @Test
        fun `데코레이터가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Controller.ts",
                    """
                    @Controller('/users')
                    class UserController {
                        @Get('/:id')
                        findOne(id: string): User {
                            return {} as User;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserController")

            // then
            assertNotNull(result)
            assertEquals("UserController", result.className)
        }

        @Test
        fun `여러 데코레이터가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Entity.ts",
                    """
                    @Entity()
                    @Table({ name: 'users' })
                    class UserEntity {
                        @PrimaryGeneratedColumn()
                        id: number;
                        
                        @Column()
                        name: string;
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserEntity")

            // then
            assertNotNull(result)
            assertEquals("UserEntity", result.className)
        }
    }

    @Nested
    @DisplayName("상속 관계 class 찾기")
    inner class FindInheritedClass {

        @Test
        fun `extends가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Dog.ts",
                    """
                    class Animal {
                        move(): void {}
                    }

                    class Dog extends Animal {
                        bark(): void {
                            console.log("Woof!");
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
                createTsFile(
                    "UserServiceImpl.ts",
                    """
                    interface UserService {
                        findAll(): User[];
                    }

                    class UserServiceImpl implements UserService {
                        findAll(): User[] {
                            return [];
                        }
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
        fun `extends와 implements가 모두 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "AdminService.ts",
                    """
                    abstract class BaseService {
                        protected log(msg: string): void {}
                    }

                    interface Auditable {
                        audit(): void;
                    }

                    class AdminService extends BaseService implements Auditable {
                        audit(): void {}
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "AdminService")

            // then
            assertNotNull(result)
            assertEquals("AdminService", result.className)
        }

        @Test
        fun `다중 implements가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "MultiImpl.ts",
                    """
                    interface Readable {
                        read(): string;
                    }

                    interface Writable {
                        write(data: string): void;
                    }

                    class FileHandler implements Readable, Writable {
                        read(): string { return ""; }
                        write(data: string): void {}
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "FileHandler")

            // then
            assertNotNull(result)
            assertEquals("FileHandler", result.className)
        }
    }

    @Nested
    @DisplayName("접근 제어자가 있는 class 찾기")
    inner class FindClassWithAccessModifiers {

        @Test
        fun `private, protected, public 멤버가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Person.ts",
                    """
                    class Person {
                        public name: string;
                        protected age: number;
                        private ssn: string;
                        
                        constructor(name: string, age: number, ssn: string) {
                            this.name = name;
                            this.age = age;
                            this.ssn = ssn;
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

        @Test
        fun `readonly 멤버가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Config.ts",
                    """
                    class AppConfig {
                        readonly apiUrl: string = "https://api.example.com";
                        readonly timeout: number = 5000;
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "AppConfig")

            // then
            assertNotNull(result)
            assertEquals("AppConfig", result.className)
        }

        @Test
        fun `static 멤버가 있는 class를 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Counter.ts",
                    """
                    class Counter {
                        static count: number = 0;
                        
                        static increment(): void {
                            Counter.count++;
                        }
                        
                        static getCount(): number {
                            return Counter.count;
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Counter")

            // then
            assertNotNull(result)
            assertEquals("Counter", result.className)
        }
    }

    @Nested
    @DisplayName("찾기 실패 케이스")
    inner class FindFailure {

        @Test
        fun `존재하지 않는 타입은 null을 반환한다`() {
            // given
            val file =
                createTsFile(
                    "User.ts",
                    """
                    class User {
                        name: string;
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
            val file = createTsFile("empty.ts", "")

            // when
            val result = finder.findClassByName(file, "Empty")

            // then
            assertNull(result)
        }

        @Test
        fun `함수만 있는 파일에서 class를 찾으면 null을 반환한다`() {
            // given
            val file =
                createTsFile(
                    "utils.ts",
                    """
                    function hello(): string {
                        return "Hello";
                    }

                    const world = (): string => "World";
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Utils")

            // then
            assertNull(result)
        }

        @Test
        fun `enum은 찾지 못한다`() {
            // given
            val file =
                createTsFile(
                    "Status.ts",
                    """
                    enum Status {
                        Pending = "PENDING",
                        Active = "ACTIVE",
                        Inactive = "INACTIVE"
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Status")

            // then
            assertNull(result) // enum_declaration이 declarationTypes에 없음
        }

        @Test
        fun `const 객체는 찾지 못한다`() {
            // given
            val file =
                createTsFile(
                    "config.ts",
                    """
                    const Config = {
                        port: 8080,
                        host: "localhost"
                    } as const;
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
        fun `ts가 아닌 파일은 예외를 던진다`() {
            // given
            val jsFile = tempDir.resolve("script.js")
            jsFile.writeText("class MyClass {}")

            // when & then
            val exception =
                assertFailsWith<IllegalArgumentException> {
                    finder.findClassByName(jsFile, "MyClass")
                }
            assertContains(exception.message!!, "Expected .ts file")
        }
    }

    @Nested
    @DisplayName("ClassLocation 검증")
    inner class ClassLocationValidation {

        @Test
        fun `ClassLocation의 모든 필드가 올바르게 설정된다`() {
            // given
            val file =
                createTsFile(
                    "Person.ts",
                    """
                    import { Injectable } from '@nestjs/common';

                    @Injectable()
                    export class PersonService {
                        private persons: Person[] = [];
                        
                        findAll(): Person[] {
                            return this.persons;
                        }
                        
                        findById(id: number): Person | undefined {
                            return this.persons.find(p => p.id === id);
                        }
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "PersonService")

            // then
            assertNotNull(result)
            assertEquals("PersonService", result.className)
            assertEquals(file.toAbsolutePath().toString(), result.filePath)
            assertEquals(4, result.lineNumber)
            assertContains(result.sourceCode, "class PersonService")
            assertContains(result.sourceCode, "findAll()")
        }
    }

    @Nested
    @DisplayName("Utility Types 찾기")
    inner class FindUtilityTypes {

        @Test
        fun `mapped type을 찾는다`() {
            // given
            val file =
                createTsFile(
                    "Readonly.ts",
                    """
                    type MyReadonly<T> = {
                        readonly [P in keyof T]: T[P];
                    };
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "MyReadonly")

            // then
            assertNotNull(result)
            assertEquals("MyReadonly", result.className)
        }

        @Test
        fun `template literal type을 찾는다`() {
            // given
            val file =
                createTsFile(
                    "EventName.ts",
                    """
                    type EventName<T extends string> = `${'$'}{T}Changed`;
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "EventName")

            // then
            assertNotNull(result)
            assertEquals("EventName", result.className)
        }
    }

    @Nested
    @DisplayName("import 추출")
    inner class ExtractImports {

        @Test
        fun `named import를 추출한다`() {
            // given
            val file =
                createTsFile(
                    "UserService.ts",
                    """
                    import { User } from './domain/User';

                    class UserService {
                        getUser(): User | undefined { return undefined; }
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
                createTsFile(
                    "UserService.ts",
                    """
                    import { User, UserDto } from './domain/User';
                    import { UserRepository } from './repository/UserRepository';
                    import { Injectable } from '@nestjs/common';

                    @Injectable()
                    class UserService {
                        constructor(private readonly repo: UserRepository) {}
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "UserService")

            // then
            assertNotNull(result)
            assertEquals(3, result.imports.size)
        }

        @Test
        fun `default import를 추출한다`() {
            // given
            val file =
                createTsFile(
                    "App.ts",
                    """
                    import React from 'react';
                    import axios from 'axios';

                    class App {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "App")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "React")
        }

        @Test
        fun `namespace import를 추출한다`() {
            // given
            val file =
                createTsFile(
                    "Utils.ts",
                    """
                    import * as fs from 'fs';
                    import * as path from 'path';

                    class Utils {}
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
        fun `type import를 추출한다`() {
            // given
            val file =
                createTsFile(
                    "Handler.ts",
                    """
                    import type { Request, Response } from 'express';

                    class Handler {
                        handle(req: Request, res: Response): void {}
                    }
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Handler")

            // then
            assertNotNull(result)
            assertEquals(1, result.imports.size)
            assertContains(result.imports[0], "type")
        }

        @Test
        fun `alias import를 추출한다`() {
            // given
            val file =
                createTsFile(
                    "Service.ts",
                    """
                    import { User as UserEntity } from './entities/User';
                    import { default as Config } from './config';

                    class Service {}
                    """
                        .trimIndent(),
                )

            // when
            val result = finder.findClassByName(file, "Service")

            // then
            assertNotNull(result)
            assertEquals(2, result.imports.size)
            assertContains(result.imports[0], "as UserEntity")
        }

        @Test
        fun `import가 없으면 빈 리스트를 반환한다`() {
            // given
            val file =
                createTsFile(
                    "Simple.ts",
                    """
                    class Simple {}
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
