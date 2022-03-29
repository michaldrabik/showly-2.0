package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.ui_base.notifications.AnnouncementManager
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainAnnouncementsCase @Inject constructor(
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements() {
    announcementManager.refreshShowsAnnouncements()
    announcementManager.refreshMoviesAnnouncements()
  }
}
