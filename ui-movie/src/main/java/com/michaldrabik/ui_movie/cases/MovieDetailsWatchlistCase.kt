package com.michaldrabik.ui_movie.cases

import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsWatchlistCase @Inject constructor(
  private val cloud: Cloud,
  private val userManager: UserTraktManager,
  private val moviesRepository: MoviesRepository
) {

  suspend fun isWatchlist(movie: Movie) =
    moviesRepository.watchlistMovies.load(movie.ids.trakt) != null

  suspend fun addToWatchlist(movie: Movie) =
    moviesRepository.watchlistMovies.insert(movie.ids.trakt)

  suspend fun removeFromWatchlist(movie: Movie) =
    moviesRepository.watchlistMovies.delete(movie.ids.trakt)

  suspend fun removeTraktWatchlist(movie: Movie) {
    val token = userManager.checkAuthorization()
    val request = SyncExportRequest(movies = listOf(SyncExportItem.create(movie.traktId)))
    cloud.traktApi.postDeleteWatchlist(token.token, request)
  }
}
