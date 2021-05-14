package com.michaldrabik.repository.ratings

import com.michaldrabik.common.di.AppScope
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
