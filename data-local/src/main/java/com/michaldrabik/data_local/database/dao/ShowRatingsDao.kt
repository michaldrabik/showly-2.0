package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.michaldrabik.data_local.database.model.ShowRatings

@Dao
interface ShowRatingsDao : BaseDao<ShowRatings> {

  @Query("SELECT * FROM shows_ratings WHERE id_trakt == :traktId")
  suspend fun getById(traktId: Long): ShowRatings?
}
