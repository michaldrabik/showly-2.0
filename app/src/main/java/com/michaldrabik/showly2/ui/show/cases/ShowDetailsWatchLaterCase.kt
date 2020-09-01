package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsWatchLaterCase @Inject constructor(
  private val cloud: Cloud,
  private val userManager: UserTraktManager,
  private val showsRepository: ShowsRepository
) {

  suspend fun isSeeLater(show: Show) =
    showsRepository.seeLaterShows.load(show.ids.trakt) != null

  suspend fun addToWatchLater(show: Show) =
    showsRepository.seeLaterShows.insert(show.ids.trakt)

  suspend fun removeFromWatchLater(show: Show) =
    showsRepository.seeLaterShows.delete(show.ids.trakt)

  suspend fun removeTraktSeeLater(show: Show) {
    val token = userManager.checkAuthorization()
    val request = SyncExportRequest(listOf(SyncExportItem.create(show.traktId)))
    cloud.traktApi.postDeleteWatchlist(token.token, request)
  }
}
