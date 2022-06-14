package com.michaldrabik.ui_base.trakt.imports

import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktImportListsRunner @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager,
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")

    var syncedCount = 0
    checkAuthorization()

    resetRetries()
    syncedCount += runLists()

    Timber.d("Finished with success.")
    return syncedCount
  }

  private suspend fun runLists(): Int {
    return try {
      importLists()
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runLists HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runLists()
      } else {
        throw error
      }
    }
  }

  private suspend fun importLists(): Int {
    Timber.d("Importing custom lists...")
    val nowUtcMillis = nowUtcMillis()
    val moviesEnabled = settingsRepository.isMoviesEnabled

    val localLists = localSource.customLists.getAll()
      .map { mappers.customList.fromDatabase(it) }
    val remoteLists = remoteSource.trakt.fetchSyncLists()
      .map { mappers.customList.fromNetwork(it) }

    remoteLists.forEach { remoteList ->
      Timber.d("Processing '${remoteList.name}' ...")
      val local = localLists.find { it.idTrakt == remoteList.idTrakt }
      transactions.withTransaction {
        when {
          local == null -> {
            Timber.d("Local list not found. Creating...")
            val listDb = mappers.customList.toDatabase(remoteList)
            val id = localSource.customLists.insert(listOf(listDb)).first()
            importListItems(id, remoteList.idTrakt!!, moviesEnabled, nowUtcMillis)
          }
          remoteList.updatedAt.isEqual(local.updatedAt).not() -> {
            Timber.d("Local list found and timestamp is different. Updating...")
            if (remoteList.updatedAt.isAfter(local.updatedAt)) {
              val listDb = mappers.customList.toDatabase(remoteList)
                .copy(id = local.id)
              localSource.customLists.update(listOf(listDb))
            }
            importListItems(local.id, local.idTrakt!!, moviesEnabled, nowUtcMillis)
          }
          else -> {
            Timber.d("Local list found but timestamp is the same. Skipping...")
          }
        }
      }
    }

    return remoteLists.size
  }

  private suspend fun importListItems(
    listId: Long,
    listIdTrakt: Long,
    moviesEnabled: Boolean,
    nowUtcMillis: Long,
  ) {
    Timber.d("Importing list items...")

    val localItems = localSource.customListsItems.getItemsById(listId)
    val items = remoteSource.trakt.fetchSyncListItems(listIdTrakt, moviesEnabled)
      .filter { item ->
        localItems.none {
          it.idTrakt == item.getTraktId() && it.type == item.getType()
        }
      }
      .filter { it.movie != null || it.show != null }

    val shows = items
      .filter { it.show != null }
      .map { mappers.show.fromNetwork(it.show!!) }
    localSource.shows.upsert(shows.map { mappers.show.toDatabase(it) })
    Timber.d("Shows to insert: ${shows.size}")

    val movies = items
      .filter { it.movie != null }
      .map { mappers.movie.fromNetwork(it.movie!!) }
    localSource.movies.upsert(movies.map { mappers.movie.toDatabase(it) })
    Timber.d("Movies to insert: ${movies.size}")

    items.forEach { remoteItem ->
      remoteItem.show?.let { remoteShow ->
        val show = shows.first { remoteShow.ids?.trakt == it.traktId }
        val itemDb = CustomListItem(
          id = 0,
          idList = listId,
          idTrakt = show.traktId,
          type = Mode.SHOWS.type,
          rank = 0,
          listedAt = remoteItem.lastListedMillis(),
          createdAt = nowUtcMillis,
          updatedAt = nowUtcMillis
        )
        localSource.customListsItems.insertItem(itemDb)
      }
      remoteItem.movie?.let { remoteMovie ->
        val movie = movies.first { remoteMovie.ids?.trakt == it.traktId }
        localSource.movies.upsert(listOf(mappers.movie.toDatabase(movie)))
        val itemDb = CustomListItem(
          id = 0,
          idList = listId,
          idTrakt = movie.traktId,
          type = Mode.MOVIES.type,
          rank = 0,
          listedAt = remoteItem.lastListedMillis(),
          createdAt = nowUtcMillis,
          updatedAt = nowUtcMillis
        )
        localSource.customListsItems.insertItem(itemDb)
      }
    }

    if (items.isNotEmpty()) {
      Timber.d("Updating list timestamp...")
      localSource.customLists.updateTimestamp(listId, nowUtcMillis)
    }
  }
}
