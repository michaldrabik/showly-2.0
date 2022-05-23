package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_hidden.cases

import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class RemoveTraktHiddenCase @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val userManager: UserTraktManager,
) {

  suspend fun removeTraktHidden(traktIds: List<IdTrakt>, mode: Mode) {
    userManager.checkAuthorization()
    val items = traktIds.map { SyncExportItem.create(it.id) }

    when (mode) {
      Mode.SHOW -> {
        val request = SyncExportRequest(shows = items)
        remoteSource.trakt.deleteHiddenShow(request)
      }
      Mode.MOVIE -> {
        val request = SyncExportRequest(movies = items)
        remoteSource.trakt.deleteHiddenMovie(request)
      }
      else -> throw IllegalStateException()
    }
  }
}
