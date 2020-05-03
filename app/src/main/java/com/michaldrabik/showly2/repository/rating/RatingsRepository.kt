package com.michaldrabik.showly2.repository.rating

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.TraktRating
import com.michaldrabik.showly2.model.mappers.Mappers
import javax.inject.Inject

@AppScope
class RatingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers
) {

  private var showsCache: MutableList<TraktRating>? = null
  private var episodesCache: MutableList<TraktRating>? = null

  suspend fun loadRating(token: String, show: Show): TraktRating? {
    if (showsCache == null) {
      val ratings = cloud.traktApi.fetchShowsRatings(token)
      showsCache = ratings.map {
        val id = IdTrakt(it.show.ids.trakt ?: -1)
        TraktRating(id, it.rating)
      }.toMutableList()
    }
    return showsCache?.find { it.idTrakt == show.ids.trakt }
  }

  suspend fun loadRating(token: String, episode: Episode): TraktRating? {
    if (episodesCache == null) {
      val ratings = cloud.traktApi.fetchEpisodesRatings(token)
      episodesCache = ratings.map {
        val id = IdTrakt(it.episode.ids.trakt ?: -1)
        TraktRating(id, it.rating)
      }.toMutableList()
    }
    return episodesCache?.find { it.idTrakt == episode.ids.trakt }
  }

  suspend fun addRating(token: String, show: Show, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.show.toNetwork(show),
      rating
    )
    showsCache?.run {
      val index = indexOfFirst { it.idTrakt == show.ids.trakt }
      if (index != -1) removeAt(index)
      add(TraktRating(show.ids.trakt, rating))
    }
  }

  suspend fun addRating(token: String, episode: Episode, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.episode.toNetwork(episode),
      rating
    )
    episodesCache?.run {
      val index = indexOfFirst { it.idTrakt == episode.ids.trakt }
      if (index != -1) removeAt(index)
      add(TraktRating(episode.ids.trakt, rating))
    }
  }

  fun clear() {
    showsCache = null
    episodesCache = null
  }
}
