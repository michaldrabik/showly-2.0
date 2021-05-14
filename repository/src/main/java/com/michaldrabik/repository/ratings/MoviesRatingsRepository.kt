package com.michaldrabik.repository.ratings

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

@AppScope
class MoviesRatingsRepository @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers,
) {

  private var moviesCache: MutableList<TraktRating>? = null

  suspend fun preloadMoviesRatings(token: String) {
    if (moviesCache == null) {
      val ratings = cloud.traktApi.fetchMoviesRatings(token)
      moviesCache = ratings.map { rate ->
        val id = IdTrakt(rate.movie.ids.trakt ?: -1)
        val date = rate.rated_at?.let { ZonedDateTime.parse(it) } ?: nowUtc()
        TraktRating(id, rate.rating, date)
      }.toMutableList()
    }
  }

  suspend fun loadMoviesRatings(token: String): List<TraktRating> {
    preloadMoviesRatings(token)
    return moviesCache?.toList() ?: emptyList()
  }

  suspend fun loadRating(token: String, movie: Movie): TraktRating? {
    preloadMoviesRatings(token)
    return moviesCache?.find { it.idTrakt == movie.ids.trakt }
  }

  suspend fun addRating(token: String, movie: Movie, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.movie.toNetwork(movie),
      rating
    )
    moviesCache?.run {
      val index = indexOfFirst { it.idTrakt == movie.ids.trakt }
      if (index != -1) removeAt(index)
      add(TraktRating(movie.ids.trakt, rating))
    }
  }

  suspend fun deleteRating(token: String, movie: Movie) {
    cloud.traktApi.deleteRating(
      token,
      mappers.movie.toNetwork(movie)
    )
    moviesCache?.run {
      val index = indexOfFirst { it.idTrakt == movie.ids.trakt }
      if (index != -1) removeAt(index)
    }
  }

  suspend fun loadRatings(movie: Movie) =
    cloud.omdbApi.fetchOmdbData(movie.ids.imdb.id)
      .let { mappers.ratings.fromNetwork(it) }

  fun clear() {
    moviesCache = null
  }
}
