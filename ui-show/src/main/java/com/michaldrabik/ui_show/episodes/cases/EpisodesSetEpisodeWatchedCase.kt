package com.michaldrabik.ui_show.episodes.cases

import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.repository.settings.SettingsRepository
import com.michaldrabik.repository.shows.ShowsRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_show.sections.seasons.helpers.SeasonsCache
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class EpisodesSetEpisodeWatchedCase @Inject constructor(
  private val showsRepository: ShowsRepository,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val userTraktManager: UserTraktManager,
  private val seasonsCache: SeasonsCache,
  private val settingsRepository: SettingsRepository,
) {

  suspend fun setEpisodeWatched(
    episodeBundle: EpisodeBundle,
    isChecked: Boolean
  ): Result {
    val (episode, _, show) = episodeBundle

    val isMyShows = showsRepository.myShows.exists(show.ids.trakt)
    val isWatchlist = showsRepository.watchlistShows.exists(show.ids.trakt)
    val isHidden = showsRepository.hiddenShows.exists(show.ids.trakt)
    val isCollection = isMyShows || isWatchlist || isHidden

    when {
      isChecked -> {
        episodesManager.setEpisodeWatched(episodeBundle)
        if (isMyShows) {
          quickSyncManager.scheduleEpisodes(
            episodesIds = listOf(episode.ids.trakt.id),
            showId = show.traktId,
            clearProgress = false
          )
        }
        return Result.SUCCESS
      }
      else -> {
        episodesManager.setEpisodeUnwatched(episodeBundle)
        quickSyncManager.clearEpisodes(listOf(episode.ids.trakt.id))

        val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
        val isSeasonLocal = seasonsCache.areSeasonsLocal(show.ids.trakt)

        val showRemoveTrakt = userTraktManager.isAuthorized() && traktQuickRemoveEnabled && !isSeasonLocal && isCollection
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
