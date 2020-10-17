package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.common.di.AppScope
import com.michaldrabik.common.extensions.nowUtc
import com.michaldrabik.storage.database.AppDatabase
import com.michaldrabik.storage.database.model.EpisodeWatchlist
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.SortOrder
import com.michaldrabik.ui_model.SortOrder.EPISODES_LEFT
import com.michaldrabik.ui_model.SortOrder.NAME
import com.michaldrabik.ui_model.SortOrder.RECENTLY_WATCHED
import com.michaldrabik.ui_progress.ProgressItem
import com.michaldrabik.ui_repository.PinnedItemsRepository
import com.michaldrabik.ui_repository.mappers.Mappers
import com.michaldrabik.ui_repository.shows.ShowsRepository
import java.util.Locale.ROOT
import javax.inject.Inject

@AppScope
class ProgressLoadItemsCase @Inject constructor(
  private val database: AppDatabase,
  private val mappers: Mappers,
  private val showsRepository: ShowsRepository,
  private val pinnedItemsRepository: PinnedItemsRepository
) {

  suspend fun loadMyShows() = showsRepository.myShows.loadAll()

  suspend fun loadWatchlistItem(show: Show): ProgressItem {
    val episodes = database.episodesDao().getAllForShowWatchlist(show.traktId)
      .filter { it.seasonNumber != 0 }
    val seasons = database.seasonsDao().getAllForShow(show.traktId)
      .filter { it.seasonNumber != 0 }

    val episodesCount = episodes.count()
    val unwatchedEpisodes = episodes.filter { !it.isWatched }
    val unwatchedEpisodesCount = unwatchedEpisodes.count()

    val nextEpisode = unwatchedEpisodes
      .sortedWith(compareBy<EpisodeWatchlist> { it.seasonNumber }.thenBy { it.episodeNumber })
      .firstOrNull() ?: return ProgressItem.EMPTY

    val upcomingEpisode = unwatchedEpisodes
      .filter { it.firstAired != null }
      .sortedBy { it.firstAired }
      .firstOrNull { it.firstAired?.isAfter(nowUtc()) == true }

    val isPinned = pinnedItemsRepository.isItemPinned(show.traktId)
    val season = seasons.first { it.idTrakt == nextEpisode.idSeason }
    val episode = database.episodesDao().getById(nextEpisode.idTrakt)
    val upEpisode = upcomingEpisode?.let {
      val epDb = database.episodesDao().getById(it.idTrakt)
      mappers.episode.fromDatabase(epDb)
    } ?: Episode.EMPTY

    return ProgressItem(
      show,
      mappers.season.fromDatabase(season),
      mappers.episode.fromDatabase(episode),
      upEpisode,
      Image.createUnavailable(ImageType.POSTER),
      episodesCount,
      episodesCount - unwatchedEpisodesCount,
      isPinned = isPinned
    )
  }

  fun prepareWatchlistItems(
    input: List<ProgressItem>,
    searchQuery: String,
    sortOrder: SortOrder
  ): List<ProgressItem> {
    val items = input
      .filter { it.episodesCount != 0 && it.episode.firstAired != null }
      .groupBy { it.episode.hasAired(it.season) }

    val aired = (items[true] ?: emptyList())
      .sortedWith(
        when (sortOrder) {
          NAME -> compareBy { it.show.title.toUpperCase(ROOT) }
          RECENTLY_WATCHED -> compareByDescending { it.show.updatedAt }
          EPISODES_LEFT -> compareBy { it.episodesCount - it.watchedEpisodesCount }
          else -> throw IllegalStateException("Invalid sort order")
        }
      )

    val notAired = (items[false] ?: emptyList())
      .sortedBy { it.episode.firstAired?.toInstant()?.toEpochMilli() }

    return (aired + notAired)
      .filter {
        if (searchQuery.isBlank()) true
        else it.show.title.contains(searchQuery, true) || it.episode.title.contains(searchQuery, true)
      }
      .toMutableList()
  }
}
