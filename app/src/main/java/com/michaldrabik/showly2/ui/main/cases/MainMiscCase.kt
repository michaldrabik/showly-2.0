package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.utilities.Mode
import com.michaldrabik.ui_base.utilities.Mode.MOVIES
import com.michaldrabik.ui_base.utilities.Mode.SHOWS
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import javax.inject.Inject

@AppScope
class MainMiscCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val announcementManager: AnnouncementManager
) {

  suspend fun refreshAnnouncements(context: Context) = announcementManager.refreshEpisodesAnnouncements(context)

  fun clear() = ratingsRepository.clear()

  suspend fun setMode(mode: Mode) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(moviesActive = mode == MOVIES))
  }

  suspend fun getMode(): Mode {
    val isMovies = settingsRepository.load().moviesActive
    return if (isMovies) MOVIES else SHOWS
  }
}
