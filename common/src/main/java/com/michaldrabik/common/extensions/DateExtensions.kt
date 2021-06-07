package com.michaldrabik.common.extensions

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

fun nowUtcDay(): LocalDate = LocalDate.now()

fun nowUtcMillis(): Long = nowUtc().toMillis()

fun dateFromMillis(millis: Long): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("UTC"))

fun dateIsoStringFromMillis(millis: Long): String = dateFromMillis(millis).format(DateTimeFormatter.ISO_INSTANT)

fun ZonedDateTime.toMillis() = this.toInstant().toEpochMilli()

fun ZonedDateTime.toLocalZone(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())
