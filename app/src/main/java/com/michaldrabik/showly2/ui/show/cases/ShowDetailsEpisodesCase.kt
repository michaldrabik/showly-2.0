package com.michaldrabik.showly2.ui.show.cases

import com.michaldrabik.network.Cloud
import com.michaldrabik.showly2.di.scope.AppScope
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.mappers.Mappers
import javax.inject.Inject

@AppScope
class ShowDetailsEpisodesCase @Inject constructor(
  private val cloud: Cloud,
  private val mappers: Mappers
) {

  suspend fun loadNextEpisode(traktId: IdTrakt): Episode? {
    val episode = cloud.traktApi.fetchNextEpisode(traktId.id) ?: return null
    return mappers.episode.fromNetwork(episode)
  }

  suspend fun loadSeasons(show: Show): List<Season> =
    cloud.traktApi.fetchSeasons(show.ids.trakt.id)
      .map { mappers.season.fromNetwork(it) }
}
