package com.michaldrabik.ui_repository.movies

import androidx.room.withTransaction
import com.michaldrabik.common.Config
import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.network.Cloud
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.DiscoverMovie
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_repository.mappers.Mappers
import javax.inject.Inject

@AppScope
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
    genres: List<Genre>
  ): List<Movie> {
    val remoteMovies = mutableListOf<Movie>()
    val anticipatedMovies = mutableListOf<Movie>()
    val popularMovies = mutableListOf<Movie>()
    val genresQuery = genres.joinToString(",") { it.slug }

    val trendingMovies = cloud.traktApi.fetchTrendingMovies(genresQuery).map { mappers.movie.fromNetwork(it) }

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
