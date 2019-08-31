package com.michaldrabik.showly2.ui.discover

import com.michaldrabik.network.Cloud
import com.michaldrabik.network.trakt.model.AirTime
import com.michaldrabik.network.trakt.model.Ids
import com.michaldrabik.network.trakt.model.Show
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.TrendingShow
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Show as ShowDb

class DiscoverInteractor @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase
) {

  suspend fun loadTrendingShows(): List<Show> {
    val localShows = database.trendingShowsDao().getAll()
    if (localShows.isNotEmpty()) {
      return localShows.map {
        Show(
          Ids(it.idTrakt, it.idSlug ?: "", it.idTvdb ?: -1, it.idImdb ?: "", it.idTmdb ?: -1, -1),
          it.title ?: "",
          it.year ?: -1,
          it.overview ?: "",
          "",
          1,
          AirTime("", "", ""),
          "",
          "",
          "",
          "",
          "",
          "",
          1F,
          1L,
          1L,
          emptyList(),
          1
        )
      }
    }


    val remoteShows = cloud.traktApi.fetchTrendingShows()
    database.showsDao().upsert(remoteShows.map {
      ShowDb(
        it.ids.trakt,
        it.ids.tvdb,
        it.ids.tmdb,
        it.ids.imdb,
        it.ids.slug,
        it.title,
        it.year,
        it.overview
      )
    })
    database.trendingShowsDao().clearAndInsert(remoteShows.map { TrendingShow(idTrakt = it.ids.trakt) })
    return remoteShows
  }

  private fun onError(error: Throwable) {
    //TODO
  }
}