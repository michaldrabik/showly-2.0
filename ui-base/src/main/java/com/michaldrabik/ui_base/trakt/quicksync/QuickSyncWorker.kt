package com.michaldrabik.ui_base.trakt.quicksync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError.AccountLimitsError
import com.michaldrabik.common.errors.ShowlyError.UnauthorizedError
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TraktQuickSyncSuccess
import com.michaldrabik.ui_base.events.TraktSyncAuthError
import com.michaldrabik.ui_base.trakt.TraktNotificationWorker
import com.michaldrabik.ui_base.trakt.quicksync.runners.QuickSyncListsRunner
import com.michaldrabik.ui_base.trakt.quicksync.runners.QuickSyncRunner
import com.michaldrabik.ui_base.utilities.extensions.notificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit.SECONDS

@HiltWorker
class QuickSyncWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val quickSyncRunner: QuickSyncRunner,
  private val quickSyncListsRunner: QuickSyncListsRunner,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val eventsManager: EventsManager,
) : TraktNotificationWorker(context, workerParams) {

  companion object {
    private const val TAG = "TRAKT_QUICK_SYNC_WORK"
    private const val SYNC_NOTIFICATION_PROGRESS_ID = 916
    private const val SYNC_NOTIFICATION_ERROR_ID = 917

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

  override suspend fun doWork(): Result {
    Timber.d("Initialized.")
    val theme = settingsRepository.theme

    notificationManager().notify(
      SYNC_NOTIFICATION_PROGRESS_ID,
      createProgressNotification(theme, null, 0, 0, true)
    )

    try {
      var count = quickSyncRunner.run()
      count += quickSyncListsRunner.run()
      if (count > 0) {
        eventsManager.sendEvent(TraktQuickSyncSuccess(count))
        Analytics.logTraktQuickSyncSuccess(count)
      }
    } catch (error: Throwable) {
      handleError(error)
    } finally {
      clearRunners()
      notificationManager().cancel(SYNC_NOTIFICATION_PROGRESS_ID)
      Timber.d("Quick Sync completed.")
    }

    return Result.success()
  }

  private suspend fun handleError(error: Throwable) {
    val showlyError = ErrorHelper.parse(error)
    if (showlyError is UnauthorizedError) {
      eventsManager.sendEvent(TraktSyncAuthError)
      userManager.revokeToken()
    }
    val notificationMessage = when (showlyError) {
      is AccountLimitsError -> R.string.errorAccountListsLimitsReached
      is UnauthorizedError -> R.string.errorTraktAuthorization
      else -> R.string.textTraktQuickSyncErrorFull
    }
    val theme = settingsRepository.theme
    applicationContext.notificationManager().notify(
      SYNC_NOTIFICATION_ERROR_ID,
      createErrorNotification(theme, R.string.textTraktQuickSyncError, notificationMessage)
    )
    Logger.record(error, "QuickSyncWorker::handleError()")
  }

  private fun clearRunners() {
    arrayOf(
      quickSyncRunner,
      quickSyncListsRunner,
    ).forEach {
      it.progressListener = null
    }
  }
}
