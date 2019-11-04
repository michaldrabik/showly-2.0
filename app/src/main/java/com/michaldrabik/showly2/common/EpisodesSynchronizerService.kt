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

class EpisodesSynchronizerService : JobIntentService(), CoroutineScope {

  companion object {
    const val ACTION_SYNC_SUCCESS = "ACTION_SYNC_SUCCESS"

    private const val TAG = "EpisodesSyncService"
    private const val JOB_ID = 999

    fun initialize(context: Context) {
      enqueueWork(context, EpisodesSynchronizerService::class.java, JOB_ID, Intent())
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject
  lateinit var synchronizer: EpisodesSynchronizer

  override fun onHandleWork(intent: Intent) {
    Log.i(TAG, "Sync service initialized")
    appComponent().inject(this)
    runBlocking {
      try {
        synchronizer.synchronize()
        notifySuccess()
      } catch (t: Throwable) {
        Log.e(TAG, t.toString())
      }
    }
  }

  private fun notifySuccess() {
    val i = Intent(ACTION_SYNC_SUCCESS)
    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(i)
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Log.i(TAG, "Sync service destroyed")
    super.onDestroy()
  }
}