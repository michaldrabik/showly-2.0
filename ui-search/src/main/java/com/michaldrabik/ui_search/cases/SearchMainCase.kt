package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.repository.TranslationsRepository
import com.michaldrabik.repository.movies.MoviesRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.SearchResult
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Translation
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class SearchMainCase @Inject constructor(
  private val translationsRepository: TranslationsRepository,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
) {

  val language by lazy { translationsRepository.getLanguage() }

  suspend fun loadMyShowsIds() = showsRepository.myShows.loadAllIds()

  suspend fun loadWatchlistShowsIds() = showsRepository.watchlistShows.loadAllIds()

  suspend fun loadMyMoviesIds() = moviesRepository.myMovies.loadAllIds()

  suspend fun loadWatchlistMoviesIds() = moviesRepository.watchlistMovies.loadAllIds()

  suspend fun loadTranslation(searchResult: SearchResult): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return when {
      searchResult.isShow -> translationsRepository.loadTranslation(searchResult.show, language, onlyLocal = true)
      else -> translationsRepository.loadTranslation(searchResult.movie, language, onlyLocal = true)
    }
  }

  suspend fun loadTranslation(show: Show): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(show, language)
  }

  suspend fun loadTranslation(movie: Movie): Translation? {
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return translationsRepository.loadTranslation(movie, language)
  }
}
