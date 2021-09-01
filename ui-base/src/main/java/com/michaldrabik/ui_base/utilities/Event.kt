package com.michaldrabik.ui_base.utilities

class Event<T>(
  private val action: T,
) {

  private var isConsumed: Boolean = false

  fun consume(): T? =
    if (!isConsumed) {
      isConsumed = true
      action
    } else {
      null
    }
}
