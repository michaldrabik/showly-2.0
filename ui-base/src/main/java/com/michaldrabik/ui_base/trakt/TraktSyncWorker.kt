package com.michaldrabik.ui_base.trakt

import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy.KEEP
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.michaldrabik.ui_model.TraktSyncSchedule
import timber.log.Timber

class TraktSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

  companion object {
    private const val TAG = "TRAKT_SYNC_WORK"

    fun schedule(
      workManager: WorkManager,
      schedule: TraktSyncSchedule,
      cancelExisting: Boolean,
    ) {
      if (cancelExisting) {
        workManager.cancelUniqueWork(TAG)
      }

      if (schedule == TraktSyncSchedule.OFF) {
        cancelAll(workManager)
        Timber.i("Trakt sync scheduled: $schedule")
        return
      }

      val request = PeriodicWorkRequestBuilder<TraktSyncWorker>(schedule.duration, schedule.durationUnit)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setInitialDelay(schedule.duration, schedule.durationUnit)
        .addTag(TAG)
        .build()

      workManager.enqueueUniquePeriodicWork(TAG, KEEP, request)
      Timber.i("Trakt sync scheduled: $schedule")
    }

    fun cancelAll(workManager: WorkManager) {
      workManager.cancelUniqueWork(TAG)
    }
  }

  override fun doWork(): Result {
    TraktSyncService.createIntent(
      applicationContext,
      isImport = true,
      isExport = true,
      isSilent = true
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
