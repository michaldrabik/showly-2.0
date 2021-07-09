package com.michaldrabik.data_local.database.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class DateConverter {

  @TypeConverter
  fun stringToDate(value: Long?) =
    value?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC")) }

  @TypeConverter
  fun dateToString(date: ZonedDateTime?) =
    date?.toInstant()?.toEpochMilli()
}
