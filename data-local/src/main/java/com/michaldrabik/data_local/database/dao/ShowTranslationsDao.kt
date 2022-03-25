package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ShowTranslation
import com.michaldrabik.data_local.sources.ShowTranslationsLocalDataSource

@Dao
interface ShowTranslationsDao : BaseDao<ShowTranslation>, ShowTranslationsLocalDataSource {

  @Query("SELECT * FROM shows_translations WHERE id_trakt == :traktId AND language == :language")
  override suspend fun getById(traktId: Long, language: String): ShowTranslation?

  @Query("SELECT * FROM shows_translations WHERE language == :language")
  override suspend fun getAll(language: String): List<ShowTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(translation: ShowTranslation)

  @Query("DELETE FROM shows_translations WHERE language IN (:languages)")
  override suspend fun deleteByLanguage(languages: List<String>)
}
