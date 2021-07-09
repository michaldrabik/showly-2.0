package com.michaldrabik.repository.shows.ratings

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowsRatingsRepository @Inject constructor(
  val external: ShowsExternalRatingsRepository,
  private val cloud: Cloud,
  private val mappers: Mappers,
) {

  private var showsCache: MutableList<TraktRating>? = null
  private var episodesCache: MutableList<TraktRating>? = null

  suspend fun preloadShowsRatings(token: String) {
    if (showsCache == null) {
      val ratings = cloud.traktApi.fetchShowsRatings(token)
      showsCache = ratings.map { rate ->
        val id = IdTrakt(rate.show.ids.trakt ?: -1)
        val date = rate.rated_at?.let { ZonedDateTime.parse(it) } ?: nowUtc()
        TraktRating(id, rate.rating, date)
      }.toMutableList()
    }
  }

  suspend fun preloadEpisodesRatings(token: String) {
    if (episodesCache == null) {
      val ratings = cloud.traktApi.fetchEpisodesRatings(token)
      episodesCache = ratings.map {
        val id = IdTrakt(it.episode.ids.trakt ?: -1)
        TraktRating(id, it.rating)
      }.toMutableList()
    }
  }

  suspend fun loadShowsRatings(token: String): List<TraktRating> {
    preloadShowsRatings(token)
    return showsCache?.toList() ?: emptyList()
  }

  suspend fun loadRating(token: String, show: Show): TraktRating? {
    preloadShowsRatings(token)
    return showsCache?.find { it.idTrakt == show.ids.trakt }
  }

  suspend fun loadRating(token: String, episode: Episode, onlyCache: Boolean = false): TraktRating? {
    if (!onlyCache) preloadEpisodesRatings(token)
    return episodesCache?.find { it.idTrakt == episode.ids.trakt }
  }

  fun loadRating(episode: Episode) = episodesCache?.find { it.idTrakt == episode.ids.trakt }

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

  suspend fun deleteRating(token: String, show: Show) {
    cloud.traktApi.deleteRating(
      token,
      mappers.show.toNetwork(show)
    )
    showsCache?.run {
      val index = indexOfFirst { it.idTrakt == show.ids.trakt }
      if (index != -1) removeAt(index)
    }
  }

  suspend fun deleteRating(token: String, episode: Episode) {
    cloud.traktApi.deleteRating(
      token,
      mappers.episode.toNetwork(episode)
    )
    episodesCache?.run {
      val index = indexOfFirst { it.idTrakt == episode.ids.trakt }
      if (index != -1) removeAt(index)
    }
  }

  fun clear() {
    showsCache = null
    episodesCache = null
  }
}
