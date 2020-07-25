package com.michaldrabik.showly2.common.trakt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.TraktSyncAuthError
import com.michaldrabik.showly2.common.events.TraktSyncError
import com.michaldrabik.showly2.common.events.TraktSyncProgress
import com.michaldrabik.showly2.common.events.TraktSyncStart
import com.michaldrabik.showly2.common.events.TraktSyncSuccess
import com.michaldrabik.showly2.common.trakt.exports.TraktExportWatchedRunner
import com.michaldrabik.showly2.common.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.showly2.common.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.showly2.model.error.TraktAuthError
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.showly2.utilities.extensions.notificationManager
import com.michaldrabik.showly2.utilities.extensions.nowUtcMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class TraktSyncService : TraktNotificationsService(), CoroutineScope {

  companion object {
    const val KEY_LAST_SYNC_TIMESTAMP = "KEY_LAST_SYNC_TIMESTAMP"

    private const val ARG_IS_IMPORT = "ARG_IS_IMPORT"
    private const val ARG_IS_EXPORT = "ARG_IS_EXPORT"
    private const val ARG_IS_SILENT = "ARG_IS_SILENT"

    private const val SYNC_NOTIFICATION_PROGRESS_ID = 826
    private const val SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID = 827
    private const val SYNC_NOTIFICATION_COMPLETE_ERROR_ID = 828

    fun createIntent(
      context: Context,
      isImport: Boolean,
      isExport: Boolean,
      isSilent: Boolean = false
    ) = Intent(context, TraktSyncService::class.java).apply {
      putExtra(ARG_IS_IMPORT, isImport)
      putExtra(ARG_IS_EXPORT, isExport)
      putExtra(ARG_IS_SILENT, isSilent)
    }
  }

  override val coroutineContext = Job() + Dispatchers.IO
  private val runners = mutableListOf<TraktSyncRunner>()

  @Inject
  lateinit var importWatchedRunner: TraktImportWatchedRunner

  @Inject
  lateinit var importWatchlistRunner: TraktImportWatchlistRunner

  @Inject
  lateinit var exportWatchedRunner: TraktExportWatchedRunner

  @Inject
  lateinit var exportWatchlistRunner: TraktExportWatchlistRunner

  @Inject
  @Named("miscPreferences")
  lateinit var miscPreferences: SharedPreferences

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
    runners.addAll(
      arrayOf(importWatchedRunner, importWatchlistRunner, exportWatchedRunner, exportWatchlistRunner)
    )
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Timber.d("Service initialized.")

    val isImport = intent?.extras?.getBoolean(ARG_IS_IMPORT) ?: false
    val isExport = intent?.extras?.getBoolean(ARG_IS_EXPORT) ?: false
    val isSilent = intent?.extras?.getBoolean(ARG_IS_SILENT) ?: false

    if (runners.any { it.isRunning }) {
      Timber.d("Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Timber.d("Sync started.")
    launch {
      try {
        EventsManager.sendEvent(TraktSyncStart)

        if (isImport) {
          val resultCount = runImportWatched()
          runImportWatchlist(resultCount)
        }

        if (isExport) {
          runExportWatched()
          runExportWatchlist()
        }

        miscPreferences.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, nowUtcMillis()).apply()

        EventsManager.sendEvent(TraktSyncSuccess)
        if (!isSilent) {
          notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
        }
      } catch (t: Throwable) {
        if (t is TraktAuthError) EventsManager.sendEvent(TraktSyncAuthError)
        EventsManager.sendEvent(TraktSyncError)
        if (!isSilent) {
          notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
        }
        FirebaseCrashlytics.getInstance().recordException(t)
      } finally {
        Timber.d("Sync completed.")
        notificationManager().cancel(SYNC_NOTIFICATION_PROGRESS_ID)
        clear()
        stopSelf()
      }
    }
    return START_NOT_STICKY
  }

  private suspend fun runImportWatched(): Int {
    importWatchedRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val status = "Importing \'${show.title}\'..."
      val notification = createProgressNotification().run {
        setContentText(status)
        setProgress(total, progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktSyncProgress(status))
    }
    return importWatchedRunner.run()
  }

  private suspend fun runImportWatchlist(totalProgress: Int) {
    importWatchlistRunner.progressListener = { show: Show, progress: Int, total: Int ->
      val status = "Importing \'${show.title}\'..."
      val notification = createProgressNotification().run {
        setContentText(status)
        setProgress(totalProgress + total, totalProgress + progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      EventsManager.sendEvent(TraktSyncProgress(status))
    }
    importWatchlistRunner.run()
  }

  private suspend fun runExportWatched() {
    val status = "Exporting progress..."
    val notification = createProgressNotification().run {
      setContentText(status)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    EventsManager.sendEvent(TraktSyncProgress(status))
    exportWatchedRunner.run()
  }

  private suspend fun runExportWatchlist() {
    val status = "Exporting watchlist..."
    val notification = createProgressNotification().run {
      setContentText(status)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    EventsManager.sendEvent(TraktSyncProgress(status))
    exportWatchlistRunner.run()
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }

  private fun clear() = runners.forEach { it.isRunning = false }

  override fun onBind(intent: Intent?): IBinder? = null
}
