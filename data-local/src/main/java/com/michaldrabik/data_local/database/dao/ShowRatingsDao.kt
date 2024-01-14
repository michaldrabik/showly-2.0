package com.michaldrabik.data_local.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.michaldrabik.data_local.database.model.ShowRatings
import com.michaldrabik.data_local.sources.ShowRatingsLocalDataSource

@Dao
interface ShowRatingsDao : BaseDao<ShowRatings>, ShowRatingsLocalDataSource {

  @Transaction
  override suspend fun upsert(entity: ShowRatings) {
    val local = getById(entity.idTrakt)
    if (local != null) {
      update(
        listOf(
          local.copy(
            trakt = entity.trakt,
            imdb = entity.imdb,
            metascore = entity.metascore,
            rottenTomatoes = entity.rottenTomatoes,
            rottenTomatoesUrl = entity.rottenTomatoesUrl,
            updatedAt = entity.updatedAt
          )
        )
      )
      return
    }
    insert(listOf(entity))
  }

  @Query("SELECT * FROM shows_ratings WHERE id_trakt == :traktId")
  override suspend fun getById(traktId: Long): ShowRatings?
}
