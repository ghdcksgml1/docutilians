package com.devtilians.docutilians.common

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.file.Path
import java.time.LocalDateTime

/** execution_log.json 파일에 실행 로그를 추가하는 클래스 */
class ExecutionLogger(private val logFile: Path) {
    private val gson = Gson()

    fun log(
        command: String,
        success: Boolean,
        message: String? = null,
        changedFiles: List<String> = emptyList(),
    ) {
        val entry =
            ExecutionLogEntry(
                timestamp = LocalDateTime.now(),
                command = command,
                success = success,
                message = message,
                changedFiles = changedFiles,
            )
        val logList = readLogList().toMutableList()
        logList.add(entry)
        writeLogList(logList)
    }

    private fun readLogList(): List<ExecutionLogEntry> {
        val file = logFile.toFile()
        if (!file.exists()) return emptyList()
        val json = file.readText()
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<ExecutionLogEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun writeLogList(logList: List<ExecutionLogEntry>) {
        val file = logFile.toFile()
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(logList))
    }
}
