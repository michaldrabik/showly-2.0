package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.SettingsRepository
import javax.inject.Inject

@AppScope
class MainModesCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setMode(mode: Mode) {
    settingsRepository.mode = mode
  }

  fun getMode(): Mode {
    val isMoviesEnabled = settingsRepository.isMoviesEnabled
    val isMovies = settingsRepository.mode == MOVIES
    return if (isMoviesEnabled && isMovies) MOVIES else SHOWS
  }
}
