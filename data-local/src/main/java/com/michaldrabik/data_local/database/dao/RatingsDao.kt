package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.Rating

@Dao
interface RatingsDao : BaseDao<Rating> {

  @Query("SELECT * FROM ratings")
  suspend fun getAll(): List<Rating>

  @Query("SELECT * FROM ratings WHERE type == :type")
  suspend fun getAllByType(type: String): List<Rating>

  @Query("SELECT * FROM ratings WHERE id_trakt IN (:idsTrakt) AND type == :type")
  suspend fun getAllByType(idsTrakt: List<Long>, type: String): List<Rating>

  @Query("DELETE FROM ratings WHERE type == :type")
  suspend fun deleteAllByType(type: String)

  @Query("DELETE FROM ratings WHERE id_trakt == :traktId AND type == :type")
  suspend fun deleteByType(traktId: Long, type: String)

  @Transaction
  suspend fun replaceAll(ratings: List<Rating>, type: String) {
    deleteAllByType(type)
    insert(ratings)
  }

  @Transaction
  suspend fun replace(rating: Rating) {
    deleteByType(rating.idTrakt, rating.type)
    insert(listOf(rating))
  }
}
