package com.michaldrabik.ui_settings.helpers

import androidx.annotation.StringRes
import com.michaldrabik.ui_settings.R

enum class WidgetTransparency(
  val value: Int,
  @StringRes val displayName: Int
) {
  SOLID(100, R.string.textTransparency100),
  LOW(75, R.string.textTransparency25),
  MEDIUM(50, R.string.textTransparency50),
  HIGH(25, R.string.textTransparency75),
  TRANSPARENT(0, R.string.textTransparency0);

  companion object {
    fun fromValue(value: Int) = values().first { it.value == value }
  }
}
