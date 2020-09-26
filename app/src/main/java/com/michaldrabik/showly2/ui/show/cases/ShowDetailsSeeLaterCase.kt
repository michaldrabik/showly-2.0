package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.SyncExportItem
import com.michaldrabik.network.trakt.model.SyncExportRequest
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class ShowDetailsSeeLaterCase @Inject constructor(
  private val cloud: Cloud,
  private val userManager: UserTraktManager,
  private val showsRepository: ShowsRepository
) {

  suspend fun isSeeLater(show: Show) =
    showsRepository.seeLaterShows.load(show.ids.trakt) != null

  suspend fun addToSeeLater(show: Show) =
    showsRepository.seeLaterShows.insert(show.ids.trakt)

  suspend fun removeFromSeeLater(show: Show) =
    showsRepository.seeLaterShows.delete(show.ids.trakt)

  suspend fun removeTraktSeeLater(show: Show) {
    val token = userManager.checkAuthorization()
    val request = SyncExportRequest(listOf(SyncExportItem.create(show.traktId)))
    cloud.traktApi.postDeleteWatchlist(token.token, request)
  }
}
