package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.shows.ShowsRepository
import javax.inject.Inject

@AppScope
class SearchSuggestionsCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
) {

  private var showsCache: List<Show>? = null
  private var translationsCache: Map<Long, Translation>? = null

  suspend fun loadSuggestions(query: String, limit: Int): List<Show> {
    if (query.trim().isBlank()) return emptyList()

    val language = settingsRepository.getLanguage()
    if (showsCache == null) {
      val collection = showsRepository.loadCollection()
      val discover = showsRepository.discoverShows.loadAllCached()
      showsCache = (collection + discover).distinctBy { it.traktId }
    }
    if (translationsCache == null && language != Config.DEFAULT_LANGUAGE) {
      translationsCache = translationsRepository.loadAllShowsLocal(language)
    }

    return showsCache
      ?.filter {
        it.title.contains(query, true) ||
          translationsCache?.get(it.traktId)?.title?.contains(query, true) == true
      }
      ?.take(limit)
      ?.sortedByDescending { it.votes }
      ?: emptyList()
  }
}
