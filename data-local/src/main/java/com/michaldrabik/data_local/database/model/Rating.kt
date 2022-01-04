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
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "type") var type: String,
  @ColumnInfo(name = "rating") var rating: Int,
  @ColumnInfo(name = "season_number") var seasonNumber: Int?,
  @ColumnInfo(name = "episode_number") var episodeNumber: Int?,
  @ColumnInfo(name = "rated_at") var ratedAt: ZonedDateTime,
  @ColumnInfo(name = "created_at") var createdAt: ZonedDateTime,
  @ColumnInfo(name = "updated_at") var updatedAt: ZonedDateTime
)
