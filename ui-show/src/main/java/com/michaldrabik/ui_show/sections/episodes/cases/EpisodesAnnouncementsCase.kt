package com.michaldrabik.ui_show.sections.episodes.cases

import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodesAnnouncementsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements(idTrakt: IdTrakt) {
    val isMyShow = showsRepository.myShows.exists(idTrakt)
    if (isMyShow) {
      announcementManager.refreshShowsAnnouncements()
    }
  }
}
