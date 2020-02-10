package com.michaldrabik.showly2.common.events

/**
 * Very simple event bus to replace deprecated Android Local broadcasts manager.
 * At this point this bus does not care about threading at all which should not be an issue.
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
