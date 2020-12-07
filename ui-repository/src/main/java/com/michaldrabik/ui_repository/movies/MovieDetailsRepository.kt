package com.michaldrabik.ui_repository.movies

import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.MoviesSyncLog
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class MovieDetailsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
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

  suspend fun loadComments(idTrakt: IdTrakt, limit: Int) =
    cloud.traktApi.fetchMovieComments(idTrakt.id, limit)
      .map { mappers.comment.fromNetwork(it) }
}
