package com.michaldrabik.ui_base

import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.utilities.MessageEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

@Suppress("PropertyName")
open class BaseViewModel2 : ViewModel() {

  val _messageState = MutableSharedFlow<MessageEvent>()
  val messageState = _messageState.asSharedFlow()

  protected fun rethrowCancellation(t: Throwable) {
    if (t is CancellationException) {
      Timber.d("Rethrowing CancellationException")
      throw t
    }
  }
}
