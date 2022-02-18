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
      settingsRepository.sorting.myShowsWatchingSortOrder,
      settingsRepository.sorting.myShowsWatchingSortType
    )
    UPCOMING -> Pair(
      settingsRepository.sorting.myShowsUpcomingSortOrder,
      settingsRepository.sorting.myShowsUpcomingSortType
    )
    FINISHED -> Pair(
      settingsRepository.sorting.myShowsFinishedSortOrder,
      settingsRepository.sorting.myShowsFinishedSortType
    )
    ALL -> Pair(
      settingsRepository.sorting.myShowsAllSortOrder,
      settingsRepository.sorting.myShowsAllSortType
    )
    else -> error("Should not be used here.")
  }

  fun setSectionSortOrder(
    section: MyShowsSection,
    sortOrder: SortOrder,
    sortType: SortType
  ) = when (section) {
    WATCHING -> {
      settingsRepository.sorting.myShowsWatchingSortOrder = sortOrder
      settingsRepository.sorting.myShowsWatchingSortType = sortType
    }
    UPCOMING -> {
      settingsRepository.sorting.myShowsUpcomingSortOrder = sortOrder
      settingsRepository.sorting.myShowsUpcomingSortType = sortType
    }
    FINISHED -> {
      settingsRepository.sorting.myShowsFinishedSortOrder = sortOrder
      settingsRepository.sorting.myShowsFinishedSortType = sortType
    }
    ALL -> {
      settingsRepository.sorting.myShowsAllSortOrder = sortOrder
      settingsRepository.sorting.myShowsAllSortType = sortType
    }
    else -> error("Should not be used here.")
  }
}
