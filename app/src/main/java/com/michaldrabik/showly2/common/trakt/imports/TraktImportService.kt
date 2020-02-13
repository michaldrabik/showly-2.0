package com.michaldrabik.showly2.common.trakt.imports

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.TraktImportAuthError
import com.michaldrabik.showly2.common.events.TraktImportError
import com.michaldrabik.showly2.common.events.TraktImportProgress
import com.michaldrabik.showly2.common.events.TraktImportStart
import com.michaldrabik.showly2.common.events.TraktImportSuccess
import com.michaldrabik.showly2.common.trakt.TraktSyncService
import com.michaldrabik.showly2.model.error.TraktAuthError
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraktImportService : TraktSyncService() {

  companion object {
    private const val IMPORT_NOTIFICATION_PROGRESS_ID = 826
    private const val IMPORT_NOTIFICATION_COMPLETE_SUCCESS_ID = 827
    private const val IMPORT_NOTIFICATION_COMPLETE_ERROR_ID = 828
  }

  @Inject lateinit var importWatchedRunner: TraktImportWatchedRunner
  @Inject lateinit var importWatchlistRunner: TraktImportWatchlistRunner

  override val tag = "TraktImportService"
  override val serviceId = "Showly Trakt Import"

  override val notificationTitleRes = R.string.textTraktImport
  override val notificationProgressRes = R.string.textTraktImportRunning
  override val notificationSuccessRes = R.string.textTraktImportComplete
  override val notificationErrorRes = R.string.textTraktImportError

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
    runners.addAll(listOf(importWatchedRunner, importWatchlistRunner))
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.d(tag, "Service initialized.")
    if (runners.any { it.isRunning }) {
      Log.d(tag, "Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(IMPORT_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Log.d(tag, "Import started.")
    launch {
      try {
        EventsManager.sendEvent(TraktImportStart)

        val resultCount = runImportWatched()
        runImportWatchlist(resultCount)

        EventsManager.sendEvent(TraktImportSuccess)
        notificationManager().notify(IMPORT_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
      } catch (t: Throwable) {
        if (t is TraktAuthError) EventsManager.sendEvent(TraktImportAuthError)
        EventsManager.sendEvent(TraktImportError)
        notificationManager().notify(IMPORT_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
        Crashlytics.logException(t)
      } finally {
        Log.d(tag, "Import completed.")
        notificationManager().cancel(IMPORT_NOTIFICATION_PROGRESS_ID)
        clear()
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private suspend fun runImportWatched(): Int {
    importWatchedRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val notification = createProgressNotification().run {
        setContentText("Processing \'${show.title}\'...")
        setProgress(total, progress, false)
      }
      notificationManager().notify(IMPORT_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktImportProgress)
    }
    return importWatchedRunner.run()
  }

  private suspend fun runImportWatchlist(totalProgress: Int) {
    importWatchlistRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val notification = createProgressNotification().run {
        setContentText("Processing \'${show.title}\'...")
        setProgress(totalProgress + total, totalProgress + progress, false)
      }
      notificationManager().notify(IMPORT_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktImportProgress)
    }
    importWatchlistRunner.run()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
