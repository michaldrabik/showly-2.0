package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
  tableName = "episodes", foreignKeys = [ForeignKey(
    entity = Season::class,
    parentColumns = arrayOf("id_trakt"),
    childColumns = arrayOf("id_season"),
    onDelete = CASCADE
  )]
)
data class Episode(
  @PrimaryKey @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_season") var idSeason: Long,
  @ColumnInfo(name = "id_show_trakt") var idShowTrakt: Long,
  @ColumnInfo(name = "season_number") var seasonNumber: Int,
  @ColumnInfo(name = "episode_number") var episodeNumber: Int,
  @ColumnInfo(name = "episode_overview") var episodeOverview: String,
  @ColumnInfo(name = "episode_title") var title: String,
  @ColumnInfo(name = "first_aired") var firstAired: String,
  @ColumnInfo(name = "comments_count") var commentsCount: Int,
  @ColumnInfo(name = "rating") var rating: Float,
  @ColumnInfo(name = "runtime") var runtime: Int,
  @ColumnInfo(name = "votes_count") var votesCount: Int,
  @ColumnInfo(name = "is_watched") var isWatched: Boolean
)