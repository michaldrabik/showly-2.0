package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.ALL
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyShowsSortingCase @Inject constructor(
  private val settingsRepository: SettingsRepository,
) {

  fun loadSectionSortOrder(section: MyShowsSection) = when (section) {
    WATCHING -> Pair(
      settingsRepository.sortSettings.myShowsWatchingSortOrder,
      settingsRepository.sortSettings.myShowsWatchingSortType
    )
    UPCOMING -> Pair(
      settingsRepository.sortSettings.myShowsUpcomingSortOrder,
      settingsRepository.sortSettings.myShowsUpcomingSortType
    )
    FINISHED -> Pair(
      settingsRepository.sortSettings.myShowsFinishedSortOrder,
      settingsRepository.sortSettings.myShowsFinishedSortType
    )
    ALL -> Pair(
      settingsRepository.sortSettings.myShowsAllSortOrder,
      settingsRepository.sortSettings.myShowsAllSortType
    )
    else -> error("Should not be used here.")
  }

  fun setSectionSortOrder(
    section: MyShowsSection,
    sortOrder: SortOrder,
    sortType: SortType
  ) = when (section) {
    WATCHING -> {
      settingsRepository.sortSettings.myShowsWatchingSortOrder = sortOrder
      settingsRepository.sortSettings.myShowsWatchingSortType = sortType
    }
    UPCOMING -> {
      settingsRepository.sortSettings.myShowsUpcomingSortOrder = sortOrder
      settingsRepository.sortSettings.myShowsUpcomingSortType = sortType
    }
    FINISHED -> {
      settingsRepository.sortSettings.myShowsFinishedSortOrder = sortOrder
      settingsRepository.sortSettings.myShowsFinishedSortType = sortType
    }
    ALL -> {
      settingsRepository.sortSettings.myShowsAllSortOrder = sortOrder
      settingsRepository.sortSettings.myShowsAllSortType = sortType
    }
    else -> error("Should not be used here.")
  }
}
