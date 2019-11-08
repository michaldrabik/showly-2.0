package com.michaldrabik.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.michaldrabik.storage.database.model.Show

@Dao
interface ShowsDao {

  @Query("SELECT * FROM shows")
  suspend fun getAll(): List<Show>

  @Query("SELECT * FROM shows WHERE id_trakt IN (:ids)")
  suspend fun getAll(ids: List<Long>): List<Show>

  @Query("SELECT * FROM shows WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(shows: List<Show>): List<Long>

  @Update(onConflict = OnConflictStrategy.REPLACE)
  suspend fun update(shows: List<Show>)

  @Transaction
  suspend fun upsert(shows: List<Show>) {
    val result = insert(shows)

    val updateList = mutableListOf<Show>()
    result.forEachIndexed { index, id ->
      if (id == -1L) updateList.add(shows[index])
    }

    if (updateList.isNotEmpty()) update(updateList)
  }
}