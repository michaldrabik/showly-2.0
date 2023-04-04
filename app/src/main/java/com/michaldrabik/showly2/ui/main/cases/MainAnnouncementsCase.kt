package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class MainAnnouncementsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements() {
    withContext(dispatchers.IO) {
      announcementManager.refreshShowsAnnouncements()
      announcementManager.refreshMoviesAnnouncements()
    }
  }
}
