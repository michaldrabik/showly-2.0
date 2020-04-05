package com.michaldrabik.showly2.repository.rating

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.ShowRating
import com.michaldrabik.showly2.model.mappers.Mappers
import javax.inject.Inject

@AppScope
class RatingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers
) {

  private var cache: MutableList<ShowRating>? = null

  suspend fun loadRating(token: String, show: Show): ShowRating? {
    if (cache == null) {
      val ratings = cloud.traktApi.fetchShowsRatings(token)
      cache = ratings.map {
        val id = IdTrakt(it.show.ids.trakt ?: -1)
        ShowRating(id, it.rating)
      }.toMutableList()
    }
    return cache?.find { it.idTrakt == show.ids.trakt }
  }

  suspend fun addRating(token: String, show: Show, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.show.toNetwork(show),
      rating
    )
    cache?.run {
      val index = indexOfFirst { it.idTrakt == show.ids.trakt }
      if (index != -1) removeAt(index)
      add(ShowRating(show.ids.trakt, rating))
    }
  }

  fun clear() {
    cache = null
  }
}
