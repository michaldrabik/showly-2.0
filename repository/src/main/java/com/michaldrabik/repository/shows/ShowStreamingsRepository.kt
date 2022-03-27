package com.michaldrabik.repository.shows

import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.StreamingsRepository
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.StreamingService
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowStreamingsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val mappers: Mappers,
) : StreamingsRepository() {

  suspend fun getLocalStreamings(show: Show, countryCode: String): Pair<List<StreamingService>, ZonedDateTime?> {
    val localItems = localSource.showStreamings.getById(show.traktId)
    val mappedItems = mappers.streamings.fromDatabaseShow(localItems, show.title, countryCode)

    val processedItems = processItems(mappedItems, countryCode)
    val date = localItems.firstOrNull()?.createdAt
    return Pair(processedItems, date)
  }

  suspend fun loadRemoteStreamings(show: Show, countryCode: String): List<StreamingService> {
    val remoteItems = remoteSource.tmdb.fetchShowWatchProviders(show.ids.tmdb.id, countryCode) ?: return emptyList()

    val entities = mappers.streamings.toDatabaseShow(show.ids, remoteItems)
    localSource.showStreamings.replace(show.traktId, entities)

    return processItems(remoteItems, show.title, countryCode)
  }

  suspend fun deleteCache() = localSource.showStreamings.deleteAll()
}
