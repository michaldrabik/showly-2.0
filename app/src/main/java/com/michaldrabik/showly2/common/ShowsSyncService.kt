package com.michaldrabik.showly2.common

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.crashlytics.android.Crashlytics
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.common.events.EventsManager
import com.michaldrabik.showly2.common.events.ShowsSyncComplete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ShowsSyncService : JobIntentService(), CoroutineScope {

  companion object {
    private const val TAG = "ShowsSyncService"
    private const val JOB_ID = 999

    fun initialize(context: Context) {
      enqueueWork(context, ShowsSyncService::class.java, JOB_ID, Intent())
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject
  lateinit var showsSyncRunner: ShowsSyncRunner

  override fun onHandleWork(intent: Intent) {
    Log.i(TAG, "Sync service initialized")
    appComponent().inject(this)
    val syncCount = runBlocking {
      try {
        showsSyncRunner.synchronize()
      } catch (t: Throwable) {
        Crashlytics.logException(t)
        Log.e(TAG, t.toString())
      }
    }
    if (syncCount > 0) notifyComplete()
  }

  private fun notifyComplete() = EventsManager.sendEvent(ShowsSyncComplete)

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Log.i(TAG, "Sync service destroyed")
    super.onDestroy()
  }
}
