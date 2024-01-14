package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.RelatedShow

interface RelatedShowsLocalDataSource {

  suspend fun insert(items: List<RelatedShow>): List<Long>

  suspend fun getAllById(traktId: Long): List<RelatedShow>

  suspend fun getAll(): List<RelatedShow>

  suspend fun deleteById(traktId: Long)
}
