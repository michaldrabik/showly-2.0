package com.michaldrabik.storage.database.model

import androidx.room.ColumnInfo

data class EpisodeWatchlist(
  @ColumnInfo(name = "id_trakt") var idTrakt: Long,
  @ColumnInfo(name = "id_show_trakt") var idShowTrakt: Long,
  @ColumnInfo(name = "id_season") var idSeason: Long,
  @ColumnInfo(name = "season_number") var seasonNumber: Int,
  @ColumnInfo(name = "episode_number") var episodeNumber: Int,
  @ColumnInfo(name = "is_watched") var isWatched: Boolean
)
