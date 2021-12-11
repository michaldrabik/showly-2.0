package com.michaldrabik.ui_base

import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.utilities.MessageEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber

@Suppress("PropertyName")
open class BaseViewModel : ViewModel() {

  protected companion object {
    const val SUBSCRIBE_STOP_TIMEOUT = 5000L
  }

  protected val _messageChannel = Channel<MessageEvent>(Channel.BUFFERED)
  val messageChannel = _messageChannel.receiveAsFlow()

  protected fun rethrowCancellation(error: Throwable) {
    if (error is CancellationException) {
      Timber.d("Rethrowing CancellationException")
      throw error
    } else {
      Timber.e(error)
    }
  }
}
