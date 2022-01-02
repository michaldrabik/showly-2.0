package com.michaldrabik.repository.shows.ratings

import androidx.room.withTransaction
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_local.database.AppDatabase
import com.michaldrabik.data_local.database.model.Rating
import com.michaldrabik.data_remote.Cloud
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowsRatingsRepository @Inject constructor(
  val external: ShowsExternalRatingsRepository,
  private val cloud: Cloud,
  private val database: AppDatabase,
  private val mappers: Mappers
) {

  companion object {
    private const val TYPE_SHOW = "show"
    private const val TYPE_EPISODE = "episode"
    private const val TYPE_SEASON = "season"
    private const val CHUNK_SIZE = 250
  }

  suspend fun preloadRatings(token: String) = supervisorScope {

    suspend fun preloadShowsRatings(token: String) {
      val ratings = cloud.traktApi.fetchShowsRatings(token)
      val entities = ratings
        .filter { it.rated_at != null && it.show.ids.trakt != null }
        .map { mappers.userRatingsMapper.toDatabaseShow(it) }
      database.ratingsDao().replaceAll(entities, TYPE_SHOW)
    }

    suspend fun preloadEpisodesRatings(token: String) {
      val ratings = cloud.traktApi.fetchEpisodesRatings(token)
      val entities = ratings
        .filter { it.rated_at != null && it.episode.ids.trakt != null }
        .map { mappers.userRatingsMapper.toDatabaseEpisode(it) }
      database.ratingsDao().replaceAll(entities, TYPE_EPISODE)
    }

    suspend fun preloadSeasonsRatings(token: String) {
      val ratings = cloud.traktApi.fetchSeasonsRatings(token)
      val entities = ratings
        .filter { it.rated_at != null && it.season.ids.trakt != null }
        .map { mappers.userRatingsMapper.toDatabaseSeason(it) }
      database.ratingsDao().replaceAll(entities, TYPE_SEASON)
    }

    val errorHandler = CoroutineExceptionHandler { _, _ -> Timber.e("Failed to preload some of ratings.") }
    launch(errorHandler) { preloadShowsRatings(token) }
    launch(errorHandler) { preloadEpisodesRatings(token) }
    launch(errorHandler) { preloadSeasonsRatings(token) }
  }

  suspend fun loadShowsRatings(): List<TraktRating> {
    val ratings = database.ratingsDao().getAllByType(TYPE_SHOW)
    return ratings.map {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun loadRatings(shows: List<Show>): List<TraktRating> {
    val ratings = mutableListOf<Rating>()
    shows.chunked(CHUNK_SIZE).forEach { chunk ->
      val items = database.ratingsDao().getAllByType(chunk.map { it.traktId }, TYPE_SHOW)
      ratings.addAll(items)
    }
    return ratings.map {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun loadRatingsSeasons(seasons: List<Season>): List<TraktRating> {
    val ratings = mutableListOf<Rating>()
    seasons.chunked(CHUNK_SIZE).forEach { chunk ->
      val items = database.ratingsDao().getAllByType(chunk.map { it.ids.trakt.id }, TYPE_SEASON)
      ratings.addAll(items)
    }
    return ratings.map {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun loadRating(episode: Episode): TraktRating? {
    val rating = database.ratingsDao().getAllByType(listOf(episode.ids.trakt.id), TYPE_EPISODE)
    return rating.firstOrNull()?.let {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun loadRating(season: Season): TraktRating? {
    val rating = database.ratingsDao().getAllByType(listOf(season.ids.trakt.id), TYPE_SEASON)
    return rating.firstOrNull()?.let {
      mappers.userRatingsMapper.fromDatabase(it)
    }
  }

  suspend fun addRating(token: String, show: Show, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.show.toNetwork(show),
      rating
    )
    val entity = mappers.userRatingsMapper.toDatabaseShow(show, rating, nowUtc())
    database.ratingsDao().replace(entity)
  }

  suspend fun addRating(token: String, episode: Episode, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.episode.toNetwork(episode),
      rating
    )
    val entity = mappers.userRatingsMapper.toDatabaseEpisode(episode, rating, nowUtc())
    database.ratingsDao().replace(entity)
  }

  suspend fun addRating(token: String, season: Season, rating: Int) {
    cloud.traktApi.postRating(
      token,
      mappers.season.toNetwork(season),
      rating
    )
    val entity = mappers.userRatingsMapper.toDatabaseSeason(season, rating, nowUtc())
    database.ratingsDao().replace(entity)
  }

  suspend fun deleteRating(token: String, show: Show) {
    cloud.traktApi.deleteRating(
      token,
      mappers.show.toNetwork(show)
    )
    database.ratingsDao().deleteByType(show.traktId, TYPE_SHOW)
  }

  suspend fun deleteRating(token: String, episode: Episode) {
    cloud.traktApi.deleteRating(
      token,
      mappers.episode.toNetwork(episode)
    )
    database.ratingsDao().deleteByType(episode.ids.trakt.id, TYPE_EPISODE)
  }

  suspend fun deleteRating(token: String, season: Season) {
    cloud.traktApi.deleteRating(
      token,
      mappers.season.toNetwork(season)
    )
    database.ratingsDao().deleteByType(season.ids.trakt.id, TYPE_SEASON)
  }

  suspend fun clear() {
    with(database) {
      withTransaction {
        ratingsDao().deleteAllByType(TYPE_EPISODE)
        ratingsDao().deleteAllByType(TYPE_SEASON)
        ratingsDao().deleteAllByType(TYPE_SHOW)
      }
    }
  }
}
