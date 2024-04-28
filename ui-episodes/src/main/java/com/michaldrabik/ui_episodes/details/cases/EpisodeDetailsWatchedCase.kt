package com.michaldrabik.ui_episodes.details.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_local.sources.EpisodesLocalDataSource
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@ViewModelScoped
class EpisodeDetailsWatchedCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val episodesDataSource: EpisodesLocalDataSource,
) {

  suspend fun getLastWatchedAt(showId: IdTrakt, episode: Episode): ZonedDateTime? {
    return withContext(dispatchers.IO) {
      episodesDataSource.getById(showId.id, episode.ids.trakt.id)?.lastWatchedAt
    }
  }
}
