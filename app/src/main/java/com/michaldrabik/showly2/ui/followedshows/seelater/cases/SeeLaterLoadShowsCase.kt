package com.michaldrabik.showly2.ui.followedshows.seelater.cases

import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder.DATE_ADDED
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
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
    }
  }
}
