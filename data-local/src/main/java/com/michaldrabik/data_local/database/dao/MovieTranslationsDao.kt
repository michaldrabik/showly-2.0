package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.MovieTranslation
import com.michaldrabik.data_local.sources.MovieTranslationsLocalDataSource

@Dao
interface MovieTranslationsDao : BaseDao<MovieTranslation>, MovieTranslationsLocalDataSource {

  @Query("SELECT * FROM movies_translations WHERE id_trakt == :traktId AND language == :language")
  override suspend fun getById(traktId: Long, language: String): MovieTranslation?

  @Query("SELECT * FROM movies_translations WHERE language == :language")
  override suspend fun getAll(language: String): List<MovieTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  override suspend fun insert(translation: MovieTranslation)

  @Query("DELETE FROM movies_translations WHERE language IN (:languages)")
  override suspend fun deleteByLanguage(languages: List<String>)
}
