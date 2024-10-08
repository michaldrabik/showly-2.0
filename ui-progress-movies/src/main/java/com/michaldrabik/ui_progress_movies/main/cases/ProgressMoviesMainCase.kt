package com.michaldrabik.ui_progress_movies.main.cases

import com.michaldrabik.repository.PinnedItemsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Ids
import com.michaldrabik.ui_model.Movie
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressMoviesMainCase @Inject constructor(
  private val moviesRepository: MoviesRepository,
  private val pinnedItemsRepository: PinnedItemsRepository,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun addToMyMovies(
    movie: Movie,
    customDate: ZonedDateTime?,
  ) {
    moviesRepository.myMovies.insert(movie.ids.trakt, customDate)
    pinnedItemsRepository.removePinnedItem(movie)
    quickSyncManager.scheduleMovies(listOf(movie.traktId), customDate)
  }

  suspend fun addToMyMovies(movieId: IdTrakt) {
    addToMyMovies(
      movie = Movie.EMPTY.copy(Ids.EMPTY.copy(trakt = movieId)),
      customDate = null,
    )
  }
}
