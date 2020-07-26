package com.michaldrabik.showly2.common.trakt.quicksync

import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.showly2.common.trakt.TraktNotificationsService
import com.michaldrabik.showly2.serviceComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class QuickSyncService : TraktNotificationsService(), CoroutineScope {

  companion object {
    private const val SYNC_NOTIFICATION_PROGRESS_ID = 916

    fun createIntent(context: Context) = Intent(context, QuickSyncService::class.java)
  }

  override val coroutineContext = Job() + Dispatchers.IO

  @Inject
  lateinit var quickSyncRunner: QuickSyncRunner

  override fun onCreate() {
    super.onCreate()
    serviceComponent().inject(this)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Timber.d("Service initialized.")

    if (quickSyncRunner.isRunning) {
      Timber.d("Already running. Skipping...")
      return START_NOT_STICKY
    }
    startForeground(SYNC_NOTIFICATION_PROGRESS_ID, createProgressNotification().build())

    Timber.d("Sync started.")
    launch {
      try {
//        EventsManager.sendEvent(TraktSyncStart)

        quickSyncRunner.run()

//        EventsManager.sendEvent(TraktSyncSuccess)
//        if (!isSilent) {
//          notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification())
//        }
      } catch (t: Throwable) {
//        if (t is TraktAuthError) EventsManager.sendEvent(TraktSyncAuthError)
//        EventsManager.sendEvent(TraktSyncError)
//        if (!isSilent) {
//          notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_ERROR_ID, createErrorNotification())
//        }
        FirebaseCrashlytics.getInstance().recordException(t)
      } finally {
        Timber.d("Quick Sync completed.")
//        notificationManager().cancel(SYNC_NOTIFICATION_PROGRESS_ID)
        clear()
        stopSelf()
      }
    }

    return START_NOT_STICKY
  }

//  private suspend fun runImportWatched(): Int {
//    importWatchedRunner.progressListener = { show: Show, progress: Int, total: Int ->
//      val status = "Importing \'${show.title}\'..."
//      val notification = createProgressNotification().run {
//        setContentText(status)
//        setProgress(total, progress, false)
//      }
//      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
//      EventsManager.sendEvent(TraktSyncProgress(status))
//    }
//    return importWatchedRunner.run()
//  }
//
//  private suspend fun runImportWatchlist(totalProgress: Int) {
//    importWatchlistRunner.progressListener = { show: Show, progress: Int, total: Int ->
//      val status = "Importing \'${show.title}\'..."
//      val notification = createProgressNotification().run {
//        setContentText(status)
//        setProgress(totalProgress + total, totalProgress + progress, false)
//      }
//      notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
//      EventsManager.sendEvent(TraktSyncProgress(status))
//    }
//    importWatchlistRunner.run()
//  }
//
//  private suspend fun runExportWatched() {
//    val status = "Exporting progress..."
//    val notification = createProgressNotification().run {
//      setContentText(status)
//    }
//    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
//    EventsManager.sendEvent(TraktSyncProgress(status))
//    exportWatchedRunner.run()
//  }
//
//  private suspend fun runExportWatchlist() {
//    val status = "Exporting watchlist..."
//    val notification = createProgressNotification().run {
//      setContentText(status)
//    }
//    notificationManager().notify(SYNC_NOTIFICATION_PROGRESS_ID, notification.build())
//    EventsManager.sendEvent(TraktSyncProgress(status))
//    exportWatchlistRunner.run()
//  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    super.onDestroy()
  }

  private fun clear() {
    quickSyncRunner.isRunning = false
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
