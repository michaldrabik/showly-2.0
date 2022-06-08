package com.michaldrabik.ui_show.sections.seasons.cases

import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchedSeasonCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val settingsRepository: SettingsRepository,
  private val episodesManager: EpisodesManager,
  private val userManager: UserTraktManager,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun setSeasonWatched(
    show: Show,
    season: Season,
    isChecked: Boolean,
    isLocal: Boolean
  ): Result {
    val bundle = SeasonBundle(season, show)

    val isMyShows = showsRepository.myShows.exists(show.ids.trakt)
    val isWatchlist = showsRepository.watchlistShows.exists(show.ids.trakt)
    val isHidden = showsRepository.hiddenShows.exists(show.ids.trakt)
    val isCollection = isMyShows || isWatchlist || isHidden

    when {
      isChecked -> {
        val episodesAdded = episodesManager.setSeasonWatched(bundle)
        if (isMyShows) {
          quickSyncManager.scheduleEpisodes(episodesAdded.map { it.ids.trakt.id })
        }
        return Result.SUCCESS
      }
      else -> {
        episodesManager.setSeasonUnwatched(bundle)
        quickSyncManager.clearEpisodes(season.episodes.map { it.ids.trakt.id })

        val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
        val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !isLocal && isCollection
        if (showRemoveTrakt) {
          return Result.REMOVE_FROM_TRAKT
        }
        return Result.SUCCESS
      }
    }
  }

  enum class Result {
    SUCCESS,
    REMOVE_FROM_TRAKT
  }
}
