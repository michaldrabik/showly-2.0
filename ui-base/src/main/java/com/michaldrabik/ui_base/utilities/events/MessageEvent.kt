package com.michaldrabik.ui_base.utilities.events

import androidx.annotation.StringRes

sealed class MessageEvent(
  val textResId: Int
) : Event<Int>(textResId) {

  data class Info(
    @StringRes val textRestId: Int,
    val isIndefinite: Boolean = false
  ) : MessageEvent(textRestId)

  data class Error(
    @StringRes val textRestId: Int
  ) : MessageEvent(textRestId)
}
