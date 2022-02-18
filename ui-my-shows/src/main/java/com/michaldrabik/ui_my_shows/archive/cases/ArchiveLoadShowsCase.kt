package com.michaldrabik.ui_my_shows.archive.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_my_shows.helpers.FollowedShowsItemSorter
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ArchiveLoadShowsCase @Inject constructor(
  private val sorter: FollowedShowsItemSorter,
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadShows(searchQuery: String): List<Pair<Show, Translation?>> {
    val translations =
      if (language == Config.DEFAULT_LANGUAGE) emptyMap()
      else translationsRepository.loadAllShowsLocal(language)

    val sortOrder = settingsRepository.sorting.hiddenShowsSortOrder
    val sortType = settingsRepository.sorting.hiddenShowsSortType

    return showsRepository.hiddenShows.loadAll()
      .map { it to translations[it.traktId] }
      .filterByQuery(searchQuery)
      .sortedWith(sorter.sort(sortOrder, sortType))
  }

  private fun List<Pair<Show, Translation?>>.filterByQuery(query: String) =
    this.filter {
      it.first.title.contains(query, true) ||
        it.second?.title?.contains(query, true) == true
    }

  suspend fun loadTranslation(show: Show, onlyLocal: Boolean): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(show, language, onlyLocal)
  }
}
