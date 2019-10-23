package com.michaldrabik.showly2.ui.myshows

import com.michaldrabik.showly2.Config.MY_SHOWS_RECENTS_AMOUNT
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.NAME
import com.michaldrabik.showly2.model.SortOrder.NEWEST
import com.michaldrabik.showly2.model.SortOrder.RATING
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class MyShowsInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  private val searchItemsCache = mutableListOf<Show>()

  suspend fun loadRecentShows() =
    database.followedShowsDao().getAllRecent()
      .map { mappers.show.fromDatabase(it) }
      .take(MY_SHOWS_RECENTS_AMOUNT)

  suspend fun loadShows(section: MyShowsSection): List<Show> {
    val sortOrder = loadSortOrder(section)
    val shows = database.followedShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }
      .filter { section.statuses.contains(it.status) }
    return sortBy(sortOrder, shows)
  }

  suspend fun loadSettings() =
    mappers.settings.fromDatabase(database.settingsDao().getAll()!!)

  private suspend fun loadSortOrder(section: MyShowsSection): SortOrder {
    val settings = loadSettings()
    return when (section) {
      RUNNING -> settings.myShowsRunningSortBy
      COMING_SOON -> settings.myShowsIncomingSortBy
      MyShowsSection.ENDED -> settings.myShowsEndedSortBy
    }
  }

  private fun sortBy(sortOrder: SortOrder, shows: List<Show>) =
    when (sortOrder) {
      NAME -> shows.sortedBy { it.title }
      NEWEST -> shows.sortedByDescending { it.year }
      RATING -> shows.sortedByDescending { it.rating }
    }

  suspend fun setSectionSortOrder(section: MyShowsSection, order: SortOrder) {
    val settings = loadSettings()
    val newSettings = when (section) {
      RUNNING -> settings.copy(myShowsRunningSortBy = order)
      MyShowsSection.ENDED -> settings.copy(myShowsEndedSortBy = order)
      COMING_SOON -> settings.copy(myShowsIncomingSortBy = order)
    }
    database.settingsDao().upsert(mappers.settings.toDatabase(newSettings))
  }

  suspend fun searchMyShows(query: String?): List<Show> {
    if (query.isNullOrBlank()) return emptyList()

    if (searchItemsCache.isEmpty()) {
      val allShows = database.followedShowsDao().getAll()
        .map { mappers.show.fromDatabase(it) }
      searchItemsCache.clear()
      searchItemsCache.addAll(allShows)
    }

    return searchItemsCache
      .filter {
        it.title.contains(query, true) || it.network.contains(query, true)
      }
      .sortedBy { it.title }
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)

  fun clearCache() = searchItemsCache.clear()
}