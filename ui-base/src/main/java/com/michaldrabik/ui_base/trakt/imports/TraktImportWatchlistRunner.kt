package com.michaldrabik.ui_base.trakt.imports

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.SeeLaterShow
import com.michaldrabik.ui_base.trakt.TraktSyncRunner
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_repository.TraktAuthToken
import com.michaldrabik.ui_repository.TranslationsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_repository.mappers.Mappers
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AppScope
class TraktImportWatchlistRunner @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val translationsRepository: TranslationsRepository,
  userTraktManager: UserTraktManager
) : TraktSyncRunner(userTraktManager) {

  override suspend fun run(): Int {
    Timber.d("Initialized.")
    isRunning = true

    Timber.d("Checking authorization...")
    val authToken = checkAuthorization()

    try {
      importWatchlist(authToken)
    } catch (error: Throwable) {
      if (retryCount < MAX_RETRY_COUNT) {
        Timber.w("importWatchlist HTTP failed. Will retry in $RETRY_DELAY_MS ms... $error")
        retryCount += 1
        delay(RETRY_DELAY_MS)
        run()
      } else {
        retryCount = 0
        isRunning = false
        throw error
      }
    }

    isRunning = false
    retryCount = 0

    Timber.d("Finished with success.")
    return 0
  }

  private suspend fun importWatchlist(token: TraktAuthToken) {
    Timber.d("Importing watchlist...")
    val syncResults = cloud.traktApi.fetchSyncWatchlist(token.token)
      .filter { it.show != null }
      .distinctBy { it.show!!.ids?.trakt }

    val localShowsIds =
      database.seeLaterShowsDao().getAllTraktIds()
        .plus(database.myShowsDao().getAllTraktIds())
        .plus(database.archiveShowsDao().getAllTraktIds())
        .distinct()

    syncResults
      .forEachIndexed { index, result ->
        delay(200)
        Timber.d("Processing \'${result.show!!.title}\'...")
        val showUi = mappers.show.fromNetwork(result.show!!)
        progressListener?.invoke(showUi, index, syncResults.size)
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
          updateTranslation(showUi)
        } catch (t: Throwable) {
          Timber.w("Processing \'${result.show!!.title}\' failed. Skipping...")
        }
      }
  }

  private suspend fun updateTranslation(showUi: Show) {
    try {
      val locale = Locale.getDefault()
      if (locale.language !== Config.DEFAULT_LANGUAGE) {
        Timber.d("Fetching \'${showUi.title}\' translation...")
        translationsRepository.updateLocalShowTranslation(showUi, locale)
      }
    } catch (error: Throwable) {
      Timber.w("Processing \'${showUi.title}\' translation failed. Skipping translation...")
    }
  }
}
