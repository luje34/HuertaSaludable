package com.example.huertasaludable.data.remote

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

internal object ApiDateCodec {
    private val localDateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)

    fun localDateFromEpochMillis(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()

    fun epochMillisFromLocalDate(value: String): Long =
        LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    fun localDateTimeFromEpochMillis(epochMillis: Long): String =
        localDateTimeFormatter.format(
            Instant.ofEpochMilli(epochMillis).atZone(ZoneOffset.UTC).toLocalDateTime()
        )

    fun epochMillisFromLocalDateTime(value: String): Long =
        LocalDateTime.parse(value).toInstant(ZoneOffset.UTC).toEpochMilli()

    fun nullableLocalDateFromEpochMillis(epochMillis: Long?): String? =
        epochMillis?.let(::localDateFromEpochMillis)

    fun nullableEpochMillisFromLocalDate(value: String?): Long? =
        value?.let(::epochMillisFromLocalDate)

    fun nullableLocalDateTimeFromEpochMillis(epochMillis: Long?): String? =
        epochMillis?.let(::localDateTimeFromEpochMillis)

    fun nullableEpochMillisFromLocalDateTime(value: String?): Long? =
        value?.let(::epochMillisFromLocalDateTime)
}
