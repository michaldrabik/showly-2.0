package com.michaldrabik.showly2.ui.main.cases

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.movies.MovieDetailsRepository
import com.michaldrabik.repository.shows.ShowDetailsRepository
import com.michaldrabik.showly2.ui.main.helpers.DeepLinkBundle
import com.michaldrabik.ui_model.IdImdb
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

    val searchResult = cloud.traktApi.fetchSearchId("imdb", imdbId.id)
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
}
