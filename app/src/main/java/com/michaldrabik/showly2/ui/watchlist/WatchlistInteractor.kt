package com.michaldrabik.showly2.ui.watchlist

import android.content.Context
import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.common.trakt.quicksync.QuickSyncManager
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.watchlist.WatchlistRepository
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesManager
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.EpisodeWatchlist
import javax.inject.Inject

@AppScope
class WatchlistInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesProvider: ShowImagesProvider,
  private val mappers: Mappers,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val watchlistRepository: WatchlistRepository
) {

  suspend fun loadWatchlistItem(show: Show): WatchlistItem {
    val episodes = database.episodesDao().getAllForShowWatchlist(show.traktId)
    val seasons = database.seasonsDao().getAllForShow(show.traktId)

    val episodesCount = episodes.count()
    val unwatchedEpisodes = episodes.filter { !it.isWatched }
    val unwatchedEpisodesCount = unwatchedEpisodes.count()

    val nextEpisode = unwatchedEpisodes
      .sortedWith(compareBy<EpisodeWatchlist> { it.seasonNumber }.thenBy { it.episodeNumber })
      .firstOrNull() ?: return WatchlistItem.EMPTY

    val upcomingEpisode = unwatchedEpisodes
      .filter { it.firstAired != null }
      .sortedBy { it.firstAired }
      .firstOrNull { it.firstAired?.isAfter(nowUtc()) == true }

    val isPinned = watchlistRepository.isItemPinned(show.traktId)
    val season = seasons.first { it.idTrakt == nextEpisode.idSeason }
    val episode = database.episodesDao().getById(nextEpisode.idTrakt)
    val upEpisode = upcomingEpisode?.let {
      val epDb = database.episodesDao().getById(it.idTrakt)
      mappers.episode.fromDatabase(epDb)
    } ?: Episode.EMPTY

    return WatchlistItem(
      show,
      mappers.season.fromDatabase(season),
      mappers.episode.fromDatabase(episode),
      upEpisode,
      Image.createUnavailable(POSTER),
      episodesCount,
      episodesCount - unwatchedEpisodesCount,
      isPinned = isPinned
    )
  }

  suspend fun setEpisodeWatched(context: Context, item: WatchlistItem) {
    val bundle = EpisodeBundle(item.episode, item.season, item.show)
    episodesManager.setEpisodeWatched(bundle)
    quickSyncManager.scheduleEpisodes(context, listOf(item.episode.ids.trakt.id))
  }

  fun addPinnedItem(item: WatchlistItem) =
    watchlistRepository.addPinnedItem(item.show.traktId)

  fun removePinnedItem(item: WatchlistItem) =
    watchlistRepository.removePinnedItem(item.show.traktId)

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesProvider.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(show, type, force)
}
