package com.michaldrabik.ui_base

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

object Logger {

  fun record(error: Throwable, key: Pair<String, String>? = null) {
    Timber.e(error)
    if (error is CancellationException) {
      return
    }
    FirebaseCrashlytics.getInstance().run {
      key?.let { setCustomKey(it.first, it.second) }
      recordException(error)
    }
  }
}
