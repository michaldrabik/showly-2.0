package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ShowTranslation

@Dao
interface ShowTranslationsDao : BaseDao<ShowTranslation> {

  @Query("SELECT * FROM shows_translations WHERE id_trakt == :traktId AND language == :language")
  suspend fun getById(traktId: Long, language: String): ShowTranslation?

  @Query("SELECT * FROM shows_translations WHERE language == :language")
  suspend fun getAll(language: String): List<ShowTranslation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(translation: ShowTranslation)

  @Query("DELETE FROM shows_translations WHERE language IN (:languages)")
  suspend fun deleteByLanguage(languages: List<String>)
}
