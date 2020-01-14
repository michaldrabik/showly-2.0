package com.michaldrabik.showly2.common.events

/**
 * Simple event bus to replace deprecated Android Local broadcasts manager.
 */
object EventsManager {

  private val observers = mutableSetOf<EventObserver>()

  fun registerObserver(observer: EventObserver) {
    observers.add(observer)
  }

  fun removeObserver(observer: EventObserver) {
    observers.remove(observer)
  }

  fun sendEvent(event: Event) {
    observers.forEach { it.onNewEvent(event) }
  }
}
