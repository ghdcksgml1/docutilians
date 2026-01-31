package com.devtilians.docutilians.common

import java.time.LocalDateTime

/** 실행 로그 엔트리 (명령, 성공여부, 메시지, 변경 파일 등) */
data class ExecutionLogEntry(
    val timestamp: LocalDateTime,
    val command: String,
    val success: Boolean,
    val message: String? = null,
    val changedFiles: List<String> = emptyList(),
)
