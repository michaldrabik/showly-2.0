package com.michaldrabik.ui_movie.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsHiddenCase @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun isHidden(movie: Movie) =
    moviesRepository.hiddenMovies.exists(movie.ids.trakt)

  suspend fun addToHidden(movie: Movie) {
    database.runTransaction {
      with(moviesRepository) {
        hiddenMovies.insert(movie.ids.trakt)
        myMovies.delete(movie.ids.trakt)
        watchlistMovies.delete(movie.ids.trakt)
      }
    }
    pinnedItemsRepository.removePinnedItem(movie)
  }

  suspend fun removeFromHidden(movie: Movie) =
    moviesRepository.hiddenMovies.delete(movie.ids.trakt)
}
