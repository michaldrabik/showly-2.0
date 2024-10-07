package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.MovieSearch
import com.michaldrabik.data_local.database.model.ShowSearch
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.images.MovieImagesProvider
import com.michaldrabik.repository.images.ShowImagesProvider
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_search.recycler.SearchListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@ViewModelScoped
class SearchSuggestionsCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val settingsRepository: SettingsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  private var showsCache: List<ShowSearch>? = null
  private var moviesCache: List<MovieSearch>? = null
  private var showTranslationsCache: Map<Long, Translation>? = null
  private var movieTranslationsCache: Map<Long, Translation>? = null

  suspend fun loadSuggestions(query: String) =
    withContext(dispatchers.IO) {
      preloadCache()
      val spoilers = settingsRepository.spoilers.getAll()

      val showsDef = async { loadShows(query.trim(), 5) }
      val moviesDef = async { loadMovies(query.trim(), 5) }

      val suggestions = (showsDef.await() + moviesDef.await()).map {
        when (it) {
          is Show -> SearchResult(0, it, Movie.EMPTY)
          is Movie -> SearchResult(0, Show.EMPTY, it)
          else -> throw IllegalStateException()
        }
      }

      suggestions
        .map {
          async {
            val isFollowed =
              if (it.isShow) {
                showsRepository.myShows.exists(it.show.ids.trakt)
              } else {
                moviesRepository.myMovies.exists(it.movie.ids.trakt)
              }

            val isWatchlist =
              if (it.isShow) {
                showsRepository.watchlistShows.exists(it.show.ids.trakt)
              } else {
                moviesRepository.watchlistMovies.exists(it.movie.ids.trakt)
              }

            val image =
              if (it.isShow) {
                showsImagesProvider.findCachedImage(it.show, ImageType.POSTER)
              } else {
                moviesImagesProvider.findCachedImage(it.movie, ImageType.POSTER)
              }

            SearchListItem(
              id = UUID.randomUUID(),
              show = it.show,
              movie = it.movie,
              image = image,
              order = it.order,
              isFollowed = isFollowed,
              isWatchlist = isWatchlist,
              translation = loadTranslation(it),
              spoilers = spoilers,
            )
          }
        }.awaitAll()
        .sortedByDescending { it.votes }
    }

  suspend fun preloadCache() =
    withContext(dispatchers.IO) {
      val language = translationsRepository.getLanguage()
      val moviesEnabled = settingsRepository.isMoviesEnabled

      if (showsCache == null) {
        showsCache = localSource.shows.getAllForSearch()
      }
      if (moviesEnabled && moviesCache == null) {
        moviesCache = localSource.movies.getAllForSearch()
      }

      if (translationsRepository.getLanguage() != Config.DEFAULT_LANGUAGE) {
        if (showTranslationsCache == null) {
          showTranslationsCache = translationsRepository.loadAllShowsLocal(language)
        }
        if (moviesEnabled && movieTranslationsCache == null) {
          movieTranslationsCache = translationsRepository.loadAllMoviesLocal(language)
        }
      }
    }

  private suspend fun loadShows(
    query: String,
    limit: Int,
  ): List<Show> {
    if (query.trim().isBlank()) {
      return emptyList()
    }

    val cachedIds = showsCache
      ?.filter {
        it.title.contains(query, true) ||
          showTranslationsCache?.get(it.idTrakt)?.title?.contains(query, true) == true
      }?.take(limit)
      ?.map { it.idTrakt }

    return localSource.shows
      .getAll(cachedIds ?: emptyList())
      .map { mappers.show.fromDatabase(it) }
  }

  private suspend fun loadMovies(
    query: String,
    limit: Int,
  ): List<Movie> {
    if (query.trim().isBlank()) {
      return emptyList()
    }

    val cachedIds = moviesCache
      ?.filter {
        it.title.contains(query, true) ||
          movieTranslationsCache?.get(it.idTrakt)?.title?.contains(query, true) == true
      }?.take(limit)
      ?.map { it.idTrakt }

    return localSource.movies
      .getAll(cachedIds ?: emptyList())
      .map { mappers.movie.fromDatabase(it) }
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
