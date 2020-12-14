package com.michaldrabik.ui_movie.cases

import androidx.room.withTransaction
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.movies.MoviesRepository
import javax.inject.Inject

@AppScope
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

  suspend fun removeTraktHistory(movie: Movie) {
    val token = userManager.checkAuthorization()
    val request = SyncExportRequest(movies = listOf(SyncExportItem.create(movie.traktId)))
    cloud.traktApi.postDeleteProgress(token.token, request)
  }
}
