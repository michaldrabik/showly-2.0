package com.michaldrabik.ui_settings.helpers

import androidx.annotation.StringRes
import com.michaldrabik.ui_settings.R

enum class AppLanguage(
  val code: String,
  val displayNameRaw: String,
  @StringRes val displayName: Int,
) {
  ENGLISH("en", "English", R.string.textLanguageEnglish),
  GERMAN("de", "German", R.string.textLanguageGerman),
  FRENCH("fr", "French", R.string.textLanguageFrench),
  ITALIAN("it", "Italian", R.string.textLanguageItalian),
  SPANISH("es", "Spanish", R.string.textLanguageSpanish),
  PORTUGAL_BRASIL("pt", "Portuguese", R.string.textLanguagePortugalBrasil),
  POLISH("pl", "Polish", R.string.textLanguagePolish),
  RUSSIAN("ru", "Russian", R.string.textLanguageRussian),
  FINNISH("fi", "Finnish", R.string.textLanguageFinnish),
  TURKISH("tr", "Turkish", R.string.textLanguageTurkish),
  ARABIC("ar", "Arabic", R.string.textLanguageArabic);

  companion object {
    fun fromCode(code: String) = values().first { it.code == code }
  }
}
