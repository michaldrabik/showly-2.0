package com.michaldrabik.ui_base

import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.utilities.MessageEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

@Suppress("PropertyName")
open class BaseViewModel : ViewModel() {

  protected companion object {
    const val SUBSCRIBE_STOP_TIMEOUT = 3000L
  }

  protected val _messageState = MutableSharedFlow<MessageEvent>()
  val messageState = _messageState.asSharedFlow()

  protected fun rethrowCancellation(error: Throwable) {
    if (error is CancellationException) {
      Timber.d("Rethrowing CancellationException")
      throw error
    } else {
      Timber.e(error)
    }
  }
}
