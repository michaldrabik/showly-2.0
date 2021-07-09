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
  @PrimaryKey @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_season") var idSeason: Long,
  @ColumnInfo(name = "id_show_trakt") var idShowTrakt: Long,
  @ColumnInfo(name = "id_show_tvdb") var idShowTvdb: Long,
  @ColumnInfo(name = "id_show_imdb") var idShowImdb: String,
  @ColumnInfo(name = "id_show_tmdb") var idShowTmdb: Long,
  @ColumnInfo(name = "season_number") var seasonNumber: Int,
  @ColumnInfo(name = "episode_number") var episodeNumber: Int,
  @ColumnInfo(name = "episode_overview") var episodeOverview: String,
  @ColumnInfo(name = "episode_title") var title: String,
  @ColumnInfo(name = "first_aired") var firstAired: ZonedDateTime?,
  @ColumnInfo(name = "comments_count") var commentsCount: Int,
  @ColumnInfo(name = "rating") var rating: Float,
  @ColumnInfo(name = "runtime") var runtime: Int,
  @ColumnInfo(name = "votes_count") var votesCount: Int,
  @ColumnInfo(name = "is_watched") var isWatched: Boolean
)
