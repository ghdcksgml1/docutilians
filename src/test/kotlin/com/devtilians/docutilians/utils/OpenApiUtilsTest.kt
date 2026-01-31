package com.devtilians.docutilians.utils

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class OpenApiUtilsTest {

    @Test
    fun invalidInputText() {
        // given
        val inputText = "This is not a valid OpenAPI specification."

        // when
        val result = OpenApiUtils.validateOpenApi(inputText)

        // then
        assertEquals(false, result)
    }

    @Test
    fun validInputText() {
        // given
        val inputText =
            """
            openapi: 3.0.0
            info:
              title: Sample API
              version: 1.0.0
            paths:
              /sample:
                get:
                  responses:
                    '200':
                      description: Successful response  
            """
                .trimIndent()

        // when
        val result = OpenApiUtils.validateOpenApi(inputText)

        // then
        assertEquals(true, result)
    }
}
