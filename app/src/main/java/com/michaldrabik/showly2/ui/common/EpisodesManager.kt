package com.michaldrabik.showly2.ui.common

import androidx.room.withTransaction
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Ids
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.SeasonBundle
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Episode as EpisodeDb

@AppScope
class EpisodesManager @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun getWatchedSeasonsIds(show: Show): List<Ids> {
    return database.seasonsDao().getAllForShow(show.ids.trakt)
      .filter { it.isWatched }
      .map { Ids.EMPTY.copy(trakt = it.idTrakt) }
  }

  suspend fun getWatchedEpisodesIds(show: Show): List<Ids> {
    return database.episodesDao().getAllForShow(show.ids.trakt)
      .filter { it.isWatched }
      .map { Ids.EMPTY.copy(trakt = it.idTrakt) }
  }

  suspend fun setSeasonWatched(seasonBundle: SeasonBundle) {
    database.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, true)
      val localSeason = database.seasonsDao().getById(season.ids.trakt)
      if (localSeason == null) {
        database.seasonsDao().upsert(dbSeason)
      }

      val watchedEpisodes = database.episodesDao().getAllForSeason(season.id).filter { it.isWatched }
      val toAdd = mutableListOf<EpisodeDb>()

      season.episodes.forEach { ep ->
        if (watchedEpisodes.none { it.idTrakt == ep.id }) {
          val dbEpisode = mappers.episode.toDatabase(ep, season, show.ids.trakt, true)
          toAdd.add(dbEpisode)
        }
      }

      database.episodesDao().upsert(toAdd)
      database.seasonsDao().update(dbSeason)
    }
  }

  suspend fun setSeasonUnwatched(seasonBundle: SeasonBundle) {
    database.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)
      val watchedEpisodes = database.episodesDao().getAllForSeason(season.id).filter { it.isWatched }
      val toSet = watchedEpisodes.map { it.copy(isWatched = false) }

      database.episodesDao().upsert(toSet)
      database.seasonsDao().update(dbSeason)
    }
  }

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
      onEpisodeSet(season, show)
    }
  }

  suspend fun setEpisodeUnwatched(episodeBundle: EpisodeBundle) {
    database.withTransaction {
      val (episode, season, show) = episodeBundle

      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)

      database.episodesDao().upsert(dbEpisode.copy(isWatched = false))
      onEpisodeSet(season, show)
    }
  }

  private suspend fun onEpisodeSet(season: Season, show: Show) {
    val localEpisodes = database.episodesDao().getAllForSeason(season.ids.trakt)
    val isWatched = localEpisodes.count { it.isWatched } == season.episodeCount
    val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, isWatched)
    database.seasonsDao().update(dbSeason)
  }
}