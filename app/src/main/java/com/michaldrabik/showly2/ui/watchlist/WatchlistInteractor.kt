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

    val shows = database.followedShowsDao().getAll()
    val episodes = database.episodesDao().getAllForShows(shows.map { it.idTrakt })
    val episodesUnwatched = episodes.filter { !it.isWatched && it.firstAired != null }

    return shows
      .filter { show -> episodesUnwatched.any { it.idShowTrakt == show.idTrakt } }
      .map { show ->
        val showEpisodes = episodesUnwatched.filter { it.idShowTrakt == show.idTrakt }
        val episode = showEpisodes.asSequence()
          .sortedBy { it.idTrakt }
          .first()
        val season = database.seasonsDao().getById(episode.idSeason)!!

        val episodesCount = episodes.count { it.idShowTrakt == show.idTrakt }
        val watchedEpisodesCount = episodesCount - episodes.count {
          it.idShowTrakt == show.idTrakt && !it.isWatched
        }

        WatchlistItem(
          mappers.show.fromDatabase(show),
          mappers.season.fromDatabase(season),
          mappers.episode.fromDatabase(episode),
          unavailableImage,
          episodesCount,
          watchedEpisodesCount
        )
      }
      .sortedBy { it.show.title }
      .toList()
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}