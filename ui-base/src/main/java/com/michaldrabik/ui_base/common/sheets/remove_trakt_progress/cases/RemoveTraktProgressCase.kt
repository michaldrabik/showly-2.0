package com.michaldrabik.ui_base.common.sheets.remove_trakt_progress.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.trakt.model.SyncExportItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.repository.UserTraktManager
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

  suspend fun removeTraktProgress(traktId: IdTrakt, mode: Mode) {
    val token = userManager.checkAuthorization()
    val item = SyncExportItem.create(traktId.id)

    val request = when (mode) {
      Mode.SHOWS -> SyncExportRequest(shows = listOf(item))
      Mode.MOVIES -> SyncExportRequest(movies = listOf(item))
    }

    cloud.traktApi.postDeleteProgress(token.token, request)
    if (mode == Mode.SHOWS) {
      episodesManager.setAllUnwatched(traktId)
    }
  }
}
