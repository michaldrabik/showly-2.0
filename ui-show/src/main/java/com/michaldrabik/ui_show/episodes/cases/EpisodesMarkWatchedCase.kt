package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class EpisodesMarkWatchedCase @Inject constructor(
  private val episodesManager: EpisodesManager,
) {

  suspend fun markWatchedEpisodes(
    show: Show,
    season: SeasonListItem
  ): SeasonListItem = coroutineScope {
    val (watchedSeasonsIds, watchedEpisodesIds) = awaitAll(
      async { episodesManager.getWatchedSeasonsIds(show) },
      async { episodesManager.getWatchedEpisodesIds(show) }
    )

    val isSeasonWatched = watchedSeasonsIds.any { id -> id == season.id }
    val episodes = season.episodes.map { episodeItem ->
      val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
      episodeItem.copy(season = season.season, isWatched = isEpisodeWatched)
    }

    season.copy(episodes = episodes, isWatched = isSeasonWatched)
  }

  suspend fun markWatchedEpisodes(
    show: Show,
    seasonsList: List<SeasonListItem>?
  ): List<SeasonListItem> =
    coroutineScope {
      val items = mutableListOf<SeasonListItem>()

      val (watchedSeasonsIds, watchedEpisodesIds) = awaitAll(
        async { episodesManager.getWatchedSeasonsIds(show) },
        async { episodesManager.getWatchedEpisodesIds(show) }
      )

      seasonsList?.forEach { item ->
        val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
        val episodes = item.episodes.map { episodeItem ->
          val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
          episodeItem.copy(season = item.season, isWatched = isEpisodeWatched)
        }
        val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
        items.add(updated)
      }

      items
    }
}
