package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.common.Mode
import com.michaldrabik.common.Mode.MOVIES
import com.michaldrabik.common.Mode.SHOWS
import com.michaldrabik.repository.settings.SettingsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
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
