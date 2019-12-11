package com.michaldrabik.showly2.common.trakt

import android.util.Log
import androidx.room.withTransaction
import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.TraktAuthError
import com.michaldrabik.showly2.repository.TraktAuthToken
import com.michaldrabik.showly2.repository.UserTraktManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.SeeLaterShow
import kotlinx.coroutines.delay
import javax.inject.Inject
import com.michaldrabik.network.trakt.model.Show as ShowNetwork

@AppScope
class TraktImportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val userTraktManager: UserTraktManager
) {

  var progressListener: ((ShowNetwork, Int, Int) -> Unit)? = null
  var isRunning = false

  suspend fun run() {
    isRunning = true
    Log.d(TAG, "Initialized.")
    val authToken: TraktAuthToken
    try {
      Log.d(TAG, "Checking authorization...")
      authToken = userTraktManager.checkAuthorization()
    } catch (t: Throwable) {
      isRunning = false
      throw TraktAuthError(t.message)
    }

    importWatchlist(authToken)

    isRunning = false
    Log.d(TAG, "Finished with success.")
  }

  private suspend fun importWatchlist(token: TraktAuthToken) {
    Log.d(TAG, "Importing watchlist...")
    val syncResults = cloud.traktApi.fetchSyncWatchlist(token.token).distinctBy { it.show.ids.trakt }
    val localShowsIds =
      database.seeLaterShowsDao().getAll().map { it.idTrakt }
        .plus(database.myShowsDao().getAll().map { it.idTrakt })

    syncResults
      .forEachIndexed { index, result ->
        Log.d(TAG, "Processing \'${result.show.title}\'...")
        progressListener?.invoke(result.show, index, syncResults.size)
        try {
          val showId = result.show.ids.trakt
          database.withTransaction {
            if (showId !in localShowsIds) {
              val show = mappers.show.fromNetwork(result.show)
              val showDb = mappers.show.toDatabase(show)
              database.showsDao().upsert(listOf(showDb))
              database.seeLaterShowsDao().insert(SeeLaterShow.fromTraktId(showId, nowUtcMillis()))
            }
          }
        } catch (t: Throwable) {
          Log.w(TAG, "Processing \'${result.show.title}\' failed. Skipping...")
        }

        delay(200)
      }
  }

  companion object {
    private const val TAG = "TraktImportWatchlist"
  }
}