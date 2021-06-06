package com.michaldrabik.ui_base.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.CustomListItem
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.SettingsRepository
import com.michaldrabik.repository.TraktAuthToken
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktImportListsRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val settingsRepository: SettingsRepository,
  userTraktManager: UserTraktManager,
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    var syncedCount = 0
    val authToken = checkAuthorization()

    resetRetries()
    syncedCount += runLists(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return syncedCount
  }

  private suspend fun runLists(authToken: TraktAuthToken): Int {
    return try {
      importLists(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("runLists HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        runLists(authToken)
      } else {
        isRunning = false
        throw error
      }
    }
  }

  private suspend fun importLists(token: TraktAuthToken): Int {
    Timber.d("Importing custom lists...")
    val nowUtcMillis = nowUtcMillis()
    val moviesEnabled = settingsRepository.isMoviesEnabled

    val localLists = database.customListsDao().getAll()
      .map { mappers.customList.fromDatabase(it) }
    val remoteLists = cloud.traktApi.fetchSyncLists(token.token)
      .map { mappers.customList.fromNetwork(it) }

    remoteLists.forEach { remoteList ->
      Timber.d("Processing '${remoteList.name}' ...")
      val local = localLists.find { it.idTrakt == remoteList.idTrakt }
      database.withTransaction {
        when {
          local == null -> {
            Timber.d("Local list not found. Creating...")
            val listDb = mappers.customList.toDatabase(remoteList)
            val id = database.customListsDao().insert(listOf(listDb)).first()
            importListItems(id, remoteList.idTrakt!!, token, moviesEnabled, nowUtcMillis)
          }
          remoteList.updatedAt.isEqual(local.updatedAt).not() -> {
            Timber.d("Local list found and timestamp is different. Updating...")
            if (remoteList.updatedAt.isAfter(local.updatedAt)) {
              val listDb = mappers.customList.toDatabase(remoteList)
                .copy(id = local.id)
              database.customListsDao().update(listOf(listDb))
            }
            importListItems(local.id, local.idTrakt!!, token, moviesEnabled, nowUtcMillis)
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
    token: TraktAuthToken,
    moviesEnabled: Boolean,
    nowUtcMillis: Long,
  ) {
    Timber.d("Importing list items...")

    val localItems = database.customListsItemsDao().getItemsById(listId)
    val items = cloud.traktApi.fetchSyncListItems(token.token, listIdTrakt, moviesEnabled)
      .filter { item ->
        localItems.none {
          it.idTrakt == item.getTraktId() && it.type == item.getType()
        }
      }
      .filter { it.movie != null || it.show != null }

    val shows = items
      .filter { it.show != null }
      .map { mappers.show.fromNetwork(it.show!!) }
    database.showsDao().upsert(shows.map { mappers.show.toDatabase(it) })
    Timber.d("Shows to insert: ${shows.size}")

    val movies = items
      .filter { it.movie != null }
      .map { mappers.movie.fromNetwork(it.movie!!) }
    database.moviesDao().upsert(movies.map { mappers.movie.toDatabase(it) })
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
        database.customListsItemsDao().insertItem(itemDb)
      }
      remoteItem.movie?.let { remoteMovie ->
        val movie = movies.first { remoteMovie.ids?.trakt == it.traktId }
        database.moviesDao().upsert(listOf(mappers.movie.toDatabase(movie)))
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
        database.customListsItemsDao().insertItem(itemDb)
      }
    }

    if (items.isNotEmpty()) {
      Timber.d("Updating list timestamp...")
      database.customListsDao().updateTimestamp(listId, nowUtcMillis)
    }
  }
}
