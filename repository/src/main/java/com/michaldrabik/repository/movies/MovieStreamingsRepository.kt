package com.michaldrabik.repository.movies

import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.StreamingsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.StreamingService
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieStreamingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) : StreamingsRepository() {

  suspend fun getLocalStreamings(movie: Movie, countryCode: String): Pair<List<StreamingService>, ZonedDateTime?> {
    val localItems = database.movieStreamingsDao().getById(movie.traktId)
    val mappedItems = mappers.streamings.fromDatabaseMovie(localItems, movie.title, countryCode)

    val processedItems = processItems(mappedItems, countryCode)
    val date = localItems.firstOrNull()?.createdAt
    return Pair(processedItems, date)
  }

  suspend fun loadRemoteStreamings(movie: Movie, countryCode: String): List<StreamingService> {
    val remoteItems = cloud.tmdbApi.fetchMovieWatchProviders(movie.ids.tmdb.id, countryCode) ?: return emptyList()

    val entities = mappers.streamings.toDatabaseMovie(movie.ids, remoteItems)
    database.movieStreamingsDao().replace(movie.traktId, entities)

    return processItems(remoteItems, movie.title, countryCode)
  }

  suspend fun deleteCache() = database.movieStreamingsDao().deleteAll()
}
