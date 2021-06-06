package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Movie as MovieDb
import com.michaldrabik.data_local.database.model.Show as ShowDb

@ViewModelScoped
class SearchSuggestionsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository
) {

  private var showsCache: List<ShowDb>? = null
  private var moviesCache: List<MovieDb>? = null
  private var showTranslationsCache: Map<Long, Translation>? = null
  private var movieTranslationsCache: Map<Long, Translation>? = null

  suspend fun preloadCache() {
    val language = translationsRepository.getLanguage()
    val moviesEnabled = settingsRepository.isMoviesEnabled

    if (showsCache == null) showsCache = database.showsDao().getAll()
    if (moviesEnabled && moviesCache == null) moviesCache = database.moviesDao().getAll()

    if (translationsRepository.getLanguage() != Config.DEFAULT_LANGUAGE) {
      if (showTranslationsCache == null) {
        showTranslationsCache = translationsRepository.loadAllShowsLocal(language)
      }
      if (moviesEnabled && movieTranslationsCache == null) {
        movieTranslationsCache = translationsRepository.loadAllMoviesLocal(language)
      }
    }
  }

  suspend fun loadShows(query: String, limit: Int): List<Show> {
    if (query.trim().isBlank()) return emptyList()
    preloadCache()
    return showsCache
      ?.filter {
        it.title.contains(query, true) ||
          showTranslationsCache?.get(it.idTrakt)?.title?.contains(query, true) == true
      }
      ?.take(limit)
      ?.map { mappers.show.fromDatabase(it) }
      ?: emptyList()
  }

  suspend fun loadMovies(query: String, limit: Int): List<Movie> {
    if (query.trim().isBlank()) return emptyList()
    preloadCache()
    return moviesCache
      ?.filter {
        it.title.contains(query, true) ||
          movieTranslationsCache?.get(it.idTrakt)?.title?.contains(query, true) == true
      }
      ?.take(limit)
      ?.map { mappers.movie.fromDatabase(it) }
      ?: emptyList()
  }

  fun clearCache() {
    showsCache = null
    moviesCache = null
    showTranslationsCache = null
    movieTranslationsCache = null
  }
}
