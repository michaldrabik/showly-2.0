package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_watchlist.cases

import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class RemoveTraktWatchlistCase @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val userManager: UserTraktManager,
) {

  suspend fun removeTraktWatchlist(traktIds: List<IdTrakt>, mode: Mode) {
    userManager.checkAuthorization()
    val items = traktIds.map { SyncExportItem.create(it.id) }

    val request = when (mode) {
      Mode.SHOW -> SyncExportRequest(shows = items)
      Mode.MOVIE -> SyncExportRequest(movies = items)
      else -> throw IllegalStateException()
    }

    remoteSource.trakt.postDeleteWatchlist(request)
  }
}
