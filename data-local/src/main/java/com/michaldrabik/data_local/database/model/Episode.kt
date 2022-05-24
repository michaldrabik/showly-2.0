package com.michaldrabik.data_local.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.michaldrabik.data_local.database.converters.DateConverter
import java.time.ZonedDateTime

@Entity(
  tableName = "episodes",
  foreignKeys = [
    ForeignKey(
      entity = Season::class,
      parentColumns = arrayOf("id_trakt"),
      childColumns = arrayOf("id_season"),
      onDelete = CASCADE
    )
  ],
  indices = [
    Index("id_season"),
    Index("id_show_trakt")
  ]
)
@TypeConverters(DateConverter::class)
data class Episode(
  @PrimaryKey @ColumnInfo(name = "id_trakt") val idTrakt: Long,
  @ColumnInfo(name = "id_season") val idSeason: Long,
  @ColumnInfo(name = "id_show_trakt") val idShowTrakt: Long,
  @ColumnInfo(name = "id_show_tvdb") val idShowTvdb: Long,
  @ColumnInfo(name = "id_show_imdb") val idShowImdb: String,
  @ColumnInfo(name = "id_show_tmdb") val idShowTmdb: Long,
  @ColumnInfo(name = "season_number") val seasonNumber: Int,
  @ColumnInfo(name = "episode_number") val episodeNumber: Int,
  @ColumnInfo(name = "episode_number_abs") val episodeNumberAbs: Int?,
  @ColumnInfo(name = "episode_overview") val episodeOverview: String,
  @ColumnInfo(name = "episode_title") val title: String,
  @ColumnInfo(name = "first_aired") val firstAired: ZonedDateTime?,
  @ColumnInfo(name = "comments_count") val commentsCount: Int,
  @ColumnInfo(name = "rating") val rating: Float,
  @ColumnInfo(name = "runtime") val runtime: Int,
  @ColumnInfo(name = "votes_count") val votesCount: Int,
  @ColumnInfo(name = "is_watched") val isWatched: Boolean,
  @ColumnInfo(name = "last_watched_at") val lastWatchedAt: ZonedDateTime?,
)
