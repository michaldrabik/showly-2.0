package com.michaldrabik.repository

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.repository.movies.ratings.MoviesRatingsRepository
import com.michaldrabik.repository.shows.ratings.ShowsRatingsRepository
import javax.inject.Inject

@AppScope
class RatingsRepository @Inject constructor(
  val shows: ShowsRatingsRepository,
  val movies: MoviesRatingsRepository,
) {

  fun clear() {
    shows.clear()
    movies.clear()
  }
}
