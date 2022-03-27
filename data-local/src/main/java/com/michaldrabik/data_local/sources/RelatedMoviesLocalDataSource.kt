package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.RelatedMovie

interface RelatedMoviesLocalDataSource {

  suspend fun insert(items: List<RelatedMovie>): List<Long>

  suspend fun getAllById(traktId: Long): List<RelatedMovie>

  suspend fun getAll(): List<RelatedMovie>

  suspend fun deleteById(traktId: Long)
}
