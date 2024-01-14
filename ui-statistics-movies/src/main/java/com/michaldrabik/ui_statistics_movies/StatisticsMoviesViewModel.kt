package com.michaldrabik.ui_statistics_movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_statistics_movies.cases.StatisticsMoviesLoadRatingsCase
import com.michaldrabik.ui_statistics_movies.views.ratings.recycler.StatisticsMoviesRatingItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsMoviesViewModel @Inject constructor(
  private val ratingsCase: StatisticsMoviesLoadRatingsCase,
  private val moviesRepository: MoviesRepository,
) : ViewModel() {

  private val totalTimeSpentState = MutableStateFlow<Int?>(null)
  private val totalWatchedMoviesState = MutableStateFlow<Int?>(null)
  private val topGenresState = MutableStateFlow<List<Genre>?>(null)
  private val ratingsState = MutableStateFlow<List<StatisticsMoviesRatingItem>?>(null)

  fun loadData(initialDelay: Long = 150L) {
    viewModelScope.launch {
      val myMovies = moviesRepository.myMovies.loadAll().distinctBy { it.traktId }
      val genres = extractTopGenres(myMovies)

      delay(initialDelay) // Let transition finish peacefully.

      totalWatchedMoviesState.value = myMovies.count()
      totalTimeSpentState.value = myMovies.sumOf { it.runtime }
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

  private fun extractTopGenres(movies: List<Movie>) =
    movies
      .flatMap { it.genres }
      .asSequence()
      .filter { it.isNotBlank() }
      .distinct()
      .map { genre -> Pair(Genre.fromSlug(genre), movies.count { genre in it.genres }) }
      .sortedByDescending { it.second }
      .map { it.first }
      .toList()
      .filterNotNull()

  val uiState = combine(
    totalWatchedMoviesState,
    totalTimeSpentState,
    topGenresState,
    ratingsState,
  ) { s1, s2, s3, s4 ->
    StatisticsMoviesUiState(
      totalWatchedMovies = s1,
      totalTimeSpentMinutes = s2,
      topGenres = s3,
      ratings = s4,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = StatisticsMoviesUiState()
  )
}
