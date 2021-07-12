package com.michaldrabik.repository.movies

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.MoviesSyncLog
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject

class MovieDetailsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun load(idTrakt: IdTrakt, force: Boolean = false): Movie {
    val local = database.moviesDao().getById(idTrakt.id)
    if (force || local == null || nowUtcMillis() - local.updatedAt > Config.MOVIE_DETAILS_CACHE_DURATION) {
      val remote = cloud.traktApi.fetchMovie(idTrakt.id)
      val movie = mappers.movie.fromNetwork(remote)
      database.moviesDao().upsert(listOf(mappers.movie.toDatabase(movie)))
      database.moviesSyncLogDao().upsert(MoviesSyncLog(movie.traktId, nowUtcMillis()))
      return movie
    }
    return mappers.movie.fromDatabase(local)
  }

  suspend fun delete(idTrakt: IdTrakt) {
    with(database) {
      moviesDao().deleteById(idTrakt.id)
    }
  }
}
