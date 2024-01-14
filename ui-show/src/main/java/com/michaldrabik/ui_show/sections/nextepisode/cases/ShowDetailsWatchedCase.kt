package com.michaldrabik.ui_show.sections.nextepisode.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.sources.EpisodesLocalDataSource
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Show
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsWatchedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesLocalDataSource: EpisodesLocalDataSource,
) {

  suspend fun isWatched(
    show: Show,
    episode: Episode,
  ): Boolean = withContext(dispatchers.IO) {
    return@withContext episodesLocalDataSource.isEpisodeWatched(
      showTraktId = show.traktId,
      episodeTraktId = episode.ids.trakt.id
    )
  }
}
