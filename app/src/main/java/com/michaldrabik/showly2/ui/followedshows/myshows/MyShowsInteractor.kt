package com.michaldrabik.showly2.ui.followedshows.myshows

import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.ALL
import com.michaldrabik.showly2.model.MyShowsSection.FINISHED
import com.michaldrabik.showly2.model.MyShowsSection.UPCOMING
import com.michaldrabik.showly2.model.MyShowsSection.WATCHING
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus.RETURNING
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.repository.settings.SettingsRepository
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.followedshows.myshows.recycler.MyShowsItem
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class MyShowsInteractor @Inject constructor(
  private val imagesProvider: ShowImagesProvider,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val database: AppDatabase
) {

  suspend fun loadSettings() = settingsRepository.load()

  suspend fun loadAllShows() = showsRepository.myShows.loadAll()

  suspend fun filterSectionShows(
    allShows: List<MyShowsItem>,
    section: MyShowsSection
  ): List<MyShowsItem> {
    val sortOrder = loadSortOrder(section)
    val shows = allShows
      .filter {
        val seasons = database.seasonsDao().getAllForShow(it.show.traktId)
        val airedSeasons = seasons.filter { s -> s.seasonFirstAired?.isBefore(nowUtc()) == true }
        when (section) {
          WATCHING -> {
            airedSeasons.any { s -> !s.isWatched }
          }
          FINISHED -> {
            section.statuses.contains(it.show.status) && seasons.all { s -> s.isWatched }
          }
          UPCOMING -> {
            section.statuses.contains(it.show.status) ||
              (it.show.status == RETURNING && airedSeasons.all { s -> s.isWatched })
          }
          else -> true
        }
      }
    return sortBy(sortOrder, shows)
  }

  suspend fun loadRecentShows(): List<Show> {
    val amount = loadSettings().myShowsRecentsAmount
    return showsRepository.myShows.loadAllRecent(amount)
  }

  suspend fun loadSortOrder(section: MyShowsSection): SortOrder {
    val settings = loadSettings()
    return when (section) {
      WATCHING -> settings.myShowsRunningSortBy
      UPCOMING -> settings.myShowsIncomingSortBy
      FINISHED -> settings.myShowsEndedSortBy
      ALL -> settings.myShowsAllSortBy
      else -> error("Should not be used here.")
    }
  }

  private fun sortBy(sortOrder: SortOrder, shows: List<MyShowsItem>) =
    when (sortOrder) {
      NAME -> shows.sortedBy { it.show.title }
      NEWEST -> shows.sortedByDescending { it.show.year }
      RATING -> shows.sortedByDescending { it.show.rating }
      else -> throw IllegalStateException("Unsupported sort type.")
    }

  suspend fun setSectionSortOrder(section: MyShowsSection, order: SortOrder) {
    val settings = loadSettings()
    val newSettings = when (section) {
      WATCHING -> settings.copy(myShowsRunningSortBy = order)
      FINISHED -> settings.copy(myShowsEndedSortBy = order)
      UPCOMING -> settings.copy(myShowsIncomingSortBy = order)
      ALL -> settings.copy(myShowsAllSortBy = order)
      else -> error("Should not be used here.")
    }
    settingsRepository.update(newSettings)
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesProvider.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(show, type, force)
}
