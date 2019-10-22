package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.storage.database.converters.DateConverter
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "seasons")
@TypeConverters(DateConverter::class)
data class Season(
  @PrimaryKey @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_show_trakt") var idShowTrakt: Long,
  @ColumnInfo(name = "season_number") var seasonNumber: Int,
  @ColumnInfo(name = "season_title") var seasonTitle: String,
  @ColumnInfo(name = "season_overview") var seasonOverview: String,
  @ColumnInfo(name = "season_first_aired") var seasonFirstAired: ZonedDateTime?,
  @ColumnInfo(name = "episodes_count") var episodesCount: Int,
  @ColumnInfo(name = "episodes_aired_count") var episodesAiredCount: Int,
  @ColumnInfo(name = "is_watched") var isWatched: Boolean
)