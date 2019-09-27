package com.michaldrabik.showly2.ui.common

import androidx.room.withTransaction
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Season as SeasonDb

@AppScope
class EpisodesManager @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun setEpisodeWatched(episodeBundle: EpisodeBundle) {
    database.withTransaction {
      val (episode, season, show) = episodeBundle

      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)
      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)

      val localSeason = database.seasonsDao().getById(season.ids.trakt)
      if (localSeason == null) {
        database.seasonsDao().upsert(dbSeason)
      }
      database.episodesDao().upsert(dbEpisode)
      onEpisodeSet(season, dbSeason)
    }
  }

  suspend fun setEpisodeUnwatched(episodeBundle: EpisodeBundle) {
    database.withTransaction {
      val (episode, season, show) = episodeBundle

      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)
      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)

      database.episodesDao().upsert(dbEpisode.copy(isWatched = false))
      onEpisodeSet(season, dbSeason)
    }
  }

  private suspend fun onEpisodeSet(season: Season, dbSeason: SeasonDb) {
    val localEpisodes = database.episodesDao().getAllForSeason(season.ids.trakt)
    if (localEpisodes.count { it.isWatched } == season.episodeCount) {
      database.seasonsDao().update(dbSeason.copy(isWatched = true))
    } else {
      database.seasonsDao().update(dbSeason.copy(isWatched = false))
    }
  }

  suspend fun getWatchedEpisodesIds(season: Season): List<Ids> {
    return database.episodesDao().getAllForSeason(season.ids.trakt)
      .filter { it.isWatched }
      .map { Ids.EMPTY.copy(trakt = it.idTrakt) }
  }
}