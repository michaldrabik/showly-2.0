package com.michaldrabik.ui_movie.cases

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMyMoviesCase @Inject constructor(
  private val database: AppDatabase,
  private val cloud: Cloud,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val userManager: UserTraktManager
) {

  suspend fun isMyMovie(movie: Movie) =
    moviesRepository.myMovies.load(movie.ids.trakt) != null

  suspend fun addToMyMovies(movie: Movie) {
    database.withTransaction {
      moviesRepository.myMovies.insert(movie.ids.trakt)
      moviesRepository.watchlistMovies.delete(movie.ids.trakt)
    }
  }

  suspend fun removeFromMyMovies(movie: Movie) {
    database.withTransaction {
      moviesRepository.myMovies.delete(movie.ids.trakt)
      pinnedItemsRepository.removePinnedItem(movie)
    }
  }
}
