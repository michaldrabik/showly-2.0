package com.michaldrabik.ui_show.sections.seasons.cases

import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_show.quicksetup.QuickSetupListItem
import com.michaldrabik.ui_show.sections.seasons.recycler.SeasonListItem
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsQuickProgressCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun setQuickProgress(
    selectedItem: QuickSetupListItem,
    seasonsItems: List<SeasonListItem>,
    show: Show
  ) = coroutineScope {
    val isMyShows = async { showsRepository.myShows.exists(show.ids.trakt) }
    val isWatchlist = async { showsRepository.watchlistShows.exists(show.ids.trakt) }
    val isHidden = async { showsRepository.hiddenShows.exists(show.ids.trakt) }

    val isCollection = isMyShows.await() || isWatchlist.await() || isHidden.await()
    val episodesAdded = mutableListOf<Episode>()

    episodesManager.setAllUnwatched(show.ids.trakt, skipSpecials = true)
    val seasons = seasonsItems.map { it.season }
    seasons
      .filter { !it.isSpecial() && it.number < selectedItem.season.number }
      .forEach { season ->
        val bundle = SeasonBundle(season, show)
        episodesManager.setSeasonWatched(bundle).apply {
          episodesAdded.addAll(this)
        }
      }

    val season = seasons.find { it.number == selectedItem.season.number }
    season?.episodes
      ?.filter { it.number <= selectedItem.episode.number }
      ?.forEach { episode ->
        val bundle = EpisodeBundle(episode, season, show)
        episodesManager.setEpisodeWatched(bundle)
        episodesAdded.add(episode)
      }

    if (isCollection) {
      val episodesIds = episodesAdded.map { it.ids.trakt.id }
      quickSyncManager.clearEpisodes()
      quickSyncManager.scheduleEpisodes(
        episodesIds = episodesIds,
        showId = show.traktId,
        clearProgress = true
      )
    }
  }
}
