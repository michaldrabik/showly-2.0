package com.michaldrabik.ui_base.episodes

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.EpisodesSyncLog
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.michaldrabik.data_local.database.model.Episode as EpisodeDb
import com.michaldrabik.data_local.database.model.Season as SeasonDb

@Singleton
class EpisodesManager @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
) {

  suspend fun getWatchedSeasonsIds(show: Show) =
    localSource.seasons.getAllWatchedIdsForShows(listOf(show.traktId))

  suspend fun getWatchedEpisodesIds(show: Show) =
    localSource.episodes.getAllWatchedIdsForShows(listOf(show.traktId))

  suspend fun setSeasonWatched(seasonBundle: SeasonBundle): List<Episode> {
    val toAdd = mutableListOf<EpisodeDb>()
    transactions.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, true)
      val localSeason = localSource.seasons.getById(season.ids.trakt.id)
      if (localSeason == null) {
        localSource.seasons.upsert(listOf(dbSeason))
      }

      val episodes = localSource.episodes.getAllForSeason(season.ids.trakt.id).filter { it.isWatched }

      season.episodes.forEach { ep ->
        if (episodes.none { it.idTrakt == ep.ids.trakt.id }) {
          val dbEpisode = mappers.episode.toDatabase(ep, season, show.ids.trakt, true)
          toAdd.add(dbEpisode)
        }
      }

      localSource.episodes.upsert(toAdd)
      localSource.seasons.update(listOf(dbSeason))
      localSource.myShows.updateTimestamp(show.traktId, nowUtcMillis())
    }
    return toAdd.map { mappers.episode.fromDatabase(it) }
  }

  suspend fun setSeasonUnwatched(seasonBundle: SeasonBundle) {
    transactions.withTransaction {
      val (season, show) = seasonBundle

      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)
      val watchedEpisodes = localSource.episodes.getAllForSeason(season.ids.trakt.id).filter { it.isWatched }
      val toSet = watchedEpisodes.map { it.copy(isWatched = false) }

      val isShowFollowed = showsRepository.myShows.load(show.ids.trakt) != null

      when {
        isShowFollowed -> {
          localSource.episodes.upsert(toSet)
          localSource.seasons.update(listOf(dbSeason))
        }
        else -> {
          localSource.episodes.delete(toSet)
          localSource.seasons.delete(listOf(dbSeason))
        }
      }
    }
  }

  suspend fun setEpisodeWatched(episodeId: Long, seasonId: Long, showId: IdTrakt) {
    val episodeDb = localSource.episodes.getAllForSeason(seasonId).find { it.idTrakt == episodeId }!!
    val seasonDb = localSource.seasons.getById(seasonId)!!
    val show = showsRepository.myShows.load(showId)!!
    setEpisodeWatched(
      EpisodeBundle(
        mappers.episode.fromDatabase(episodeDb),
        mappers.season.fromDatabase(seasonDb),
        show
      )
    )
  }

  suspend fun setEpisodeWatched(episodeBundle: EpisodeBundle) {
    transactions.withTransaction {
      val (episode, season, show) = episodeBundle

      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)
      val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, false)

      val localSeason = localSource.seasons.getById(season.ids.trakt.id)
      if (localSeason == null) {
        localSource.seasons.upsert(listOf(dbSeason))
      }
      localSource.episodes.upsert(listOf(dbEpisode))
      localSource.myShows.updateTimestamp(show.traktId, nowUtcMillis())
      onEpisodeSet(season, show)
    }
  }

  suspend fun setEpisodeUnwatched(episodeBundle: EpisodeBundle) {
    transactions.withTransaction {
      val (episode, season, show) = episodeBundle

      val isShowFollowed = showsRepository.myShows.load(show.ids.trakt) != null
      val dbEpisode = mappers.episode.toDatabase(episode, season, show.ids.trakt, true)

      when {
        isShowFollowed -> localSource.episodes.upsert(listOf(dbEpisode.copy(isWatched = false)))
        else -> localSource.episodes.delete(listOf(dbEpisode))
      }

      onEpisodeSet(season, show)
    }
  }

  suspend fun setAllUnwatched(showId: IdTrakt, skipSpecials: Boolean = false) {
    transactions.withTransaction {
      val watchedEpisodes = localSource.episodes.getAllByShowId(showId.id)
      val watchedSeasons = localSource.seasons.getAllByShowId(showId.id)

      val updateEpisodes = watchedEpisodes
        .filter { if (skipSpecials) it.seasonNumber > 0 else true }
        .map { it.copy(isWatched = false) }
      val updateSeasons = watchedSeasons
        .filter { if (skipSpecials) it.seasonNumber > 0 else true }
        .map { it.copy(isWatched = false) }

      localSource.episodes.upsert(updateEpisodes)
      localSource.seasons.update(updateSeasons)
    }
  }

  @Suppress("UNCHECKED_CAST")
  suspend fun invalidateSeasons(show: Show, newSeasons: List<Season>) {
    if (newSeasons.isEmpty()) {
      return
    }
    coroutineScope {
      val (localSeasons, localEpisodes) = awaitAll(
        async { localSource.seasons.getAllByShowId(show.traktId) },
        async { localSource.episodes.getAllByShowId(show.traktId) }
      )
      localSeasons as List<SeasonDb>
      localEpisodes as List<EpisodeDb>

      val seasonsToAdd = mutableListOf<SeasonDb>()
      val episodesToAdd = mutableListOf<EpisodeDb>()

      newSeasons.forEach { season ->
        var isAnyEpisodeUnwatched = false

        season.episodes.forEach { newEpisode ->
          val localEpisode = localEpisodes.find { it.episodeNumber == newEpisode.number && it.seasonNumber == newEpisode.season }

          val isWatched = localEpisode?.isWatched ?: false
          if (!isWatched) isAnyEpisodeUnwatched = true

          val episodeDb = mappers.episode.toDatabase(
            episode = newEpisode,
            season = season,
            showId = show.ids.trakt,
            isWatched = isWatched
          )
          episodesToAdd.add(episodeDb)
        }

        val seasonDb = mappers.season.toDatabase(
          season = season,
          showId = show.ids.trakt,
          isWatched = !isAnyEpisodeUnwatched
        )
        seasonsToAdd.add(seasonDb)
      }

      transactions.withTransaction {
        localSource.episodes.deleteAllForShow(show.traktId)
        localSource.seasons.deleteAllForShow(show.traktId)

        localSource.seasons.upsert(seasonsToAdd)
        localSource.episodes.upsertChunked(episodesToAdd)

        localSource.episodesSyncLog.upsert(EpisodesSyncLog(show.traktId, nowUtcMillis()))
      }

      Timber.d("Episodes updated: ${episodesToAdd.size} Seasons updated: ${seasonsToAdd.size}")
    }
  }

  private suspend fun onEpisodeSet(season: Season, show: Show) {
    val localEpisodes = localSource.episodes.getAllForSeason(season.ids.trakt.id)
    val isWatched = localEpisodes.count { it.isWatched } == season.episodeCount
    val dbSeason = mappers.season.toDatabase(season, show.ids.trakt, isWatched)
    localSource.seasons.update(listOf(dbSeason))
  }
}
