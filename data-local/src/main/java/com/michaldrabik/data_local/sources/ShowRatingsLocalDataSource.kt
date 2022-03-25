package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.ShowRatings

interface ShowRatingsLocalDataSource {

  suspend fun upsert(entity: ShowRatings)

  suspend fun getById(traktId: Long): ShowRatings?
}
