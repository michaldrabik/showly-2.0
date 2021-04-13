package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.MovieTranslation

@Dao
interface MovieTranslationsDao : BaseDao<MovieTranslation> {

  @Query("SELECT * FROM movies_translations WHERE id_trakt == :traktId AND language == :language")
  suspend fun getById(traktId: Long, language: String): MovieTranslation?

  @Query("SELECT * FROM movies_translations WHERE language == :language")
  suspend fun getAll(language: String): List<MovieTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(translation: MovieTranslation)

  @Query("DELETE FROM movies_translations WHERE language IN (:languages)")
  suspend fun deleteByLanguage(languages: List<String>)
}
