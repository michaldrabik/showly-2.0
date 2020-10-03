package com.michaldrabik.ui_show.helpers

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
