package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.RatingsRepository
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.episodes.EpisodesManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.episodes.EpisodeListItem
import com.michaldrabik.ui_show.helpers.SeasonsBundle
import com.michaldrabik.ui_show.seasons.SeasonListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsEpisodesCase @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val ratingsRepository: RatingsRepository,
  private val translationsRepository: TranslationsRepository,
  private val episodesManager: EpisodesManager,
  private val dateFormatProvider: DateFormatProvider
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(traktId.id) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadSeasons(show: Show, isOnline: Boolean): SeasonsBundle = coroutineScope {
    val showSpecialSeasons = settingsRepository.load().specialSeasonsEnabled
    try {
      if (!isOnline) loadLocalSeasons(show, showSpecialSeasons)

      val remoteSeasons = cloud.traktApi.fetchSeasons(show.traktId)
        .map { mappers.season.fromNetwork(it) }
        .filter { it.episodes.isNotEmpty() }
        .filter { if (!showSpecialSeasons) !it.isSpecial() else true }

      val isFollowed = showsRepository.myShows.load(show.ids.trakt) != null
      if (isFollowed) {
        episodesManager.invalidateSeasons(show, remoteSeasons)
      }

      val seasonsItems = mapToSeasonItems(remoteSeasons, show)
      SeasonsBundle(seasonsItems, isLocal = false)
    } catch (error: Throwable) {
      loadLocalSeasons(show, showSpecialSeasons)
    }
  }

  private suspend fun loadLocalSeasons(show: Show, showSpecials: Boolean): SeasonsBundle {
    val localEpisodes = database.episodesDao().getAllByShowId(show.traktId)
    val localSeasons = database.seasonsDao().getAllByShowId(show.traktId).map { season ->
      val seasonEpisodes = localEpisodes.filter { ep -> ep.idSeason == season.idTrakt }
      mappers.season.fromDatabase(season, seasonEpisodes)
    }
      .filter { it.episodes.isNotEmpty() }
      .filter { if (!showSpecials) !it.isSpecial() else true }

    val seasonsItems = mapToSeasonItems(localSeasons, show)
    return SeasonsBundle(seasonsItems, isLocal = true)
  }

  private suspend fun mapToSeasonItems(remoteSeasons: List<Season>, show: Show) = coroutineScope {
    val format = dateFormatProvider.loadFullHourFormat()
    val seasonsRatings = ratingsRepository.shows.loadRatingsSeasons(remoteSeasons)
    remoteSeasons
      .map {
        val userRating = seasonsRatings.find { rating -> rating.idTrakt == it.ids.trakt }
        val episodes = it.episodes.map { episode ->
          async {
            val rating = ratingsRepository.shows.loadRating(episode)
            val translation = translationsRepository.loadTranslation(episode, show.ids.trakt, onlyLocal = true)
            EpisodeListItem(episode, it, false, translation, rating, format)
          }
        }.awaitAll()
        SeasonListItem(show, it, episodes, isWatched = false, userRating = userRating, updatedAt = nowUtcMillis())
      }
      .sortedByDescending { it.season.number }
  }
}
