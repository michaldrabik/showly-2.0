package com.michaldrabik.showly2.utilities.extensions

import com.michaldrabik.showly2.Config
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.Temporal

fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

fun nowUtcMillis(): Long = nowUtc().toInstant().toEpochMilli()

fun ZonedDateTime.toMillis() = this.toInstant().toEpochMilli()

fun ZonedDateTime.toLocalTimeZone(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())

fun Temporal.toDisplayString(): String = Config.DISPLAY_DATE_FORMAT.format(this)
