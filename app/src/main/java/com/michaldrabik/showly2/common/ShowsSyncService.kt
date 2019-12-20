package com.michaldrabik.showly2.common

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.michaldrabik.showly2.appComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ShowsSyncService : JobIntentService(), CoroutineScope {

  companion object {
    const val ACTION_SHOWS_SYNC_FINISHED = "ACTION_SHOWS_SYNC_FINISHED"

    private const val TAG = "ShowsSyncService"
    private const val JOB_ID = 999

    fun initialize(context: Context) {
      enqueueWork(context, ShowsSyncService::class.java, JOB_ID, Intent())
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject
  lateinit var synchronizer: ShowsSynchronizer

  override fun onHandleWork(intent: Intent) {
    Log.i(TAG, "Sync service initialized")
    appComponent().inject(this)
    val syncCount = runBlocking {
      try {
        synchronizer.synchronize()
      } catch (t: Throwable) {
        Log.e(TAG, t.toString())
      }
    }
    if (syncCount > 0) notifySuccess()
  }

  private fun notifySuccess() {
    val i = Intent(ACTION_SHOWS_SYNC_FINISHED)
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Log.i(TAG, "Sync service destroyed")
    super.onDestroy()
  }
}