package com.michaldrabik.showly2.common.events

interface EventObserver {
  fun onNewEvent(event: Event)
}
