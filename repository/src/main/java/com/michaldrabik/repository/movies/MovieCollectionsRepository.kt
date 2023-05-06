package com.michaldrabik.repository.movies

import com.michaldrabik.common.ConfigVariant.COLLECTIONS_CACHE_DURATION
import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.data_local.database.model.MovieCollectionItem
import com.michaldrabik.data_local.sources.MovieCollectionsItemsLocalDataSource
import com.michaldrabik.data_local.sources.MovieCollectionsLocalDataSource
import com.michaldrabik.data_local.sources.MoviesLocalDataSource
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.trakt.TraktRemoteDataSource
import com.michaldrabik.repository.mappers.CollectionMapper
import com.michaldrabik.repository.mappers.MovieMapper
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MovieCollection
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieCollectionsRepository @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: TraktRemoteDataSource,
  private val moviesLocalSource: MoviesLocalDataSource,
  private val movieCollectionsLocalSource: MovieCollectionsLocalDataSource,
  private val movieCollectionsItemsLocalSource: MovieCollectionsItemsLocalDataSource,
  private val collectionMapper: CollectionMapper,
  private val movieMapper: MovieMapper,
  private val transactions: TransactionsProvider,
) {

  suspend fun loadCollection(collectionId: IdTrakt) = withContext(dispatchers.IO) {
    movieCollectionsLocalSource.getById(collectionId.id)
  }

  suspend fun loadCollections(movieId: IdTrakt): Pair<List<MovieCollection>, Source> =
    withContext(dispatchers.IO) {
      val now = nowUtc()
      val localCollections = movieCollectionsLocalSource.getByMovieId(movieId.id)

      val localTimestamp = localCollections.firstOrNull()?.updatedAt
      localTimestamp?.let { timestamp ->
        if (now.toMillis() - timestamp.toMillis() < COLLECTIONS_CACHE_DURATION) {
          return@withContext Pair(
            localCollections.map { collectionMapper.fromEntity(it) },
            Source.LOCAL
          )
        }
      }

      val remoteCollections = remoteSource.fetchMovieCollections(movieId.id)
      val collections = remoteCollections.map { collectionMapper.fromNetwork(it) }

      updateLocalCollections(collections, movieId, now)

      return@withContext Pair(
        collections,
        Source.REMOTE
      )
    }

  suspend fun loadCollectionItems(collectionId: IdTrakt): List<Movie> =
    withContext(dispatchers.IO) {
      val now = nowUtc()
      val localItems = movieCollectionsItemsLocalSource.getById(collectionId.id)

      val localTimestamp = localItems.firstOrNull()?.updatedAt
      localTimestamp?.let { timestamp ->
        if (now.toMillis() - timestamp < COLLECTIONS_CACHE_DURATION) {
          return@withContext localItems.map { movieMapper.fromDatabase(it) }
        }
      }

      val remoteItems = remoteSource.fetchMovieCollectionItems(collectionId.id)
      val items = remoteItems.map { movieMapper.fromNetwork(it) }

      transactions.withTransaction {
        val entities = items.mapIndexed { index, movie ->
          MovieCollectionItem(
            rank = index,
            idTrakt = movie.traktId,
            idTraktCollection = collectionId.id,
            createdAt = now,
            updatedAt = now
          )
        }
        moviesLocalSource.upsert(items.map { movieMapper.toDatabase(it) })
        movieCollectionsItemsLocalSource.replace(collectionId.id, entities)

        // Fill up collection with other movies that belong in it.
        val collection = movieCollectionsLocalSource.getById(collectionId.id)
        collection?.let { coll ->
          val insertEntities = entities
            .filter { it.idTrakt != coll.idTraktMovie }
            .map { coll.copy(id = 0, idTraktMovie = it.idTrakt) }
          movieCollectionsLocalSource.insertAll(insertEntities)
        }
      }

      return@withContext items
    }

  private suspend fun updateLocalCollections(
    collections: List<MovieCollection>,
    movieId: IdTrakt,
    now: ZonedDateTime,
  ) {
    var entities = collections.map {
      collectionMapper.toEntity(
        movieId = movieId.id,
        input = it,
        updatedAt = now,
        createdAt = now
      )
    }
    if (entities.isEmpty()) {
      entities = listOf(
        collectionMapper.toEntity(
          movieId.id,
          MovieCollection.EMPTY
        )
      )
    }
    movieCollectionsLocalSource.replaceByMovieId(movieId.id, entities)
  }

  enum class Source { LOCAL, REMOTE }
}
