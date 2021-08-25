package com.michaldrabik.ui_base.utilities

import androidx.annotation.StringRes

class MessageEvent(
  @StringRes private val messageResId: Int,
  val type: Type,
  val indefinite: Boolean = false,
) {

  companion object {
    fun info(@StringRes messageResId: Int, indefinite: Boolean = false) = MessageEvent(messageResId, Type.INFO, indefinite)
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

  fun peek(): Int = messageResId

  enum class Type {
    INFO,
    ERROR
  }
}
