package com.devtilians.docutilians.common

import java.time.LocalDateTime

/** 단일 에러 로그 엔트리(타임스탬프, 메시지, 예외 타입 등) */
data class ErrorLogEntry(
    val timestamp: LocalDateTime,
    val message: String,
    val exceptionType: String? = null,
    val stackTrace: String? = null,
)
