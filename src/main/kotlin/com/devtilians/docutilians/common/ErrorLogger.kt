package com.devtilians.docutilians.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Path
import java.time.LocalDateTime

/** error.json 파일에 에러 로그를 추가하는 클래스 */
class ErrorLogger(private val errorLogFile: Path) {
    private val gson = Gson()

    fun logError(message: String, exception: Throwable? = null) {
        val entry =
            ErrorLogEntry(
                timestamp = LocalDateTime.now(),
                message = message,
                exceptionType = exception?.javaClass?.name,
                stackTrace = exception?.stackTraceToString(),
            )
        val logList = readLogList().toMutableList()
        logList.add(entry)
        writeLogList(logList)
    }

    private fun readLogList(): List<ErrorLogEntry> {
        val file = errorLogFile.toFile()
        if (!file.exists()) return emptyList()
        val json = file.readText()
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<ErrorLogEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun writeLogList(logList: List<ErrorLogEntry>) {
        val file = errorLogFile.toFile()
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(logList))
    }
}
