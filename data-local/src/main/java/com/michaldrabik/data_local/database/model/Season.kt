package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "seasons",
  indices = [Index("id_show_trakt")]
)
@TypeConverters(DateConverter::class)
data class Season(
  @PrimaryKey @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "id_show_trakt") val idShowTrakt: Long,
  @ColumnInfo(name = "season_number") val seasonNumber: Int,
  @ColumnInfo(name = "season_title") val seasonTitle: String,
  @ColumnInfo(name = "season_overview") val seasonOverview: String,
  @ColumnInfo(name = "season_first_aired") val seasonFirstAired: ZonedDateTime?,
  @ColumnInfo(name = "episodes_count") val episodesCount: Int,
  @ColumnInfo(name = "episodes_aired_count") val episodesAiredCount: Int,
  @ColumnInfo(name = "rating") val rating: Float?,
  @ColumnInfo(name = "is_watched") val isWatched: Boolean
)
