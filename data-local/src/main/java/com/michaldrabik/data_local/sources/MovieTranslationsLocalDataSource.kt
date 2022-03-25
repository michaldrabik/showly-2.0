package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MovieTranslation

interface MovieTranslationsLocalDataSource {

  suspend fun getById(traktId: Long, language: String): MovieTranslation?

  suspend fun getAll(language: String): List<MovieTranslation>

  suspend fun insert(translation: MovieTranslation)

  suspend fun deleteByLanguage(languages: List<String>)
}
