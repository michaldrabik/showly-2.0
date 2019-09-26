package com.michaldrabik.showly2.ui.common

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class EpisodesManager @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun addEpisodeToWatched(episode: Episode, season: Season, showId: Long) {
    val dbSeason = mappers.season.toDatabase(season, showId, false)
    val dbEpisode = mappers.episode.toDatabase(episode, season, showId, true)
    database.episodesDao().upsert(dbEpisode)
  }

  suspend fun removeEpisodeFromWatched(episode: Episode) {
    database.episodesDao().delete(episode.ids.trakt)
  }

}