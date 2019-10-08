package com.michaldrabik.showly2.ui.myshows

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.MyShowsSection
import com.michaldrabik.showly2.model.MyShowsSection.COMING_SOON
import com.michaldrabik.showly2.model.MyShowsSection.RUNNING
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowStatus.*
import com.michaldrabik.showly2.model.SortOrder
import com.michaldrabik.showly2.model.SortOrder.*
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

  companion object {
    private const val RECENT_SHOWS_AMOUNT = 4
  }

  suspend fun loadRecentShows() =
    database.followedShowsDao().getAllRecent()
      .map { mappers.show.fromDatabase(it) }
      .take(RECENT_SHOWS_AMOUNT)

  suspend fun loadRunningShows(): List<Show> {
    val sortOrder = loadSortOrder(RUNNING)
    val shows = database.followedShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }
      .filter { it.status == RETURNING }

    return sortBy(sortOrder, shows)
  }

  suspend fun loadEndedShows(): List<Show> {
    val sortOrder = loadSortOrder(MyShowsSection.ENDED)
    val shows = database.followedShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }
      .filter { it.status in arrayOf(ENDED, CANCELED) }

    return sortBy(sortOrder, shows)
  }

  suspend fun loadIncomingShows(): List<Show> {
    val sortOrder = loadSortOrder(COMING_SOON)
    val shows = database.followedShowsDao().getAll()
      .map { mappers.show.fromDatabase(it) }
      .filter { it.status in arrayOf(IN_PRODUCTION, PLANNED) }

    return sortBy(sortOrder, shows)
  }

  suspend fun loadSortOrder(section: MyShowsSection): SortOrder {
    val settings = mappers.settings.fromDatabase(database.settingsDao().getAll()!!)
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
    val settings = mappers.settings.fromDatabase(database.settingsDao().getAll()!!)
    val newSettings = when (section) {
      RUNNING -> settings.copy(myShowsRunningSortBy = order)
      MyShowsSection.ENDED -> settings.copy(myShowsEndedSortBy = order)
      COMING_SOON -> settings.copy(myShowsIncomingSortBy = order)
    }
    database.settingsDao().upsert(mappers.settings.toDatabase(newSettings))
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}