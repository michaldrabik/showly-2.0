package com.michaldrabik.repository.movies

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.DiscoverMovie
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.data_remote.Config.TRAKT_TRENDING_MOVIES_LIMIT
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Movie
import javax.inject.Inject

class DiscoverMoviesRepository @Inject constructor(
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  suspend fun isCacheValid(): Boolean {
    val stamp = database.discoverMoviesDao().getMostRecent()?.createdAt ?: 0
    return nowUtcMillis() - stamp < Config.DISCOVER_MOVIES_CACHE_DURATION
  }

  suspend fun loadAllCached(): List<Movie> {
    val cachedMovies = database.discoverMoviesDao().getAll().map { it.idTrakt }
    val movies = database.moviesDao().getAll(cachedMovies)

    return cachedMovies
      .map { id -> movies.first { it.idTrakt == id } }
      .map { mappers.movie.fromDatabase(it) }
  }

  // TODO This logic should probably sit in a case and not repository.
  suspend fun loadAllRemote(
    showAnticipated: Boolean,
    showCollection: Boolean,
    collectionSize: Int,
    genres: List<Genre>
  ): List<Movie> {
    val remoteMovies = mutableListOf<Movie>()
    val anticipatedMovies = mutableListOf<Movie>()
    val popularMovies = mutableListOf<Movie>()
    val genresQuery = genres.joinToString(",") { it.slug }

    val limit =
      if (showCollection) TRAKT_TRENDING_MOVIES_LIMIT
      else TRAKT_TRENDING_MOVIES_LIMIT + (collectionSize / 2)
    val trendingMovies = cloud.traktApi.fetchTrendingMovies(genresQuery, limit)
      .map { mappers.movie.fromNetwork(it) }

    if (genres.isNotEmpty()) {
      // Wa are adding popular results for genres filtered content to add more results.
      val popular = cloud.traktApi.fetchPopularMovies(genresQuery).map { mappers.movie.fromNetwork(it) }
      popularMovies.addAll(popular)
    }

    if (showAnticipated) {
      val movies = cloud.traktApi.fetchAnticipatedMovies(genresQuery).map { mappers.movie.fromNetwork(it) }.toMutableList()
      anticipatedMovies.addAll(movies)
    }

    trendingMovies.forEachIndexed { index, movie ->
      addIfMissing(remoteMovies, movie)
      if (index % 4 == 0 && anticipatedMovies.isNotEmpty()) {
        val element = anticipatedMovies.removeAt(0)
        addIfMissing(remoteMovies, element)
      }
    }
    popularMovies.forEach { show -> addIfMissing(remoteMovies, show) }

    if (!showAnticipated) {
      return remoteMovies.filter { !it.status.isAnticipated() }
    }

    return remoteMovies
  }

  suspend fun cacheDiscoverMovies(movies: List<Movie>) {
    database.withTransaction {
      val timestamp = nowUtcMillis()
      database.moviesDao().upsert(movies.map { mappers.movie.toDatabase(it) })
      database.discoverMoviesDao().replace(
        movies.map {
          DiscoverMovie(
            idTrakt = it.ids.trakt.id,
            createdAt = timestamp,
            updatedAt = timestamp
          )
        }
      )
    }
  }

  private fun addIfMissing(movies: MutableList<Movie>, movie: Movie) {
    if (movies.any { it.ids.trakt == movie.ids.trakt }) return
    movies.add(movie)
  }
}
