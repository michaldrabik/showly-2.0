package com.michaldrabik.common.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS

fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

fun nowUtcDay(): LocalDate = LocalDate.now()

fun nowUtcMillis(): Long = nowUtc().toMillis()

fun dateFromMillis(millis: Long): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("UTC"))

fun dateIsoStringFromMillis(millis: Long): String = dateFromMillis(millis).format(DateTimeFormatter.ISO_INSTANT)

fun ZonedDateTime.toMillis() = this.toInstant().toEpochMilli()

fun ZonedDateTime.toLocalZone(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())

fun ZonedDateTime.isSameDayOrAfter(date: ZonedDateTime): Boolean =
  this.isEqual(date.truncatedTo(DAYS)) || this.isAfter(date.truncatedTo(DAYS))

fun String?.toZonedDateTime(): ZonedDateTime? = if (this.isNullOrBlank()) null else ZonedDateTime.parse(this)
