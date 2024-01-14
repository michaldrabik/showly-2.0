package com.michaldrabik.ui_show.sections.nextepisode.cases

import com.michaldrabik.common.dispatchers.CoroutineDispatchers
import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsNextEpisodeCase @Inject constructor(
  private val dispatchers: CoroutineDispatchers,
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers,
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? = withContext(dispatchers.IO) {
    val episode = remoteSource.trakt.fetchNextEpisode(traktId.id) ?: return@withContext null
    return@withContext mappers.episode.fromNetwork(episode)
  }
}
