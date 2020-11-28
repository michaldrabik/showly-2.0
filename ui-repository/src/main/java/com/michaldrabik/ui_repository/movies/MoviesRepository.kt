package com.michaldrabik.ui_repository.movies

import com.michaldrabik.common.di.AppScope
import javax.inject.Inject

@AppScope
class MoviesRepository @Inject constructor(
  val discoverMovies: DiscoverMoviesRepository,
  val movieDetails: MovieDetailsRepository
) {
}
