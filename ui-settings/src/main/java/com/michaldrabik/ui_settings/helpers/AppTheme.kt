package com.michaldrabik.ui_settings.helpers

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.michaldrabik.ui_settings.R

enum class AppTheme(
  val code: Int,
  @StringRes val displayName: Int
) {
  DARK(MODE_NIGHT_YES, R.string.textThemeDark);

  companion object {
    fun fromCode(code: Int) = values().first { it.code == code }
  }
}
