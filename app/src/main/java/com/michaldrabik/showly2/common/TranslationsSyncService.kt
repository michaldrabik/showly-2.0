package com.michaldrabik.showly2.common

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.michaldrabik.showly2.serviceComponent
import com.michaldrabik.ui_base.events.EventsManager
import com.michaldrabik.ui_base.events.TranslationsSyncComplete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class TranslationsSyncService : JobIntentService(), CoroutineScope {

  companion object {
    private const val JOB_ID = 888

    fun initialize(context: Context) {
      enqueueWork(context, TranslationsSyncService::class.java, JOB_ID, Intent())
    }
  }

  override val coroutineContext = Job() + Dispatchers.Main

  @Inject
  lateinit var syncRunner: TranslationsSyncRunner

  override fun onHandleWork(intent: Intent) {
    Timber.d("Sync service initialized")
    serviceComponent().inject(this)
    val syncCount = runBlocking {
      try {
        syncRunner.run()
      } catch (t: Throwable) {
        Timber.e(t.toString())
        val exception = Throwable(TranslationsSyncService::class.simpleName, t)
        FirebaseCrashlytics.getInstance().recordException(exception)
        0
      }
    }

    if (syncCount > 0) EventsManager.sendEvent(TranslationsSyncComplete)
  }

  override fun onDestroy() {
    coroutineContext.cancelChildren()
    Timber.d("Sync service destroyed")
    super.onDestroy()
  }
}
