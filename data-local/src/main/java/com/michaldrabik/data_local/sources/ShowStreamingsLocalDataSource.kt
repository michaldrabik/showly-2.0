package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.ShowStreaming

interface ShowStreamingsLocalDataSource {

  suspend fun replace(traktId: Long, entities: List<ShowStreaming>)

  suspend fun getById(traktId: Long): List<ShowStreaming>

  suspend fun deleteById(traktId: Long)

  suspend fun deleteAll()
}
