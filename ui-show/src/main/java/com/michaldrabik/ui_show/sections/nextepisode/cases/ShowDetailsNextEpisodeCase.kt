package com.michaldrabik.ui_show.sections.nextepisode.cases

import com.michaldrabik.data_remote.RemoteDataSource
import com.michaldrabik.repository.mappers.Mappers
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ShowDetailsNextEpisodeCase @Inject constructor(
  private val remoteSource: RemoteDataSource,
  private val mappers: Mappers
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? {
    val episode = remoteSource.trakt.fetchNextEpisode(traktId.id) ?: return null
    return mappers.episode.fromNetwork(episode)
  }
}
