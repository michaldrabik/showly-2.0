package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ShowTranslation
import com.michaldrabik.data_local.sources.ShowTranslationsLocalDataSource

@Dao
interface ShowTranslationsDao : BaseDao<ShowTranslation>, ShowTranslationsLocalDataSource {

  @Query("SELECT * FROM shows_translations WHERE id_trakt == :traktId AND language == :language AND country == :country")
  override suspend fun getById(traktId: Long, language: String, country: String): ShowTranslation?

  @Query("SELECT * FROM shows_translations WHERE language == :language AND country == :country")
  override suspend fun getAll(language: String, country: String): List<ShowTranslation>

  @Insert(onConflict = REPLACE)
  override suspend fun insertSingle(translation: ShowTranslation)

  @Query("DELETE FROM shows_translations WHERE language IN (:languages)")
  override suspend fun deleteByLanguage(languages: List<String>)

  @Query("DELETE FROM shows_translations WHERE country IN (:countries)")
  override suspend fun deleteByCountry(countries: List<String>)
}
