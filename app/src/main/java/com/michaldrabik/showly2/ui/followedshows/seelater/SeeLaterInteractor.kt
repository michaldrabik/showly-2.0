package com.michaldrabik.showly2.ui.followedshows.seelater

import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.DATE_ADDED
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SeeLaterInteractor @Inject constructor(
  private val imagesManager: ImagesManager,
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

  suspend fun setSortOrder(sortOrder: SortOrder) {
    val settings = settingsRepository.load()
    settingsRepository.update(settings.copy(seeLaterShowsSortBy = sortOrder))
  }

  suspend fun loadSortOrder(): SortOrder {
    return settingsRepository.load().seeLaterShowsSortBy
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}
