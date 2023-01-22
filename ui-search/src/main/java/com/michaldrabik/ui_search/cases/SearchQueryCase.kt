package com.michaldrabik.ui_search.cases

import com.michaldrabik.common.Config
import com.michaldrabik.data_remote.RemoteDataSource
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
import kotlinx.coroutines.coroutineScope
import java.util.UUID
import javax.inject.Inject
import com.michaldrabik.data_remote.trakt.model.SearchResult as SearchResultNetwork

@ViewModelScoped
class SearchQueryCase @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  private val showsRepository: ShowsRepository,
  private val moviesRepository: MoviesRepository,
  private val translationsRepository: TranslationsRepository,
  private val showsImagesProvider: ShowImagesProvider,
  private val moviesImagesProvider: MovieImagesProvider,
) {

  suspend fun searchByQuery(query: String): List<SearchListItem> = coroutineScope {
    val withMovies = settingsRepository.isMoviesEnabled
    val myShowsIds = showsRepository.myShows.loadAllIds()
    val watchlistShowsIds = showsRepository.watchlistShows.loadAllIds()
    val myMoviesIds = moviesRepository.myMovies.loadAllIds()
    val watchlistMoviesIds = moviesRepository.watchlistMovies.loadAllIds()

    val results = remoteSource.trakt.fetchSearch(query, withMovies)
    results
      .sortedWith(
        compareByDescending<SearchResultNetwork> { it.score }
          .thenByDescending { it.getVotes() }
      )
      .map {
        async {
          val result = SearchResult(
            it.score ?: 0F,
            it.show?.let { s -> mappers.show.fromNetwork(s) } ?: Show.EMPTY,
            it.movie?.let { m -> mappers.movie.fromNetwork(m) } ?: Movie.EMPTY
          )

          val isFollowed =
            if (result.isShow) result.traktId in myShowsIds
            else result.traktId in myMoviesIds

          val isWatchlist =
            if (result.isShow) result.traktId in watchlistShowsIds
            else result.traktId in watchlistMoviesIds

          val image = loadImage(result)
          val translation = loadTranslation(result)

          SearchListItem(
            id = UUID.randomUUID(),
            show = result.show,
            movie = result.movie,
            image = image,
            score = result.score,
            isFollowed = isFollowed,
            isWatchlist = isWatchlist,
            translation = translation
          )
        }
      }.awaitAll()
  }

  private suspend fun loadImage(result: SearchResult) = when {
    result.isShow -> showsImagesProvider.findCachedImage(result.show, ImageType.POSTER)
    else -> moviesImagesProvider.findCachedImage(result.movie, ImageType.POSTER)
  }

  private suspend fun loadTranslation(result: SearchResult): Translation? {
    val language = translationsRepository.getLanguage()
    if (language == Config.DEFAULT_LANGUAGE) return Translation.EMPTY
    return when {
      result.isShow -> translationsRepository.loadTranslation(result.show, language, onlyLocal = true)
      else -> translationsRepository.loadTranslation(result.movie, language, onlyLocal = true)
    }
  }
}
