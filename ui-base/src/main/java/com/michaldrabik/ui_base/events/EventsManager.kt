package com.michaldrabik.ui_base.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsManager @Inject constructor() {

  private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 10)
  val events = _events.asSharedFlow()

  suspend fun sendEvent(event: Event) {
    _events.emit(event)
    Timber.d("Event emitted: $event")
  }
}
