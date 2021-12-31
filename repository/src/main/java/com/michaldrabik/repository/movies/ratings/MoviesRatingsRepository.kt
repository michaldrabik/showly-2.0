package com.michaldrabik.repository.movies.ratings

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Rating
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.TraktRating
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRatingsRepository @Inject constructor(
  val external: MoviesExternalRatingsRepository,
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers,
) {

  companion object {
    private const val TYPE_MOVIE = "movie"
  }

  suspend fun preloadMoviesRatings(token: String) {
    val ratings = cloud.traktApi.fetchMoviesRatings(token)
    val entities = ratings
      .filter { it.rated_at != null && it.movie.ids.trakt != null }
      .map { mappers.userRatingsMapper.toDatabaseMovie(it) }
    database.ratingsDao().replaceAll(entities, TYPE_MOVIE)
  }

  suspend fun loadMoviesRatings(): List<TraktRating> {
    val ratings = database.ratingsDao().getAllByType(TYPE_MOVIE)
    return ratings.map {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun loadRatings(movies: List<Movie>): List<TraktRating> {
    val ratings = mutableListOf<Rating>()
    movies.chunked(250).forEach { chunk ->
      val items = database.ratingsDao().getAllByType(chunk.map { it.traktId }, TYPE_MOVIE)
      ratings.addAll(items)
    }
    return ratings.map {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun addRating(token: String, movie: Movie, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.movie.toNetwork(movie),
      rating
    )
    val entity = mappers.userRatingsMapper.toDatabaseMovie(movie, rating, nowUtc())
    database.ratingsDao().replace(entity)
  }

  suspend fun deleteRating(token: String, movie: Movie) {
    cloud.traktApi.deleteRating(
      token,
      mappers.movie.toNetwork(movie)
    )
    database.ratingsDao().deleteByType(movie.traktId, TYPE_MOVIE)
  }

  suspend fun clear() {
    database.ratingsDao().deleteAllByType(TYPE_MOVIE)
  }
}
