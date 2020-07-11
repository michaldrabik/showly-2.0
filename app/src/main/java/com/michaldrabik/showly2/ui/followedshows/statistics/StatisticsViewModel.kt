package com.michaldrabik.showly2.ui.followedshows.statistics

import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.followedshows.statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.storage.database.AppDatabase
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val database: AppDatabase,
  private val mappers: Mappers
) : BaseViewModel<StatisticsUiModel>() {

  fun loadMostWatchedShows() {
    viewModelScope.launch {
      val myShows = showsRepository.myShows.loadAll()
      val myShowsIds = myShows.map { it.traktId }
      val episodes = database.episodesDao().getAllWatchedForShows(myShowsIds)
      val seasons = database.seasonsDao().getAllWatchedForShows(myShowsIds)

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
        .take(3)
        .map {
          it.copy(image = imagesProvider.findCachedImage(it.show, POSTER))
        }

      uiState = StatisticsUiModel(mostWatchedShows = mostWatchedShows)
    }
  }
}
