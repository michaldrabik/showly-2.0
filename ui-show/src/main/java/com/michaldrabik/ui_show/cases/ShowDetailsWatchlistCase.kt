package com.michaldrabik.ui_show.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.utilities.extensions.runTransaction
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchlistCase @Inject constructor(
  private val database: AppDatabase,
  private val showsRepository: ShowsRepository
) {

  suspend fun isWatchlist(show: Show) =
    showsRepository.watchlistShows.load(show.ids.trakt) != null

  suspend fun addToWatchlist(show: Show) {
    database.runTransaction {
      with(showsRepository) {
        watchlistShows.insert(show.ids.trakt)
        myShows.delete(show.ids.trakt)
        hiddenShows.delete(show.ids.trakt)
      }
    }
  }

  suspend fun removeFromWatchlist(show: Show) =
    showsRepository.watchlistShows.delete(show.ids.trakt)
}
