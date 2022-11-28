package com.michaldrabik.ui_my_shows.watchlist.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.common.ListViewMode
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class WatchlistViewModeCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun setListViewMode(viewMode: ListViewMode) {
    settingsRepository.viewMode.watchlistShowsViewMode = viewMode.name
  }

  fun getListViewMode(): ListViewMode {
    val viewMode = settingsRepository.viewMode.watchlistShowsViewMode
    return ListViewMode.valueOf(viewMode)
  }
}
