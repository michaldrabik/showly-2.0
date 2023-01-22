package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_search.recycler.SearchListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.UUID
import javax.inject.Inject
import com.michaldrabik.data_local.database.model.Movie as MovieDb
import com.michaldrabik.data_local.database.model.Show as ShowDb

@ViewModelScoped
class SearchSuggestionsCase @Inject constructor(
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  private var showsCache: List<ShowDb>? = null
  private var moviesCache: List<MovieDb>? = null
  private var showTranslationsCache: Map<Long, Translation>? = null
  private var movieTranslationsCache: Map<Long, Translation>? = null

  suspend fun preloadCache() {
    val language = translationsRepository.getLanguage()
    val moviesEnabled = settingsRepository.isMoviesEnabled

    if (showsCache == null) showsCache = localSource.shows.getAll()
    if (moviesEnabled && moviesCache == null) moviesCache = localSource.movies.getAll()

    if (translationsRepository.getLanguage() != Config.DEFAULT_LANGUAGE) {
      if (showTranslationsCache == null) {
        showTranslationsCache = translationsRepository.loadAllShowsLocal(language)
      }
      if (moviesEnabled && movieTranslationsCache == null) {
        movieTranslationsCache = translationsRepository.loadAllMoviesLocal(language)
      }
    }
  }

  suspend fun loadSuggestions(query: String) = coroutineScope {
    val showsDef = async { loadShows(query.trim(), 5) }
    val moviesDef = async { loadMovies(query.trim(), 5) }

    val suggestions = (showsDef.await() + moviesDef.await()).map {
      when (it) {
        is Show -> SearchResult(0F, it, Movie.EMPTY)
        is Movie -> SearchResult(0F, Show.EMPTY, it)
        else -> throw IllegalStateException()
      }
    }

    suggestions.map {
      val image =
        if (it.isShow) showsImagesProvider.findCachedImage(it.show, ImageType.POSTER)
        else moviesImagesProvider.findCachedImage(it.movie, ImageType.POSTER)
      val translation = loadTranslation(it)
      SearchListItem(
        id = UUID.randomUUID(),
        show = it.show,
        movie = it.movie,
        image = image,
        score = it.score,
        isFollowed = false,
        isWatchlist = false,
        translation = translation
      )
    }.sortedByDescending { it.votes }
  }

  private suspend fun loadShows(query: String, limit: Int): List<Show> {
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

  private suspend fun loadMovies(query: String, limit: Int): List<Movie> {
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

  private suspend fun loadTranslation(result: SearchResult): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return when {
      result.isShow -> translationsRepository.loadTranslation(result.show, language, onlyLocal = true)
      else -> translationsRepository.loadTranslation(result.movie, language, onlyLocal = true)
    }
  }

  fun clearCache() {
    showsCache = null
    moviesCache = null
    showTranslationsCache = null
    movieTranslationsCache = null
  }
}
