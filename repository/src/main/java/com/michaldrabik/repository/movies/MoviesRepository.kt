package com.michaldrabik.repository.movies

import com.michaldrabik.ui_model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepository @Inject constructor(
  val discoverMovies: DiscoverMoviesRepository,
  val relatedMovies: RelatedMoviesRepository,
  val movieDetails: MovieDetailsRepository,
  val myMovies: MyMoviesRepository,
  val watchlistMovies: WatchlistMoviesRepository
) {

  suspend fun loadCollection(): List<Movie> {
    val myMovies = myMovies.loadAll()
    val watchlistMovies = watchlistMovies.loadAll()
    return (myMovies + watchlistMovies).distinctBy { it.traktId }
  }
}
