package com.michaldrabik.repository.shows

import com.michaldrabik.common.Config
import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.data_local.LocalDataSource
import com.michaldrabik.data_local.database.model.DiscoverShow
import com.michaldrabik.data_local.utilities.TransactionsProvider
import com.michaldrabik.data_remote.Config.TRAKT_TRENDING_SHOWS_LIMIT
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.Network
import com.michaldrabik.ui_model.Show
import javax.inject.Inject

class DiscoverShowsRepository @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val localSource: LocalDataSource,
  private val transactions: TransactionsProvider,
  private val mappers: Mappers
) {

  suspend fun isCacheValid(): Boolean {
    val stamp = localSource.discoverShows.getMostRecent()?.createdAt ?: 0
    return nowUtcMillis() - stamp < Config.DISCOVER_SHOWS_CACHE_DURATION
  }

  suspend fun loadAllCached(): List<Show> {
    val cachedShows = localSource.discoverShows.getAll().map { it.idTrakt }
    val shows = localSource.shows.getAll(cachedShows)

    return cachedShows
      .map { id -> shows.first { it.idTrakt == id } }
      .map { mappers.show.fromDatabase(it) }
  }

  // TODO This logic should probably sit in a case and not repository.
  suspend fun loadAllRemote(
    showAnticipated: Boolean,
    showCollection: Boolean,
    collectionSize: Int,
    genres: List<Genre>,
    networks: List<Network>
  ): List<Show> {
    val remoteShows = mutableListOf<Show>()
    val anticipatedShows = mutableListOf<Show>()
    val popularShows = mutableListOf<Show>()

    val genresQuery = genres.joinToString(",") { it.slug }
    val networksQuery = networks.joinToString(",") { it.channels.joinToString(",") }

    val limit =
      if (showCollection) TRAKT_TRENDING_SHOWS_LIMIT
      else TRAKT_TRENDING_SHOWS_LIMIT + (collectionSize / 2)
    val trendingShows = remoteSource.trakt.fetchTrendingShows(genresQuery, networksQuery, limit)
      .map { mappers.show.fromNetwork(it) }

    if (genres.isNotEmpty() || networks.isNotEmpty()) {
      // Wa are adding popular results for genres/networks filtered content to add more results.
      val popular = remoteSource.trakt.fetchPopularShows(genresQuery, networksQuery)
        .map { mappers.show.fromNetwork(it) }
      popularShows.addAll(popular)
    }

    if (showAnticipated) {
      val shows = remoteSource.trakt.fetchAnticipatedShows(genresQuery, networksQuery).map { mappers.show.fromNetwork(it) }.toMutableList()
      anticipatedShows.addAll(shows)
    }

    trendingShows.forEachIndexed { index, show ->
      addIfMissing(remoteShows, show)
      if (index % 4 == 0 && anticipatedShows.isNotEmpty()) {
        val element = anticipatedShows.removeAt(0)
        addIfMissing(remoteShows, element)
      }
    }
    popularShows.forEach { show -> addIfMissing(remoteShows, show) }

    if (!showAnticipated) {
      return remoteShows.filter { !it.status.isAnticipated() }
    }

    return remoteShows
  }

  suspend fun cacheDiscoverShows(shows: List<Show>) {
    transactions.withTransaction {
      val timestamp = nowUtcMillis()
      localSource.shows.upsert(shows.map { mappers.show.toDatabase(it) })
      localSource.discoverShows.replace(
        shows.map {
          DiscoverShow(
            idTrakt = it.ids.trakt.id,
            createdAt = timestamp,
            updatedAt = timestamp
          )
        }
      )
    }
  }

  private fun addIfMissing(shows: MutableList<Show>, show: Show) {
    if (shows.any { it.ids.trakt == show.ids.trakt }) return
    shows.add(show)
  }
}
