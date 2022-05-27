package com.michaldrabik.ui_base.trakt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.events.Event
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.events.TraktSyncError
import com.michaldrabik.ui_base.events.TraktSyncProgress
import com.michaldrabik.ui_base.events.TraktSyncStart
import com.michaldrabik.ui_base.events.TraktSyncSuccess
import com.michaldrabik.ui_base.trakt.exports.TraktExportListsRunner
import com.michaldrabik.ui_base.trakt.exports.TraktExportWatchedRunner
import com.michaldrabik.ui_base.trakt.exports.TraktExportWatchlistRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportListsRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportWatchedRunner
import com.michaldrabik.ui_base.trakt.imports.TraktImportWatchlistRunner
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class TraktSyncService : TraktNotificationsService() {

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
      isSilent: Boolean = false,
    ) = Intent(context, TraktSyncService::class.java).apply {
      putExtra(ARG_IS_IMPORT, isImport)
      putExtra(ARG_IS_EXPORT, isExport)
      putExtra(ARG_IS_SILENT, isSilent)
    }
  }

  private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
  private val runners = mutableListOf<TraktSyncRunner>()

  @Inject lateinit var settingsRepository: SettingsRepository
  @Inject lateinit var eventsManager: EventsManager
  @Inject lateinit var userManager: UserTraktManager
  @Inject lateinit var syncStatusProvider: TraktSyncStatusProvider

  @Inject lateinit var importWatchedRunner: TraktImportWatchedRunner
  @Inject lateinit var importWatchlistRunner: TraktImportWatchlistRunner
  @Inject lateinit var importListsRunner: TraktImportListsRunner

  @Inject lateinit var exportWatchedRunner: TraktExportWatchedRunner
  @Inject lateinit var exportWatchlistRunner: TraktExportWatchlistRunner
  @Inject lateinit var exportListsRunner: TraktExportListsRunner

  @Inject @Named("miscPreferences")
  lateinit var miscPreferences: SharedPreferences

  override fun onCreate() {
    super.onCreate()
    runners.addAll(
      arrayOf(
        importWatchedRunner,
        importWatchlistRunner,
        importListsRunner,
        exportWatchedRunner,
        exportWatchlistRunner,
        exportListsRunner
      )
    )
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Timber.d("Service initialized.")

    val isImport = intent?.extras?.getBoolean(ARG_IS_IMPORT) ?: false
    val isExport = intent?.extras?.getBoolean(ARG_IS_EXPORT) ?: false
    val isSilent = intent?.extras?.getBoolean(ARG_IS_SILENT) ?: false
    val theme = settingsRepository.theme

    if (runners.any { it.isRunning }) {
      Timber.d("Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification(theme).build())

    Timber.d("Sync started.")
    serviceScope.launch {
      try {
        syncStatusProvider.setSyncing(true)
        eventsManager.sendEvent(TraktSyncStart)

        if (isImport) {
          var resultCount = runImportWatched()
          resultCount += runImportWatchlist(resultCount)
          runImportLists(resultCount)
        }

        if (isExport) {
          runExportWatched()
          runExportWatchlist()
          runExportLists()
        }

        miscPreferences.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, nowUtcMillis()).apply()

        eventsManager.sendEvent(TraktSyncSuccess)
        syncStatusProvider.setSyncing(false)
        Analytics.logTraktFullSyncSuccess(isImport, isExport)
        if (!isSilent) {
          notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification(theme))
        }
      } catch (error: Throwable) {
        handleError(error, isSilent)
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
    val theme = settingsRepository.theme
    importWatchedRunner.progressListener = { title: String, progress: Int, total: Int ->
      val status = "Importing \'$title\'..."
      val notification = createProgressNotification(theme).run {
        setContentText(status)
        setProgress(total, progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      sendEvent(TraktSyncProgress(status))
    }
    return importWatchedRunner.run()
  }

  private suspend fun runImportWatchlist(totalProgress: Int): Int {
    val theme = settingsRepository.theme
    importWatchlistRunner.progressListener = { title: String, progress: Int, total: Int ->
      val status = "Importing \'$title\'..."
      val notification = createProgressNotification(theme).run {
        setContentText(status)
        setProgress(totalProgress + total, totalProgress + progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      sendEvent(TraktSyncProgress(status))
    }
    return importWatchlistRunner.run()
  }

  private suspend fun runImportLists(totalProgress: Int) {
    val theme = settingsRepository.theme
    importListsRunner.progressListener = { title: String, progress: Int, total: Int ->
      val status = "Importing \'$title\'..."
      val notification = createProgressNotification(theme).run {
        setContentText(status)
        setProgress(totalProgress + total, totalProgress + progress, false)
      }
      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
      sendEvent(TraktSyncProgress(status))
    }
    importListsRunner.run()
  }

  private suspend fun runExportWatched() {
    val status = "Exporting progress..."
    val theme = settingsRepository.theme
    val notification = createProgressNotification(theme).run {
      setContentText(status)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    eventsManager.sendEvent(TraktSyncProgress(status))
    exportWatchedRunner.run()
  }

  private suspend fun runExportWatchlist() {
    val status = "Exporting watchlist..."
    val theme = settingsRepository.theme
    val notification = createProgressNotification(theme).run {
      setContentText(status)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    eventsManager.sendEvent(TraktSyncProgress(status))
    exportWatchlistRunner.run()
  }

  private suspend fun runExportLists() {
    val status = "Exporting custom lists..."
    val theme = settingsRepository.theme
    val notification = createProgressNotification(theme).run {
      setContentText(status)
    }
    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
    eventsManager.sendEvent(TraktSyncProgress(status))
    exportListsRunner.run()
  }

  private suspend fun handleError(error: Throwable, isSilent: Boolean) {
    val showlyError = ErrorHelper.parse(error)
    if (showlyError is ShowlyError.UnauthorizedError) {
      eventsManager.sendEvent(TraktSyncAuthError)
      userManager.revokeToken()
    }
    eventsManager.sendEvent(TraktSyncError)
    syncStatusProvider.setSyncing(false)
    if (!isSilent) {
      val message =
        if (showlyError is ShowlyError.UnauthorizedError) R.string.errorTraktAuthorization
        else R.string.textTraktSyncErrorFull
      notificationManager().notify(
        SYNC_NOTIFICATION_COMPLETE_ERROR_ID,
        createErrorNotification(R.string.textTraktSyncError, message)
      )
    }
    Logger.record(error, "Source" to "${TraktSyncService::class.simpleName}")
  }

  private fun sendEvent(event: Event) {
    serviceScope.launch {
      eventsManager.sendEvent(event)
    }
  }

  private fun clear() {
    runners.forEach { it.isRunning = false }
  }

  override fun onDestroy() {
    serviceScope.cancel()
    importWatchedRunner.progressListener = null
    importWatchlistRunner.progressListener = null
    importListsRunner.progressListener = null
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
