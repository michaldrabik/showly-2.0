package com.michaldrabik.ui_base.trakt

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.R
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
import com.michaldrabik.ui_model.TraktSyncSchedule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import javax.inject.Named

@HiltWorker
class TraktSyncWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val importWatchedRunner: TraktImportWatchedRunner,
  private val importWatchlistRunner: TraktImportWatchlistRunner,
  private val importListsRunner: TraktImportListsRunner,
  private val exportWatchedRunner: TraktExportWatchedRunner,
  private val exportWatchlistRunner: TraktExportWatchlistRunner,
  private val exportListsRunner: TraktExportListsRunner,
  private val settingsRepository: SettingsRepository,
  private val eventsManager: EventsManager,
  private val userManager: UserTraktManager,
  @Named("miscPreferences") private val miscPreferences: SharedPreferences,
) : TraktNotificationWorker(context, workerParams) {

  companion object {
    const val TAG_ID = "TRAKT_SYNC_WORK_ID"
    private const val TAG = "TRAKT_SYNC_WORK"
    private const val TAG_ONE_OFF = "TRAKT_SYNC_WORK_ONE_OFF"

    private const val SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID = 827
    private const val SYNC_NOTIFICATION_COMPLETE_PROGRESS_ID = 823
    private const val SYNC_NOTIFICATION_COMPLETE_ERROR_ID = 828
    private const val SYNC_NOTIFICATION_COMPLETE_ERROR_LISTS_ID = 832

    const val KEY_LAST_SYNC_TIMESTAMP = "KEY_LAST_SYNC_TIMESTAMP"
    private const val ARG_IS_IMPORT = "ARG_IS_IMPORT"
    private const val ARG_IS_EXPORT = "ARG_IS_EXPORT"
    private const val ARG_IS_SILENT = "ARG_IS_SILENT"

    private const val TRAKT_LISTS_INFO_URL = "https://twitter.com/trakt/status/1536751362943332352?s=20&t=bdlxpzlDIclkLqdihaAXqw"

    fun scheduleOneOff(
      workManager: WorkManager,
      isImport: Boolean,
      isExport: Boolean,
      isSilent: Boolean,
    ) {
      val inputData = workDataOf(
        ARG_IS_IMPORT to isImport,
        ARG_IS_EXPORT to isExport,
        ARG_IS_SILENT to isSilent
      )

      val request = OneTimeWorkRequestBuilder<TraktSyncWorker>()
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build()
        )
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setInputData(inputData)
        .addTag(TAG_ID)
        .addTag(TAG_ONE_OFF)
        .build()

      workManager.enqueueUniqueWork(TAG_ONE_OFF, ExistingWorkPolicy.KEEP, request)
    }

    fun schedulePeriodic(
      workManager: WorkManager,
      schedule: TraktSyncSchedule,
      cancelExisting: Boolean,
    ) {
      if (cancelExisting) {
        workManager.cancelUniqueWork(TAG)
      }

      if (schedule == TraktSyncSchedule.OFF) {
        cancelAllPeriodic(workManager)
        Timber.i("Trakt sync scheduled: $schedule")
        return
      }

      val inputData = workDataOf(
        ARG_IS_IMPORT to true,
        ARG_IS_EXPORT to true,
        ARG_IS_SILENT to true
      )

      val request = PeriodicWorkRequestBuilder<TraktSyncWorker>(schedule.duration, schedule.durationUnit)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setInputData(inputData)
        .setInitialDelay(schedule.duration, schedule.durationUnit)
        .addTag(TAG_ID)
        .addTag(TAG)
        .build()

      workManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP, request)
      Timber.i("Trakt sync scheduled: $schedule")
    }

    fun cancelAllPeriodic(workManager: WorkManager) {
      workManager.cancelUniqueWork(TAG)
    }
  }

  override suspend fun doWork(): Result {
    val isImport = inputData.getBoolean(ARG_IS_IMPORT, false)
    val isExport = inputData.getBoolean(ARG_IS_EXPORT, false)
    val isSilent = inputData.getBoolean(ARG_IS_SILENT, false)
    val theme = settingsRepository.theme

    try {
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
      Analytics.logTraktFullSyncSuccess(isImport, isExport)
      if (!isSilent) {
        notificationManager().notify(SYNC_NOTIFICATION_COMPLETE_SUCCESS_ID, createSuccessNotification(theme))
      }
      return Result.success()
    } catch (error: Throwable) {
      handleError(error, isSilent)
      return Result.failure()
    } finally {
      clearRunners()
      notificationManager().cancel(SYNC_NOTIFICATION_COMPLETE_PROGRESS_ID)
    }
  }

  override suspend fun getForegroundInfo(): ForegroundInfo {
    val theme = settingsRepository.theme
    val notification = createProgressNotification(theme, null, 0, 0, true)
    return ForegroundInfo(SYNC_NOTIFICATION_COMPLETE_PROGRESS_ID, notification)
  }

  private suspend fun runImportWatched(): Int {
    val theme = settingsRepository.theme
    importWatchedRunner.progressListener = { title: String, progress: Int, total: Int ->
      val status = "Importing \'$title\'..."
      setProgressNotification(theme, status, total, progress, false)
      eventsManager.sendEvent(TraktSyncProgress(status))
    }
    return importWatchedRunner.run()
  }

  private suspend fun runImportWatchlist(totalProgress: Int): Int {
    val theme = settingsRepository.theme
    importWatchlistRunner.progressListener = { title: String, progress: Int, total: Int ->
      val status = "Importing \'$title\'..."
      setProgressNotification(theme, status, totalProgress + total, totalProgress + progress, false)
      eventsManager.sendEvent(TraktSyncProgress(status))
    }
    return importWatchlistRunner.run()
  }

  private suspend fun runImportLists(totalProgress: Int) {
    val theme = settingsRepository.theme
    importListsRunner.progressListener = { title: String, progress: Int, total: Int ->
      val status = "Importing \'$title\'..."
      setProgressNotification(theme, status, totalProgress + total, totalProgress + progress, false)
      eventsManager.sendEvent(TraktSyncProgress(status))
    }
    importListsRunner.run()
  }

  private suspend fun runExportWatched() {
    val status = "Exporting progress..."
    val theme = settingsRepository.theme
    setProgressNotification(theme, status, 0, 0, true)
    eventsManager.sendEvent(TraktSyncProgress(status))
    exportWatchedRunner.run()
  }

  private suspend fun runExportWatchlist() {
    val status = "Exporting watchlist..."
    val theme = settingsRepository.theme
    setProgressNotification(theme, status, 0, 0, true)
    eventsManager.sendEvent(TraktSyncProgress(status))
    try {
      exportWatchlistRunner.run()
    } catch (error: Throwable) {
      handleListsError(error, R.string.errorTraktSyncWatchlistLimitsReached)
    }
  }

  private suspend fun runExportLists() {
    val status = "Exporting custom lists..."
    val theme = settingsRepository.theme
    setProgressNotification(theme, status, 0, 0, true)
    eventsManager.sendEvent(TraktSyncProgress(status))
    try {
      exportListsRunner.run()
    } catch (error: Throwable) {
      handleListsError(error, R.string.errorTraktSyncListsLimitsReached)
    }
  }

  private fun setProgressNotification(
    theme: Int,
    content: String?,
    maxProgress: Int,
    progress: Int,
    isIntermediate: Boolean,
  ) {
    notificationManager().notify(
      SYNC_NOTIFICATION_COMPLETE_PROGRESS_ID,
      createProgressNotification(theme, content, maxProgress, progress, isIntermediate)
    )
  }

  private suspend fun handleError(error: Throwable, isSilent: Boolean) {
    val showlyError = ErrorHelper.parse(error)
    if (showlyError is ShowlyError.UnauthorizedError) {
      eventsManager.sendEvent(TraktSyncAuthError)
      userManager.revokeToken()
    } else {
      eventsManager.sendEvent(TraktSyncError)
    }
    if (!isSilent) {
      val message =
        if (showlyError is ShowlyError.UnauthorizedError) R.string.errorTraktAuthorization
        else R.string.textTraktSyncErrorFull

      val theme = settingsRepository.theme
      notificationManager().notify(
        SYNC_NOTIFICATION_COMPLETE_ERROR_ID,
        createErrorNotification(theme, R.string.textTraktSyncError, message)
      )
    }
    Logger.record(error, "TraktSyncWorker::handleError()")
  }

  private fun handleListsError(
    error: Throwable,
    @StringRes notificationMessageResId: Int,
  ) {
    when (ErrorHelper.parse(error)) {
      ShowlyError.AccountLimitsError -> {
        val theme = settingsRepository.theme

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TRAKT_LISTS_INFO_URL))
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        val action = NotificationCompat.Action(R.drawable.ic_info, "More Info", pendingIntent)

        notificationManager().notify(
          SYNC_NOTIFICATION_COMPLETE_ERROR_LISTS_ID,
          createErrorNotification(theme, R.string.textTraktSync, notificationMessageResId, action)
        )
      }

      else -> throw error
    }
  }

  private fun clearRunners() {
    arrayOf(
      importWatchedRunner,
      importWatchedRunner,
      importWatchlistRunner,
      importListsRunner,
      exportWatchedRunner,
      exportWatchlistRunner,
      exportListsRunner,
    ).forEach {
      it.progressListener = null
    }
  }
}
