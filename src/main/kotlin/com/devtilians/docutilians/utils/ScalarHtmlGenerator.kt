package com.devtilians.docutilians.utils

object ScalarHtmlGenerator {

    fun generateHtmlContent(openApiYamlFilename: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>API Docs</title>
                <meta charset="utf-8"/>
            </head>
            <body>
            <script
                    id="api-reference"
                    data-url="./$openApiYamlFilename">
            </script>
            <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
            </body>
            </html>
        """
            .trimIndent()
    }
}
