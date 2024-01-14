package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "ratings",
  primaryKeys = ["id_trakt", "type"],
  indices = [
    Index(value = ["id_trakt", "type"], unique = false),
  ]
)
@TypeConverters(DateConverter::class)
data class Rating(
  @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "type") val type: String,
  @ColumnInfo(name = "rating") val rating: Int,
  @ColumnInfo(name = "season_number") val seasonNumber: Int?,
  @ColumnInfo(name = "episode_number") val episodeNumber: Int?,
  @ColumnInfo(name = "rated_at") val ratedAt: ZonedDateTime,
  @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime
)
