package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Show

@Dao
interface ShowsDao : BaseDao<Show> {

  @Query("SELECT * FROM shows")
  suspend fun getAll(): List<Show>

  @Query("SELECT * FROM shows WHERE id_trakt IN (:ids)")
  suspend fun getAll(ids: List<Long>): List<Show>

  @Transaction
  suspend fun getAllChunked(ids: List<Long>): List<Show> = ids
    .chunked(500)
    .fold(
      mutableListOf(),
      { acc, chunk ->
        acc += getAll(chunk)
        acc
      }
    )

  @Query("SELECT * FROM shows WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): Show?

  @Query("DELETE FROM shows where id_trakt == :traktId")
  suspend fun deleteById(traktId: Long)

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
