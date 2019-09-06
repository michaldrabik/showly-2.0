package com.michaldrabik.showly2.ui.search

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesInteractor
import com.michaldrabik.storage.database.AppDatabase
import kotlinx.coroutines.delay
import javax.inject.Inject

@AppScope
class SearchInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val imagesInteractor: ImagesInteractor,
  private val mappers: Mappers
) {

  suspend fun searchForShow(query: String): List<Show> {
    val shows = cloud.traktApi.searchForShow(query)
    delay(3000)
    return shows.map { mappers.show.fromNetwork(it) }
  }
}