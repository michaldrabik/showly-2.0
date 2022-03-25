package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.News

interface NewsLocalDataSource {

  suspend fun getAllByType(type: String): List<News>

  suspend fun replaceForType(items: List<News>, type: String)

  suspend fun deleteAllByType(type: String): Int
}
