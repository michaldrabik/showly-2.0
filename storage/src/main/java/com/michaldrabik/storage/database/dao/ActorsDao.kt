package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.storage.database.model.Actor
import com.michaldrabik.storage.database.model.Show

@Dao
interface ActorsDao {

  @Query("SELECT * FROM actors WHERE id_tvdb_show = :tvdbId")
  suspend fun getAllByShow(tvdbId: Long): List<Actor>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(actors: List<Actor>)

  @Query("DELETE FROM actors WHERE id_tvdb_show = :tvdbId")
  suspend fun deleteAllByShow(tvdbId: Long)

  @Transaction
  suspend fun deleteAllAndInsert(actors: List<Actor>, showTvdbId: Long) {
    deleteAllByShow(showTvdbId)
    upsert(actors)
  }
}