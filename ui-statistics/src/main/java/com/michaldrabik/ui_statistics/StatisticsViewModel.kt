package com.michaldrabik.ui_statistics

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.shows.ShowsRepository
import com.michaldrabik.ui_statistics.cases.StatisticsLoadRatingsCase
import com.michaldrabik.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
  private val ratingsCase: StatisticsLoadRatingsCase,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val database: AppDatabase,
  private val mappers: Mappers
) : BaseViewModel<StatisticsUiModel>() {

  private var takeLimit = 5

  fun loadMostWatchedShows(limit: Int = 0) {
    takeLimit += limit
    viewModelScope.launch {
      val includeArchive = settingsRepository.load().archiveShowsIncludeStatistics
      uiState = StatisticsUiModel(archivedShowsIncluded = includeArchive)

      val language = settingsRepository.getLanguage()
      val myShows = showsRepository.myShows.loadAll()
      val archiveShows = if (includeArchive) showsRepository.archiveShows.loadAll() else emptyList()

      val allShows = (myShows + archiveShows).distinctBy { it.traktId }
      val allShowsIds = allShows.map { it.traktId }

      val episodes = batchEpisodes(allShowsIds)
      val seasons = batchSeasons(allShowsIds)

      val genres = extractTopGenres(allShows)
      val mostWatchedShows = allShows
        .map { show ->
          val translation = loadTranslation(language, show)
          StatisticsMostWatchedItem(
            show = allShows.first { it.traktId == show.traktId },
            seasonsCount = seasons.filter { it.idShowTrakt == show.traktId }.count().toLong(),
            episodes = episodes
              .filter { it.idShowTrakt == show.traktId }
              .map { mappers.episode.fromDatabase(it) },
            image = Image.createUnknown(POSTER),
            isArchived = archiveShows.any { it.traktId == show.traktId },
            translation = translation
          )
        }
        .sortedByDescending { item -> item.episodes.sumBy { it.runtime } }
        .take(takeLimit)
        .map {
          it.copy(image = imagesProvider.findCachedImage(it.show, POSTER))
        }

      delay(150) // Let transition finish peacefully.
      uiState = StatisticsUiModel(
        mostWatchedShows = mostWatchedShows,
        mostWatchedTotalCount = allShowsIds.size,
        totalTimeSpentMinutes = episodes.sumBy { it.runtime }.toLong(),
        totalWatchedEpisodes = episodes.count().toLong(),
        totalWatchedEpisodesShows = episodes.distinctBy { it.idShowTrakt }.count().toLong(),
        topGenres = genres,
      )
    }
  }

  fun loadRatings() {
    viewModelScope.launch {
      uiState = try {
        val ratings = ratingsCase.loadRatings()
        StatisticsUiModel(ratings = ratings)
      } catch (t: Throwable) {
        StatisticsUiModel(ratings = emptyList())
      }
    }
  }

  private suspend fun batchEpisodes(
    showsIds: List<Long>,
    allEpisodes: MutableList<Episode> = mutableListOf()
  ): List<Episode> {
    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allEpisodes

    val episodes = database.episodesDao().getAllWatchedForShows(batch)
    allEpisodes.addAll(episodes)

    return batchEpisodes(showsIds.filter { it !in batch }, allEpisodes)
  }

  private suspend fun batchSeasons(
    showsIds: List<Long>,
    allSeasons: MutableList<Season> = mutableListOf()
  ): List<Season> {
    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allSeasons

    val seasons = database.seasonsDao().getAllWatchedForShows(batch)
    allSeasons.addAll(seasons)

    return batchSeasons(showsIds.filter { it !in batch }, allSeasons)
  }

  private fun extractTopGenres(shows: List<Show>) =
    shows
      .flatMap { it.genres }
      .asSequence()
      .filter { it.isNotBlank() }
      .distinct()
      .map { genre -> Pair(Genre.fromSlug(genre), shows.count { genre in it.genres }) }
      .sortedByDescending { it.second }
      .map { it.first }
      .toList()
      .filterNotNull()

  private suspend fun loadTranslation(language: String, show: Show) =
    if (language == Config.DEFAULT_LANGUAGE) null
    else translationsRepository.loadTranslation(show, language, true)
}
