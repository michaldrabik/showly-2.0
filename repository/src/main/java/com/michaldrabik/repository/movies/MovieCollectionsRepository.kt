package com.michaldrabik.repository.movies

import com.michaldrabik.common.ConfigVariant.COLLECTIONS_CACHE_DURATION
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.sources.MovieCollectionsLocalDataSource
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.repository.mappers.CollectionsMapper
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.MovieCollection
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieCollectionsRepository @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: TraktRemoteDataSource,
  private val localSource: MovieCollectionsLocalDataSource,
  private val mapper: CollectionsMapper,
) {

  suspend fun loadCollections(movieId: IdTrakt): Pair<List<MovieCollection>, Source> =
    withContext(dispatchers.IO) {
      val now = nowUtc()
      val localCollections = localSource.getByMovieId(movieId.id)

      val localTimestamp = localCollections.firstOrNull()?.updatedAt
      localTimestamp?.let { timestamp ->
        if (now.toMillis() - timestamp.toMillis() < COLLECTIONS_CACHE_DURATION) {
          return@withContext Pair(
            localCollections.map { mapper.fromEntity(it) },
            Source.LOCAL
          )
        }
      }

      val remoteCollections = remoteSource.fetchMovieCollections(movieId.id)
      val collections = remoteCollections.map { mapper.fromNetwork(it) }

      val entities = collections.map {
        mapper.toEntity(
          movieId = movieId.id,
          input = it,
          updatedAt = now,
          createdAt = now
        )
      }
      localSource.replace(movieId.id, entities)

      return@withContext Pair(
        collections,
        Source.REMOTE
      )
    }

  enum class Source { LOCAL, REMOTE }
}
