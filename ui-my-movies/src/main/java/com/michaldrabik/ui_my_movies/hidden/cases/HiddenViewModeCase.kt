package com.michaldrabik.ui_my_movies.hidden.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.ListViewMode
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class HiddenViewModeCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setNextViewMode(): ListViewMode {
    val viewModes = ListViewMode.values()
    val index = viewModes.indexOf(getListViewMode()) + 1
    val nextIndex = if (index >= viewModes.size) 0 else index
    settingsRepository.viewMode.hiddenMoviesViewMode = viewModes[nextIndex].name
    return viewModes[nextIndex]
  }

  fun getListViewMode(): ListViewMode {
    val viewMode = settingsRepository.viewMode.hiddenMoviesViewMode
    return ListViewMode.valueOf(viewMode)
  }
}
