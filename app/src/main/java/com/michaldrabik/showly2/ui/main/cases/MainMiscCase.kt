package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.showly2.common.notifications.AnnouncementManager
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.repository.rating.RatingsRepository
import javax.inject.Inject

@AppScope
class MainMiscCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val announcementManager: AnnouncementManager
) {

  suspend fun refreshAnnouncements(context: Context) = announcementManager.refreshEpisodesAnnouncements(context)

  fun clear() = ratingsRepository.clear()
}
