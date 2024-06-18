package com.michaldrabik.ui_model.locale

data class AppLocale(
  val language: AppLanguage,
  val country: AppCountry,
) {

  companion object {
    const val DELIMITER = "_"
    fun default() = AppLocale(
      language = AppLanguage.default(),
      country = AppCountry.default(),
    )
    fun fromCode(code: String) = AppLocale(
      language = AppLanguage.fromCode(code.substringBefore(DELIMITER)),
      country = AppCountry.fromCode(code.substringAfter(DELIMITER)),
    )
  }

  fun code() = "${language.code}${DELIMITER}${country.code}".lowercase()
}
