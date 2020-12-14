package com.michaldrabik.ui_repository.movies

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.RelatedMovie
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
class RelatedMoviesRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun loadAll(movie: Movie): List<Movie> {
    val related = database.relatedMoviesDao().getAllById(movie.ids.trakt.id)
    val latest = related.maxByOrNull { it.updatedAt }

    if (latest != null && nowUtcMillis() - latest.updatedAt < Config.RELATED_CACHE_DURATION) {
      val relatedIds = related.map { it.idTrakt }
      return database.moviesDao().getAll(relatedIds)
        .map { mappers.movie.fromDatabase(it) }
    }

    val remote = cloud.traktApi.fetchRelatedMovies(movie.ids.trakt.id)
      .map { mappers.movie.fromNetwork(it) }

    cacheRelated(remote, movie.ids.trakt)

    return remote
  }

  private suspend fun cacheRelated(movies: List<Movie>, movieId: IdTrakt) {
    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.moviesDao().upsert(movies.map { mappers.movie.toDatabase(it) })
      database.relatedMoviesDao().deleteById(movieId.id)
      database.relatedMoviesDao().insert(movies.map { RelatedMovie.fromTraktId(it.ids.trakt.id, movieId.id, timestamp) })
    }
  }
}
