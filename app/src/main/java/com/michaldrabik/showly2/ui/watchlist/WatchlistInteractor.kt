package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.POSTER
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
    val unavailableImage = Image.createUnavailable(POSTER)

    val showsDb = database.followedShowsDao().getAll()
    val episodesDb = database.episodesDao()
      .getAllUnwatchedForShows(showsDb.map { it.idTrakt })
      .filter { it.firstAired.isNotBlank() }

    val items = showsDb.asSequence()
      .filter { show ->
        episodesDb.any { it.idShowTrakt == show.idTrakt }
      }
      .map { mappers.show.fromDatabase(it) }
      .map { show ->
        val episode = episodesDb.asSequence()
          .filter { it.idShowTrakt == show.id }
          .sortedBy { it.idTrakt }
          .first()
        WatchlistItem(show, mappers.episode.fromDatabase(episode), unavailableImage)
      }
      .sortedBy { it.show.title }
      .toList()

    return items.map {
      val image = findCachedImage(it.show, POSTER)
      it.copy(image = image)
    }
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}