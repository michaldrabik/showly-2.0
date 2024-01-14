package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.TranslationsSyncLog

interface TranslationsShowsSyncLogLocalDataSource {

  suspend fun getAll(): List<TranslationsSyncLog>

  suspend fun getById(idTrakt: Long): TranslationsSyncLog?

  suspend fun upsert(log: TranslationsSyncLog)

  suspend fun deleteAll()
}
