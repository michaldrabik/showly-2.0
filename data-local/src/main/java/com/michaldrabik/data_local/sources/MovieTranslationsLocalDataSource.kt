package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MovieTranslation

interface MovieTranslationsLocalDataSource {

  suspend fun getById(traktId: Long, language: String, country: String): MovieTranslation?

  suspend fun getAll(language: String, country: String): List<MovieTranslation>

  suspend fun insertSingle(translation: MovieTranslation)

  suspend fun deleteByLanguage(languages: List<String>)

  suspend fun deleteByCountry(countries: List<String>)
}
