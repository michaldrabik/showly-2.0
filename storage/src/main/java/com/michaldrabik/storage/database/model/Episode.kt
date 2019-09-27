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
  @ColumnInfo(name = "is_watched") var isWatched: Boolean
)