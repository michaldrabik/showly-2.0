package com.michaldrabik.showly2.ui.main.cases

import android.content.Context
import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_base.images.MovieImagesProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_repository.RatingsRepository
import com.michaldrabik.ui_repository.SettingsRepository
import timber.log.Timber
import javax.inject.Inject

@AppScope
class MainMiscCase @Inject constructor(
  private val ratingsRepository: RatingsRepository,
  private val settingsRepository: SettingsRepository,
  private val showImagesProvider: ShowImagesProvider,
  private val movieImagesProvider: MovieImagesProvider,
  private val announcementManager: AnnouncementManager,
) {

  suspend fun refreshAnnouncements(context: Context) {
    announcementManager.refreshShowsAnnouncements(context)
    announcementManager.refreshMoviesAnnouncements(context)
  }

  fun clear() {
    ratingsRepository.clear()
    showImagesProvider.clear()
    movieImagesProvider.clear()
    Timber.d("Clearing...")
  }

  fun setMode(mode: Mode) {
    settingsRepository.mode = mode
  }

  fun getMode(): Mode {
    val isMoviesEnabled = settingsRepository.isMoviesEnabled
    val isMovies = settingsRepository.mode == MOVIES
    return if (isMoviesEnabled && isMovies) MOVIES else SHOWS
  }

  fun moviesEnabled() = settingsRepository.isMoviesEnabled
}
