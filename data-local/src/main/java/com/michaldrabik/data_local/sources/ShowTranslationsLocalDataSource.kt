package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.ShowTranslation

interface ShowTranslationsLocalDataSource {

  suspend fun getById(traktId: Long, language: String, country: String): ShowTranslation?

  suspend fun getAll(language: String, country: String): List<ShowTranslation>

  suspend fun insertSingle(translation: ShowTranslation)

  suspend fun deleteByLanguage(languages: List<String>)

  suspend fun deleteByCountry(countries: List<String>)
}
