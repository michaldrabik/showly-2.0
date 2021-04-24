package com.michaldrabik.ui_settings.helpers

import androidx.annotation.StringRes
import com.michaldrabik.ui_settings.R

enum class AppLanguage(
  val code: String,
  @StringRes val displayName: Int
) {
  ENGLISH("en", R.string.textLanguageEnglish),
  GERMAN("de", R.string.textLanguageGerman),
  FRENCH("fr", R.string.textLanguageFrench),
  ITALIAN("it", R.string.textLanguageItalian),
  SPANISH("es", R.string.textLanguageSpanish),
  PORTUGAL_BRASIL("pt", R.string.textLanguagePortugalBrasil),
  POLISH("pl", R.string.textLanguagePolish),
  RUSSIAN("ru", R.string.textLanguageRussian),
  TURKISH("tr", R.string.textLanguageTurkish),
  ARABIC("ar", R.string.textLanguageArabic);

  companion object {
    fun fromCode(code: String) = values().first { it.code == code }
  }
}
