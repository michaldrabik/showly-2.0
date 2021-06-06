package com.michaldrabik.ui_show.cases

import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchlistCase @Inject constructor(
  private val cloud: Cloud,
  private val userManager: UserTraktManager,
  private val showsRepository: ShowsRepository
) {

  suspend fun isWatchlist(show: Show) =
    showsRepository.watchlistShows.load(show.ids.trakt) != null

  suspend fun addToWatchlist(show: Show) =
    showsRepository.watchlistShows.insert(show.ids.trakt)

  suspend fun removeFromWatchlist(show: Show) =
    showsRepository.watchlistShows.delete(show.ids.trakt)

  suspend fun removeTraktWatchlist(show: Show) {
    val token = userManager.checkAuthorization()
    val request = SyncExportRequest(listOf(SyncExportItem.create(show.traktId)))
    cloud.traktApi.postDeleteWatchlist(token.token, request)
  }
}
