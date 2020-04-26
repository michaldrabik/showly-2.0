package com.michaldrabik.showly2.common.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.common.trakt.TraktSyncRunner
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.SeeLaterShow
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AppScope
class TraktImportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()
    importWatchlist(authToken)

    isRunning = false
    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun importWatchlist(token: TraktAuthToken) {
    Timber.d("Importing watchlist...")
    val syncResults = cloud.traktApi.fetchSyncWatchlist(token.token)
      .filter { it.show != null }
      .distinctBy { it.show!!.ids?.trakt }

    val localShowsIds =
      database.seeLaterShowsDao().getAll().map { it.idTrakt }
        .plus(database.myShowsDao().getAll().map { it.idTrakt })

    syncResults
      .forEachIndexed { index, result ->
        delay(200)
        Timber.d("Processing \'${result.show!!.title}\'...")
        progressListener?.invoke(result.show!!, index, syncResults.size)
        try {
          val showId = result.show!!.ids?.trakt ?: -1
          database.withTransaction {
            if (showId !in localShowsIds) {
              val show = mappers.show.fromNetwork(result.show!!)
              val showDb = mappers.show.toDatabase(show)
              database.showsDao().upsert(listOf(showDb))
              database.seeLaterShowsDao().insert(SeeLaterShow.fromTraktId(showId, nowUtcMillis()))
            }
          }
        } catch (t: Throwable) {
          Timber.w("Processing \'${result.show!!.title}\' failed. Skipping...")
        }
      }
  }
}
