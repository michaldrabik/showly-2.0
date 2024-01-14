package com.michaldrabik.ui_episodes.details.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.sources.EpisodesLocalDataSource
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class EpisodeDetailsWatchedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesDataSource: EpisodesLocalDataSource,
) {

  suspend fun isWatched(showId: IdTrakt, episode: Episode): Boolean {
    return withContext(dispatchers.IO) {
      val episodes = episodesDataSource.getAllByShowId(showId.id, episode.season)
      episodes.find {
        it.idShowTrakt == showId.id &&
          it.idTrakt == episode.ids.trakt.id
      }?.isWatched == true
    }
  }
}
