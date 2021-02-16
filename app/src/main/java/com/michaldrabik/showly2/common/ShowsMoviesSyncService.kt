package com.michaldrabik.showly2.common

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.michaldrabik.showly2.common.movies.MoviesSyncRunner
import com.michaldrabik.showly2.common.shows.ShowsSyncRunner
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.ShowsMoviesSyncComplete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class ShowsMoviesSyncService : JobIntentService(), CoroutineScope {

  companion object {
    private const val JOB_ID = 999

    fun initialize(context: Context) {
      enqueueWork(context, ShowsMoviesSyncService::class.java, JOB_ID, Intent())
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject lateinit var showsSyncRunner: ShowsSyncRunner
  @Inject lateinit var moviesSyncRunner: MoviesSyncRunner

  override fun onHandleWork(intent: Intent) {
    Timber.d("Sync service initialized")
    serviceComponent().inject(this)
    var syncCount = 0
    runBlocking {
      try {
        syncCount += showsSyncRunner.run()
      } catch (t: Throwable) {
        Logger.record(t, "Source" to "ShowsSyncRunner")
      }
      try {
        syncCount += moviesSyncRunner.run()
      } catch (t: Throwable) {
        Logger.record(t, "Source" to "MoviesSyncRunner")
      }
    }

    if (syncCount > 0) {
      EventsManager.sendEvent(ShowsMoviesSyncComplete)
    }
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Timber.d("Sync service destroyed")
    super.onDestroy()
  }
}
