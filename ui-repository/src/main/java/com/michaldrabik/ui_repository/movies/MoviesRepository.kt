package com.michaldrabik.ui_repository.movies

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.ui_repository.shows.DiscoverShowsRepository
import javax.inject.Inject

@AppScope
class MoviesRepository @Inject constructor(
  val discoverMovies: DiscoverShowsRepository,
) {
}
