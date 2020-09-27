package com.michaldrabik.ui_base.events

import timber.log.Timber

/**
 * Very simple event bus to replace deprecated Android Local broadcasts manager.
 * At this point this bus does not care about threading at all which should not be an issue.
 */
object EventsManager {

  private val observers = mutableSetOf<EventObserver>()

  fun registerObserver(observer: EventObserver) {
    observers.add(observer)
    Timber.d("Events observer registered: $observer")
  }

  fun removeObserver(observer: EventObserver) {
    observers.remove(observer)
    Timber.d("Events observer removed: $observer")
  }

  fun sendEvent(event: Event) {
    observers.forEach { it.onNewEvent(event) }
    Timber.d("Event sent to ${observers.size} observers. $event")
  }
}
