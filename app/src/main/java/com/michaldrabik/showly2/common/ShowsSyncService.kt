package com.michaldrabik.showly2.common

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.crashlytics.android.Crashlytics
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.ShowsSyncComplete
import com.michaldrabik.showly2.serviceComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class ShowsSyncService : JobIntentService(), CoroutineScope {

  companion object {
    private const val JOB_ID = 999

    fun initialize(context: Context) {
      enqueueWork(context, ShowsSyncService::class.java, JOB_ID, Intent())
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject
  lateinit var showsSyncRunner: ShowsSyncRunner

  override fun onHandleWork(intent: Intent) {
    Timber.d("Sync service initialized")
    serviceComponent().inject(this)
    val syncCount = runBlocking {
      try {
        showsSyncRunner.run()
      } catch (t: Throwable) {
        Timber.e(t.toString())
        Crashlytics.logException(t)
        0
      }
    }
    if (syncCount > 0) notifyComplete()
  }

  private fun notifyComplete() = EventsManager.sendEvent(ShowsSyncComplete)

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Timber.d("Sync service destroyed")
    super.onDestroy()
  }
}
