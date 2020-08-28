package com.michaldrabik.showly2.ui.followedshows.statistics

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.statistics.cases.StatisticsLoadRatingsCase
import com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Episode
import com.michaldrabik.storage.database.model.Season
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
  private val ratingsCase: StatisticsLoadRatingsCase,
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val database: AppDatabase,
  private val mappers: Mappers
) : BaseViewModel<StatisticsUiModel>() {

  private var takeLimit = 5

  fun loadMostWatchedShows(limit: Int = 0) {
    takeLimit += limit
    viewModelScope.launch {
      val myShows = showsRepository.myShows.loadAll()
      val myShowsIds = myShows.map { it.traktId }
      val episodes = batchEpisodes(myShowsIds)
      val seasons = batchSeasons(myShowsIds)

      val genres = extractTopGenres(myShows)
      val mostWatchedShows = myShowsIds
        .map { showId ->
          StatisticsMostWatchedItem(
            show = myShows.first { it.traktId == showId },
            seasonsCount = seasons.filter { it.idShowTrakt == showId }.count().toLong(),
            episodes = episodes
              .filter { it.idShowTrakt == showId }
              .map { mappers.episode.fromDatabase(it) },
            image = Image.createUnknown(POSTER)
          )
        }
        .sortedByDescending { item -> item.episodes.sumBy { it.runtime } }
        .take(takeLimit)
        .map {
          it.copy(image = imagesProvider.findCachedImage(it.show, POSTER))
        }

      uiState = StatisticsUiModel(
        mostWatchedShows = mostWatchedShows,
        mostWatchedTotalCount = myShowsIds.size,
        totalTimeSpentMinutes = episodes.sumBy { it.runtime }.toLong(),
        totalWatchedEpisodes = episodes.count().toLong(),
        totalWatchedEpisodesShows = episodes.distinctBy { it.idShowTrakt }.count().toLong(),
        topGenres = genres
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

  private fun extractTopGenres(myShows: List<Show>) =
    myShows
      .flatMap { it.genres }
      .asSequence()
      .distinct()
      .map { genre -> Pair(genre, myShows.count { genre in it.genres }) }
      .sortedByDescending { it.second }
      .map { it.first }
      .toList()
}
