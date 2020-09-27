package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_repository.RatingsRepository
import javax.inject.Inject

@AppScope
class MainMiscCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val announcementManager: AnnouncementManager
) {

  suspend fun refreshAnnouncements(context: Context) = announcementManager.refreshEpisodesAnnouncements(context)

  fun clear() = ratingsRepository.clear()
}
