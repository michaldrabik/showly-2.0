package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.ShowMapper
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TrendingShow
import java.lang.System.currentTimeMillis
import javax.inject.Inject

@AppScope
class DiscoverInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mapper: ShowMapper
) {

  suspend fun loadTrendingShows(): List<Show> {
    val stamp = database.trendingShowsDao().getMostRecent()?.createdAt ?: 0
    if (currentTimeMillis() - stamp < Config.TRENDING_SHOWS_CACHE_DURATION) {
      return database.trendingShowsDao().getAll().map { mapper.fromDatabase(it) }
    }

    val remoteShows = cloud.traktApi.fetchTrendingShows().map { mapper.fromNetwork(it) }
    database.showsDao().upsert(remoteShows.map { mapper.toDatabase(it) })
    val timestamp = currentTimeMillis()
    database.trendingShowsDao().deleteAllAndInsert(remoteShows.map {
      TrendingShow(idTrakt = it.ids.trakt, createdAt = timestamp, updatedAt = timestamp)
    })

    return remoteShows
  }

  private fun onError(error: Throwable) {
    //TODO
  }
}