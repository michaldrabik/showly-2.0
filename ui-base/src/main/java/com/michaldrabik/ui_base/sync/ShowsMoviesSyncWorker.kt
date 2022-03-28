package com.michaldrabik.ui_base.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.KEEP
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.ShowsMoviesSyncComplete
import com.michaldrabik.ui_base.sync.runners.MoviesSyncRunner
import com.michaldrabik.ui_base.sync.runners.ShowsSyncRunner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class ShowsMoviesSyncWorker @AssistedInject constructor(
  @Assisted val context: Context,
  @Assisted workerParams: WorkerParameters,
  private val showsSyncRunner: ShowsSyncRunner,
  private val moviesSyncRunner: MoviesSyncRunner,
  private val eventsManager: EventsManager
) : CoroutineWorker(context, workerParams) {

  companion object {
    private const val TAG = "ShowsMoviesSyncWorker"

    fun schedule(workManager: WorkManager) {
      val request = OneTimeWorkRequestBuilder<ShowsMoviesSyncWorker>()
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .addTag(TAG)
        .build()

      workManager.enqueueUniqueWork(TAG, KEEP, request)
      Timber.i("ShowsMoviesSyncWorker scheduled.")
    }
  }

  override suspend fun doWork() = withContext(Dispatchers.IO) {
    Timber.d("Doing work...")

    val showsAsync = async {
      try {
        Timber.d("Starting shows runner...")
        showsSyncRunner.run()
      } catch (error: Throwable) {
        Timber.e(error)
        0
      }
    }

    val moviesAsync = async {
      try {
        Timber.d("Starting movies runner...")
        moviesSyncRunner.run()
      } catch (error: Throwable) {
        Timber.e(error)
        0
      }
    }

    val (showsCount, moviesCount) = awaitAll(showsAsync, moviesAsync)
    eventsManager.sendEvent(ShowsMoviesSyncComplete(showsCount + moviesCount))

    Timber.d("Work finished. Shows: $showsCount Movies: $moviesCount")
    Result.success()
  }
}
