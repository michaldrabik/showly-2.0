package com.michaldrabik.ui_base.utilities

import androidx.annotation.StringRes

class MessageEvent(
  @StringRes private val messageResId: Int,
  val type: Type
) {

  companion object {
    fun info(@StringRes messageResId: Int) = MessageEvent(messageResId, Type.INFO)
    fun error(@StringRes messageResId: Int) = MessageEvent(messageResId, Type.ERROR)
  }

  private var isConsumed: Boolean = false

  fun consume(): Int? =
    if (!isConsumed) {
      isConsumed = true
      messageResId
    } else {
      null
    }

  enum class Type {
    INFO,
    ERROR
  }
}
