package com.michaldrabik.ui_base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.utilities.MessageEvent
import kotlinx.coroutines.CancellationException
import timber.log.Timber

@Suppress("PropertyName")
open class BaseViewModel2 : ViewModel() {

  protected val _messageLiveData = MutableLiveData<MessageEvent>()
  val messageLiveData: LiveData<MessageEvent> get() = _messageLiveData

  protected fun rethrowCancellation(t: Throwable) {
    if (t is CancellationException) {
      Timber.d("Rethrowing CancellationException")
      throw t
    }
  }
}
