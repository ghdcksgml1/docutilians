package com.devtilians.docutilians.utils

object ScalarHtmlGenerator {

    fun generateHtmlContent(openApiYamlFilename: String): String {
        return """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
                <meta http-equiv="X-UA-Compatible" content="ie=edge">
                <title>Document</title>
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
