package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.EpisodeTranslation

interface EpisodeTranslationsLocalDataSource {

  suspend fun getById(traktEpisodeId: Long, traktShowId: Long, language: String, country: String): EpisodeTranslation?

  suspend fun getByIds(traktEpisodeIds: List<Long>, traktShowId: Long, language: String, country: String): List<EpisodeTranslation>

  suspend fun insertSingle(translation: EpisodeTranslation)

  suspend fun deleteByLanguage(languages: List<String>)

  suspend fun deleteByCountry(countries: List<String>)
}
