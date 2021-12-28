package com.michaldrabik.ui_base.utilities

open class Event<T>(
  private val action: T,
) {

  private var isConsumed: Boolean = false

  fun peek(): T? = action

  fun consume(): T? =
    if (!isConsumed) {
      isConsumed = true
      action
    } else {
      null
    }
}
