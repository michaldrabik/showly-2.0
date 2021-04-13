package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michaldrabik.data_local.database.model.TranslationsMoviesSyncLog

@Dao
interface TranslationsMoviesSyncLogDao {

  @Query("SELECT * from sync_movies_translations_log")
  suspend fun getAll(): List<TranslationsMoviesSyncLog>

  @Query("SELECT * from sync_movies_translations_log WHERE id_movie_trakt == :idTrakt")
  suspend fun getById(idTrakt: Long): TranslationsMoviesSyncLog?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(log: TranslationsMoviesSyncLog)

  @Query("DELETE FROM sync_movies_translations_log")
  suspend fun deleteAll()
}
