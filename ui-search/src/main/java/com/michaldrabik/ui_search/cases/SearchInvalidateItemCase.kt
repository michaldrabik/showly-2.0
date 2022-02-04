package com.michaldrabik.ui_search.cases

import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_search.recycler.SearchListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class SearchInvalidateItemCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
) {

  suspend fun checkFollowedState(item: SearchListItem) = coroutineScope {
    when {
      item.isShow -> {
        val (isMy, isWatchlist) = awaitAll(
          async { showsRepository.myShows.exists(item.show.ids.trakt) },
          async { showsRepository.watchlistShows.exists(item.show.ids.trakt) },
        )
        Pair(isMy, isWatchlist)
      }
      item.isMovie -> {
        val (isMy, isWatchlist) = awaitAll(
          async { moviesRepository.myMovies.exists(item.movie.ids.trakt) },
          async { moviesRepository.watchlistMovies.exists(item.movie.ids.trakt) },
        )
        Pair(isMy, isWatchlist)
      }
      else -> throw IllegalStateException()
    }
  }
}
