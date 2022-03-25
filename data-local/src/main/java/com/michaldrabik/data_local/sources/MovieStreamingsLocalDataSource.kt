package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MovieStreaming

interface MovieStreamingsLocalDataSource {

  suspend fun replace(traktId: Long, entities: List<MovieStreaming>)

  suspend fun getById(traktId: Long): List<MovieStreaming>

  suspend fun deleteById(traktId: Long)

  suspend fun deleteAll()
}
