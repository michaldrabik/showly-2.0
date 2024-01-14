package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.TranslationsMoviesSyncLog

interface TranslationsMoviesSyncLogLocalDataSource {

  suspend fun getAll(): List<TranslationsMoviesSyncLog>

  suspend fun getById(idTrakt: Long): TranslationsMoviesSyncLog?

  suspend fun upsert(log: TranslationsMoviesSyncLog)

  suspend fun deleteAll()
}
