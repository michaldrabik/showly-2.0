package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MovieRatings

interface MovieRatingsLocalDataSource {

  suspend fun upsert(entity: MovieRatings)

  suspend fun getById(traktId: Long): MovieRatings?
}
