package com.michaldrabik.ui_progress.main.cases

import android.content.Context
import com.michaldrabik.ui_base.episodes.EpisodesManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_progress.ProgressItem
import javax.inject.Inject

class ProgressEpisodesCase @Inject constructor(
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
) {

  suspend fun setEpisodeWatched(context: Context, item: ProgressItem) {
    val bundle = EpisodeBundle(item.episode, item.season, item.show)
    episodesManager.setEpisodeWatched(bundle)
    quickSyncManager.scheduleEpisodes(context, listOf(item.episode.ids.trakt.id))
  }
}
