package com.michaldrabik.ui_settings.helpers

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.michaldrabik.ui_settings.R

enum class AppTheme(
  val code: Int,
  @StringRes val displayName: Int
) {
  DARK(MODE_NIGHT_YES, R.string.textThemeDark),
  LIGHT(MODE_NIGHT_NO, R.string.textThemeLight),
  SYSTEM(MODE_NIGHT_FOLLOW_SYSTEM, R.string.textThemeSystem);

  companion object {
    fun fromCode(code: Int) = values().first { it.code == code }
  }
}
