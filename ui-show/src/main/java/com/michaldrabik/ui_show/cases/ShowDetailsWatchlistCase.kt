package com.michaldrabik.ui_show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
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
