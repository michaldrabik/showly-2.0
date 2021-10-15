package com.michaldrabik.ui_movie.cases

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsArchiveCase @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun isArchived(movie: Movie) =
    moviesRepository.hiddenMovies.isHidden(movie.ids.trakt)

  suspend fun addToArchive(movie: Movie) {
    database.withTransaction {
      moviesRepository.hiddenMovies.insert(movie.ids.trakt)
    }
    pinnedItemsRepository.removePinnedItem(movie)
  }

  suspend fun removeFromArchive(movie: Movie) =
    moviesRepository.hiddenMovies.delete(movie.ids.trakt)
}
