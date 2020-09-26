package com.michaldrabik.showly2.ui.followedshows.seelater.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SeeLaterLoadShowsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository
) {

  suspend fun loadShows(): List<Show> {
    val sortType = settingsRepository.load().seeLaterShowsSortBy
    val shows = showsRepository.seeLaterShows.loadAll()
    return when (sortType) {
      NAME -> shows.sortedBy { it.title }
      DATE_ADDED -> shows.sortedByDescending { it.updatedAt }
      RATING -> shows.sortedByDescending { it.rating }
      NEWEST -> shows.sortedByDescending { it.year }
      else -> error("Should not be used here.")
    }
  }
}
