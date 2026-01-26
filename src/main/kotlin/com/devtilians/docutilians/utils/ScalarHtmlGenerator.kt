object ScalarHtmlGenerator {

    fun generateHtmlContent(openApiYamlContent: String): String {
        // </script> 태그만 이스케이프 (HTML 파싱 보호)
        val safeYaml = openApiYamlContent.replace("</script>", "<\\/script>")

        return """
            <!doctype html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>API Reference</title>
            </head>
            <body>
                <script id="api-reference" type="application/yaml">
$safeYaml
                </script>
                <script src="https://cdn.jsdelivr.net/npm/@scalar/api-reference"></script>
            </body>
            </html>
        """
            .trimIndent()
    }
}
