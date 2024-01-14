package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodesAnnouncementsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val showsRepository: ShowsRepository,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements(idTrakt: IdTrakt) = withContext(dispatchers.IO) {
    val isMyShow = showsRepository.myShows.exists(idTrakt)
    if (isMyShow) {
      announcementManager.refreshShowsAnnouncements()
    }
  }
}
