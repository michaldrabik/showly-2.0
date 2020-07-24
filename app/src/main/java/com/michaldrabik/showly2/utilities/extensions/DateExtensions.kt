package com.michaldrabik.showly2.utilities.extensions

import com.michaldrabik.showly2.Config
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.Temporal

fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

fun nowUtcMillis(): Long = nowUtc().toInstant().toEpochMilli()

fun dateFromMillis(millis: Long): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("UTC"))

fun ZonedDateTime.toMillis() = this.toInstant().toEpochMilli()

fun ZonedDateTime.toLocalTimeZone(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())

fun Temporal.toDisplayString(): String = Config.DISPLAY_DATE_FORMAT.format(this)

fun Temporal.toDayOnlyDisplayString(): String = Config.DISPLAY_DATE_DAY_ONLY_FORMAT.format(this)
