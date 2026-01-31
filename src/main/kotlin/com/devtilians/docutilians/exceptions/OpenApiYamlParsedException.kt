package com.devtilians.docutilians.exceptions

class OpenApiYamlParsedException(message: String = "Failed to parse yaml.") :
    RuntimeException(message)
