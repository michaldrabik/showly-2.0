package com.michaldrabik.showly2.ui.show.seasons.episodes

import androidx.room.withTransaction
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.SeasonBundle
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.EpisodesSyncLog
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Episode as EpisodeDb
import com.michaldrabik.storage.database.model.Season as SeasonDb

@AppScope
class EpisodesManager @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun getWatchedSeasonsIds(show: Show) =
    database.seasonsDao().getAllWatchedForShow(show.ids.trakt.id)

  suspend fun getWatchedEpisodesIds(show: Show) =
    database.episodesDao().getAllWatchedForShow(show.ids.trakt.id)

  suspend fun setSeasonWatched(seasonBundle: SeasonBundle) {
    database.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, true)
      val localSeason = database.seasonsDao().getById(season.ids.trakt.id)
      if (localSeason == null) {
        database.seasonsDao().upsert(listOf(dbSeason))
      }

      val episodes = database.episodesDao().getAllForSeason(season.ids.trakt.id).filter { it.isWatched }
      val toAdd = mutableListOf<EpisodeDb>()

      season.episodes.forEach { ep ->
        if (episodes.none { it.idTrakt == ep.ids.trakt.id }) {
          val dbEpisode = mappers.episode.toDatabase(ep, season, show.ids.trakt, true)
          toAdd.add(dbEpisode)
        }
      }

      database.episodesDao().upsert(toAdd)
      database.seasonsDao().update(listOf(dbSeason))
    }
  }

  suspend fun setSeasonUnwatched(seasonBundle: SeasonBundle) {
    database.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)
      val watchedEpisodes = database.episodesDao().getAllForSeason(season.ids.trakt.id).filter { it.isWatched }
      val toSet = watchedEpisodes.map { it.copy(isWatched = false) }

      val isShowFollowed = showsRepository.myShows.load(show.ids.trakt) != null

      when {
        isShowFollowed -> {
          database.episodesDao().upsert(toSet)
          database.seasonsDao().update(listOf(dbSeason))
        }
        else -> {
          database.episodesDao().delete(toSet)
          database.seasonsDao().delete(listOf(dbSeason))
        }
      }
    }
  }

  suspend fun setEpisodeWatched(episodeBundle: EpisodeBundle) {
    database.withTransaction {
      val (episode, season, show) = episodeBundle

      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)
      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)

      val localSeason = database.seasonsDao().getById(season.ids.trakt.id)
      if (localSeason == null) {
        database.seasonsDao().upsert(listOf(dbSeason))
      }
      database.episodesDao().upsert(listOf(dbEpisode))
      onEpisodeSet(season, show)
    }
  }

  suspend fun setEpisodeUnwatched(episodeBundle: EpisodeBundle) {
    database.withTransaction {
      val (episode, season, show) = episodeBundle

      val isShowFollowed = showsRepository.myShows.load(show.ids.trakt) != null
      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)

      when {
        isShowFollowed -> database.episodesDao().upsert(listOf(dbEpisode.copy(isWatched = false)))
        else -> database.episodesDao().delete(listOf(dbEpisode))
      }

      onEpisodeSet(season, show)
    }
  }

  suspend fun invalidateEpisodes(show: Show, newSeasons: List<Season>) {
    if (newSeasons.isEmpty()) return

    val localSeasons = database.seasonsDao().getAllByShowId(show.ids.trakt.id)
    val localEpisodes = database.episodesDao().getAllForShows(listOf(show.ids.trakt.id))

    val seasonsToAdd = mutableListOf<SeasonDb>()
    val episodesToAdd = mutableListOf<EpisodeDb>()

    newSeasons.forEach { newSeason ->
      val localSeason = localSeasons.find { it.idTrakt == newSeason.ids.trakt.id }
      val seasonDb = mappers.season.toDatabase(
        newSeason,
        show.ids.trakt,
        localSeason?.isWatched ?: false
      )
      seasonsToAdd.add(seasonDb)

      newSeason.episodes.forEach { newEpisode ->
        val localEpisode = localEpisodes.find { it.idTrakt == newEpisode.ids.trakt.id }
        val episodeDb = mappers.episode.toDatabase(
          newEpisode,
          newSeason,
          show.ids.trakt,
          localEpisode?.isWatched ?: false
        )
        episodesToAdd.add(episodeDb)
      }
    }

    if (seasonsToAdd.isNotEmpty()) database.seasonsDao().upsert(seasonsToAdd)
    if (episodesToAdd.isNotEmpty()) database.episodesDao().upsert(episodesToAdd)

    database.episodesSyncLogDao().upsert(EpisodesSyncLog(show.ids.trakt.id, nowUtcMillis()))
  }

  private suspend fun onEpisodeSet(season: Season, show: Show) {
    val localEpisodes = database.episodesDao().getAllForSeason(season.ids.trakt.id)
    val isWatched = localEpisodes.count { it.isWatched } == season.episodeCount
    val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, isWatched)
    database.seasonsDao().update(listOf(dbSeason))
  }
}