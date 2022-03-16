package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.Show

interface ShowsLocalDataSource {

  suspend fun getAll(): List<Show>

  suspend fun getAll(ids: List<Long>): List<Show>

  suspend fun getAllChunked(ids: List<Long>): List<Show>

  suspend fun getById(traktId: Long): Show?

  suspend fun getByTmdbId(tmdbId: Long): Show?

  suspend fun getBySlug(slug: String): Show?

  suspend fun getById(imdbId: String): Show?

  suspend fun deleteById(traktId: Long)

  suspend fun upsert(shows: List<Show>)
}
