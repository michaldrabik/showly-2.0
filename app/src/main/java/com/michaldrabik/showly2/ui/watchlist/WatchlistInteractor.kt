package com.michaldrabik.showly2.ui.watchlist

import com.michaldrabik.showly2.common.images.ShowImagesProvider
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.EpisodeBundle
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.ImageType
import com.michaldrabik.showly2.model.ImageType.POSTER
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import com.michaldrabik.showly2.repository.shows.ShowsRepository
import com.michaldrabik.showly2.ui.show.seasons.episodes.EpisodesManager
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.storage.database.AppDatabase
import javax.inject.Inject
import com.michaldrabik.storage.database.model.Episode as EpisodeDb
import com.michaldrabik.storage.database.model.Season as SeasonDb

@AppScope
class WatchlistInteractor @Inject constructor(
  private val database: AppDatabase,
  private val imagesProvider: ShowImagesProvider,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager
) {

  private var allSeasons: MutableList<SeasonDb> = mutableListOf()
  private var allEpisodes: MutableList<EpisodeDb> = mutableListOf()

  suspend fun preloadEpisodes(showsIds: List<Long>) {
    allSeasons.run {
      clear()
      addAll(database.seasonsDao().getAllForShows(showsIds))
    }
    allEpisodes.run {
      clear()
      addAll(database.episodesDao().getAllForShows(showsIds))
    }
  }

  fun loadWatchlistItem(show: Show): WatchlistItem {
    val episodes = allEpisodes.filter { it.idShowTrakt == show.traktId }
    val seasons = allSeasons.filter { it.idShowTrakt == show.traktId }

    val episodesCount = episodes.count()
    val unwatchedEpisodes = episodes.filter { !it.isWatched }
    val unwatchedEpisodesCount = unwatchedEpisodes.count()

    val episode = unwatchedEpisodes
      .sortedWith(compareBy<EpisodeDb> { it.seasonNumber }.thenBy { it.episodeNumber })
      .firstOrNull() ?: return WatchlistItem.EMPTY

    val season = seasons.first { it.idTrakt == episode.idSeason }

    return WatchlistItem(
      show,
      mappers.season.fromDatabase(season),
      mappers.episode.fromDatabase(episode),
      Image.createUnavailable(POSTER),
      episodesCount,
      episodesCount - unwatchedEpisodesCount
    )
  }

  suspend fun setEpisodeWatched(item: WatchlistItem) {
    val bundle = EpisodeBundle(item.episode, item.season, item.show)
    episodesManager.setEpisodeWatched(bundle)
  }

  suspend fun findCachedImage(show: Show, type: ImageType) =
    imagesProvider.findCachedImage(show, type)

  suspend fun loadMissingImage(show: Show, type: ImageType, force: Boolean) =
    imagesProvider.loadRemoteImage(show, type, force)
}
