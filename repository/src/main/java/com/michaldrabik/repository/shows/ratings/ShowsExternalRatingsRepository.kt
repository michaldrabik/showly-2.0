package com.michaldrabik.repository.shows.ratings

import com.michaldrabik.common.ConfigVariant
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Show
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowsExternalRatingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  suspend fun loadRatings(show: Show): Ratings {
    val localRatings = database.showRatingsDao().getById(show.traktId)
    localRatings?.let {
      if (nowUtcMillis() - it.updatedAt < ConfigVariant.RATINGS_CACHE_DURATION) {
        return mappers.ratings.fromDatabase(it)
      }
    }

    val remoteRatings = cloud.omdbApi.fetchOmdbData(show.ids.imdb.id)
      .let { mappers.ratings.fromNetwork(it) }
      .copy(trakt = Ratings.Value(String.format(Locale.ENGLISH, "%.1f", show.rating), false))

    val dbRatings = mappers.ratings.toShowDatabase(show.ids.trakt, remoteRatings)
    database.showRatingsDao().upsert(dbRatings)

    return remoteRatings
  }
}
