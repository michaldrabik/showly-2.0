package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MovieDetailsRepository
import com.michaldrabik.repository.shows.ShowDetailsRepository
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkBundle
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver.Companion.SOURCE_IMDB
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver.Companion.SOURCE_TMDB
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver.Companion.TMDB_TYPE_MOVIE
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkResolver.Companion.TMDB_TYPE_TV
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTmdb
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainDeepLinksCase @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val showDetailsRepository: ShowDetailsRepository,
  private val movieDetailsRepository: MovieDetailsRepository,
  private val mappers: Mappers
) {

  suspend fun findById(imdbId: IdImdb): DeepLinkBundle {
    val show = showDetailsRepository.find(imdbId)
    if (show != null) {
      return DeepLinkBundle(show = show)
    }
    val movie = movieDetailsRepository.find(imdbId)
    if (movie != null) {
      return DeepLinkBundle(movie = movie)
    }

    val searchResult = cloud.traktApi.fetchSearchId(SOURCE_IMDB, imdbId.id)
    if (searchResult.size == 1) {
      val showSearch = searchResult[0].show
      val movieSearch = searchResult[0].movie
      when {
        showSearch != null -> {
          val uiShow = mappers.show.fromNetwork(showSearch)
          database.showsDao().upsert(listOf(mappers.show.toDatabase(uiShow)))
          return DeepLinkBundle(show = uiShow)
        }
        movieSearch != null -> {
          val uiMovie = mappers.movie.fromNetwork(movieSearch)
          database.moviesDao().upsert(listOf(mappers.movie.toDatabase(uiMovie)))
          return DeepLinkBundle(movie = uiMovie)
        }
      }
    }

    return DeepLinkBundle.EMPTY
  }

  suspend fun findById(tmdbId: IdTmdb, type: String): DeepLinkBundle {
    val localShow = showDetailsRepository.find(tmdbId)
    if (localShow != null && type == TMDB_TYPE_TV) {
      return DeepLinkBundle(show = localShow)
    }
    val localMovie = movieDetailsRepository.find(tmdbId)
    if (localMovie != null && type == TMDB_TYPE_MOVIE) {
      return DeepLinkBundle(movie = localMovie)
    }

    val searchResult = cloud.traktApi.fetchSearchId(SOURCE_TMDB, tmdbId.id.toString())
    if (searchResult.isNotEmpty()) {
      searchResult
        .filter { it.show != null || it.movie != null }
        .forEach { result ->
          val show = result.show
          val movie = result.movie
          if (show != null && type == TMDB_TYPE_TV) {
            val uiShow = mappers.show.fromNetwork(show)
            database.showsDao().upsert(listOf(mappers.show.toDatabase(uiShow)))
            return DeepLinkBundle(show = uiShow)
          }
          if (movie != null && type == TMDB_TYPE_MOVIE) {
            val uiMovie = mappers.movie.fromNetwork(movie)
            database.moviesDao().upsert(listOf(mappers.movie.toDatabase(uiMovie)))
            return DeepLinkBundle(movie = uiMovie)
          }
        }
    }

    return DeepLinkBundle.EMPTY
  }
}
