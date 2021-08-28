package com.michaldrabik.ui_base.trakt.quicksync

import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber
import java.util.concurrent.TimeUnit.SECONDS

class QuickSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    private const val TAG = "TRAKT_QUICK_SYNC_WORK"

    fun schedule(workManager: WorkManager) {
      val request = OneTimeWorkRequestBuilder<QuickSyncWorker>()
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setInitialDelay(3, SECONDS)
        .addTag(TAG)
        .build()

      workManager.enqueueUniqueWork(TAG, REPLACE, request)
      Timber.i("Trakt QuickSync scheduled.")
    }
  }

  override fun doWork(): Result {
    QuickSyncService.createIntent(
      applicationContext
    ).run {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        applicationContext.startForegroundService(this)
      } else {
        applicationContext.startService(this)
      }
    }
    return Result.success()
  }
}
