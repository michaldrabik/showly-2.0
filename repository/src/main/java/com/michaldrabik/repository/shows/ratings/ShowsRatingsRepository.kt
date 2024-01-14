package com.michaldrabik.repository.shows.ratings

import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.Rating
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.RemoteDataSource
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
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers
) {

  companion object {
    private const val TYPE_SHOW = "show"
    private const val TYPE_EPISODE = "episode"
    private const val TYPE_SEASON = "season"
    private const val CHUNK_SIZE = 250
  }

  suspend fun preloadRatings() = supervisorScope {

    suspend fun preloadShowsRatings() {
      val ratings = remoteSource.trakt.fetchShowsRatings()
      val entities = ratings
        .filter { it.rated_at != null && it.show.ids.trakt != null }
        .map { mappers.userRatings.toDatabaseShow(it) }
      localSource.ratings.replaceAll(entities, TYPE_SHOW)
    }

    suspend fun preloadEpisodesRatings() {
      val ratings = remoteSource.trakt.fetchEpisodesRatings()
      val entities = ratings
        .filter { it.rated_at != null && it.episode.ids.trakt != null }
        .map { mappers.userRatings.toDatabaseEpisode(it) }
      localSource.ratings.replaceAll(entities, TYPE_EPISODE)
    }

    suspend fun preloadSeasonsRatings() {
      val ratings = remoteSource.trakt.fetchSeasonsRatings()
      val entities = ratings
        .filter { it.rated_at != null && it.season.ids.trakt != null }
        .map { mappers.userRatings.toDatabaseSeason(it) }
      localSource.ratings.replaceAll(entities, TYPE_SEASON)
    }

    val errorHandler = CoroutineExceptionHandler { _, _ -> Timber.e("Failed to preload some of ratings.") }
    launch(errorHandler) { preloadShowsRatings() }
    launch(errorHandler) { preloadEpisodesRatings() }
    launch(errorHandler) { preloadSeasonsRatings() }
  }

  suspend fun loadShowsRatings(): List<TraktRating> {
    val ratings = localSource.ratings.getAllByType(TYPE_SHOW)
    return ratings.map {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun loadRatings(shows: List<Show>): List<TraktRating> {
    val ratings = mutableListOf<Rating>()
    shows.chunked(CHUNK_SIZE).forEach { chunk ->
      val items = localSource.ratings.getAllByType(chunk.map { it.traktId }, TYPE_SHOW)
      ratings.addAll(items)
    }
    return ratings.map {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun loadRatingsSeasons(seasons: List<Season>): List<TraktRating> {
    val ratings = mutableListOf<Rating>()
    seasons.chunked(CHUNK_SIZE).forEach { chunk ->
      val items = localSource.ratings.getAllByType(chunk.map { it.ids.trakt.id }, TYPE_SEASON)
      ratings.addAll(items)
    }
    return ratings.map {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun loadRating(episode: Episode): TraktRating? {
    val rating = localSource.ratings.getAllByType(listOf(episode.ids.trakt.id), TYPE_EPISODE)
    return rating.firstOrNull()?.let {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun loadRating(season: Season): TraktRating? {
    val rating = localSource.ratings.getAllByType(listOf(season.ids.trakt.id), TYPE_SEASON)
    return rating.firstOrNull()?.let {
      mappers.userRatings.fromDatabase(it)
    }
  }

  suspend fun addRating(show: Show, rating: Int) {
    remoteSource.trakt.postRating(
      mappers.show.toNetwork(show),
      rating
    )
    val entity = mappers.userRatings.toDatabaseShow(show, rating, nowUtc())
    localSource.ratings.replace(entity)
  }

  suspend fun addRating(episode: Episode, rating: Int) {
    remoteSource.trakt.postRating(
      mappers.episode.toNetwork(episode),
      rating
    )
    val entity = mappers.userRatings.toDatabaseEpisode(episode, rating, nowUtc())
    localSource.ratings.replace(entity)
  }

  suspend fun addRating(season: Season, rating: Int) {
    remoteSource.trakt.postRating(
      mappers.season.toNetwork(season),
      rating
    )
    val entity = mappers.userRatings.toDatabaseSeason(season, rating, nowUtc())
    localSource.ratings.replace(entity)
  }

  suspend fun deleteRating(show: Show) {
    remoteSource.trakt.deleteRating(
      mappers.show.toNetwork(show)
    )
    localSource.ratings.deleteByType(show.traktId, TYPE_SHOW)
  }

  suspend fun deleteRating(episode: Episode) {
    remoteSource.trakt.deleteRating(
      mappers.episode.toNetwork(episode)
    )
    localSource.ratings.deleteByType(episode.ids.trakt.id, TYPE_EPISODE)
  }

  suspend fun deleteRating(season: Season) {
    remoteSource.trakt.deleteRating(
      mappers.season.toNetwork(season)
    )
    localSource.ratings.deleteByType(season.ids.trakt.id, TYPE_SEASON)
  }

  suspend fun clear() {
    with(localSource) {
      transactions.withTransaction {
        ratings.deleteAllByType(TYPE_EPISODE)
        ratings.deleteAllByType(TYPE_SEASON)
        ratings.deleteAllByType(TYPE_SHOW)
      }
    }
  }
}
