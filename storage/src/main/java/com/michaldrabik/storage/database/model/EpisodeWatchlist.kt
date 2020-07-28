package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.michaldrabik.storage.database.converters.DateConverter
import org.threeten.bp.ZonedDateTime

@TypeConverters(DateConverter::class)
data class EpisodeWatchlist(
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_show_trakt") var idShowTrakt: Long,
  @ColumnInfo(name = "id_season") var idSeason: Long,
  @ColumnInfo(name = "season_number") var seasonNumber: Int,
  @ColumnInfo(name = "episode_number") var episodeNumber: Int,
  @ColumnInfo(name = "first_aired") var firstAired: ZonedDateTime?,
  @ColumnInfo(name = "is_watched") var isWatched: Boolean
)
