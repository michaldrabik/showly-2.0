package com.michaldrabik.showly2.utilities

class ActionEvent<T>(
  val action: T
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
