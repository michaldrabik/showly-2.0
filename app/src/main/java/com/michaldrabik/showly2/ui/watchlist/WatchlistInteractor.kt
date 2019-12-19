package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.common.ImagesManager
import com.michaldrabik.showly2.di.AppScope
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject

@AppScope
class WatchlistInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesManager: ImagesManager,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository
) {

  suspend fun loadWatchlist(): List<WatchlistItem> {
    val unavailableImage = Image.createUnavailable(POSTER)

    val shows = showsRepository.myShows.loadAll()
    val episodes = database.episodesDao().getAllForShows(shows.map { it.ids.trakt.id })
    val episodesUnwatched = episodes.filter { !it.isWatched && it.firstAired != null }

    val allItems = shows
      .filter { show -> episodesUnwatched.any { it.idShowTrakt == show.ids.trakt.id } }
      .map { show ->
        val showEpisodes = episodesUnwatched.filter { it.idShowTrakt == show.ids.trakt.id }
        val episode = showEpisodes.asSequence()
          .sortedBy { it.idTrakt }
          .first()
        val season = database.seasonsDao().getById(episode.idSeason)!!

        val episodesCount = episodes.count { it.idShowTrakt == show.ids.trakt.id }
        val watchedEpisodesCount = episodesCount - episodes.count {
          it.idShowTrakt == show.ids.trakt.id && !it.isWatched
        }

        WatchlistItem(
          show,
          mappers.season.fromDatabase(season),
          mappers.episode.fromDatabase(episode),
          unavailableImage,
          episodesCount,
          watchedEpisodesCount
        )
      }
      .groupBy { it.episode.hasAired(it.season) }

    val aired = (allItems[true] ?: emptyList())
      .sortedWith(compareByDescending<WatchlistItem> { it.isNew() }.thenBy { it.show.title })
    val notAired = (allItems[false] ?: emptyList())
      .sortedBy { it.episode.firstAired?.toInstant()?.toEpochMilli() }

    return aired + notAired
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesManager.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesManager.loadRemoteImage(show, type, force)
}