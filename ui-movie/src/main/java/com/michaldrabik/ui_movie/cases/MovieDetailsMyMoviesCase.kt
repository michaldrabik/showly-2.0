package com.michaldrabik.ui_movie.cases

import androidx.room.withTransaction
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_model.Movie
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class MovieDetailsMyMoviesCase @Inject constructor(
  private val database: AppDatabase,
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun getAllIds() = coroutineScope {
    val (myMovies, watchlistMovies) = awaitAll(
      async { moviesRepository.myMovies.loadAllIds() },
      async { moviesRepository.watchlistMovies.loadAllIds() }
    )
    Pair(myMovies, watchlistMovies)
  }

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
