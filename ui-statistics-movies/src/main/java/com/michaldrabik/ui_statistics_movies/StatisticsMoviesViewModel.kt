package com.michaldrabik.ui_statistics_movies

import androidx.lifecycle.viewModelScope
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_statistics_movies.cases.StatisticsMoviesLoadRatingsCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsMoviesViewModel @Inject constructor(
  private val ratingsCase: StatisticsMoviesLoadRatingsCase,
  private val moviesRepository: MoviesRepository
) : BaseViewModel<StatisticsMoviesUiModel>() {

  fun loadMovies() {
    viewModelScope.launch {
      val myMovies = moviesRepository.myMovies.loadAll().distinctBy { it.traktId }
      val genres = extractTopGenres(myMovies)

      delay(150) // Let transition finish peacefully.
      uiState = StatisticsMoviesUiModel(
        totalTimeSpentMinutes = myMovies.sumBy { it.runtime }.toLong(),
        totalWatchedMovies = myMovies.count(),
        topGenres = genres
      )
    }
  }

  fun loadRatings() {
    viewModelScope.launch {
      uiState = try {
        val ratings = ratingsCase.loadRatings()
        StatisticsMoviesUiModel(ratings = ratings)
      } catch (t: Throwable) {
        StatisticsMoviesUiModel(ratings = emptyList())
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
}
