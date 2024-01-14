package com.michaldrabik.ui_progress.main.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.sources.EpisodesLocalDataSource
import com.michaldrabik.repository.EpisodesManager
import com.michaldrabik.repository.settings.SettingsSpoilersRepository
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ProgressMainEpisodesCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val spoilersSettings: SettingsSpoilersRepository,
  private val localDataSource: EpisodesLocalDataSource
) {

  suspend fun setEpisodeWatched(bundle: EpisodeBundle) {
    episodesManager.setEpisodeWatched(bundle)
    quickSyncManager.scheduleEpisodes(
      showId = bundle.show.traktId,
      episodesIds = listOf(bundle.episode.ids.trakt.id)
    )
  }

  suspend fun isWatched(
    show: Show,
    episode: Episode
  ): Boolean {
    return withContext(dispatchers.IO) {
      // No need to query DB if spoilers settings are all off in that case.
      if (!(
        spoilersSettings.isEpisodesTitleHidden ||
          spoilersSettings.isEpisodesDescriptionHidden ||
          spoilersSettings.isEpisodesImageHidden ||
          spoilersSettings.isEpisodesRatingHidden
        )
      ) {
        return@withContext false
      }
      return@withContext localDataSource.isEpisodeWatched(show.traktId, episode.ids.trakt.id)
    }
  }
}
