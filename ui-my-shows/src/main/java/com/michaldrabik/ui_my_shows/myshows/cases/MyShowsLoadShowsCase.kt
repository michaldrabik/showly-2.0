package com.michaldrabik.ui_my_shows.myshows.cases

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Season
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.MyShowsSection.FINISHED
import com.michaldrabik.ui_model.MyShowsSection.UPCOMING
import com.michaldrabik.ui_model.MyShowsSection.WATCHING
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.ShowStatus.RETURNING
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortType
import com.michaldrabik.ui_my_shows.myshows.helpers.MyShowsItemSorter
import com.michaldrabik.ui_my_shows.myshows.recycler.MyShowsItem
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MyShowsLoadShowsCase @Inject constructor(
  private val sorter: MyShowsItemSorter,
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val database: AppDatabase
) {

  suspend fun loadAllShows() = showsRepository.myShows.loadAll()

  suspend fun loadRecentShows(): List<Show> {
    val amount = settingsRepository.load().myRecentsAmount
    return showsRepository.myShows.loadAllRecent(amount)
  }

  suspend fun loadSeasonsForShows(traktIds: List<Long>, buffer: MutableList<Season> = mutableListOf()): List<Season> {
    val batch = traktIds.take(500)
    if (batch.isEmpty()) return buffer
    val seasons = database.seasonsDao().getAllByShowsIds(batch)
      .filter { it.seasonNumber != 0 }
    buffer.addAll(seasons)
    return loadSeasonsForShows(traktIds.filter { it !in batch }, buffer)
  }

  fun filterSectionShows(
    allShows: List<MyShowsItem>,
    allSeasons: List<Season>,
    section: MyShowsSection,
    sortOrder: Pair<SortOrder, SortType>,
    searchQuery: String? = null
  ): List<MyShowsItem> {
    val shows = allShows
      .filter {
        val seasons = allSeasons.filter { s -> s.idShowTrakt == it.show.traktId }
        val airedSeasons = seasons.filter { s -> s.seasonFirstAired?.isBefore(nowUtc()) == true }
        when (section) {
          WATCHING -> {
            airedSeasons.any { s -> !s.isWatched }
          }
          FINISHED -> {
            section.allowedStatuses.contains(it.show.status) && seasons.all { s -> s.isWatched }
          }
          UPCOMING -> {
            section.allowedStatuses.contains(it.show.status) ||
              (it.show.status == RETURNING && airedSeasons.all { s -> s.isWatched })
          }
          else -> true
        }
      }

    return shows
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder.first, sortOrder.second))
  }

  private fun List<MyShowsItem>.filterByQuery(query: String?) = when {
    query.isNullOrBlank() -> this
    else -> this.filter {
      it.show.title.contains(query, true) ||
        it.translation?.title?.contains(query, true) == true
    }
  }
}
