package com.michaldrabik.showly2.utilities

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {

  private val pending = AtomicBoolean(false)

  @MainThread
  override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
    if (hasActiveObservers()) {
      Timber.w("Multiple observers registered but only one will be notified of changes.")
    }

    // Observe the internal MutableLiveData
    super.observe(owner, Observer<T> { t ->
      if (pending.compareAndSet(true, false)) {
        observer.onChanged(t)
      }
    })
  }

  @MainThread
  override fun setValue(t: T?) {
    pending.set(true)
    super.setValue(t)
  }
}
