package com.michaldrabik.ui_search.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.shows.ShowsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SearchMainCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
) {

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun loadWatchlistShowsIds() = showsRepository.watchlistShows.loadAllIds()

  suspend fun loadMyMoviesIds() = moviesRepository.myMovies.loadAllIds()

  suspend fun loadWatchlistMoviesIds() = moviesRepository.watchlistMovies.loadAllIds()
}
