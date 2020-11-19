package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Show as ShowDb

@AppScope
class SearchSuggestionsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
) {

  private var showsCache: List<ShowDb>? = null
  private var translationsCache: Map<Long, Translation>? = null

  suspend fun loadSuggestions(query: String, limit: Int): List<Show> {
    if (query.trim().isBlank()) return emptyList()

    val language = settingsRepository.getLanguage()
    if (showsCache == null) {
      showsCache = database.showsDao().getAll()
    }
    if (translationsCache == null && language != Config.DEFAULT_LANGUAGE) {
      translationsCache = translationsRepository.loadAllShowsLocal(language)
    }

    return showsCache
      ?.filter {
        it.title.contains(query, true) ||
          translationsCache?.get(it.idTrakt)?.title?.contains(query, true) == true
      }
      ?.take(limit)
      ?.map { mappers.show.fromDatabase(it) }
      ?.sortedByDescending { it.votes }
      ?: emptyList()
  }

  fun clearCache() {
    showsCache = null
    translationsCache = null
  }
}
