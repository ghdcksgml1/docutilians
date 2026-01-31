package com.devtilians.docutilians.common

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** LocalDateTime을 ISO-8601 문자열로 직렬화/역직렬화하는 Gson TypeAdapter */
class LocalDateTimeTypeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    override fun read(`in`: JsonReader): LocalDateTime? {
        val str = `in`.nextString()
        return if (str.isBlank()) null else LocalDateTime.parse(str, formatter)
    }
}
