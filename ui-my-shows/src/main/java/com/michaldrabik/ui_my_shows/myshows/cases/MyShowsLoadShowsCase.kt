package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.Season
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.ALL
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.ShowStatus.RETURNING
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.DATE_ADDED
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.NEWEST
import com.michaldrabik.ui_model.SortOrder.RATING
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class MyShowsLoadShowsCase @Inject constructor(
  private val imagesProvider: ShowImagesProvider,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val database: AppDatabase
) {

  suspend fun loadSettings() = settingsRepository.load()

  suspend fun loadAllShows() = showsRepository.myShows.loadAll()

  suspend fun loadSeasonsForShows(traktIds: List<Long>, buffer: MutableList<Season> = mutableListOf()): List<Season> {
    val batch = traktIds.take(500)
    if (batch.isEmpty()) return buffer
    val seasons = database.seasonsDao().getAllForShows(batch)
      .filter { it.seasonNumber != 0 }
    buffer.addAll(seasons)
    return loadSeasonsForShows(traktIds.filter { it !in batch }, buffer)
  }

  suspend fun filterSectionShows(
    allShows: List<MyShowsItem>,
    allSeasons: List<Season>,
    section: MyShowsSection
  ): List<MyShowsItem> {
    val sortOrder = loadSortOrder(section)
    val shows = allShows
      .filter {
        val seasons = allSeasons.filter { s -> s.idShowTrakt == it.show.traktId }
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
      WATCHING -> settings.myShowsWatchingSortBy
      UPCOMING -> settings.myShowsUpcomingSortBy
      FINISHED -> settings.myShowsFinishedSortBy
      ALL -> settings.myShowsAllSortBy
      else -> error("Should not be used here.")
    }
  }

  private fun sortBy(sortOrder: SortOrder, shows: List<MyShowsItem>) =
    when (sortOrder) {
      NAME -> shows.sortedBy { it.show.title }
      NEWEST -> shows.sortedByDescending { it.show.year }
      RATING -> shows.sortedByDescending { it.show.rating }
      DATE_ADDED -> shows.sortedByDescending { it.show.updatedAt }
      else -> error("Should not be used here.")
    }

  suspend fun setSectionSortOrder(section: MyShowsSection, order: SortOrder) {
    val settings = loadSettings()
    val newSettings = when (section) {
      WATCHING -> settings.copy(myShowsWatchingSortBy = order)
      FINISHED -> settings.copy(myShowsFinishedSortBy = order)
      UPCOMING -> settings.copy(myShowsUpcomingSortBy = order)
      ALL -> settings.copy(myShowsAllSortBy = order)
      else -> error("Should not be used here.")
    }
    settingsRepository.update(newSettings)
  }

  suspend fun loadTranslation(show: Show): Translation? {
    val language = settingsRepository.load().language
    if (language == Config.DEFAULT_LANGUAGE) return null
    return translationsRepository.loadTranslation(show, language, onlyLocal = true)
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesProvider.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(show, type, force)
}
