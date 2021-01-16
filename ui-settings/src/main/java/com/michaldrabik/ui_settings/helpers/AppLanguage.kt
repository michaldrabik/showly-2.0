package com.michaldrabik.ui_settings.helpers

import androidx.annotation.StringRes
import com.michaldrabik.ui_settings.R

enum class AppLanguage(
  val code: String,
  @StringRes val displayName: Int
) {
  ENGLISH("en", R.string.textLanguageEnglish),
  POLISH("pl", R.string.textLanguagePolish),
  GERMAN("de", R.string.textLanguageGerman),
  SPANISH("es", R.string.textLanguageSpanish),
  ITALIAN("it", R.string.textLanguageItalian),
  FRENCH("fr", R.string.textLanguageFrench),
  ARABIC("ar", R.string.textLanguageArabic);

  companion object {
    fun fromCode(code: String) = values().first { it.code == code }
  }
}
