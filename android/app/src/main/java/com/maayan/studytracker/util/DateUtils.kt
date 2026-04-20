package com.maayan.studytracker.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toIso(date: LocalDate): String = date.format(ISO)

    /** Parses an ISO yyyy-MM-dd string, returning null on malformed input rather than throwing. */
    fun fromIsoOrNull(iso: String): LocalDate? = runCatching { LocalDate.parse(iso, ISO) }.getOrNull()

    /** Start of the week containing [date], with weeks starting on Sunday. */
    fun startOfWeek(date: LocalDate): LocalDate {
        // ISO dayOfWeek: Mon=1..Sun=7. We want Sun=0, Mon=1, ..., Sat=6.
        val diff = date.dayOfWeek.value % 7
        return date.minusDays(diff.toLong())
    }

    fun endOfWeek(date: LocalDate): LocalDate = startOfWeek(date).plusDays(6)
}
