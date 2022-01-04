package com.michaldrabik.ui_base.common.sheets.remove_trakt.remove_trakt_progress.cases

import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.episodes.EpisodesManager
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class RemoveTraktProgressCase @Inject constructor(
  private val cloud: Cloud,
  private val userManager: UserTraktManager,
  private val episodesManager: EpisodesManager
) {

  suspend fun removeTraktProgress(traktIds: List<IdTrakt>, mode: Mode) {
    val token = userManager.checkAuthorization()
    val items = traktIds.map { SyncExportItem.create(it.id) }

    val request = when (mode) {
      Mode.SHOW -> SyncExportRequest(shows = items)
      Mode.MOVIE -> SyncExportRequest(movies = items)
      Mode.EPISODE -> SyncExportRequest(episodes = items)
    }

    cloud.traktApi.postDeleteProgress(token.token, request)
    if (mode == Mode.SHOW && traktIds.isNotEmpty()) {
      episodesManager.setAllUnwatched(traktIds.first())
    }
  }
}
