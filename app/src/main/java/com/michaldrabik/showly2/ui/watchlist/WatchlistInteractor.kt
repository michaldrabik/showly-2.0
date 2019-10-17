package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.ui.common.ImagesManager
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class WatchlistInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers
) {

  suspend fun loadWatchlist(): List<WatchlistItem> {
    val episodesDb = database.episodesDao().getAllUnwatchedForFollowedShows()
      .filter { mappers.episode.fromDatabase(it).hasAirDate() }
    val shows = database.showsDao()
      .getByIds(episodesDb.distinctBy { it.idShowTrakt }.map { it.idShowTrakt })
      .map { mappers.show.fromDatabase(it) }

    return shows.map { show ->
      val episode = episodesDb.asSequence()
        .filter { it.idShowTrakt == show.id }
        .sortedBy { it.idTrakt }
        .first()

      val image = findCachedImage(show, ImageType.POSTER)

      WatchlistItem(show, mappers.episode.fromDatabase(episode), image)
    }
      .sortedBy { it.show.title }
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}