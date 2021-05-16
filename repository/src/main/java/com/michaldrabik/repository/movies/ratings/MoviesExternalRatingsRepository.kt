package com.michaldrabik.repository.movies.ratings

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Ratings
import javax.inject.Inject

@AppScope
class MoviesExternalRatingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun loadRatings(movie: Movie): Ratings {
    val localRatings = database.movieRatingsDao().getById(movie.traktId)
    localRatings?.let {
      if (nowUtcMillis() - it.updatedAt < Config.RATINGS_CACHE_DURATION) {
        return mappers.ratings.fromDatabase(it)
      }
    }

    val remoteRatings = cloud.omdbApi.fetchOmdbData(movie.ids.imdb.id)
      .let { mappers.ratings.fromNetwork(it) }
      .copy(trakt = Ratings.Value(String.format("%.1f", movie.rating), false))

    val dbRatings = mappers.ratings.toDatabase(movie.ids.trakt, remoteRatings)
    database.movieRatingsDao().upsert(dbRatings)

    return remoteRatings
  }
}
