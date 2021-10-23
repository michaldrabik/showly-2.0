package com.michaldrabik.ui_statistics

import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Config
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Episode
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.utilities.extensions.combine
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_statistics.cases.StatisticsLoadRatingsCase
import com.michaldrabik.ui_statistics.views.mostWatched.StatisticsMostWatchedItem
import com.michaldrabik.ui_statistics.views.ratings.recycler.StatisticsRatingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
  private val ratingsCase: StatisticsLoadRatingsCase,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val imagesProvider: ShowImagesProvider,
  private val database: AppDatabase,
  private val mappers: Mappers,
) : BaseViewModel() {

  private val mostWatchedShowsState = MutableStateFlow<List<StatisticsMostWatchedItem>?>(null)
  private val mostWatchedTotalCountState = MutableStateFlow<Int?>(null)
  private val totalTimeSpentMinutesState = MutableStateFlow<Int?>(null)
  private val totalWatchedEpisodesState = MutableStateFlow<Int?>(null)
  private val totalWatchedEpisodesShowsState = MutableStateFlow<Int?>(null)
  private val topGenresState = MutableStateFlow<List<Genre>?>(null)
  private val ratingsState = MutableStateFlow<List<StatisticsRatingItem>?>(null)

  val uiState = combine(
    mostWatchedShowsState,
    mostWatchedTotalCountState,
    totalTimeSpentMinutesState,
    totalWatchedEpisodesState,
    totalWatchedEpisodesShowsState,
    topGenresState,
    ratingsState
  ) { s1, s2, s3, s4, s5, s6, s7 ->
    StatisticsUiState(
      mostWatchedShows = s1,
      mostWatchedTotalCount = s2,
      totalTimeSpentMinutes = s3,
      totalWatchedEpisodes = s4,
      totalWatchedEpisodesShows = s5,
      topGenres = s6,
      ratings = s7
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = StatisticsUiState()
  )

  private var takeLimit = 5

  fun loadMostWatchedShows(limit: Int = 0) {
    takeLimit += limit
    viewModelScope.launch {
      val language = translationsRepository.getLanguage()
      val myShows = showsRepository.myShows.loadAll()
      val myShowsIds = myShows.map { it.traktId }

      val episodes = batchEpisodes(myShowsIds)
      val seasons = batchSeasons(myShowsIds)

      val genres = extractTopGenres(myShows)
      val mostWatchedShows = myShows
        .map { show ->
          val translation = loadTranslation(language, show)
          StatisticsMostWatchedItem(
            show = myShows.first { it.traktId == show.traktId },
            seasonsCount = seasons.filter { it.idShowTrakt == show.traktId }.count().toLong(),
            episodes = episodes
              .filter { it.idShowTrakt == show.traktId }
              .map { mappers.episode.fromDatabase(it) },
            image = Image.createUnknown(POSTER),
            translation = translation
          )
        }
        .sortedByDescending { item -> item.episodes.sumBy { it.runtime } }
        .take(takeLimit)
        .map {
          it.copy(image = imagesProvider.findCachedImage(it.show, POSTER))
        }

      delay(150) // Let transition finish peacefully.

      mostWatchedShowsState.value = mostWatchedShows
      mostWatchedTotalCountState.value = myShowsIds.size
      totalTimeSpentMinutesState.value = episodes.sumOf { it.runtime }
      totalWatchedEpisodesState.value = episodes.count()
      totalWatchedEpisodesShowsState.value = episodes.distinctBy { it.idShowTrakt }.count()
      topGenresState.value = genres
    }
  }

  fun loadRatings() {
    viewModelScope.launch {
      try {
        ratingsState.value = ratingsCase.loadRatings()
      } catch (t: Throwable) {
        ratingsState.value = emptyList()
      }
    }
  }

  private suspend fun batchEpisodes(
    showsIds: List<Long>,
    allEpisodes: MutableList<Episode> = mutableListOf(),
  ): List<Episode> {
    val batch = showsIds.take(500)
    if (batch.isEmpty()) return allEpisodes

    val episodes = database.episodesDao().getAllWatchedForShows(batch)
    allEpisodes.addAll(episodes)

    return batchEpisodes(showsIds.filter { it !in batch }, allEpisodes)
  }

  private suspend fun batchSeasons(
    showsIds: List<Long>,
    allSeasons: MutableList<Season> = mutableListOf(),
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
