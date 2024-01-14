package com.michaldrabik.showly2.ui.main.cases.deeplink

import com.michaldrabik.data_local.sources.MoviesLocalDataSource
import com.michaldrabik.data_local.sources.ShowsLocalDataSource
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MovieDetailsRepository
import com.michaldrabik.repository.shows.ShowDetailsRepository
import com.michaldrabik.showly2.utilities.deeplink.DeepLinkBundle
import com.michaldrabik.ui_model.IdImdb
import javax.inject.Inject

class ImdbDeepLinkCase @Inject constructor(
  private val traktRemoteSource: TraktRemoteDataSource,
  private val showsLocalSource: ShowsLocalDataSource,
  private val moviesLocalSource: MoviesLocalDataSource,
  private val showDetailsRepository: ShowDetailsRepository,
  private val movieDetailsRepository: MovieDetailsRepository,
  private val mappers: Mappers
) {

  companion object {
    private const val SEARCH_ID_TYPE = "imdb"
  }

  suspend fun findById(imdbId: IdImdb): DeepLinkBundle {
    val show = showDetailsRepository.find(imdbId)
    if (show != null) {
      return DeepLinkBundle(show = show)
    }

    val movie = movieDetailsRepository.find(imdbId)
    if (movie != null) {
      return DeepLinkBundle(movie = movie)
    }

    val searchResult = traktRemoteSource.fetchSearchId(SEARCH_ID_TYPE, imdbId.id)
    if (searchResult.size == 1) {
      val showSearch = searchResult[0].show
      val movieSearch = searchResult[0].movie
      when {
        showSearch != null -> {
          val uiShow = mappers.show.fromNetwork(showSearch)
          showsLocalSource.upsert(listOf(mappers.show.toDatabase(uiShow)))
          return DeepLinkBundle(show = uiShow)
        }
        movieSearch != null -> {
          val uiMovie = mappers.movie.fromNetwork(movieSearch)
          moviesLocalSource.upsert(listOf(mappers.movie.toDatabase(uiMovie)))
          return DeepLinkBundle(movie = uiMovie)
        }
      }
    }

    return DeepLinkBundle.EMPTY
  }
}
